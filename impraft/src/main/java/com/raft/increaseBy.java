package com.raft;

import java.rmi.Naming;

import com.raft.resources.serverAddress;

public class increaseBy {
        public increaseBy(){}

        public void requestCounterService(serverAddress server, int x) {
        try {
          ServerInterface request = (ServerInterface) Naming.lookup(
            "rmi://" + server.getIpAddress() + ":" + server.getPort() + "/server"
          );
          System.out.println("sending increaseby request of: " + x);
          request.quorumInvokeRPC("ADD",Integer.toString(x));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
}
