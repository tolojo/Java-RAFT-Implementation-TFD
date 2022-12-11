package com.raft;

public class Request {
    public String label;
    public String data;

    public Request(String label, String data){
        this.data = data;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getData() {
        return data;
    }
    


}
