package com.raft;

public class CounterService {

    public int increaseBy(int x){
		int counter = 0;
		counter = counter + x;
		return counter;
}

}

