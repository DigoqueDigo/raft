package maelstrom.node;
import maelstrom.message.Message;


public interface MessageHandler{
    public void handle(Message message);
}