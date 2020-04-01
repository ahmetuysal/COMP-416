import java.util.Arrays;

/**
 * @author Ahmet Uysal @ahmetuysal
 */
public class MailAddressHelper {

    private static MailAddressHelper _instance;
    private final String[] characters;
    private final int maxMailLength;

    private MailAddressHelper() {
        String[] emailAddresses = {"auysal16@ku.edu.tr", "ikoprululu16@ku.edu.tr", "fsahbaz16@ku.edu.tr"};
        // sort mail addresses from longest to shortest
        Arrays.sort(emailAddresses, (o1, o2) -> o2.length() - o1.length());

        this.maxMailLength = emailAddresses[0].length();
        this.characters = new String[this.maxMailLength];
        for (int i = 0; i < this.maxMailLength; i++) {
            StringBuilder message = new StringBuilder();
            for (int j = 0; j < emailAddresses.length; j++) {
                if (emailAddresses[j].length() <= i) {
                    break;
                }
                message.append(emailAddresses[j].charAt(i));
            }
            this.characters[i] = message.toString();
        }
        System.out.println(Arrays.toString(this.characters));
    }

    public static synchronized MailAddressHelper getInstance() {
        if (_instance == null) {
            _instance = new MailAddressHelper();
        }
        return _instance;
    }

    public String getCharactersAtIndex(int index) {
        if (index >= maxMailLength) {
            return "";
        }
        return this.characters[index];
    }

}
