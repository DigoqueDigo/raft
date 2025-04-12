package maelstrom.raft.protocols;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import maelstrom.message.IJson;


public record VoteResponse(String vId, int vTerm, boolean vVoteGranted) implements IJson{

    public VoteResponse(JsonObject body){
        this(
            body.getString("vId", null),
            body.getInt("vTerm", -1),
            body.getBoolean("vVoteGranted", false)
        );
    }

    @Override
    public JsonValue toJson(){
        return Json.object()
            .add("type", "voteResponse")
            .add("vId", vId)
            .add("vTerm", vTerm)
            .add("vVoteGranted", vVoteGranted);
    }
}
