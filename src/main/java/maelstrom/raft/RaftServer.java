package maelstrom.raft;
import maelstrom.message.MessageHandler;
import maelstrom.node.Node;
import maelstrom.node.NodeTimer;
import maelstrom.raft.handlers.BroadcastHandler;
import maelstrom.raft.handlers.LogRequestHandler;
import maelstrom.raft.handlers.LogResponseHandler;
import maelstrom.raft.handlers.VoteRequestHandler;
import maelstrom.raft.handlers.VoteResponseHandler;
import maelstrom.raft.timers.ElectionTimer;
import maelstrom.raft.timers.HeartBeatTimer;
import maelstrom.raft.state.State;


public class RaftServer{

    public void run(){

        Node node = new Node();
        State state = new State(); 

        NodeTimer electionTimer = new ElectionTimer(node, state);
        NodeTimer heartBeatTimer = new HeartBeatTimer(node, state);

        MessageHandler voteRequestHandler = new VoteRequestHandler(node, state);
        MessageHandler voteReponseHandler = new VoteResponseHandler(node, state);
        MessageHandler broadcastHandler = new BroadcastHandler(node, state);
        MessageHandler logRequestHandler = new LogRequestHandler(node, state);
        MessageHandler logResponseHandler = new LogResponseHandler(node, state);

        node.on("voteRequest", voteRequestHandler);
        node.on("voteResponse", voteReponseHandler);
        node.on("read", broadcastHandler);
        node.on("write", broadcastHandler);
        node.on("cas", broadcastHandler);
        node.on("logRequest", logRequestHandler);
        node.on("logResponse", logResponseHandler);

        electionTimer.start();
        heartBeatTimer.start();

        node.log("Starting up!");
        node.main();
    }
}