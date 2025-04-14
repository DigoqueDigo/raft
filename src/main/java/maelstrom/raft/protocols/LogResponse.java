package maelstrom.raft.protocols;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import maelstrom.message.IJson;


public record LogResponse(String fId, int fTerm, int fAck, boolean fSuccess) implements IJson{

    public LogResponse(JsonObject body){
        this(
            body.getString("fId", null),
            body.getInt("fTerm", -1),
            body.getInt("fAck", -1),
            body.getBoolean("fSuccess", false)
        );
    }

    @Override
    public JsonValue toJson(){
        return Json.object()
            .add("type", "logResponse")
            .add("fId", fId)
            .add("fTerm", fTerm)
            .add("fAck", fAck)
            .add("fSuccess", fSuccess);
    }
}
