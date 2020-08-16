package sustain.metadata.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import sustain.metadata.utility.PropertyLoader;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created by laksheenmendis on 8/15/20 at 8:12 PM
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PropertyLoader.class,MongoClientProvider.class})
public class MongoClientProviderTest {

    private final String MONGODB_HOST = "host";
    private final String MONGODB_PORT = "27017";
    @Mock
    MongoClientURI mongoClientURI;
    @Mock
    MongoClient mongoClient;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(PropertyLoader.class);
        PowerMockito.when(PropertyLoader.getMongoDBHost()).thenReturn(MONGODB_HOST);
        PowerMockito.when(PropertyLoader.getMongoDBPort()).thenReturn(MONGODB_PORT);
    }

    @Test
    public void getMongoClient() throws Exception{
        PowerMockito.spy(MongoClientProvider.class);

        Whitebox.setInternalState(MongoClientProvider.class, "mongoClient", mongoClient);
        assertSame(mongoClient, MongoClientProvider.getMongoClient());
        verifyPrivate(MongoClientProvider.class, never()).invoke("createMongoClient");
        verifyPrivate(MongoClientProvider.class, never()).invoke("getConnectionString");
    }

    @Test
    public void getMongoClient_whenNotInitialized() throws Exception{
        String connStr = "mongodb://" + MONGODB_HOST + ":" + MONGODB_PORT;
        whenNew(MongoClientURI.class).withArguments(connStr).thenReturn(mongoClientURI);
        whenNew(MongoClient.class).withArguments(mongoClientURI).thenReturn(mongoClient);

        assertSame(mongoClient, MongoClientProvider.getMongoClient());
        verifyNew(MongoClientURI.class).withArguments(connStr);
        verifyNew(MongoClient.class).withArguments(mongoClientURI);
    }

}