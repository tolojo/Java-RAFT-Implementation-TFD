package com.raft;

import java.util.*;

public class BlockingQueue<E> {

    public List<E> queue = new LinkedList<E>();

    public int limit = 10;

public BlockingQueue(){
    
}


public synchronized void enqueue(E item) throws InterruptedException{
    
    while(this.queue.size() == limit){
        wait();
    }
    if (this.queue.size() == 0){
        notifyAll();
    }
    this.queue.add(item);
}


public synchronized E dequeue() throws InterruptedException{


    while(this.queue.size() == 0){
        wait();
    }
    if (this.queue.size() == limit){
        notifyAll();
    }
        return this.queue.remove(0);
}

}
