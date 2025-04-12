package maelstrom.raft.timers;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import maelstrom.node.Node;
import maelstrom.node.NodeTimer;
import maelstrom.raft.state.State;
import com.eclipsesource.json.Json;


public class ElectionTimer implements NodeTimer{

    public static final Long LOWER_ELECTION_TIMEOUT_LIMIT = 200L;
    public static final Long UPPER_ELECTION_TIMEOUT_LIMIT = 400L;

    private Node node;
    private State state;

    private final Random random = new Random();
    private ScheduledFuture<?> electionTask;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    public ElectionTimer(Node node, State state){
        this.node = node;
        this.state = state;
    }


    @Override
    public void start(){

        long delay = LOWER_ELECTION_TIMEOUT_LIMIT + random.nextLong(UPPER_ELECTION_TIMEOUT_LIMIT - LOWER_ELECTION_TIMEOUT_LIMIT);

        this.electionTask = executor.schedule(() -> {

            if (!state.isLeader()){

                state.setCurrentRole(State.CANDIDATE_ROLE);
                state.setCurrentTerm(state.getCurrentTerm() + 1);

                state.clearVotes();
                state.addVote(node.getNodeId());
                state.setVotedFor(node.getNodeId());

                int lastTerm = 0;
                int logLength = state.getLog().size();

                if (logLength > 0){
                    lastTerm = state.getLog().get(logLength - 1).getTerm();
                }

                for (String follower : node.getNodeIds()){
                    if (!follower.equals(node.getNodeId())){
                        node.send(follower, Json.object()
                            .add("type", "voteRequest")
                            .add("cId", node.getNodeId())
                            .add("cTerm", state.getCurrentTerm())
                            .add("cLogTerm", lastTerm)
                            .add("cLogLength", logLength));
                    }
                }
            }

            this.start();

        }, delay, TimeUnit.MILLISECONDS);
    }



    public void cancel(){
        if (this.electionTask != null && !this.electionTask.isCancelled()) {
            this.electionTask.cancel(true);
        }
    }


    public void reset(){
        cancel();
        start();
    }
}