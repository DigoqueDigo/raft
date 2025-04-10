package maelstrom;
import maelstrom.raft.RaftServer;


public class Main{
    public static void main(String[] args){
        new RaftServer().run();
    }
}