package com.raft.launcher;

import com.raft.Client;
import com.raft.resources.serverAddress;
//class jdk.proxy1.$Proxy0 cannot be cast to class com.raft.ClientInterface (jdk.proxy1.$Proxy0 is in module jdk.proxy1 of loader 'app'; com.raft.ClientInterface is in unnamed module of loader 'app'
public class clientLauncher {
    
	public static void main(String[] args) {
		Client c = new Client();
		serverAddress s = new serverAddress("127.0.0.1", 8001);
		c.request(s, "GET", "a3c1231");
		//c.requestQuorum(c.getClusterArray(), "ADD", "a3c1231");
	}
}
