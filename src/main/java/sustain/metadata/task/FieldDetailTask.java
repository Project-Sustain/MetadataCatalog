package sustain.metadata.task;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.javatuples.Pair;
import sustain.metadata.Constants;
import sustain.metadata.mongodb.MongoClientProvider;
import sustain.metadata.schema.Mapper;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.input.Types;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;
import java.util.*;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.project;
import static sustain.metadata.Constants.ARRAY_TYPE_CHECK_DOCUMENT_LIMIT;

/**
 * Created by laksheenmendis on 10/19/20 at 9:53 PM
 */
public class FieldDetailTask implements Runnable {

    private FieldInfo fieldInfo;
    private CollectionMetaData collectionMetaData;
    private String collectionName;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public FieldDetailTask(FieldInfo fieldInfo, CollectionMetaData collectionMetaData, String collectionName) throws ValueNotFoundException{
        this.fieldInfo = fieldInfo;
        this.collectionMetaData = collectionMetaData;
        this.collectionName = collectionName;
        this.mongoClient = MongoClientProvider.getMongoClient();
        this.database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
    }

    @Override
    public void run() {
        String fieldName = fieldInfo.getId().getKey();

        try {
            if(!(PropertyLoader.getIgnoredFields().contains(fieldName) || fieldName.contains("geometry")))
            {
                // if the field is listed as a child field, no need to generate a separate metadata section
                boolean isChildField = isAStructuredField(collectionName, fieldName, true);
                boolean isParentField = isAStructuredField(collectionName, fieldName, false);

                List<String> ignoredCollectionFields = PropertyLoader.getIgnoredCollectionFields(collectionName);
                if( (!isChildField && ignoredCollectionFields != null && !ignoredCollectionFields.contains(fieldName)) || ignoredCollectionFields == null)
                {
                    System.out.println("Started processing for " + collectionName + "/" + fieldName);
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

                        if(isParentField && type.getString() != null)
                        {
                            getAndMapStructuredFields(collectionName, fieldName, collectionMetaData, fieldInfo);
                        }
                        else
                        {
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
            }
        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean isAStructuredField(String collectionName, String fieldName, boolean child)
    {
        List<Pair<String, String>> structuredFields = PropertyLoader.getStructuredFields(collectionName);

        if(structuredFields != null)
        {
            for(Pair<String, String> pair : structuredFields)
            {
                if(child && pair.getValue1().equals(fieldName)) // if a child
                {
                    return true;
                }
                else if(!child && pair.getValue0().equals(fieldName)) // if a parent
                {
                    return true;
                }
            }
        }

        return false;
    }

    private String getChildField(String collectionName, String parentName)
    {
        List<Pair<String, String>> structuredFields = PropertyLoader.getStructuredFields(collectionName);

        for(Pair<String, String> pair : structuredFields)
        {
            if(pair.getValue0().equals(parentName))
            {
                return pair.getValue1();
            }
        }
        return null;
    }

    private void getAndMapStructuredFields(String collectionName, String parentFieldName, CollectionMetaData collectionMetaData, FieldInfo fieldInfo) {

        String childFieldName = getChildField(collectionName, parentFieldName);
        Map<String, List<String>> parentChildMap = new HashMap<>();

        try {
            // First we need to get all the distinct values for the parent field
            List<Object> categories = getDistinctCategories(collectionName, parentFieldName);

            // Next, get all the valid distinct values for the child field per each parent category
            for(Object category : categories)
            {
                String catString = (String) category;
                List<Bson> filters = new ArrayList<Bson>();
                filters.add(Filters.eq(parentFieldName, catString));

                Bson filter = Filters.and(filters);

                MongoCollection<Document> collection = database.getCollection(collectionName);

                List<String> childCatList = collection.distinct(childFieldName, filter, String.class).into(new ArrayList<String>());

                parentChildMap.put(catString, childCatList);
            }

            Mapper.mapStructureMetaInfo(collectionMetaData, parentChildMap, fieldInfo, childFieldName);

        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        }
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
//        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());

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
//        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
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
//        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
        AggregateIterable<Document> documents = database.getCollection(collectionName).aggregate(Arrays.asList(
                group(null,
                        Accumulators.max(Constants.MAXIMUM_NUMBER, "$" + fieldName),
                        Accumulators.min(Constants.MINIMUM_NUMBER, "$" + fieldName)
                )
        ));

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
//        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
        MongoCollection<Document> collection = database.getCollection(collectionName);
        DistinctIterable<Integer> distinct = collection.distinct(fieldName, Integer.class);

        MongoCursor<Integer> iterator = distinct.iterator();
        List<Object> distinctIntegers = new ArrayList<>();

        int count = 0;
        // stop extracting the distinct values when it exceeds 25, because
        // we're going to return null, if the list includes more than 20 elements
        while (iterator.hasNext() && count < Constants.MAXIMIM_NUMERICAL_VALUES) {
            Integer s = iterator.next();
            distinctIntegers.add(s);
            count++;
        }

        // if number of distinct integers are less than 20, we consider it as a categorical field
        return distinctIntegers.size() <= 20 ? distinctIntegers:  null;
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
//        MongoDatabase database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//        DistinctIterable<String> distinct = collection.distinct(fieldName, String.class);
//        MongoCursor<String> iterator = distinct.iterator();

        List<Object> categoriclaVals = null;
        try {
            AggregateIterable<Document> aggregateIterable = database.getCollection(collectionName).aggregate(
                    Arrays.asList(
                            group("$" + fieldName)
                    )
            );

            categoriclaVals = new ArrayList<>();
            MongoCursor<Document> iterator = aggregateIterable.iterator();

            int count = 0;
            while (iterator.hasNext())
            {
                Document s = iterator.next();
                categoriclaVals.add(s.get("_id"));
                count++;

                // if it exceeds maximum, do not include in the metadata catalog
                if(count > Constants.MAXIMUM_CATEGORICAL_VALUES)
                {
                    System.out.println("Aborted processing for " + collectionName + "/" + fieldName);
                    return null;
                }
            }
        } catch (MongoCommandException e) {
            e.printStackTrace();
        }
        return categoriclaVals;
    }
}
