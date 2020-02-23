package repository;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import configuration.Configuration;
import domain.WARGame;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ikoprululu, Furkan Sahbaz @fsahbaz
 */
public class MongoDBWARRepository implements WARRepository {
    // DB Credentials from configs
    private static MongoDBWARRepository _instance;
    private String name;
    private String collection;
    private MongoClient mongoClient;
    private MongoDatabase WARDatabase;

    private MongoDBWARRepository() {
        // by default, this will connect to localhost:27017
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .build();

        this.name = Configuration.getInstance().getProperty("mongodb.name");
        this.collection = Configuration.getInstance().getProperty("mongodb.collection");
        mongoClient = MongoClients.create(settings);
        WARDatabase = mongoClient.getDatabase(name);

    }

    public static synchronized MongoDBWARRepository getInstance() {
        if (_instance == null) {
            _instance = new MongoDBWARRepository();
        }
        return _instance;
    }

    @Override
    public void insertGame(WARGame gameData) {

        Document doc2insert = gameData.generateWARDoc();
        WARDatabase.getCollection(collection).insertOne(doc2insert);

    }

    @Override
    public void retrieveGame(String objID) {

        BasicDBObject gq = new BasicDBObject("_id", new ObjectId(objID));
        FindIterable<Document> found = WARDatabase.getCollection(collection).find(gq);
        WARGame retrievedGame = new WARGame();
        retrievedGame.loadFromDoc(found.first());

    }

    @Override
    public void updateGame(WARGame gameData) {
        BasicDBObject gq = new BasicDBObject("_id", gameData.getGameID());
        WARDatabase.getCollection(collection).findOneAndReplace(gq, gameData.generateWARDoc());
    }

    @Override
    public void deleteGame(WARGame gameData) {
        BasicDBObject gq = new BasicDBObject("_id", gameData.getGameID());
        WARDatabase.getCollection(collection).deleteOne(gq);
    }
}
