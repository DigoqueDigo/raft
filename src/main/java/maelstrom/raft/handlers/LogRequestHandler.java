package maelstrom.raft.handlers;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.node.NodeTimer;
import maelstrom.raft.protocols.LogResponse;
import maelstrom.raft.protocols.LogRequest;
import maelstrom.raft.state.Log;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.AppendEntries;


public final class LogRequestHandler implements MessageHandler{

    private Node node;
    private State state;
    private NodeTimer electionTimer;


    public LogRequestHandler(Node node, State state, NodeTimer electionTimer){
        this.node = node;
        this.state = state;
        this.electionTimer = electionTimer;
    }


    @Override
    public void handle(Message message){

        synchronized (state){

            electionTimer.reset();
            LogRequest logRequest = new LogRequest(message.body);

            final String lId = logRequest.lId();
            final int lTerm = logRequest.lTerm();
            final int lPrefixTerm = logRequest.lPrefixTerm();
            final int lPrefixLength = logRequest.lPrefixLength();

            if (lTerm > state.getCurrentTerm()){
                state.setCurrentTerm(lTerm);
                state.setVotedFor(null);
                electionTimer.cancel();
            }

            if (lTerm == state.getCurrentTerm()){
                state.setCurrentRole(State.FOLLOWER_ROLE);
                state.setCurrentLeader(lId);
            }

            int prefixTerm = -1;
            int logLength = state.getLog().size();

            if (lPrefixLength > 0 && lPrefixLength - 1 < logLength){
                prefixTerm = state.getLog().get(lPrefixLength - 1).getTerm();
            }

            boolean termOK = lTerm == state.getCurrentTerm();
            boolean logOK = (logLength >= lPrefixLength) && (lPrefixLength == 0 || prefixTerm == lPrefixTerm);

            if (termOK && logOK){

                final int lCommitLength = logRequest.lCommitLength();
                final Log lSuffix = new Log(logRequest.lSuffix().asArray());
                final int ack = lPrefixLength + lSuffix.size();

                AppendEntries.append(lPrefixLength, lCommitLength, lSuffix, state);

                node.reply(message, new LogResponse(
                    node.getNodeId(),
                    state.getCurrentTerm(),
                    ack,
                    true
                ));
            }

            else{
                node.reply(message, new LogResponse(
                    node.getNodeId(),
                    state.getCurrentTerm(),
                    0,
                    false
                ));
            }
        }
    }
}