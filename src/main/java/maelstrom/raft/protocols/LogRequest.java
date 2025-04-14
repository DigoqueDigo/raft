package maelstrom.raft.protocols;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import maelstrom.message.IJson;


public record LogRequest(String lId, int lTerm, int lPrefixLength, int lPrefixTerm, int lCommitLength, JsonValue lSuffix) implements IJson{

    public LogRequest(JsonObject body){
        this(
            body.getString("lId", null),
            body.getInt("lTerm", -1),
            body.getInt("lPrefixLength", -1),
            body.getInt("lPrefixTerm", -1),
            body.getInt("lCommitLength", -1),
            body.get("lSuffix")
        );
    }

    @Override
    public JsonValue toJson(){
        return Json.object()
            .add("type", "logRequest")
            .add("lId", lId)
            .add("lTerm", lTerm)
            .add("lPrefixLength", lPrefixLength)
            .add("lPrefixTerm", lPrefixTerm)
            .add("lCommitLength", lCommitLength)
            .add("lSuffix", lSuffix);
    }
}
