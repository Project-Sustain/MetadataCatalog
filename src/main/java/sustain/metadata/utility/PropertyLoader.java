package sustain.metadata.utility;

import org.javatuples.Pair;
import sustain.metadata.Constants;
import sustain.metadata.utility.exceptions.ValueNotFoundException;

import java.util.*;

/**
 * Created by laksheenmendis on 7/31/20 at 12:21 AM
 */
public class PropertyLoader {

    private static Map<String, String> propertyValues = new HashMap<>();

    public static void loadPropertyFile()
    {
        Properties prop = FileLoader.loadFile();

        // get the property values
        propertyValues.put(Constants.PROPERTY_KEY_MONGODB_HOST, prop.getProperty("mongodb.host"));
        propertyValues.put(Constants.PROPERTY_KEY_MONGODB_PORT, prop.getProperty("mongodb.port"));
        propertyValues.put(Constants.PROPERTY_KEY_MONGODB_DB, prop.getProperty("mongodb.db"));
        propertyValues.put(Constants.PROPERTY_KEY_OUTPUT, prop.getProperty("output"));
        propertyValues.put(Constants.PROPERTY_KEY_SPECIAL_NUMERIC_FIELDS, prop.getProperty("categorical.numeric.fields"));
        propertyValues.put(Constants.PROPERTY_KEY_IGNORE_FIELDS, prop.getProperty("ignore.fields"));
        propertyValues.put(Constants.PROPERTY_KEY_COLLECTION_NAMES, prop.getProperty("collection.names"));
        propertyValues.put(Constants.PROPERTY_KEY_COLLECTION_IGNORE_FIELDS, prop.getProperty("ignore.collection.fields"));
        propertyValues.put(Constants.PROPERTY_KEY_STRUCTURED_FIELDS, prop.getProperty("structured.collection.fields"));
    }

    public static String getMongoDBHost() throws ValueNotFoundException
    {
        String host = propertyValues.get(Constants.PROPERTY_KEY_MONGODB_HOST);

        if(host != null)
        {
            return host;
        }
        throw new ValueNotFoundException("MongoDB host not found");
    }

    public static String getMongoDBPort() throws ValueNotFoundException
    {
        String host = propertyValues.get(Constants.PROPERTY_KEY_MONGODB_PORT);

        if(host != null)
        {
            return host;
        }
        throw new ValueNotFoundException("MongoDB port not found");
    }

    public static String getMongoDBDB() throws ValueNotFoundException
    {
        String host = propertyValues.get(Constants.PROPERTY_KEY_MONGODB_DB);

        if(host != null)
        {
            return host;
        }
        throw new ValueNotFoundException("MongoDB database not found");
    }

    public static String getOutputPathAndName() throws ValueNotFoundException
    {
        String outputPath = propertyValues.get(Constants.PROPERTY_KEY_OUTPUT);

        if(outputPath != null)
        {
            return outputPath;
        }
        throw new ValueNotFoundException("Output path and name not found");
    }

    public static List<String> getSpecialNumericFields() throws ValueNotFoundException
    {
        String fields = propertyValues.get(Constants.PROPERTY_KEY_SPECIAL_NUMERIC_FIELDS);

        if(fields != null)
        {
            return Arrays.asList(fields.split(","));
        }

        throw new ValueNotFoundException("Special Numeric fields not found");
    }

    public static List<String> getIgnoredFields() throws ValueNotFoundException
    {
        String fields = propertyValues.get(Constants.PROPERTY_KEY_IGNORE_FIELDS);

        if(fields != null)
        {
            return Arrays.asList(fields.split(","));
        }

        throw new ValueNotFoundException("Ignored fields not found");
    }

    public static List<String> getCollectionNames() throws ValueNotFoundException
    {
        String fields = propertyValues.get(Constants.PROPERTY_KEY_COLLECTION_NAMES);

        if(fields != null && !fields.trim().isEmpty())
        {
            return Arrays.asList(fields.split(","));
        }

        return null;
    }

    public static List<String> getIgnoredCollectionFields(String collectionName) throws ValueNotFoundException
    {
        String fields = propertyValues.get(Constants.PROPERTY_KEY_COLLECTION_IGNORE_FIELDS);

        if(fields != null && !fields.trim().isEmpty() && fields.trim().contains(collectionName + ":"))
        {
            List<String> collectionString = Arrays.asList(fields.trim().split(";"));

            for(String str : collectionString)
            {
                if(str.contains(collectionName + ":"))
                {
                    String[] splitArray = str.split(":");
                    if(splitArray.length == 2)
                    {
                        return Arrays.asList(splitArray[1].split(","));
                    }
                }
            }
        }

        return null;
    }

    public static List<Pair<String,String>> getStructuredFields(String collectionName)
    {
        String fields = propertyValues.get(Constants.PROPERTY_KEY_STRUCTURED_FIELDS);

        if(fields != null && !fields.trim().isEmpty() && fields.trim().contains(collectionName + ":"))
        {
            List<String> collectionString = Arrays.asList(fields.trim().split(";"));

            for(String str : collectionString)
            {
                if(str.contains(collectionName + ":"))
                {
                    String[] splitArray = str.split(":");
                    if(splitArray.length == 2 && splitArray[0].equals(collectionName))
                    {
                        final String[] split = splitArray[1].split("#");
                        
                        List<Pair<String,String>> returnList = new ArrayList<>();
                        for( String pair : split )
                        {
                            String[] parentAndChild = pair.split(",");
                            if(parentAndChild.length == 2)
                            {
                                returnList.add(new Pair<>(parentAndChild[0], parentAndChild[1]));
                            }
                        }
                        return returnList;
                    }
                }
            }
        }

        return null;
    }
}
