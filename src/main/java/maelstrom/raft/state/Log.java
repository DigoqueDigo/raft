package maelstrom.raft.state;
import java.util.ArrayList;
import java.util.List;
import maelstrom.message.IJson;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;


public class Log implements IJson{

    private List<LogEntry> log;


    public Log(){
        this.log = new ArrayList<>();
    }


    public Log(JsonArray logEntries){
        this.log = new ArrayList<>();
        for (JsonValue logEntry : logEntries){
            this.log.add(new LogEntry(logEntry));
        }
    }


    public int size(){
        return this.log.size();
    }


    public void truncate(int fromIndex, int toIndex){
        this.log = this.log.subList(fromIndex, toIndex);
    }


    public void addLogEntry(LogEntry logEntry){
        this.log.add(logEntry);
    }


    public LogEntry get(int index){
        return (index >= 0 && index < this.log.size()) ? this.log.get(index) : null;
    }


    public Log getSuffix(int prefixLength){
        Log suffixLog = new Log();
        while (prefixLength < this.log.size()){
            suffixLog.addLogEntry(this.log.get(prefixLength++));
        }
        return suffixLog;
    }


    @Override
    public JsonValue toJson(){
        JsonArray Jlog = new JsonArray();
        for (LogEntry entry : this.log){
            Jlog.add(entry.toJson());
        }
        return Jlog;
    }
}