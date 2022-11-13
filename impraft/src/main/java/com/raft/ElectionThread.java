package com.raft;

import com.raft.Server.serverState;
import com.raft.resources.serverAddress;
import java.rmi.Naming;
import java.util.ArrayList;

public class ElectionThread extends Thread {

  private Server server;
  private boolean isRunning;
  private int timeout;
 
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
        try {
          VoteResponse responseAux;
          BlockingQueue<VoteResponse> responsesQueue = new BlockingQueue<>(10);
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
              VoteResponse entry;
              while (true) {
                if (responsesCount > (server.getClusterArray().length/ 2)) {
                  System.out.println("Lider eleito");
                  server.setCurrentState(serverState.LEADER);
                  Thread.interrupted();
                }
                entry = responsesQueue.dequeue();
                if(entry.voteGranted == true){
                responsesCount++;
            }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          })
            .start();
        } catch (Exception e) {
          e.printStackTrace();
        }
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private synchronized void waitUntilServerIsCandidate()
    throws InterruptedException {
    while (server.getState() != serverState.CANDIDATE) wait();
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
