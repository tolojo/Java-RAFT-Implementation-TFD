package com.raft;

import java.util.Random;

public class CounterServThread extends Thread{
  private boolean isRunning;
  private int  timeout = 5 ;
  private Client client;
  private Random randomGen = new Random();
  private int increment = 0;

  public void run(){
   
    while(isRunning){
       try {
       timeout = timeout * 1000;
       sleep(timeout);
       
       
       increment = randomGen.nextInt(5);
       while (increment == 0){
        increment = randomGen.nextInt(5);
       }
       

    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    }

}

@Override
	public synchronized void start() {
		isRunning = true;
		super.start();
	}
}
