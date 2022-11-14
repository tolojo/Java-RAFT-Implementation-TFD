package com.raft.launcher;

import com.raft.Client;
import com.raft.resources.serverAddress;
public class clientLauncher {
    
	public static void main(String[] args) {
		Client c = new Client();
		serverAddress s = new serverAddress("127.0.0.1", 8002);
		//c.request(s, "GET", "a3c1231");
		c.requestQuorum(s, "ADD", "a3c1");
	}
}
