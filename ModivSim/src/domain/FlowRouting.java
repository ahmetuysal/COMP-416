package domain;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Clock;
import java.util.*;

public class FlowRouting {

    private HashMap<Integer, Map<Integer, List<Integer>>> forwardingTables = new HashMap<Integer, Map<Integer, List<Integer>>>();
    private HashMap<Integer, Node> nodes;
    private HashMap<String, List<Integer>> flows = new HashMap<String, List<Integer>>();
    private HashMap<String, List<Integer>> activeFlows = new HashMap<String, List<Integer>>();
    private HashMap<String, List<Integer>> queue = new HashMap<String, List<Integer>>();
    private Clock clock;

    public FlowRouting(HashMap<Integer, Node> nodes) {
        this.nodes = nodes;
        registerFlows();
        clock = Clock.systemDefaultZone();
    }

    public void registerFlows(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("src/flowData/flows.txt"));
            String content;
            List<String> l = null;
            while ((content = reader.readLine()) != null) {
                l = new ArrayList<>(Arrays.asList(content.split(",")));
                ArrayList<Integer> rest = new ArrayList<Integer>();
                for (int i = 1; i < l.size(); i++) {
                    rest.add(Integer.parseInt(l.get(i)));
                }
                flows.put(l.get(0), rest);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleFlows(){
        for(String flowLabel : flows.keySet()){
            List<Integer> contents = flows.get(flowLabel);
            int cost = nodes.get(contents.get(0)).getLinkBandwidth().get(contents.get(1));
            int time = contents.get(2) / cost;
            long endTime = clock.millis() + time*100;




        }

        while(!activeFlows.isEmpty());
    }
}