package com.raft;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote{
    public String send() throws RemoteException;
}
