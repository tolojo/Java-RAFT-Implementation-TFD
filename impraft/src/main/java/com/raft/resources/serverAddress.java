package com.raft.resources;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

public class serverAddress implements Serializable {
   
        private static final long serialVersionUID = 1L;
        private String ipAddress; //IP address
        private int port;		  //port
    	
        

        public serverAddress(String ipAddress, int port) {
            this.ipAddress=ipAddress;
            this.port=port;
        }

        public String getIpAddress() {
            return ipAddress;
        }
        public int getPort() {
            return port;
        }

        public static serverAddress parse(String s, int i) {
            if(s != null) {
                if(!s.isBlank()) {
                    String[] splited = s.split(";");
                    return new serverAddress(splited[0], Integer.parseInt(splited[1]));
                }
            }
            return null;
        }
        public static String getLocalIp() {
            Socket socket = null;
            try{
                socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress("google.com", 80));
                } catch (IOException e) {}
                return socket.getLocalAddress().getHostAddress();
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    
    
}
