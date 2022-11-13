package com.raft;

import java.rmi.RemoteException;

import com.raft.Server.serverState;

public class HeartBeatThread extends Thread {
    
	private Server server;
	private boolean isRunning;

	public HeartBeatThread(Server server) {
		this.server = server;
	} 



	@Override
	public void run() {
		while(isRunning) {
			try {
				waitUntilServerIsLeader();

				try {
					System.out.println("heartbeat signal");
                    server.quorumInvokeRPC("ADD","");
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
				sleep(server.getHeartBeatTimer()*1000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


    private synchronized void waitUntilServerIsLeader() throws InterruptedException {
		while(server.getState() != serverState.LEADER) {
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
