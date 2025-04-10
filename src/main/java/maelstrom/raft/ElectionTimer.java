package maelstrom.raft;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import maelstrom.node.Node;
import maelstrom.raft.state.LogEntry;
import maelstrom.raft.state.State;
import com.eclipsesource.json.Json;

/*
 * output type -> voteRequest
 *
 * cId -> identificador do candidato
 * cTerm -> termo do candidato
 * cLogTerm -> termo da ultima entrada do candidato
 * cLogLength -> tamanho do log do candidato
 */


public class ElectionTimer{

    public static final Integer LOWER_ELECTION_TIMEOUT_LIMIT = 150;
    public static final Integer UPPER_ELECTION_TIMEOUT_LIMIT = 300;

    private Node node;
    private State state;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();


    public ElectionTimer(Node node, State state){
        this.node = node;
        this.state = state;
    }


    public void start(){

        long currentTime = System.currentTimeMillis();
        int delay = random.nextInt(UPPER_ELECTION_TIMEOUT_LIMIT - LOWER_ELECTION_TIMEOUT_LIMIT);
        delay += LOWER_ELECTION_TIMEOUT_LIMIT;

        executor.schedule(() -> {

            long elapsedTime = System.currentTimeMillis() - currentTime;

            if (!state.isLeader() && elapsedTime >= UPPER_ELECTION_TIMEOUT_LIMIT){

                state.setCurrentRole(State.CANDIDATE_ROLE);
                state.setCurrentTerm(state.getCurrentTerm() + 1);

                state.clearVotes();
                state.addVote(node.getNodeId());
                state.setVotedFor(node.getNodeId());

                int lastTerm = 0;
                int logLength = state.getLog().size();
                LogEntry lastLogEntry = state.getLog().getLastLogEntry();

                if (lastLogEntry != null){
                    lastTerm = lastLogEntry.getTerm();
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


    public void shutdown(){
        executor.shutdown();
    }
}