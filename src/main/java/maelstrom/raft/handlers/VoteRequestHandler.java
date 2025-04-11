package maelstrom.raft.handlers;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.state.State;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

/*
 * input type -> voteRequest
 * output type -> voteResponse
 *
 * cId -> identificador do candidato
 * cTerm -> termo do candidato
 * cLogTerm -> termo da ultima entrada do candidato
 * cLogLength -> tamanho do log do candidato
 *
 * vTerm -> termo do eleitor
 * voteGranted -> voto no candidato
 */


public final class VoteRequestHandler implements MessageHandler{

    private Node node;
    private State state;


    public VoteRequestHandler(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void handle(Message message){

        JsonObject body = message.body;
        int cTerm = body.getInt("cTerm", -1);
        int cLogTerm = body.getInt("cLogTerm", -1);
        int cLogLength = body.getInt("cLogLength", -1);
        String cId = body.getString("cId", null);

        // o candidato tem um termo superior, atualizar o meu termo e role
        if (cTerm > state.getCurrentTerm()){
            state.setCurrentTerm(cTerm);
            state.setVotedFor(null);
            state.setCurrentRole(State.FOLLOWER_ROLE);
        }

        String votedFor = state.getVotedFor();
        int currentTerm = state.getCurrentTerm();

        int lastTerm = 0;
        int logLength = state.getLog().size();

        if (logLength > 0){
            lastTerm = state.getLog().getLastLogEntry().getTerm();
        }

        // verificar se o log do candidato esta tao atualizado quanto o meu
        boolean termOK = cTerm == currentTerm;
        boolean votedForOk = votedFor == null || votedFor.equals(cId);
        boolean logOk = (cLogTerm > lastTerm) || (cLogTerm == lastTerm && cLogLength >= logLength);

        if (termOK && logOk && votedForOk){
            state.setVotedFor(cId);
            node.reply(message, Json.object()
                .add("type", "voteResponse")
                .add("vTerm", currentTerm)
                .add("voteGranted", true));
        }

        else{
            node.reply(message, Json.object()
                .add("type", "voteResponse")
                .add("vTerm", currentTerm)
                .add("voteGranted", false));
        }
    }
}