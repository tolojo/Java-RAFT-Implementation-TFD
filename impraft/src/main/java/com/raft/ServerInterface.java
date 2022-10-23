package com.raft;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.raft.resources.serverAddress;



public interface ServerInterface extends Remote{
    public ArrayList<String> invokeRPC(ArrayList<String> oldMessage, String msg, String label) throws RemoteException;
    public String quorumInvokeRPC(serverAddress[] server,String label, String data) throws RemoteException;
}
