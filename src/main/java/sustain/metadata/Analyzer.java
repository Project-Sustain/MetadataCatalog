package sustain.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import sustain.metadata.mongodb.Connector;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.task.AnalyzeTask;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by laksheenmendis on 7/30/20 at 11:47 PM
 */
public class Analyzer {

    public static void main(String[] args) {
        PropertyLoader.loadPropertyFile();
        FileWriter writer = null;

        List<CollectionMetaData> dbMetaData = null;
        try {

            Connector connector = new Connector();

            //get user specified collection names and generate the metadata file for only those collections
            List<String> collectionNames = PropertyLoader.getCollectionNames();

            if( collectionNames != null )
            {
                dbMetaData = analyzeAndGenerateMetadata(collectionNames);
            }
            else // otherwise generate for all the collections in the database
            {
                MongoIterable<String>  allCollectionNames = connector.getCollectionNames();

                if(allCollectionNames != null)
                {
                    List<String> ignoreNames = PropertyLoader.getIgnoredCollectionNames();

                    MongoCursor<String> iterator = allCollectionNames.iterator();
                    collectionNames = new ArrayList<>();

                    while(iterator.hasNext())
                    {
                        collectionNames.add(iterator.next());
                    }
                    collectionNames.removeAll(ignoreNames);
                    dbMetaData = analyzeAndGenerateMetadata(collectionNames);
                }
            }

            // Creating Object of ObjectMapper define in Jakson Api
            ObjectMapper objectMapper = new ObjectMapper();

            writer = new FileWriter(PropertyLoader.getOutputPathAndName());
            writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dbMetaData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null)
                {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<CollectionMetaData> analyzeAndGenerateMetadata(List<String> collectionNames) {

        List<AnalyzeTask> callableTasks = new ArrayList<>();
        collectionNames.stream().forEach(collection -> { callableTasks.add(new AnalyzeTask(collection)); });

        List<CollectionMetaData> dbmetaData = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(30);
        try {
            List<Future<CollectionMetaData>> futures = executor.invokeAll(callableTasks);

            for(Future<CollectionMetaData> collectionMetaDataFuture : futures)
            {
                try {
                    //this is a blocking call
                    CollectionMetaData metaData = collectionMetaDataFuture.get();
                    dbmetaData.add(metaData);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdownNow();
        return dbmetaData;
    }


}
