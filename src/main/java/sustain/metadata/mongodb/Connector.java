package sustain.metadata.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.task.FieldDetailTask;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by laksheenmendis on 7/31/20 at 12:32 AM
 */
public class Connector {

    private MongoClient mongoClient;
    private MongoDatabase database;

    public Connector() throws ValueNotFoundException{
        this.mongoClient = MongoClientProvider.getMongoClient();
        this.database = mongoClient.getDatabase(PropertyLoader.getMongoDBDB());
    }

    public MongoIterable<String> getCollectionNames() throws ValueNotFoundException {
        MongoIterable<String> collectionIterator = database.listCollectionNames();
        return collectionIterator;
    }


    public CollectionMetaData getFieldDetails(String collectionName , List<FieldInfo> validFieldList) {

        // this collectionMetaData object is shared between all the Threads
        CollectionMetaData collectionMetaData = new CollectionMetaData(collectionName);

        List<Future> fieldFutures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        validFieldList.stream().forEach(validField -> {
            try {
                fieldFutures.add(executor.submit(new FieldDetailTask(validField, collectionMetaData, collectionName)));
            } catch (ValueNotFoundException e) {
                e.printStackTrace();
            }
        });

        for(Future future : fieldFutures) {
            try {
                while (future.get() != null) { //returns null if the task has finished correctly.
                    // block until the future returns null
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        executor.shutdownNow();
        return collectionMetaData;
    }
}
