package mqtt.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class JsonParser {
    private static JsonParser jsonParser = null;
    private static Gson gson;

    public JsonParser(){
        gson = new Gson();
    }

    public static JsonParser getInstance(){
        if (jsonParser == null)
            jsonParser = new JsonParser();

        return jsonParser;
    }

    public Map deserialize(String json){
        return gson.fromJson(json, Map.class);
    }

    public String serialize(Map<String, String> object){
        Gson gson = new GsonBuilder().create();

        return gson.toJson(object);
    }
}
