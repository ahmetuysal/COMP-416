package repository;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import controller.WARData;
import org.bson.Document;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class MongoDBWARRepository implements WARRepository {

    private static MongoDBWARRepository _instance;
    private static final String name = "WARRepo";
    private static final String collection = "WARGames";

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
        WARDatabase = mongoClient.getDatabase(name);
    }

    @Override
    public void insertGame(WARData gameData) {

        Document doc2insert = gameData.generateWARDoc();
        WARDatabase.getCollection(collection).insertOne(doc2insert);

    }

    @Override
    public void retrieveGame() {

    }

    @Override
    public void updateGame() {

    }

    @Override
    public void deleteGame() {

    }
}
