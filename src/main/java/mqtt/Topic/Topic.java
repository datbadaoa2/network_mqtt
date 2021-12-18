package mqtt.Topic;

import mqtt.Server.ClientHandler;
import mqtt.Server.Server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Topic {
    private static final String TOPIC_REGEX = "^(\\+|.+/\\+|[^#]+#|.*/\\+/.*)$";
    private Map<String, Topic> subtopics;
    private Map<String, ClientHandler> subscribers;
    private Topic parentTopic;
    private String name;

    public Topic(Topic parentTopic, String name){
        this.parentTopic = parentTopic;
        this.subtopics = new ConcurrentHashMap<String, Topic>();
        this.subscribers = new ConcurrentHashMap<String, ClientHandler>();
        this.name = name;
    }

    private boolean isRoot(){
        return this.parentTopic == null;
    }

    private boolean isLeaf(){
        return this.subtopics.isEmpty();
    }

    public Map<String, ClientHandler> getSubscribers() {
        return subscribers;
    }

    public Topic search(LinkedList<String> topicLayers, boolean isInsert){
        if (topicLayers.isEmpty())
            return this;

        String nextLayer = topicLayers.poll();

        if (Objects.equals(nextLayer, "#") || Objects.equals(nextLayer, ""))
            return this;

        Topic nextTopicLayer = subtopics.get(nextLayer);

        if (nextTopicLayer == null){
            if (isInsert) {
                subtopics.put(nextLayer, new Topic(this, nextLayer));
                nextTopicLayer = subtopics.get(nextLayer);
            }
            else
                return null;
        }

        return nextTopicLayer.search(topicLayers, isInsert);
    }

    public void addSubscriber(String clientID, ClientHandler clientHandler){
        this.subscribers.put(clientID, clientHandler);

        if (this.parentTopic == null)
            return;

        this.parentTopic.addSubscriber(clientID, clientHandler);
    }

    public void removeSubscriber(String clientID){
        this.subscribers.remove(clientID);

        if (this.parentTopic == null)
            return;

        this.parentTopic.removeSubscriber(clientID);
    }

    public String getAbsolutePath(String currentPath){
        String nextPath = this.name;

        if (!isLeaf())
            nextPath = nextPath + "/" + currentPath;

        if (isRoot())
            return nextPath;

        return this.parentTopic.getAbsolutePath(nextPath);
    }

    public LinkedList<String> topicsFromString(String string){
        String[] tokens = string.split("/");
        return new LinkedList<String>(Arrays.asList(tokens));
    }

    public Map<String, Topic> getSubtopics() {
        return subtopics;
    }

    public String getName() {
        return name;
    }

    private boolean isValid(String path) throws Exception{
        boolean isValid = true;
        if (path.contains("\\+") || path.contains("#"))
            if (!Pattern.compile(TOPIC_REGEX).matcher(path).matches())
                return false;

        String[] tokens = path.split("/");

        if (tokens.length > 128)
            return false;

        for (String token: tokens)
            if (Objects.equals(token, ""))
                return false;

        return isValid;
    }

    public Topic[] parse(String path) throws Exception {
        assert isRoot();

        if (!isValid(path))
            throw new Exception("Invalid topic");

        if (path.contains("+")){
            String[] halves = path.split("\\+");

            if (halves.length == 0)
                halves = new String[]{"", ""};

            if (halves[1].startsWith("/"))
                halves[1] = halves[1].substring(1);

            Topic prefix = search(topicsFromString(halves[0]), false);

            if (prefix == null) {
                return new Topic[]{};
            }

            List<Topic> result = new ArrayList<Topic>();

            Map <String, Topic> prefixSubtopics = prefix.getSubtopics();
            for (Topic subtopic: prefixSubtopics.values()) {
                Topic matchingTopic = subtopic.search(topicsFromString(halves[1]), false);
                if (matchingTopic != null) {
                    result.add(matchingTopic);
                }
            }

            return result.toArray(new Topic[0]);
        }

        boolean isInsert = !(path.contains("+") || path.contains("#")); // Only create new topic if there is no wildcard
        return new Topic[]{search(topicsFromString(path), isInsert)};
    }
}
