package maelstrom.raft;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.node.NodeTimer;
import maelstrom.raft.handlers.VoteRequestHandler;
import maelstrom.raft.handlers.VoteResponseHandler;
import maelstrom.raft.state.State;
import maelstrom.raft.timers.ElectionTimer;
import maelstrom.raft.timers.HeartBeatTimer;


public class RaftServer{

    public void run(){

        Node node = new Node();
        State state = new State(); 

        NodeTimer electionTimer = new ElectionTimer(node, state);
        NodeTimer heartBeatTimer = new HeartBeatTimer(node, state);

        MessageHandler voteRequestHandler = new VoteRequestHandler(node, state);
        MessageHandler voteReponseHandler = new VoteResponseHandler(node, state);

        node.on("voteRequest", voteRequestHandler);
        node.on("voteResponse", voteReponseHandler);

        electionTimer.start();
        heartBeatTimer.start();

        node.log("Starting up!");
        node.main();
    }
}