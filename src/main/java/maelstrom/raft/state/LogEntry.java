package maelstrom.raft.state;
import maelstrom.message.Message;


public class LogEntry{


    private Message message;
    private int term;


    public LogEntry(Message message, int term){
        this.message = message;
        this.term = term;
    }


    public Message getMessage(){
        return this.message;
    }


    public int getTerm(){
        return this.term;
    }
}