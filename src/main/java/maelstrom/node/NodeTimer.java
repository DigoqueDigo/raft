package maelstrom.node;


public interface NodeTimer{

    public void start();

    public void cancel();

    public void reset();
}