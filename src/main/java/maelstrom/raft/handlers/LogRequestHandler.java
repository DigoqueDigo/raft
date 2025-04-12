package maelstrom.raft.handlers;
import com.eclipsesource.json.Json;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.state.Log;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.AppendEntries;

/*
 * INPUT 
 * type -> logRequest
 * lId -> identificador do lider
 * lTerm -> termo atual do lider
 * lPrefixLength -> comprimento do log do seguidor (espectavel)
 * lPrefixTerm -> term da ultima entrada do log do seguidor (expectavel)
 * lCommitLength -> indice ate onde os logs foram committed
 * lSuffix -> entradas do log enviadas ao seguidor
 *
 * OUTPUT
 * type -> logResponse
 * fId -> identificador do seguidor
 * fTerm -> termo do seguidor
 * fAck -> comprimento do log aceite pelo seguidor
 * fSuccess -> se o seguidor aceitou o logResquest 
 */


public final class LogRequestHandler implements MessageHandler{

    private Node node;
    private State state;


    public LogRequestHandler(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void handle(Message message){

        final int lTerm = message.body.getInt("lTerm", -1);
        final int lPrefixTerm = message.body.getInt("lPrefixTerm", -1);
        final int lPrefixLength = message.body.getInt("lPrefixLength", -1);
        final String lId = message.body.getString("lId", null);

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

            final int lCommitLength = message.body.getInt("lCommitLength", -1); 
            final Log lSuffix = new Log(message.body.get("lSuffix").asArray());

            AppendEntries.append(lPrefixLength, lCommitLength, lSuffix, state);
            final int ack = lPrefixLength + lSuffix.size();

            node.reply(message, Json.object()
                .add("type", "logResponse")
                .add("fId", node.getNodeId())
                .add("fTerm", state.getCurrentTerm())
                .add("fAck", ack)
                .add("fSuccess", true));
        }

        else{
            node.reply(message, Json.object()
                .add("type", "logResponse")
                .add("fId", node.getNodeId())
                .add("fTerm", state.getCurrentTerm())
                .add("fAck", 0)
                .add("fSuccess", false));
        }
    }
}