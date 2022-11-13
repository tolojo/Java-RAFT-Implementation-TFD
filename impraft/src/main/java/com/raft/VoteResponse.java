package com.raft;

public class VoteResponse {
    public boolean voteGranted;
    public int term;

    public VoteResponse(boolean voteGranted, int term){
        this.voteGranted = voteGranted;
        this.term = term;
    }
    
}
