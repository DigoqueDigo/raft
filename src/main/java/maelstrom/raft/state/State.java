package maelstrom.raft.state;

public class State{

    public static final String FOLLOWER_ROLE = "follower";
    public static final String CANDIDATE_ROLE = "candidate";
    public static final String LEADER_ROLE = "leader";

    private int currentTerm;
    private String votedFor;
    private String currentRole;
    private Log log; 


    public State(){
        this.currentTerm = 0;
        this.votedFor = null;
        this.currentRole = "follower";
        this.log = new Log();
    }


    public Log getLog(){
        return this.log;
    }


    public int getCurrentTerm(){
        return this.currentTerm;
    }


    public void setCurrentTerm(int currentTerm){
        this.currentTerm = currentTerm;
    }


    public boolean isLeader(){
        return this.currentRole.equals(LEADER_ROLE);
    }


    public String getVotedFor(){
        return this.votedFor;
    }


    public void setCurrentRole(String role){
        this.currentRole = role;
    }


    public void setVotedFor(String votedFor){
        this.votedFor = votedFor;
    }
}