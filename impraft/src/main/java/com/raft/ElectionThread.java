package com.raft;

import com.raft.Server.serverState;
import com.raft.resources.serverAddress;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ElectionThread extends Thread {

  private Server server;
  private boolean isRunning;

 
  private long candidateId;
  private int clastLogIndex;
  private int lastTermIndex;

  public ElectionThread(Server server) {
    this.server = server;
    candidateId = server.getServerId();
    clastLogIndex = server.getLastLogIndex();
    lastTermIndex = server.getCurrentTerm();
  }

  public void run() {
    while (isRunning) {
        try {
          waitUntilServerIsCandidate();
          
          boolean responseAux;
          BlockingQueue<Boolean> responsesQueue = new BlockingQueue<>(10);
          for (int i = 0; i < server.getClusterArray().length; i++) {
            serverAddress serverAux = server.getClusterArray()[i];
            ServerInterface server = (ServerInterface) Naming.lookup(
              "rmi://" +
              serverAux.getIpAddress() +
              ":" +
              serverAux.getPort() +
              "/server"
            );
            responseAux = server.requestVoteRPC(lastTermIndex+1,candidateId,clastLogIndex,lastTermIndex);
            
            responsesQueue.enqueue(responseAux);
          }
          new Thread(() -> {
            try {
              int responsesCount = 0;
              boolean entry;
              
              while (true) {
                
                if (responsesCount > (server.getClusterArray().length/ 2)) {
                  System.out.println("Lider eleito");
                  server.setCurrentState(serverState.LEADER);
                  server.quorumInvokeRPC("ADD","");
                  Thread.interrupted();
                }
                entry = responsesQueue.dequeue();
                if(entry == true){
                responsesCount++;
            }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          })
            .start();
        
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (NotBoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private synchronized void waitUntilServerIsCandidate()
    throws InterruptedException {
      serverState state = server.getState();
    while (state != serverState.CANDIDATE) {
      state = server.getState();
      System.out.println(state + "E");
      
    }
    
    
  }

  @Override
  public synchronized void start() {
    isRunning = true;
    super.start();
  }

  public void interrupt() {
    this.interrupt();
  }
}
