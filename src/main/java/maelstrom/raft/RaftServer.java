package maelstrom.raft;
import maelstrom.node.Node;
import maelstrom.raft.handlers.VoteRequestHandler;
import maelstrom.raft.state.State;


public class RaftServer{

    public void run(){

        Node node = new Node();
        State state = new State(); 

        VoteRequestHandler voteRequestHandler = new VoteRequestHandler(node, state);

        node.on("vote_request", voteRequestHandler);
    }
}