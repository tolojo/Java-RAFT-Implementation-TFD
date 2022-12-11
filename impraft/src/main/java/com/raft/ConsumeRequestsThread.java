package com.raft;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public class ConsumeRequestsThread extends Thread {
    private Server server;

    public ConsumeRequestsThread(Server server) {
        this.server=server;
        
    }

    @Override
    public void run() {
        while (true) {
            try {
                Request request = server.pedidos.dequeue();
                try {
                    server.quorumInvokeRPC(request.getLabel(), request.getData());
                    TimeUnit.SECONDS.sleep(3);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                // Handle the exception
            }
        }
    }
}