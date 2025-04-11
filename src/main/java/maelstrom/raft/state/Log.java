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


    public int size(){
        return this.log.size();
    }


    // TODO :: REMOVER ISTO E UTILIZAR O GET
    public LogEntry getLastLogEntry(){
        int size = this.log.size();
        return size > 0 ? this.log.get(size - 1) : null;
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