package com.raft;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote{
    public String[] invokeGET() throws RemoteException;
}
