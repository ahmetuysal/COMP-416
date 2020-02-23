package repository;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
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
import java.util.Arrays;

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
    private String hostName;
    private int port;
    private MongoClient mongoClient;
    private MongoDatabase WARDatabase;

    /**
     * Initializes the MongoDBRepository, that uses MongoDB API to store the game data and perform regular updates.
     *
     */
    private MongoDBWARRepository() {
        // by default, this will connect to localhost:27017
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        this.name = Configuration.getInstance().getProperty("mongodb.name");
        this.hostName = Configuration.getInstance().getProperty("mongodb.host");
        this.port = Integer.parseInt(Configuration.getInstance().getProperty("mongodb.port"));
        mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(hostName, port))))
                        .codecRegistry(pojoCodecRegistry)
                        .build());
        WARDatabase = mongoClient.getDatabase(name);
        this.collection = "WARGames";

    }

    /**
     * Gets the {@code MongoDBWarRepository} object that stores the game data in the Mongo database that can perfrom
     * necessary operations.
     *
     * @return {@code MongoDBWarRepository} to perform database-related operations.
     */
    public static synchronized MongoDBWARRepository getInstance() {
        if (_instance == null) {
            _instance = new MongoDBWARRepository();
        }
        return _instance;
    }

    /**
     * Inserts the given game data by first generating a document from the game, and inserting the document into the
     * Mongo database.
     *
     * @param gameData Game data to be inserted into the database.
     */
    @Override
    public void insertGame(WARGame gameData) {

        Document doc2insert = gameData.generateWARDoc();
        WARDatabase.getCollection(collection).insertOne(doc2insert);

    }

    /**
     * Retrieves the game document that corresponds to the give object ID from Mongo database by first querying a
     * database object, finds the corresponding document, and loads a game from the found document data.
     *
     * @param objID ID of the game to be retrieved from the database.
     */
    @Override
    public void retrieveGame(String objID) {

        BasicDBObject gq = new BasicDBObject("_id", new ObjectId(objID));
        FindIterable<Document> found = WARDatabase.getCollection(collection).find(gq);
        WARGame retrievedGame = new WARGame();
        retrievedGame.loadFromDoc(found.first());

    }


    /**
     * Updates the given game data by querying a database object with the same object ID, and replacing the existing
     * document with a document generated with the updated data.
     *
     * @param gameData Game data to be updated in the database.
     */
    @Override
    public void updateGame(WARGame gameData) {
        BasicDBObject gq = new BasicDBObject("_id", gameData.getGameID());
        WARDatabase.getCollection(collection).findOneAndReplace(gq, gameData.generateWARDoc());
    }


    /**
     * Finds the document to be deleted by querying a database object with the given object ID, and deletes the
     * corresponding document within the database.
     *
     * @param gameData Game data to be deleted from the database.
     */
    @Override
    public void deleteGame(WARGame gameData) {
        BasicDBObject gq = new BasicDBObject("_id", gameData.getGameID());
        WARDatabase.getCollection(collection).deleteOne(gq);
    }
}
