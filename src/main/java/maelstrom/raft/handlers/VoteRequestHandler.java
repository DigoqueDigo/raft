package maelstrom.raft.handlers;
import maelstrom.message.Message;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.protocols.VoteRequest;
import maelstrom.raft.protocols.VoteResponse;
import maelstrom.raft.state.State;


public final class VoteRequestHandler implements MessageHandler{

    private Node node;
    private State state;


    public VoteRequestHandler(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void handle(Message message){

        synchronized (state){

            VoteRequest voteRequest = new VoteRequest(message.body);

            final String cId = voteRequest.cId();
            final int cTerm = voteRequest.cTerm();
            final int cLastTerm = voteRequest.cLastTerm();
            final int cLogLength = voteRequest.cLogLength();

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
                lastTerm = state.getLog().get(logLength - 1).getTerm();
            }

            // verificar se o log do candidato esta tao atualizado quanto o meu
            boolean termOK = cTerm == currentTerm;
            boolean votedForOk = votedFor == null || votedFor.equals(cId);
            boolean logOk = (cLastTerm > lastTerm) || (cLastTerm == lastTerm && cLogLength >= logLength);

            if (termOK && logOk && votedForOk){
                state.setVotedFor(cId);
                node.reply(message, new VoteResponse(
                    node.getNodeId(),
                    currentTerm,
                    true
                ));
            }

            else{
                node.reply(message, new VoteResponse(
                    node.getNodeId(),
                    currentTerm,
                    false
                ));
            }
        }
    }
}