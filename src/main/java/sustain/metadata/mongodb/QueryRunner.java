package sustain.metadata.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import sustain.metadata.Constants;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.project;
import static sustain.metadata.Constants.ARRAY_TYPE_CHECK_DOCUMENT_LIMIT;

/**
 * Created by laksheenmendis on 1/10/21 at 1:18 AM
 *
 * This class includes all queries which runs against the MongoDB
 */
public class QueryRunner {

    private MongoDatabase database;

    public QueryRunner() {
        try {
            MongoClient mongoClient = MongoClientProvider.getMongoClient();
            this.database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Document getMinMax(String collectionName, String fieldName) {

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
        }
        return doc;
    }


    public Document getMinMaxDate(String collectionName, String fieldName)
    {
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
        try {
            MongoCursor<Document> iterator = aggregateIterable.iterator();
            return iterator.hasNext() ? iterator.next() : null;
        } catch (MongoCommandException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Object> getDistinctIntegerValues(String collectionName, String fieldName) {

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

    public List<Object> getDistinctCategories(String collectionName, String fieldName) {

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

    public List<String> getChildCategories(String collectionName, String childFieldName, Bson filter)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);

        List<String> childCatList = collection.distinct(childFieldName, filter, String.class).into(new ArrayList<>());
        return childCatList;
    }

    public FindIterable<Document> findIterableType(String collectionName, String fieldName)
    {
        return database.getCollection(collectionName).find().projection(Projections.include(fieldName)).limit(ARRAY_TYPE_CHECK_DOCUMENT_LIMIT);
    }


}
