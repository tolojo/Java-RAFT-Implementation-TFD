package com.raft;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.raft.resources.serverAddress;



public interface ServerInterface extends Remote{
    public ArrayList<String> invokeRPC(ArrayList<String> oldMessage, String msg, String label,int term,
	long leaderId,
	int lastLogIndex) throws RemoteException;
    public String quorumInvokeRPC(String label, String data) throws RemoteException;
    public VoteResponse requestVoteRPC(int term, long candidateId, int lastLogIndex, int lastTermIndex) throws RemoteException;
}
