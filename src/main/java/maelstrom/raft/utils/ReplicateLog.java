package maelstrom.raft.utils;
import maelstrom.node.Node;
import maelstrom.raft.protocols.LogRequest;
import maelstrom.raft.state.Log;
import maelstrom.raft.state.State;


public final class ReplicateLog{

    public static void replicate(Node leader, String follower, State state){

        int prefixTerm = 0;
        int prefixLength = state.getSentLengthOf(follower);
        Log suffix = state.getLog().getSuffix(prefixLength);

        if (prefixLength > 0){
            prefixTerm = state.getLog().get(prefixLength - 1).getTerm();
        }

        leader.send(follower, new LogRequest(
            leader.getNodeId(),
            state.getCurrentTerm(),
            prefixLength,
            prefixTerm,
            state.getCommitLength(),
            suffix.toJson()
        ));
    }
}