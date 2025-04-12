package maelstrom.raft.handlers;
import maelstrom.message.Error;
import maelstrom.message.IJson;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.state.LogEntry;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.ReplicateLog;

/*
 * INPUT 
 * type -> read/write/cas
 * 
 * OUTPUT
 * type -> logRequest
 * lId -> identificador do lider
 * lTerm -> termo atual do liedr
 * lPrefixLength -> comprimento do log do seguidor (espectavel)
 * lPrefixTerm -> term da ultima entrada do log do seguidor (expectavel)
 * lCommitLength -> indice ate onde os logs foram committed
 * lSuffix -> entradas do log enviadas ao seguidor
 */


public class BroadcastHandler implements MessageHandler{

    private Node node;
    private State state;


    public BroadcastHandler(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void handle(Message message){

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

        else{
            // TODO :: E SUPOSTO ENCAMINHAR A MENSAGEM PARA O LIDER
            IJson error = Error.temporarilyUnavailable("I am not the leader");
            node.reply(message, error);
        }
    }
}