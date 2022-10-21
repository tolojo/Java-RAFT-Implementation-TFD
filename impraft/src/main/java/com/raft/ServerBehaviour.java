package com.raft;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerBehaviour extends Remote {

    
    public ServerResponse execute(String command, String commandID) throws RemoteException;
    
}
