package com.raft;

import java.io.Serializable;



public class Entry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String command;
    private String commandID;
}
