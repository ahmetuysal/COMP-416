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
    private int flowsNum;
    private boolean[][] links;

    public FlowRouting(HashMap<Integer, Node> nodes) {
        this.nodes = nodes;
        registerFlows();
        clock = Clock.systemDefaultZone();
        links = new boolean[nodes.size()][nodes.size()];
        for(int i=0; i<nodes.size(); i++){
            for(int j=0; j<nodes.size(); j++){
                links[i][j] = false;
            }
        }

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
        flowsNum = flows.size();
    }

    public void handle(){
        for(String flowLabel : flows.keySet()){
            List<Integer> contents = flows.get(flowLabel);
            Node currentNode = nodes.get(contents.get(0));
            int endNode = contents.get(1);
            int nodeStep = -1;
            ArrayList<Integer> path = new ArrayList<Integer>();
            int bandwidth = 0;
            boolean activated = true;
            path.add(currentNode.getNodeId());
            while(nodeStep != endNode){
                boolean linkIsEmpty = false;
                for(int i = 0; i<2; i++){
                    nodeStep = currentNode.getForwardingTable().get(endNode).get(i);
                    if(links[currentNode.getNodeId()][nodeStep] == false){
                        linkIsEmpty = true;
                        break;
                    }
                }
                if(linkIsEmpty) {
                    bandwidth += currentNode.getLinkBandwidth().get(nodeStep);
                    currentNode = nodes.get(nodeStep);
                    path.add(nodeStep);
                }else{
                    activated = false;
                    queue.put(flowLabel, contents);
                    break;
                }
            }

            if(activated) {
                for(int i=0; i<path.size()-1; i++){
                    links[path.get(i)][path.get(i+1)] = true;
                }
                System.out.println(path + " " + bandwidth);
                int time = contents.get(2) / bandwidth;
                long endTime = clock.millis() + time * 100;
                activeFlows.put(flowLabel, contents);
            }
        }

        while(!activeFlows.isEmpty());
    }

}