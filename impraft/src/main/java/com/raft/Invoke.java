package com.raft;

import java.rmi.*;
import java.rmi.server.*;


public class Invoke extends UnicastRemoteObject implements ServerInterface {

        private String message;

        public Invoke(String msg) throws RemoteException{
                message = msg;
        }
        public String send() throws RemoteException {
                return message;
        }
}
