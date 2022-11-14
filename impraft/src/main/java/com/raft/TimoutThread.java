package com.raft;
import com.raft.Server.serverState;

public class TimoutThread extends Thread {

  
  private Server server;
  private boolean isRunning;
  private int  timeout ;

  

  public TimoutThread(Server server) {
    this.server = server;
}

public void run(){
    while(isRunning){
       timeout = server.getTimeOut() * 1000;
       try {
        waitUntilServerIsFollower();
        sleep(timeout);
        server.setCurrentState(serverState.CANDIDATE);
       ElectionThread election = new ElectionThread(server);
    
        election.start();
        server.election.goOn();
        
      

    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    }

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
