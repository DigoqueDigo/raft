package maelstrom.raft.handlers;
import maelstrom.message.Error;
import maelstrom.message.IJson;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.state.LogEntry;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.ReplicateLog;


public final class BroadcastHandler implements MessageHandler{

    private Node node;
    private State state;


    public BroadcastHandler(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void handle(Message message){

        synchronized (state){

            if (state.isLeader()){

                int currentTerm = state.getCurrentTerm();
                LogEntry logEntry = new LogEntry(message, currentTerm);

                state.getLog().addLogEntry(logEntry);
                state.putAckedLengthOf(node.getNodeId(), state.getLog().size());

                for (String follower : node.getNodeIds()){
                    if (!follower.equals(node.getNodeId())){
                        ReplicateLog.replicate(node, follower, state);
                    }
                }
            }

            else if (state.getCurrentLeader() != null){
                node.forward(state.getCurrentLeader(), message);
            }

            else{
                IJson error = Error.temporarilyUnavailable("I am not the leader and dont know who is");
                node.reply(message, error);
            }
        }
    }
}