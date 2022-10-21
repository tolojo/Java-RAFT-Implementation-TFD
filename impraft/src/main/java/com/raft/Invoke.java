package com.raft;

import java.rmi.*;
import java.rmi.server.*;

import com.raft.resources.serverAddress;


public class Invoke extends UnicastRemoteObject implements ClientInterface {

        private String message;
        public Invoke(String msg) throws RemoteException{
                message = msg;
        }
        public String send() throws RemoteException {
                return message;
        }
}
