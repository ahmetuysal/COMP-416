package repository;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class MongoDBWARRepository implements WARRepository {

    private static MongoDBWARRepository _instance;

    public static synchronized MongoDBWARRepository getInstance() {
        if (_instance == null) {
            _instance = new MongoDBWARRepository();
        }
        return _instance;
    }

    private MongoClient mongoClient;
    private MongoDatabase WARDatabase;

    private MongoDBWARRepository() {
        // by default, this will connect to localhost:27017
        mongoClient = MongoClients.create();
        WARDatabase = mongoClient.getDatabase("WARGame");
    }
}
