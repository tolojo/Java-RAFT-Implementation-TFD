package com.raft;

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;


public class Invoke extends UnicastRemoteObject implements ServerInterface {

        private ArrayList<String> exception = new ArrayList<String>();
        private String output = "Invalid label";
        private ArrayList<String> responseAdded= new ArrayList<String>();
        private String output1 = "String Added";
        private ArrayList<String> responseAlreadyAdded= new ArrayList<String>();
        private String output2 = "String already in the message";
        boolean flag = false;
        public Invoke() throws RemoteException{
        }
        public ArrayList<String> invokeRPC(ArrayList<String> oldMessage,String msg, String label) throws RemoteException {
                System.out.println(label);
                exception.add(output);
                responseAdded.add(output1);
                responseAlreadyAdded.add(output2);
                if (label.equals("ADD")){
                        for(int i=0 ; i<oldMessage.size() ; i++){
                                if(oldMessage.get(i) == msg){
                                        flag = true;
                                }
                        }

                        if(flag){
                                return oldMessage;
                        }

                        else if(flag==false){
                                oldMessage.add(msg);
                                return oldMessage;
                        }
                }
                else{
                        System.out.println(exception); 
                        return oldMessage;
                }
                return oldMessage;
               
        }
}
