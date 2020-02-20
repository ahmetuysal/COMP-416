package repository;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import domain.WARGame;
import org.bson.Document;
import org.bson.types.ObjectId;

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
    public void insertGame(WARGame gameData) {

        Document doc2insert = gameData.generateWARDoc();
        WARDatabase.getCollection(collection).insertOne(doc2insert);

    }

    @Override
    public void retrieveGame(String objID) {

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(objID));
        FindIterable<Document> found = WARDatabase.getCollection(name).find(query);
        WARGame retrievedGame = new WARGame();
        retrievedGame.loadFromDoc(found.first());

    }

    @Override
    public void updateGame() {



    }

    @Override
    public void deleteGame() {

    }
}
