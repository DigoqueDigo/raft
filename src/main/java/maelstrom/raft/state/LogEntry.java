package maelstrom.raft.state;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import maelstrom.message.IJson;
import maelstrom.message.Message;


public class LogEntry implements IJson{


    private Message message;
    private int term;


    public LogEntry(Message message, int term){
        this.message = message;
        this.term = term;
    }


    public LogEntry(JsonValue jvLogEntry){
        final JsonObject joLogEntry = jvLogEntry.asObject();
        this.message = new Message(joLogEntry.get("message"));
        this.term = joLogEntry.getInt("term", -1);
    }


    public Message getMessage(){
        return this.message;
    }


    public int getTerm(){
        return this.term;
    }


    @Override
    public JsonValue toJson(){
        return Json.object()
            .add("message", this.message.toJson())
            .add("term", this.term);
    }
}