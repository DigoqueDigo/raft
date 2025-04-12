package maelstrom.raft.handlers;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.CommitLength;
import maelstrom.raft.utils.ReplicateLog;


public class LogResponseHandler implements MessageHandler{

    private Node node;
    private State state;


    public LogResponseHandler(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void handle(Message message){

        final int fAck = message.body.getInt("fAck", -1);
        final int fTerm = message.body.getInt("fTerm", -1);
        final String fId = message.body.getString("fId", null);
        final boolean fSuccess = message.body.getBoolean("fSuccess", false);

        if (fTerm > state.getCurrentTerm()){
            state.setCurrentTerm(fTerm);
            state.setCurrentRole(State.FOLLOWER_ROLE);
            state.setVotedFor(null);
            // TODO :: CANCELAR O ELECTION TIMER (ACHO QUE NAO E PRECISO)
        }

        else if (fTerm == state.getCurrentTerm() && state.isLeader()){

            if (fSuccess && fAck >= state.getAckedLengthOf(fId)){
                state.putSentLenghtOf(fId, fAck);
                state.putAckedLengthOf(fId, fAck);
                CommitLength.commit(node, state);
            }

            else if (state.getSentLengthOf(fId) > 0){
                state.putSentLenghtOf(fId, state.getSentLengthOf(fId) - 1);
                ReplicateLog.replicate(node, fId, state);
            }
        }
    }
}