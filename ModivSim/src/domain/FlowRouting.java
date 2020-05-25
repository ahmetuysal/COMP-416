package domain;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FlowRouting {

    private HashMap<Integer, Map<Integer, List<Integer>>> forwardingTables = new HashMap<Integer, Map<Integer, List<Integer>>>();
    private HashMap<Integer, Node> nodes;
    private HashMap<String, List<Integer>> flows = new HashMap<String, List<Integer>>();
    private ConcurrentHashMap<String, List<Integer>> activeFlows = new ConcurrentHashMap<String, List<Integer>>();
    private ConcurrentHashMap<String, ArrayList<Integer>> activeFlowPaths = new ConcurrentHashMap<String, ArrayList<Integer>>();
    private ConcurrentHashMap<String, Long> activeFlowTimes = new ConcurrentHashMap<String, Long>();
    private ConcurrentHashMap<String, Integer> activeFlowDurations = new ConcurrentHashMap<String, Integer>();
    private ConcurrentHashMap<String, Integer> activeFlowPathChoice = new ConcurrentHashMap<String, Integer>();
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
            ArrayList<Integer> bandwidth = new ArrayList<Integer>();
            boolean activated = true;
            path.add(currentNode.getNodeId());
            int choice = 1;
            while(nodeStep != endNode){
                boolean linkIsEmpty = false;
                for(int i = 0; i<2; i++){
                    nodeStep = currentNode.getForwardingTable().get(endNode).get(i);
                    if(links[currentNode.getNodeId()][nodeStep] == false){
                        linkIsEmpty = true;
                        if(i == 1)
                            choice = 2;
                        break;
                    }
                }
                if(linkIsEmpty) {
                    bandwidth.add(currentNode.getLinkBandwidth().get(nodeStep));
                    currentNode = nodes.get(nodeStep);
                    path.add(nodeStep);
                }else{
                    activated = false;
                    queue.put(flowLabel, contents);
                    System.out.println("Queue is updated: " + queue);
                    break;
                }
            }

            if(activated) {
                for(int i=0; i<path.size()-1; i++){
                    links[path.get(i)][path.get(i+1)] = true;
                }
                System.out.println("Path for " + flowLabel + ": " + path);
                int min = 999;
                for(int i=0; i<bandwidth.size(); i++){
                    if(bandwidth.get(i) < min)
                        min = bandwidth.get(i);
                }
                int time = (contents.get(2) / min) * 1000;
                long endTime = clock.millis() + time;
                activeFlowTimes.put(flowLabel, endTime);
                activeFlowDurations.put(flowLabel, time);
                activeFlowPaths.put(flowLabel, path);
                activeFlows.put(flowLabel, contents);
                activeFlowPathChoice.put(flowLabel, choice);
            }

            if(checkActiveFlows()){
                checkQueue();
                for(String fl : activeFlowPathChoice.keySet()){
                    if (activeFlowPathChoice.get(fl) == 2)
                        checkFirstChoice(fl);
                }
            }


        }

        while(!activeFlows.isEmpty() || !queue.isEmpty()){
            if(activeFlows.size() != 0)
                checkActiveFlows();
            if(queue.size() != 0)
                checkQueue();
            for(String fl : activeFlowPathChoice.keySet()){
                if (activeFlowPathChoice.get(fl) == 2)
                    checkFirstChoice(fl);
            }
        }
    }

    private boolean checkActiveFlows(){
        boolean modified = false;
        for(String flowLabel : activeFlows.keySet()){
            ArrayList<Integer> currentPath = activeFlowPaths.get(flowLabel);
            if(clock.millis() > activeFlowTimes.get(flowLabel)){
                for(int i=0; i< currentPath.size()-1; i++){
                    links[currentPath.get(i)][currentPath.get(i+1)] = false;
                }
                activeFlows.remove(flowLabel);
                activeFlowTimes.remove(flowLabel);
                activeFlowPaths.remove(flowLabel);
                activeFlowPathChoice.remove(flowLabel);
                activeFlowDurations.remove(flowLabel);
                System.out.println("Flow " + flowLabel + " is terminated.");
                modified = true;
            }
        }
        return modified;
    }

    private void checkFirstChoice(String flowLabel){
        ArrayList<Integer> currentPath = activeFlowPaths.get(flowLabel);
        List<Integer> contents = flows.get(flowLabel);
        Node currentNode = nodes.get(contents.get(0));
        int endNode = contents.get(1);
        int nodeStep = -1;
        ArrayList<Integer> path = new ArrayList<Integer>();
        ArrayList<Integer> bandwidth = new ArrayList<Integer>();
        boolean activated = true;
        path.add(currentNode.getNodeId());
        while(nodeStep != endNode){
            boolean linkIsEmpty = false;
            nodeStep = currentNode.getForwardingTable().get(endNode).get(0);
            if(links[currentNode.getNodeId()][nodeStep] == false){
                bandwidth.add(currentNode.getLinkBandwidth().get(nodeStep));
                currentNode = nodes.get(nodeStep);
                path.add(nodeStep);
            }else{
                activated = false;
                break;
            }
        }
        if(activated) {
            for(int i=0; i< currentPath.size()-1; i++){
                links[currentPath.get(i)][currentPath.get(i+1)] = false;
            }

            for(int i=0; i<path.size()-1; i++){
                links[path.get(i)][path.get(i+1)] = true;
            }

            System.out.println("Updated path for " + flowLabel + ": " + path);
            int min = 999;
            for(int i=0; i<bandwidth.size(); i++){
                if(bandwidth.get(i) < min)
                    min = bandwidth.get(i);
            }

            long flowTime = activeFlowTimes.get(flowLabel);
            int duration = activeFlowDurations.get(flowLabel);

            int time = (contents.get(2)*((int) (flowTime-clock.millis())/duration)) / min;
            //System.out.println("time: " + time);
            long endTime = clock.millis() + time * 1000;
            activeFlowPaths.put(flowLabel, path);
            activeFlowTimes.put(flowLabel, endTime);
        }
    }
    private void checkQueue(){
        for(String flowLabel : queue.keySet()) {
            List<Integer> contents = queue.get(flowLabel);
            Node currentNode = nodes.get(contents.get(0));
            int endNode = contents.get(1);
            int nodeStep = -1;
            ArrayList<Integer> path = new ArrayList<Integer>();
            ArrayList<Integer> bandwidth = new ArrayList<Integer>();
            boolean activated = true;
            path.add(currentNode.getNodeId());
            while (nodeStep != endNode) {
                boolean linkIsEmpty = false;
                for (int i = 0; i < 2; i++) {
                    nodeStep = currentNode.getForwardingTable().get(endNode).get(i);
                    if (links[currentNode.getNodeId()][nodeStep] == false) {
                        linkIsEmpty = true;
                        break;
                    }
                }
                if (linkIsEmpty) {
                    bandwidth.add(currentNode.getLinkBandwidth().get(nodeStep));
                    currentNode = nodes.get(nodeStep);
                    path.add(nodeStep);
                } else {
                    activated = false;
                    break;
                }
            }

            if(activated) {
                for (int i = 0; i < path.size() - 1; i++) {
                    links[path.get(i)][path.get(i + 1)] = true;
                }
                System.out.println("Path for " + flowLabel + ": " + path);
                int min = 999;
                for(int i=0; i<bandwidth.size(); i++){
                    if(bandwidth.get(i) < min)
                        min = bandwidth.get(i);
                }
                int time = contents.get(2) / min;
                long endTime = clock.millis() + time * 1000;
                activeFlowTimes.put(flowLabel, endTime);
                activeFlowPaths.put(flowLabel, path);
                activeFlows.put(flowLabel, contents);
                queue.remove(flowLabel);
                System.out.println("Queue is updated: " + queue);
            }
        }
    }
}