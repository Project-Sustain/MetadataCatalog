package sustain.metadata.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import sustain.metadata.mongodb.Connector;
import sustain.metadata.schema.input.FieldInfo;
import sustain.metadata.schema.output.CollectionMetaData;
import sustain.metadata.utility.PropertyLoader;
import sustain.metadata.utility.exceptions.ValueNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by laksheenmendis on 10/19/20 at 12:50 PM
 */
public class AnalyzeTask implements Callable<CollectionMetaData> {

    private String collectionName;

    public AnalyzeTask(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public CollectionMetaData call() throws Exception {

        List<FieldInfo> fieldInfoList = analyzeCollection();
        CollectionMetaData fieldDetails = null;

        if(fieldInfoList != null)
        {
            // filter out fields with 100% presence, others are ignored
            List<FieldInfo> validFieldList = fieldInfoList.stream().filter(x -> x.getPercentContaining() == 100L).collect(Collectors.toList());
            Connector connector = new Connector();
            fieldDetails = connector.getFieldDetails(collectionName, validFieldList);
        }

        return fieldDetails;
    }

    private List<FieldInfo> analyzeCollection()
    {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();

            // Run a shell command
            String dbAndCollection = PropertyLoader.getMongoDBDB() + "/" + collectionName;
            System.out.println("DB and Collection is :" + dbAndCollection);
            processBuilder.command(new String[]{"python3", "script.py", dbAndCollection, PropertyLoader.getMongoDBHost(), PropertyLoader.getMongoDBPort()});

            String output = getOutput(processBuilder);

            ObjectMapper objectMapper = new ObjectMapper();

            TypeFactory typeFactory = objectMapper.getTypeFactory();
            CollectionType collectionType = typeFactory.constructCollectionType(List.class, FieldInfo.class);
            List<FieldInfo> fieldInfoList = objectMapper.readValue(output, collectionType);
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

    private String getOutput(ProcessBuilder processBuilder) throws IOException, InterruptedException {

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
        return output.toString();
    }
}
