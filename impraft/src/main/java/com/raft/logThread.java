package com.raft;

import java.rmi.RemoteException;
import java.util.ArrayList;

import com.raft.Server.serverState;
import com.raft.resources.serverAddress;

public class logThread extends Thread {
    
	private Server server;
	private boolean isRunning;
    private serverAddress followerId;
    private ArrayList<String> leaderLog;
	ArrayList<String> followerLog;

	public logThread(Server server, ArrayList<String> followerLog) {
		this.server = server;
		this.followerLog = followerLog;
        followerId = server.getId();
        leaderLog = server.getWholeMessage();
	} 



	@Override
	public void run() {
		while(isRunning) {
			if(leaderLog.size()-1)
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
