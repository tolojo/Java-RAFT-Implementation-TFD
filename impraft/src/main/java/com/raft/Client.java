package com.raft;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;

public class Client implements ClientInterface{
    private String port,clusterString;
    public Client(){
        init();
    }

    public void init(){
        port="";clusterString="";
		try {
			Properties p = new Properties();
			p.load(new FileInputStream("src/main/java/com/raft/serverConfigs/ports.ini"));

			
		port = p.getProperty("port");
		clusterString = p.getProperty("cluster");
		

   
		} catch (IOException e) {
			//System.err.println("Config file not found")
			
			e.printStackTrace();
		} 
    }

   public static void main(String[] args) {
    try {
        ClientInterface client = (ClientInterface) Naming.lookup("rmi://" + "127.0.0.1" + ":"+ 8000 + "/server");
        client.invokeGET();
        System.out.println("client connected");
    } catch (Exception e) {
        // TODO: handle exception
    }
   }

@Override
public String[] invokeGET() throws RemoteException {
    // TODO Auto-generated method stub
    return null;
}

}
