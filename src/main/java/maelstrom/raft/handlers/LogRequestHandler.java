package maelstrom.raft.handlers;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.protocols.LogReponse;
import maelstrom.raft.protocols.LogRequest;
import maelstrom.raft.state.Log;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.AppendEntries;


public final class LogRequestHandler implements MessageHandler{

    private Node node;
    private State state;


    public LogRequestHandler(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void handle(Message message){

        synchronized (state){

            LogRequest logRequest = new LogRequest(message.body);

            final String lId = logRequest.lId();
            final int lTerm = logRequest.lTerm();
            final int lPrefixTerm = logRequest.lPrefixTerm();
            final int lPrefixLength = logRequest.lPrefixLength();

            if (lTerm > state.getCurrentTerm()){
                state.setCurrentTerm(lTerm);
                state.setVotedFor(null);
            }

            if (lTerm == state.getCurrentTerm()){
                state.setCurrentRole(State.FOLLOWER_ROLE);
                state.setCurrentLeader(lId);
            }

            int prefixTerm = -1;
            int logLength = state.getLog().size();

            if (lPrefixLength - 1 < logLength){
                prefixTerm = state.getLog().get(lPrefixLength - 1).getTerm();
            }

            boolean termOK = lTerm == state.getCurrentTerm();
            boolean logOK = (logLength >= lPrefixLength) && (lPrefixLength == 0 || prefixTerm == lPrefixTerm);

            if (termOK && logOK){

                final int lCommitLength = logRequest.lCommitLength(); 
                final Log lSuffix = new Log(logRequest.lSuffix().asArray());

                AppendEntries.append(lPrefixLength, lCommitLength, lSuffix, state);
                final int ack = lPrefixLength + lSuffix.size();

                node.reply(message, new LogReponse(
                    node.getNodeId(),
                    state.getCurrentTerm(),
                    ack,
                    true
                ));
            }

            else{
                node.reply(message, new LogReponse(
                    node.getNodeId(),
                    state.getCurrentTerm(),
                    0,
                    false
                ));
            }
        }
    }
}