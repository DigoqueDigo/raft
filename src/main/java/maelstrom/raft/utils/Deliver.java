package maelstrom.raft.utils;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import maelstrom.message.Message;
import maelstrom.raft.state.State;
import maelstrom.message.Error;


public final class Deliver{

    private static Map<String, BiFunction<Message, State, JsonObject>> handlers = new HashMap<>();

    static{
        handlers.put("read", (message, state) -> Deliver.handleRead(message, state));
        handlers.put("write", (message, state) -> Deliver.handleWrite(message, state));
        handlers.put("cas", (message, state) -> Deliver.handleCas(message, state));
    }


    public static JsonObject deliver(State state, Message message){

        JsonObject bodyReply = null;
        String type = message.body.getString("type", null);

        if (handlers.containsKey(type)){
            bodyReply = handlers.get(type).apply(message, state);
        }

        return bodyReply;
    }


    public static JsonObject handleRead(Message message, State state){

        JsonObject bodyReply = null;
        Integer key = message.body.getInt("key", 0);
        Integer value = state.storeRead(key);

        if (value == null){
            Error error = Error.keyDoesNotExist("key not founded");
            bodyReply = error.toJson().asObject();
        }

        else{
            bodyReply = Json.object()
                .add("type", "read_ok")
                .add("value", value);
        }

        return bodyReply;
    }


    public static JsonObject handleWrite(Message message, State state){

        Integer key = message.body.getInt("key", 0);
        Integer value = message.body.getInt("value", 0);

        state.storeWrite(key, value);

        return Json.object()
            .add("type", "write_ok");
    }


    public static JsonObject handleCas(Message message, State state){

        JsonObject bodyReply = null;

        Integer key = message.body.getInt("key", 0);
        Integer from = message.body.getInt("from", 0);
        Integer to = message.body.getInt("to", 0);
        Integer value = state.storeRead(key);

        if (value == null){
            Error error = Error.keyDoesNotExist("key not founded");
            bodyReply = error.toJson().asObject();
        }

        else if (value != from){
            Error error = Error.preconditionFailed("value and from are different");
            bodyReply = error.toJson().asObject();
        }

        else{
            state.storeWrite(key, to);
            bodyReply = Json.object()
                .add("type", "cas_ok");
        }

        return bodyReply;
    }
}