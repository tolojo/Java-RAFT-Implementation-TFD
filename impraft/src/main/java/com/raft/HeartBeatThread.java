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
		serverState state = server.getState();
		while(state != serverState.LEADER) {
			state = server.getState();
			System.out.println(state+ "L");
		}
	}


    @Override
	public synchronized void start() {
		isRunning = true;
		super.start();
	}

}
