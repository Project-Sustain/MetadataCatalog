package sustain.metadata.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCommandException;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import sustain.metadata.Constants;
import sustain.metadata.schema.Mapper;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.input.Types;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.project;
import static sustain.metadata.Constants.ARRAY_TYPE_CHECK_DOCUMENT_LIMIT;

/**
 * Created by laksheenmendis on 7/31/20 at 12:32 AM
 */
public class Connector {

    private MongoClient mongoClient = createMongoClient();

    public Connector() throws ValueNotFoundException{
    }

    public MongoIterable<String> getCollectionNames() throws ValueNotFoundException {
        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
        MongoIterable<String> collectionIterator = database.listCollectionNames();
        return collectionIterator;
    }

    private MongoClient createMongoClient() throws ValueNotFoundException {

        MongoClientURI mongoClientURI = new MongoClientURI(getConnectionString());
        return new MongoClient(mongoClientURI);
    }

    private String getConnectionString() throws ValueNotFoundException {
        StringBuilder sb = new StringBuilder("mongodb://");

        sb.append(PropertyLoader.getMongoDBHost());
        sb.append(":");
        sb.append(PropertyLoader.getMongoDBPort());

        return sb.toString();
    }

    public CollectionMetaData getFieldDetails(String collectionName , List<FieldInfo> validFieldList) {

        CollectionMetaData collectionMetaData = new CollectionMetaData(collectionName);

        for(FieldInfo fieldInfo : validFieldList)
        {
            String fieldName = fieldInfo.getId().getKey();

            try {
                if(!(PropertyLoader.getIgnoredFields().contains(fieldName) || fieldName.contains("geometry")))
                {
                    List<String> ignoredCollectionFields = PropertyLoader.getIgnoredCollectionFields(collectionName);
                    if( (ignoredCollectionFields != null && !ignoredCollectionFields.contains(fieldName)) || ignoredCollectionFields == null)
                    {
                        //identify date fields by key
                        boolean dateField = fieldName.toLowerCase().contains("date");

                        if(dateField)
                        {
                            // need to change the type of the field
                            fieldInfo.getValue().getTypes().setString(null);
                            fieldInfo.getValue().getTypes().setDate(1L);
                            getAndMapDateField(collectionName, fieldName, collectionMetaData, fieldInfo);
                        }
                        else
                        {
                            Types type = fieldInfo.getValue().getTypes();
                            if(type.getNumber() != null )
                            {
                                getAndMapNumericTypes(collectionName, fieldName, collectionMetaData, fieldInfo);
                            }
                            else if(type.getString() != null )
                            {
                                getAndMapStringType(collectionName, fieldName, collectionMetaData, fieldInfo);
                            }
                            else if(type.getArray() != null)
                            {
                                try {
                                    // array could contain different types of data (Ex; String, Integer, Double, etc)
                                    String arrayType = findArrayType(collectionName, fieldName);

                                    // extract metadata only if the array consists of a unique type
                                    if(arrayType != null)
                                    {
                                        if(arrayType.equals("String"))
                                        {
                                            getAndMapStringType(collectionName, fieldName, collectionMetaData, fieldInfo);
                                        }
                                        else
                                        {
                                            getAndMapNumericTypes(collectionName, fieldName, collectionMetaData, fieldInfo);
                                        }
                                    }
                                } catch (ValueNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } catch (ValueNotFoundException e) {
                e.printStackTrace();
            }
        }

        return collectionMetaData;
    }

    private void getAndMapDateField(String collectionName, String fieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo) {

        try {
            Document resultDoc = getMinMaxDate(collectionName, fieldName);
            if(resultDoc != null)
            {
                Mapper.mapTemporalMetaInfo(collectionMetaData, resultDoc, fieldInfo);
            }
        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Document getMinMaxDate(String collectionName, String fieldName) throws ValueNotFoundException
    {
        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());

        AggregateIterable<Document> aggregateIterable = database.getCollection(collectionName).aggregate(
                Arrays.asList(
                        project(
                                Projections.fields(
                                        Projections.excludeId(),
                                        Projections.include(fieldName),
                                        Projections.computed(
                                                fieldName,
                                                new Document("$dateFromString", new Document("dateString", "$" + fieldName))
                                        )
                                )
                        ),
                        group(null, Accumulators.max(Constants.MAXIMUM_NUMBER, "$" + fieldName),
                                Accumulators.min(Constants.MINIMUM_NUMBER, "$" + fieldName))
                )
        );


        System.out.println("Min and max values for :" + collectionName + "/" + fieldName);

        // we get only 1 document in the iterator for the above aggregation query
        Document doc = null;
        try {
            MongoCursor<Document> iterator = aggregateIterable.iterator();
            while(iterator.hasNext())
            {
                doc = iterator.next();
    //            System.out.println(doc.get(Constants.MAXIMUM_NUMBER));
    //            System.out.println(doc.get(Constants.MINIMUM_NUMBER));
            }
        } catch (MongoCommandException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private void getAndMapNumericTypes(String collectionName, String fieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo)
    {
        boolean categorical = false;
        try {
            //these integer fields should be considered as categorical
            if(PropertyLoader.getSpecialNumericFields().contains(fieldName))
            {
                List<Object> distinctIntegerValues = getDistinctIntegerValues(collectionName, fieldName);
                if(distinctIntegerValues != null)
                {
//                fieldInfo.getValue().getTypes().setArray(1L);
//                fieldInfo.getValue().getTypes().setNumber(null);
                    Mapper.mapCategoricalMetaInfo(collectionMetaData, distinctIntegerValues, fieldInfo);
                    categorical = true;
                }
            }

            if(!categorical)
            {
                Document resultDoc = getMinMax(collectionName, fieldName);
                Mapper.mapNumericMetaInfo(collectionMetaData, resultDoc, fieldInfo);
            }

        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getAndMapStringType(String collectionName, String fieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo) {
        try {
            List<Object> distinctCategories = getDistinctCategories(collectionName, fieldName);
            if(distinctCategories != null)
            {
                Mapper.mapCategoricalMetaInfo(collectionMetaData, distinctCategories, fieldInfo);
            }
        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String findArrayType(String collectionName, String fieldName) throws ValueNotFoundException {
        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
        FindIterable<Document> findIterable = database.getCollection(collectionName).find().projection(Projections.include(fieldName)).limit(ARRAY_TYPE_CHECK_DOCUMENT_LIMIT);
        MongoCursor<Document> iterator = findIterable.iterator();

        HashMap<String, Integer> typeCountMap = new HashMap<>();

        while(iterator.hasNext())
        {
            Object obj = iterator.next();
            String typeString = ((Document) obj).get(fieldName).getClass().getSimpleName().getClass().getSimpleName();
//            String typeString = obj == null ? "null" : obj.getClass().getSimpleName();

            if(typeCountMap.get(typeString) == null)
            {
                typeCountMap.put(typeString, 1);
            }
            else
            {
                typeCountMap.put(typeString, typeCountMap.get(typeString) + 1);
            }
        }

        //checks whether the HashMap has the exact number of counts as Constants.ARRAY_TYPE_CHECK_DOCUMENT_LIMIT,
        //for a particular type (ex; String, Integer, Double, etc)
        //thus we can deduce the Array consists of a particular data type
        for(String typeStr : typeCountMap.keySet())
        {
           if( typeCountMap.get(typeStr) == ARRAY_TYPE_CHECK_DOCUMENT_LIMIT)
           {
               System.out.println(typeStr);
               return typeStr;
           }
        }
        return null;
    }

    private Document getMinMax(String collectionName, String fieldName) throws ValueNotFoundException {
        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
        AggregateIterable<Document> documents = database.getCollection(collectionName).aggregate(Arrays.asList(
                group(null,
                        Accumulators.max(Constants.MAXIMUM_NUMBER, "$" + fieldName),
                        Accumulators.min(Constants.MINIMUM_NUMBER, "$" + fieldName)
                )
        ));


        System.out.println("Min and max values for :" + collectionName + "/" + fieldName);

        // we get only 1 document in the iterator for the above aggregation query
        Document doc = null;
        MongoCursor<Document> iterator = documents.iterator();
        while(iterator.hasNext())
        {
            doc = iterator.next();
//            System.out.println(doc.get(Constants.MAXIMUM_NUMBER));
//            System.out.println(doc.get(Constants.MINIMUM_NUMBER));
        }
        return doc;
    }

    private List<Object> getDistinctIntegerValues(String collectionName, String fieldName) throws ValueNotFoundException {
        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
        MongoCollection<Document> collection = database.getCollection(collectionName);
        DistinctIterable<Integer> distinct = collection.distinct(fieldName, Integer.class);

        MongoCursor<Integer> iterator = distinct.iterator();
        List<Object> distinctIntegers = new ArrayList<>();

        int count = 0;
        // stop extracting the distinct values when it exceeds 25, because
        // we're going to return null, if the list includes more than 20 elements
        while (iterator.hasNext() && count<25)
        {
            Integer s = iterator.next();
            distinctIntegers.add(s);
//            System.out.println(s);
            count++;
        }

        // if number of distinct integers are less than 20, we consider it as a categorical field
        return distinctIntegers.size() <=20 ? distinctIntegers:  null;
    }

//    private List<Object> getDistinctDoubleValues(String collectionName, String fieldName) throws ValueNotFoundException {
//        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//        DistinctIterable<Double> distinct = collection.distinct(fieldName, Double.class);
//
//        MongoCursor<Double> iterator = distinct.iterator();
//        List<Object> distinctIntegers = new ArrayList<>();
//
//        int count = 0;
//        while (iterator.hasNext() && count <25)
//        {
//            Object s = iterator.next();
//            distinctIntegers.add(s);
////            System.out.println(s);
//              count++;
//        }
//
//        // if number of distinct integers are less than 20, we consider it as a categorical field
//        return distinctIntegers.size() <=20 ? distinctIntegers:  null;
//    }

    private List<Object> getDistinctCategories(String collectionName, String fieldName) throws ValueNotFoundException {
        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//        DistinctIterable<String> distinct = collection.distinct(fieldName, String.class);
//        MongoCursor<String> iterator = distinct.iterator();

        System.out.println("Distinct Categories for :" + collectionName + "/" + fieldName);

        List<Object> categoriclaVals = null;
        try {
            AggregateIterable<Document> aggregateIterable = database.getCollection(collectionName).aggregate(
                    Arrays.asList(
                            group("$" + fieldName)
                    )
            );

            categoriclaVals = new ArrayList<>();
            MongoCursor<Document> iterator = aggregateIterable.iterator();

            while (iterator.hasNext())
            {
                Document s = iterator.next();
                categoriclaVals.add(s.get("_id"));
    //            System.out.println(s);
            }
        } catch (MongoCommandException e) {
            e.printStackTrace();
        }
        return categoriclaVals;
    }
}
