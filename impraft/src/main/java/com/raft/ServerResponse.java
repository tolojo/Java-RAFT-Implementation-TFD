package com.raft;

import com.raft.resources.serverAddress;

public class ServerResponse {
 private  serverAddress address;
 private Object response;

 public ServerResponse(serverAddress address, Object response) {
    this.address = address;
    this.response = response;
}
 
    
}
