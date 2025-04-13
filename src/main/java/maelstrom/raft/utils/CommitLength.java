package maelstrom.raft.utils;
import com.eclipsesource.json.JsonObject;
import maelstrom.message.Message;
import maelstrom.node.Node;
import maelstrom.raft.state.State;


public final class CommitLength{

    public static void commit(Node node, State state){

        while (state.getCommitLength() < state.getLog().size()){

            int acks = 0;
            int totalNodes = node.getNodeIds().size();

            for (String nodeId : node.getNodeIds()){
                if (state.getAckedLengthOf(nodeId) > state.getCommitLength()){
                    acks++;
                }
            }

            if (acks >= Math.ceil((totalNodes + 1) / 2.0)){
                int commitLength = state.getCommitLength();
                Message message = state.getLog().get(commitLength).getMessage();
                JsonObject response = Deliver.deliver(state, message);
                state.setCommitLength(commitLength + 1);
                node.reply(message, response);
            }

            else{
                break;
            }
        }
    }    
}