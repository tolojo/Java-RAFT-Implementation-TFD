package com.raft;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class raftImplementor extends UnicastRemoteObject implements ClientInterface{

    private String[] msg = {"abc"};
    public raftImplementor() throws RemoteException {
        
    }

    public String[] invokeGET() {

        return msg;
    }
    
}
