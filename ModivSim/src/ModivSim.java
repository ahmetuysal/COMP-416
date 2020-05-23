import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Ahmet Uysal @ahmetuysal
 */
public class ModivSim {
    public static void main(String[] args) {

        try (Stream<Path> walk = Files.walk(Paths.get("src/nodeData"))) {
            walk.filter(Files::isRegularFile).forEach(path -> {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
                    // TODO: read file content
                    String content;
                    while ((content = reader.readLine()) != null) {
                        List<String> l = new ArrayList<>(Arrays.asList(content.split(",")));
                        int nodeID = Integer.parseInt(l.get(0));
                        int neighborID = 0;
                        HashMap<Integer, Integer> linkCost = new HashMap<>();
                        HashMap<Integer, Integer> linkBandwidth = new HashMap<>();
                        l.remove(0);
                        System.out.println("Node: " + nodeID);
                        for (String s : l) {
                            if(s.contains("("))
                            {
                                neighborID  = Integer.parseInt(s.substring(s.indexOf("(") + 1));
                                System.out.println("Neighbor: " + neighborID);
                            }
                            else if(s.contains(")"))
                            {
                                linkBandwidth.put(neighborID, Integer.parseInt(s.substring(0, s.indexOf(")"))));
                                System.out.println("BW: " + linkBandwidth.get(neighborID));
                            }
                            else
                            {
                                linkCost.put(neighborID, Integer.parseInt(s));
                                System.out.println("Cost: " + linkCost.get(neighborID));
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
