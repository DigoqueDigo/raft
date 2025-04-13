package maelstrom.raft.handlers;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.node.NodeTimer;
import maelstrom.raft.protocols.VoteResponse;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.ReplicateLog;


public final class VoteResponseHandler implements MessageHandler{

    private Node node;
    private State state;
    private NodeTimer electionTimer;


    public VoteResponseHandler(Node node, State state, NodeTimer electionTimer){
        this.node = node;
        this.state = state;
        this.electionTimer = electionTimer;
    }


    @Override
    public void handle(Message message){

        synchronized (state){

            VoteResponse voteResponse = new VoteResponse(message.body);

            final String vId = voteResponse.vId();
            final int vTerm = voteResponse.vTerm();
            final boolean vVoteGranted = voteResponse.vVoteGranted();

            boolean termOK = vTerm == state.getCurrentTerm();
            boolean roleOK = state.getCurrentRole().equals(State.CANDIDATE_ROLE);

            if (vTerm > state.getCurrentTerm()){
                state.setCurrentTerm(vTerm);
                state.setCurrentRole(State.FOLLOWER_ROLE);
                state.setVotedFor(null);
                electionTimer.cancel();
                // TODO :: CANCELAR O ELECTION TIMER (FEITO)
            }

            else if (roleOK && termOK && vVoteGranted){

                state.addVote(vId);
                int totalNodes = node.getNodeIds().size();
                int votesReceived = state.getvotesReceived();
                int logSize = state.getLog().size();

                if (votesReceived >= Math.ceil((totalNodes + 1) / 2.0)){

                    state.setCurrentRole(State.LEADER_ROLE);
                    state.setCurrentLeader(node.getNodeId());
                    electionTimer.cancel();

                    // TODO :: CANCELAR LEADER ELECTION (FEITO)

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
}