package com.raft;


import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements Remote, Serializable{ 
    
    private LinkedList<Server> cluster = new LinkedList<>();
    private String port,clusterString;
    private ExecutorService  executor;

    public Server(){
        init();
    }

    private void init() {
        port="";clusterString="";
		try {
			Properties p = new Properties();
			p.load(new FileInputStream("src/main/java/com/raft/serverConfigs/ports.ini"));

			
		int port = Integer.parseInt(p.getProperty("port"));
		String clusterString = p.getProperty("cluster");
		executor = Executors.newFixedThreadPool(clusterString.split(";").length);

        //Regist this server 
		Registry registry = LocateRegistry.createRegistry(port);
		registry.bind("rmi://"+p.getProperty("ip")+":"+port+"/server", UnicastRemoteObject.exportObject(this, 0));

		} catch (IOException e) {
			//System.err.println("Config file not found")
			
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }		
    }


    public static void main(String[] args) throws AlreadyBoundException  {
        try {

            Server s = new Server();
			Naming.rebind("rmi://" + "127.0.0.1" + ":"+ 8000 + "/server", s);
            System.out.println("Server is ready");

            
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}