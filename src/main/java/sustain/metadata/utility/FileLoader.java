package sustain.metadata.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by laksheenmendis on 7/31/20 at 12:08 AM
 */
public class FileLoader {
    public static Properties loadFile(){

        Properties prop = new Properties();

        String path = "./config.properties";

        try {
            FileInputStream file = new FileInputStream(path);

            // load the properties file
            prop.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

}
