package com.raft;

import com.raft.Server.serverState;
import com.raft.resources.serverAddress;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class ElectionThread extends Thread {

  private Server server;
  private boolean isRunning;

 
  private serverAddress candidateId;
  private int clastLogIndex;
  private int lastTermIndex;

  private int falhas =0;

  public ElectionThread(Server server) {
    this.server = server;
    candidateId = server.getId();
    clastLogIndex = server.getLastLogIndex();
    lastTermIndex = server.getCurrentTerm();
    
  }

  public void run() {
    while (isRunning) {
        try {
          waitUntilServerIsCandidate();
          System.out.println("entrei na election");
          System.out.println(server.getState());
          boolean responseAux;
          BlockingQueue<Boolean> responsesQueue = new BlockingQueue<>(10);
          for (int i = 0; i < server.getClusterArray().length; i++) {
            serverAddress serverAux = server.getClusterArray()[i];
            ServerInterface serverI;
            try {
              serverI = (ServerInterface) Naming.lookup(
                "rmi://" +
                serverAux.getIpAddress() +
                ":" +
                serverAux.getPort() +
                "/server"
              );
            
            
            responseAux = serverI.requestVoteRPC(lastTermIndex+1,candidateId,clastLogIndex,lastTermIndex);
            responsesQueue.enqueue(responseAux);
          } 
            catch (RemoteException e) {
              falhas++;
              e.printStackTrace();
              continue;
            }
          }
          new Thread(() -> {
            try {
              if(falhas>=(server.getClusterArray().length/2)+1){
                System.out.println("Não existe a maioria");
                isRunning = false;
                wait();
              }

              int responsesCount = 0;
              boolean entry;
              boolean oneTimeRun = true;
              while (oneTimeRun) {
                System.out.println(responsesCount);
                
                if (responsesCount > (server.getClusterArray().length/ 2)) {
                  System.out.println("Lider eleito");
                  server.setCurrentState(serverState.LEADER);
                  System.out.println("last term index: "+lastTermIndex);
                  System.out.println("last term index + 1: "+lastTermIndex+1);
                  server.setCurrentTerm(((Integer) lastTermIndex) + 1);
                  System.out.println(server.getCurrentTerm());
                  
                  server.quorumInvokeRPC("ADD","");
                  server.heartbeat.goOn();
                  oneTimeRun = false;
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
      } 
       catch (NotBoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private synchronized void waitUntilServerIsCandidate()
    throws InterruptedException {
    while (server.getState()!= serverState.CANDIDATE) {
     wait();
      
    }
  }

  public synchronized void goOn() {
		notifyAll();
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
