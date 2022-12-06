package com.raft;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.raft.resources.serverAddress;



public interface ServerInterface extends Remote{
    public ArrayList<String> invokeRPC(int id, ArrayList<String> oldMessage, String msg, String label,int term,
	serverAddress leaderId, int lastLogIndex,boolean commit) throws RemoteException;
    public String quorumInvokeRPC(String label, String data) throws RemoteException;
    public boolean requestVoteRPC(int term, serverAddress candidateId, int lastLogIndex, int lastTermIndex) throws RemoteException;
}
