package maelstrom.raft.handlers;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.protocols.LogReponse;
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

        LogReponse logResponse = new LogReponse(message.body);

        final int fAck = logResponse.fAck();
        final int fTerm = logResponse.fTerm();
        final String fId = logResponse.fId();
        final boolean fSuccess = logResponse.fSuccess();

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