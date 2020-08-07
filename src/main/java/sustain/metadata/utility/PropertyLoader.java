package sustain.metadata.utility;

import sustain.metadata.Constants;
import sustain.metadata.utility.exceptions.ValueNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

}
