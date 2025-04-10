package maelstrom.raft;
import maelstrom.node.MessageHandler;
import maelstrom.node.Node;
import maelstrom.raft.handlers.VoteRequestHandler;
import maelstrom.raft.handlers.VoteResponseHandler;
import maelstrom.raft.state.State;


public class RaftServer{

    public void run(){

        Node node = new Node();
        State state = new State(); 

        MessageHandler voteRequestHandler = new VoteRequestHandler(node, state);
        MessageHandler voteReponseHandler = new VoteResponseHandler(node, state);

        node.on("voteRequest", voteRequestHandler);
        node.on("voteResponse", voteReponseHandler);
    }
}