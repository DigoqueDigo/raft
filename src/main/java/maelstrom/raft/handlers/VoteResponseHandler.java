package maelstrom.raft.handlers;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.ReplicateLog;

/*
 * input type -> voteResponse
 *
 * vTerm -> termo do eleitor
 * voteGranted -> voto no candidato
 */


public class VoteResponseHandler implements MessageHandler{

    private Node node;
    private State state;


    public VoteResponseHandler(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void handle(Message message){

        String voterId = message.src;
        int vTerm = message.body.getInt("vTerm", -1);
        boolean voteGranted = message.body.getBoolean("voteGranted", false);

        boolean termOK = vTerm == state.getCurrentTerm();
        boolean roleOK = state.getCurrentRole().equals(State.CANDIDATE_ROLE);

        if (vTerm > state.getCurrentTerm()){
            state.setCurrentTerm(vTerm);
            state.setCurrentRole(State.FOLLOWER_ROLE);
            state.setVotedFor(null);

            // TODO :: CANCELAR O ELECTION TIMER (NAO PERCEBO POERQUE)
        }

        else if (roleOK && termOK && voteGranted){

            state.addVote(voterId);
            int totalNodes = node.getNodeIds().size();
            int votesReceived = state.getvotesReceived();
            int logSize = state.getLog().size();

            if (votesReceived >= Math.ceil((totalNodes + 1 / 2.0))){

                state.setCurrentRole(State.LEADER_ROLE);
                state.setCurrentLeader(node.getNodeId());
                
                // TODO :: CANCELAR LEADER ELECTION (ACHO QUE NAO E PRECISO) (AFINAL E)

                for (String follower : node.getNodeIds()){
                    if (!follower.equals(node.getNodeId())){
                        state.putSentLenghtOf(follower, logSize);
                        state.putAckedLengthOf(follower, 0);
                        ReplicateLog.replicate(node, follower, state);
                    }
                }
            }
        }
    }
}