package com.raft;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import com.raft.resources.serverAddress;

public class ResponsesThread extends Thread {

    private ArrayBlockingQueue<ArrayList<String>> responses = new ArrayBlockingQueue<>(10);
    private serverAddress server;
    private ArrayList<String> wholeMessage;
    private String    data;
    private String    label;
    private ArrayList<String>response;
    private int currentTerm;
    private serverAddress leaderId;
    private int lastLogIndex;


    public ResponsesThread(serverAddress server, ArrayList<String> wholeMessage, String data, String label,int currentTerm, serverAddress leaderId, int lastLogIndex){
        this.server = server;
        this.wholeMessage=wholeMessage;
        this.data=data;
        this.label=label;
        this.currentTerm = currentTerm;
        this.leaderId = new serverAddress();
        this.lastLogIndex = lastLogIndex;
    }
    @Override
    public void run(){
        try {
            ServerInterface serverResponse = (ServerInterface) Naming.lookup("rmi://" + server.getIpAddress() + ":" + server.getPort() + "/server");
            response = serverResponse.invokeRPC(0,wholeMessage, data, label,currentTerm,leaderId, lastLogIndex);
            responses.put(response);




        } catch (Exception e) {
            e.printStackTrace();            
        }
        



    }




}
