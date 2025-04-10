package maelstrom.node;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.function.Consumer;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import maelstrom.message.Error;
import maelstrom.message.IJson;
import maelstrom.message.Message;


public class Node{

    private long nextMessageId = 0;
    private String nodeId = "uninitialized";
    private List<String> nodeIds = new ArrayList<String>();
    private final Map<String, Consumer<Message>> requestHandlers = new HashMap<String, Consumer<Message>>();


    // Get nodeid
    public String getNodeId(){
        return this.nodeId;
    }


    // Get nodeids
    public List<String> getNodeIds(){
        return this.nodeIds;
    }


    // Registers a request handler for the given type of message
    public Node on(String type, Consumer<Message> handler){
        this.requestHandlers.put(type, handler);
        return this;
    }


    // Generate a new message ID
    private long newMessageId(){
        final long id = nextMessageId;
        this.nextMessageId++;
        return id;
    }


    // Log a message to stderr
    public void log(String message){
        TimeZone tz = TimeZone.getDefault();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(tz);
        System.err.println(df.format(new Date()) + " " + message);
        System.err.flush();
    }


    // Send a message to stdout
    private void send(final Message message){
        log("Sending  " + message.toJson());
        System.out.println(message.toJson());
        System.out.flush();
    }


    // Send a message to a specific node, assigns a message ID if one is not set
    public void send(String dest, JsonObject body){
        if (body.getLong("msg_id", -1) == -1){
            body = Json.object().merge(body).set("msg_id", newMessageId());
        }
        send(new Message(this.nodeId, dest, body));
    }


    // Reply to a specific request message with a JsonObject body
    public void reply(Message request, JsonObject body){
        final Long msg_id = request.body.getLong("msg_id", -1);
        final JsonObject body2 = Json.object().merge(body).set("in_reply_to", msg_id);
        send(request.src, body2);
    }


    // Reply to a message with a Json-coercable object as the body
    public void reply(Message request, IJson body){
        reply(request, body.toJson().asObject());
    }


    // Handle an init message, setting up our state
    private void handleInit(Message message){
        this.nodeId = message.body.getString("node_id", null);
        for (JsonValue id : message.body.get("node_ids").asArray()){
            this.nodeIds.add(id.asString());
        }
        reply(message, Json.object().add("type", "init_ok"));
    }

    
    // Handles a parsed message from STDIN
    public void handleMessage(Message message) throws Error{
        final String type = message.body.getString("type", null);
        Consumer<Message> handler = requestHandlers.get(type);    
        log("Handling " + message);

        if (handler != null){
            handler.accept(message);
        }

        else{
            throw Error.notSupported("Don't know how to handle a request of type " + type);
        }
    }


    // The mainloop, consumes lines of JSON from STDIN
    public void main(){

        final Scanner scanner = new Scanner(System.in);
        on("init", (message) -> handleInit(message));

        try{
            while (scanner.hasNextLine()){
                final String line = scanner.nextLine();
                final Message message = new Message(Json.parse(line).asObject());
                handleMessage(message);
            }
        }

        catch (Exception e){
            log("Fatal error! " + e);
            e.printStackTrace();
        }

        finally{
            scanner.close();
        }
    }
}