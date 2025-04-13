package maelstrom.raft.timers;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import maelstrom.node.Node;
import maelstrom.node.NodeTimer;
import maelstrom.raft.state.State;
import maelstrom.raft.utils.ReplicateLog;


public class HeartBeatTimer implements NodeTimer{

    public static final Long HEART_BEAT_DELAY = 0L;
    public static final Long HEART_BEAT_PERIOD = 200L;
    
    private Node node;
    private State state;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    public HeartBeatTimer(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void start(){
        executor.scheduleAtFixedRate(() -> {
            synchronized (state){
                if (state.isLeader()){
                    for (String follower : node.getNodeIds()){
                        if (!follower.equals(node.getNodeId())){
                            ReplicateLog.replicate(node, follower, state);
                        }
                    }
                }
            }
        }, HEART_BEAT_DELAY, HEART_BEAT_PERIOD, TimeUnit.MILLISECONDS);
    }


    @Override
    public void cancel(){
        throw new UnsupportedOperationException("Unimplemented method 'cancel'");
    }


    @Override
    public void reset(){
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }
}