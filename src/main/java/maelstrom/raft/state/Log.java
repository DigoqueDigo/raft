package maelstrom.raft.state;
import java.util.ArrayList;
import java.util.List;


public class Log{

    private List<LogEntry> log;


    public Log(){
        this.log = new ArrayList<>();
    }


    public int size(){
        return this.log.size();
    }


    public LogEntry getLastLogEntry(){
        int size = this.log.size();
        return size > 0 ? this.log.get(size - 1) : null;
    }
}