import repository.MongoDBWARRepository;

/**
 * @author Ahmet Uysal @ahmetuysal, Ipek Koprululu @ipekkoprululu, Furkan Sahbaz @fsahbaz
 */
public class Main {

    public static void main(String[] args) {
        MongoDBWARRepository.getInstance() ;
        new Server(Server.DEFAULT_SERVER_PORT);
    }
}
