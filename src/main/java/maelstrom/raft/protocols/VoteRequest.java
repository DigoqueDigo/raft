package maelstrom.raft.protocols;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import maelstrom.message.IJson;


public record VoteRequest(String cId, int cTerm, int cLogLength, int cLastTerm) implements IJson{

    public VoteRequest(JsonObject body){
        this(
            body.getString("cId", null),
            body.getInt("cTerm", -1),
            body.getInt("cLogLength", -1),
            body.getInt("cLastTerm", -1)
        );
    }

    @Override
    public JsonValue toJson(){
        return Json.object()    
            .add("type", "voteRequest")
            .add("cId", cId)
            .add("cTerm", cTerm)
            .add("cLogLength", cLogLength)
            .add("cLastTerm", cLastTerm);
    }
}