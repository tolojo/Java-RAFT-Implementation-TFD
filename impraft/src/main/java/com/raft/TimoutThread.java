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
        System.out.println(server.getState());
      

    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    }

}
private synchronized void waitUntilServerIsFollower() throws InterruptedException {
    serverState state = server.getState();
    while(state != serverState.FOLLOWER){
        state = server.getState();
        
    }
}



@Override
	public synchronized void start() {
		isRunning = true;
		super.start();
	}
}
