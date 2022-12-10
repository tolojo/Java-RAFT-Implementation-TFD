package com.raft.launcher;

import java.util.Random;

import com.raft.Client;
import com.raft.resources.serverAddress;
import com.raft.increaseBy;

public class clientLauncher {
    
	public static void main(String[] args) {
		Client c = new Client();
		increaseBy increase = new increaseBy();
		serverAddress s = new serverAddress("127.0.0.1", 8000);
		//c.request(s, "GET", "a3c1231");
		new Thread(()->{
			while (true){
			Random randG = new Random();
			int random = randG.nextInt(5);
			while (random < 1) random = randG.nextInt(5);
			increase.requestCounterService(s,random);
			try {
				Thread.sleep(9000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}).start();
		
	}
}
