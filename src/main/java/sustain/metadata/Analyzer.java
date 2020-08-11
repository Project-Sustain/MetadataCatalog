package sustain.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import sustain.metadata.mongodb.Connector;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by laksheenmendis on 7/30/20 at 11:47 PM
 */
public class Analyzer {

    public static void main(String[] args) {
        PropertyLoader.loadPropertyFile();
        FileWriter writer = null;

        MongoIterable<String> collectionNames;
        try {

            List<CollectionMetaData> dbMetaData = new ArrayList<>();

            Connector connector = new Connector();
            collectionNames = connector.getCollectionNames();

            if(collectionNames != null)
            {
                MongoCursor<String> iterator = collectionNames.iterator();

                while(iterator.hasNext())
                {
                    String collection = iterator.next();
//                    String collection = "tract_total_population";
                    List<FieldInfo> fieldInfoList = analyzeCollection(collection);
                    // filter out fields with 100% presence, others are ignored
                    if(fieldInfoList != null)
                    {
                        List<FieldInfo> validFieldList = fieldInfoList.stream().filter(x -> x.getPercentContaining() == 100L).collect(Collectors.toList());
                        dbMetaData.add(connector.getFieldDetails(collection, validFieldList));
                    }
                }

                // Creating Object of ObjectMapper define in Jakson Api
                ObjectMapper objectMapper = new ObjectMapper();

                writer = new FileWriter(PropertyLoader.getOutputPathAndName());
                writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dbMetaData));
            }
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

    private static List<FieldInfo> analyzeCollection(String collectionName)
    {
//        String homeDirectory = System.getProperty("user.home");
//        System.out.println("Home directory is "+ homeDirectory);

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

//        String specialStr =  "\"var collection = '" +collectionName+ "', outputFormat='json'\"";
//        System.out.println("Special String is :" + specialStr);

        try {

            ProcessBuilder processBuilder = new ProcessBuilder();

            // Run a shell command
            String dbAndCollection = PropertyLoader.getMongoDBDB() + "/" + collectionName;
            System.out.println("DB and Collection is :" + dbAndCollection);

//            processBuilder.command("sh", "-c","mongo", PropertyLoader.getMongoDBDB(),"--host", PropertyLoader.getMongoDBHost(), "--quiet", "--eval", specialStr, "variety.js");
//            processBuilder.command(new String[]{"python3", "script.py", PropertyLoader.getMongoDBDB(), PropertyLoader.getMongoDBHost(), "users"});
            processBuilder.command(new String[]{"python3", "script.py", dbAndCollection, PropertyLoader.getMongoDBHost(), PropertyLoader.getMongoDBPort()});

            Process process = processBuilder.start();
            System.out.println("Process started");

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
                output.append("\n");
            }

            int subprocessExited = process.waitFor();
            if (subprocessExited==0) {
                System.out.println("Success!");
                System.out.println(output);
            } else {
                //abnormal...
                System.out.println("Failed to load for collection :" +collectionName);
                //System.out.println(output);
            }

            System.out.println("Process waiting over");

            ObjectMapper objectMapper = new ObjectMapper();

            TypeFactory typeFactory = objectMapper.getTypeFactory();
            CollectionType collectionType = typeFactory.constructCollectionType(List.class, FieldInfo.class);
            List<FieldInfo> fieldInfoList = objectMapper.readValue(output.toString(), collectionType);
            return fieldInfoList;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ValueNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
