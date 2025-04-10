package maelstrom.message;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


public class Message implements IJson{

    public final String src;
    public final String dest;
    public final JsonObject body;


    public Message(String src, String dest, JsonObject body){
        this.src = src;
        this.dest = dest;
        this.body = body;
    }


    public Message(String src, String dest, IJson body){
        this(src, dest, body.toJson().asObject());
    }


    public Message(JsonValue jvMessage){
        final JsonObject joMessage = jvMessage.asObject();
        this.src  = joMessage.getString("src", null);
        this.dest = joMessage.getString("dest", null);
        this.body = joMessage.get("body").asObject();
    }


    public String toString(){
        return "(msg " + src + " " + dest + " " + body + ")";
    }


    @Override
    public JsonValue toJson(){
        return Json.object()
            .add("src", src)
            .add("dest", dest)
            .add("body", body);
    }
}