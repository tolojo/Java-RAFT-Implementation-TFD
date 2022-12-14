package com.raft;
import com.raft.Server.serverState;

public class TimoutThread extends Thread {

  
  private Server server;
  private boolean isRunning;
  private int  timeout ;
  ElectionThread election;
  

  public TimoutThread(Server server) {
    this.server = server;
    election = new ElectionThread(server, this);
}

public void run(){
    while(isRunning){
       timeout = server.getTimeOut() * 1000;
       try {
        waitUntilServerIsFollower();
        sleep(timeout);
        server.setCurrentState(serverState.CANDIDATE);
        
        election.start();
        isRunning = false;
        //server.election.goOn();
        
      

    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    }

}
public void stopElection(){
    election.stop();
}
private synchronized void waitUntilServerIsFollower() throws InterruptedException {
    while(server.getState()!= serverState.FOLLOWER){
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
}
