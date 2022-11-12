package com.raft;

import com.raft.Server.serverState;

public class ElectionThread extends Thread{
    
  private Server server;
  private boolean isRunning;
  private int  timeout ;

  

  public ElectionThread(Server server) {
    this.server = server;
}

public void run(){
    while(isRunning){
        try {
            waitUntilServerIsCandidate();
            
        
        
        
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
    }

}
private synchronized void waitUntilServerIsCandidate() throws InterruptedException {
    while(server.getState() != serverState.CANDIDATE) 
        wait();
}



@Override
	public synchronized void start() {
		isRunning = true;
		super.start();

	}
    
    public void interrupt(){
        this.interrupt();
    }
    
}
