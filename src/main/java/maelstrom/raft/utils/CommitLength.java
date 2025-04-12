package maelstrom.raft.utils;
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
                Deliver.deliver(state, state.getLog().get(commitLength).getMessage());
                state.setCommitLength(commitLength + 1);
            }

            else{
                break;
            }
        }
    }    
}