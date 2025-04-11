package maelstrom.raft.utils;
import com.eclipsesource.json.Json;
import maelstrom.node.Node;
import maelstrom.raft.state.Log;
import maelstrom.raft.state.State;

/*
 * output type -> logRequest
 * 
 * lId -> identificador do lider
 * lTerm -> termo atual do liedr
 * prefixLength -> comprimento do log do seguidor (espectavel)
 * prefixTerm -> term da ultima entrada do log do seguidor (expectavel)
 * commitLenght -> indice ate onde os logs foram committed
 * suffix -> entradas do log enviadas ao seguidor
 */


public final class ReplicateLog{

    public static void replicate(Node leader, String follower, State state){

        int prefixTerm = 0;
        int prefixLength = state.getSentLengthOf(follower);
        Log suffix = state.getLog().getSuffix(prefixLength);

        if (prefixLength > 0){
            prefixTerm = state.getLog().get(prefixLength - 1).getTerm();
        }

        leader.send(follower, Json.object()
            .add("type", "logRequest")
            .add("lId", leader.getNodeId())
            .add("lTerm", state.getCurrentTerm())
            .add("prefixLength", prefixLength)
            .add("prefixTerm", prefixTerm)
            .add("commitLength", state.getCommitLength())
            .add("suffix", suffix.toJson()));
    }
}