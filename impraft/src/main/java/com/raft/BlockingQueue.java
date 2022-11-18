package com.raft;

import java.util.*;

public class BlockingQueue<E> {

    private Queue<E> queue = new ArrayDeque<E>();

    private  int capacity;

public BlockingQueue(int c){
    this.capacity = c;
    
}


public synchronized void enqueue(E item) throws InterruptedException{
    
    while(this.queue.size() == capacity){
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
    if (this.queue.size() == capacity){
        notifyAll();
    }
        E t = queue.remove();
        return t;
}

public int getSize() {
    return this.queue.size();
}

}
