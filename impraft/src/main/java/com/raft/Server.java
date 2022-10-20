package com.raft;


import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.raft.resources.serverAddress;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements Remote,Invoke, Serializable{ 
    public static final String CONFIG_INI = "config.ini";
    private String port,clusterString;
    private ExecutorService  executor;
	private serverAddress id;
	private ExecutorService  connectorService = Executors.newFixedThreadPool(1);
	private String path;

    public Server(String path){
		this.path = path;
        init();
    }

    private void init() {
        port="";clusterString="";
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(path+File.separator+CONFIG_INI));

		id = new serverAddress(p.getProperty("ip"), Integer.parseInt(p.getProperty("port")));
		executor = Executors.newFixedThreadPool(clusterString.split(";").length);

        //Regist this server 
		registServer();

		} catch (IOException e) {
			System.err.println("Config file not found");
			
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
            e.printStackTrace();
        }		
    }

	private void registServer() throws RemoteException, AlreadyBoundException, AccessException, MalformedURLException {
		Registry registry = LocateRegistry.createRegistry(id.getPort());
		Object server = UnicastRemoteObject.exportObject(this, 0);
		System.setProperty( "java.rmi.server.hostname", "127.0.0.1");
		registry.bind("rmi://"+id.getIpAddress()+":"+id.getPort()+"/server", (Remote)server);
		Naming.rebind("rmi://"+id.getIpAddress()+":"+id.getPort()+"/server", (Remote)server);
		System.out.println(id.getIpAddress()+":"+id.getPort()+" connected");
	}

	public ServerResponse response(String message, serverAddress clientid, String label) throws RemoteException{

		try{
			return Invoke.getInvoke(message,clientid,label);
			}
		catch (Exception e){
			System.out.println(e);
		}
		
	}
	

}