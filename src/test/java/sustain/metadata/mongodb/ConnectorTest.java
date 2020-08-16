package sustain.metadata.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import sustain.metadata.utility.PropertyLoader;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

/**
 * Created by laksheenmendis on 8/15/20 at 11:22 AM
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Connector.class, MongoClientProvider.class, PropertyLoader.class})
public class ConnectorTest {
    private static final String MONGO_DB = "mongodb";
    @Mock
    private MongoClient mongoClient;
    private Connector connector;
    @Mock
    private MongoDatabase databse;
    @Mock
    private MongoIterable<String> collectionIterator;

    @Test
    public void getCollectionNames() throws Exception{
        when(mongoClient.getDatabase(MONGO_DB)).thenReturn(databse);
        when(databse.listCollectionNames()).thenReturn(collectionIterator);

        assertSame(collectionIterator, connector.getCollectionNames());
    }

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(MongoClientProvider.class);
        PowerMockito.when(MongoClientProvider.getMongoClient()).thenReturn(mongoClient);
        PowerMockito.mockStatic(PropertyLoader.class);
        PowerMockito.when(PropertyLoader.getMongoDBDB()).thenReturn(MONGO_DB);
        connector = new Connector();
    }

}