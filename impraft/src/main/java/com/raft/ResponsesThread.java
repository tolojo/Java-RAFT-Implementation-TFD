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


    public ResponsesThread(serverAddress server, ArrayList<String> wholeMessage, String data, String label){
        this.server = server;
        this.wholeMessage=wholeMessage;
        this.data=data;
        this.label=label;

    }
    @Override
    public void run(){
        try {
            ServerInterface serverResponse = (ServerInterface) Naming.lookup("rmi://" + server.getIpAddress() + ":" + server.getPort() + "/server");
            response = serverResponse.invokeRPC(wholeMessage, data, label);
            responses.put(response);




        } catch (Exception e) {
            e.printStackTrace();            
        }
        



    }




}
