package com.raft;

import java.rmi.*;
import java.rmi.server.*;

import com.raft.resources.serverAddress;


public interface Invoke extends Remote {
    
        public ServerResponse getInvoke(String message, serverAddress clientid, String label) throws RemoteException;
}
