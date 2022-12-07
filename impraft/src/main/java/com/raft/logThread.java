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


		
	}
	public void sendEntry(int id, String msg, serverAddress followerId, Server server, int lastLogIndex){
		try {
			ArrayList<String> aux;
			ServerInterface serverI = (ServerInterface) Naming.lookup(
			      "rmi://" +
			      followerId.getIpAddress() +
			      ":" +
			      followerId.getPort() +
			      "/server"
			    );
				aux = serverI.invokeRPC(id,leaderLog,msg,"ADD",server.getCurrentTerm(),server.getId(), lastLogIndex, false);
				aux = serverI.invokeRPC(id,leaderLog,msg,"ADD",server.getCurrentTerm(),server.getId(), lastLogIndex, true);
				if(lastLogIndex!= aux.size()-1){
					
				};

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
