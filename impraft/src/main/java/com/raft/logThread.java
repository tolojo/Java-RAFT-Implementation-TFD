package com.raft;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.raft.Server.serverState;
import com.raft.resources.serverAddress;

public class logThread extends Thread {
    
	private Server server;
	private boolean isRunning;
    private serverAddress followerId;
	private ArrayList<String> leaderLog;
	private int lastLogIndexFollower;
	
	

	

	public logThread(Server server, int lastLogIndexFollower, serverAddress followerId) {
		this.server = server;
		this.lastLogIndexFollower = lastLogIndexFollower;
		this.followerId = followerId;
		leaderLog = server.getWholeMessage();
		
	} 



	@Override
	public void run() {
		if(server.getLastLogIndex()!= lastLogIndexFollower){
			
		}
		else{
			stop();
		}
		
	}
	public void sendEntry(int id, String msg, serverAddress followerId ){
		try {
			ServerInterface server = (ServerInterface) Naming.lookup(
			      "rmi://" +
			      followerId.getIpAddress() +
			      ":" +
			      followerId.getPort() +
			      "/server"
			    );
				server.invokeRPC();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void goOn() {
		notifyAll();
	}


    @Override
	public synchronized void start() {
		
		super.start();
	}

}
