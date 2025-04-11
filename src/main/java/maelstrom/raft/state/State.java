package maelstrom.raft.state;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class State{

    public static final String FOLLOWER_ROLE = "follower";
    public static final String CANDIDATE_ROLE = "candidate";
    public static final String LEADER_ROLE = "leader";

    private int currentTerm;
    private int commitLength;

    private String votedFor;
    private String currentRole;
    private String currentLeader;
    private Set<String> votesReceived;

    private Log log;
    private Map<String, Integer> sentLength;
    private Map<String, Integer> ackedLength;


    public State(){
        this.currentTerm = 0;
        this.commitLength = 0;
        this.votedFor = null;
        this.currentLeader = null;
        this.currentRole = FOLLOWER_ROLE;
        this.votesReceived = new HashSet<>();
        this.sentLength = new HashMap<>();
        this.ackedLength = new HashMap<>();
        this.log = new Log();
    }


    public Log getLog(){
        return this.log;
    }


    public int getCurrentTerm(){
        return this.currentTerm;
    }


    public int getCommitLength(){
        return this.commitLength;
    }


    public String getVotedFor(){
        return this.votedFor;
    }


    public String getCurrentRole(){
        return this.currentRole;
    }


    public int getvotesReceived(){
        return this.votesReceived.size();
    }


    public void setCurrentTerm(int currentTerm){
        this.currentTerm = currentTerm;
    }


    public void setCurrentRole(String role){
        this.currentRole = role;
    }


    public void setVotedFor(String votedFor){
        this.votedFor = votedFor;
    }


    public void setCurrentLeader(String currentLeader){
        this.currentLeader = currentLeader;
    }


    public boolean isLeader(){
        return this.currentRole.equals(LEADER_ROLE);
    }


    public void clearVotes(){
        this.votesReceived.clear();
    }


    public void addVote(String voterId){
        this.votesReceived.add(voterId);
    }


    public void putSentLenghtOf(String follower, int length){
        this.sentLength.put(follower, length);
    }


    public int getSentLengthOf(String follower){
        return this.sentLength.get(follower);
    }


    public void putAckedLengthOf(String follower, int length){
        this.ackedLength.put(follower, length);
    }
}