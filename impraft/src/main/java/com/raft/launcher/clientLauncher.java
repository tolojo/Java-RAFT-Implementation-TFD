package com.raft.launcher;

import java.util.Random;

import com.raft.Client;
import com.raft.resources.serverAddress;
public class clientLauncher {
    
	public static void main(String[] args) {
		Client c = new Client();
		serverAddress s = new serverAddress("127.0.0.1", 8002);
		//c.request(s, "GET", "a3c1231");
		c.requestQuorum(s, "ADD", "a3c1");
		
		
		new Thread(()->{
			Random randG = new Random();
			int random = randG.nextInt(5);
			while (random < 1) random = randG.nextInt(5);
		c.requestCounterService(s, 0);
		}).start();
		
	}
}
