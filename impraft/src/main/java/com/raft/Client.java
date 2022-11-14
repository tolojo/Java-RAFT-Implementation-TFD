package com.raft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Properties;

import com.raft.resources.serverAddress;

public class Client {
  
    private String path;
    private String port,clusterString;
    private serverAddress[] clusterArray;
	private String clientid;
	private ArrayList<String> wholeMessage = new ArrayList<String>();


    public Client(){

		clientid = serverAddress.getLocalIp();
        init();
        connectToServer();
    }

    public void init(){
        port="";clusterString="";
		try {
			Properties p = new Properties();
			p.load(new FileInputStream("impraft/src/main/java/com/raft/client/config.ini"));

            String[] clusterString = p.getProperty("cluster").split(";");
			clusterArray = new serverAddress[clusterString.length];
			for (int i = 0; i < clusterString.length; i++) {
				String[] splited = clusterString[i].split(":");
				clusterArray[i] = new serverAddress(splited[0], Integer.parseInt(splited[1]));
			}
   
		} catch (IOException e) {
			//System.err.println("Config file not found")
			
			e.printStackTrace();
		} 
    }
	public serverAddress[] getClusterArray(){
		return clusterArray;
	}

    public void connectToServer() {
		for (int i = 0; i < clusterArray.length; i++) {
			serverAddress address = clusterArray[i];
			try { 	
				Remote server = (Remote) Naming.lookup("rmi://" + address.getIpAddress() + ":" + address.getPort() + "/server");
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	/*public void request(serverAddress serverId,String label, String msg){
			try { 
				
				ServerInterface request = (ServerInterface) Naming.lookup("rmi://" + serverId.getIpAddress() + ":" + serverId.getPort() + "/server");
				if(wholeMessage ==null){
					 wholeMessage = request.invokeRPC(null, msg,label);
				}
				else {wholeMessage = request.invokeRPC(wholeMessage, msg,label);
					}
					System.out.println(wholeMessage);
			} catch (Exception e) {
				e.printStackTrace();
				
			}
	}*/
	public void requestQuorum(serverAddress serverCluster,String label, String msg){
		try { 

			ServerInterface request = (ServerInterface) Naming.lookup("rmi://" + serverCluster.getIpAddress() + ":" + serverCluster.getPort() + "/server");
			request.quorumInvokeRPC(label, msg);
		
		} catch (Exception e) {
			e.printStackTrace();
			
	}
	

}

}
