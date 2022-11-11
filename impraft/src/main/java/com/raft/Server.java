package com.raft;

import com.raft.resources.serverAddress;
import java.io.*;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server
		extends Leader
		implements ServerInterface, Remote, Serializable {

	public static final String CONFIG_INI = "config.ini";
	private String port, clusterString, clusterAux;
	private serverAddress[] clusterArray;
	private ExecutorService executor;
	private serverAddress id;
	private ExecutorService connectorService = Executors.newFixedThreadPool(1);
	private String path;
	private ArrayList<String> exception = new ArrayList<String>();
	ArrayList<ArrayList> responses = new ArrayList<>();

	private serverState currentState;
	private int timeout = 0; // random timer, garantir que tomos têm um timer para se tornarem candidates
	private Random randomGen = new Random();
	private int heartBeatTimer = 5;

	private ArrayList<String> wholeMessage = new ArrayList<String>();
	private int term = 0;
	TimoutThread timeoutThread ;
	enum serverState {
		FOLLOWER,
		CANDIDATE,
		LEADER,
	}

	

	public Server(String path) {
		term = 1;
		currentState = serverState.FOLLOWER;
		String placeholder = "";
		wholeMessage.add(placeholder);
		this.path = path;
		init();
		resetTimer();
		timeoutThread = new TimoutThread(this);
		timeoutThread.start();
		
	}

	private void init() {
		port = "";
		clusterString = "";
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(path + File.separator + CONFIG_INI));
			String[] clusterAux = p.getProperty("cluster").split(";");
			clusterArray = new serverAddress[clusterAux.length];
			id = new serverAddress(
					p.getProperty("ip"),
					Integer.parseInt(p.getProperty("port")));
			executor = Executors.newFixedThreadPool(clusterString.split(";").length);

			for (int i = 0; i < clusterAux.length; i++) {
				String[] splited = clusterAux[i].split(":");
				clusterArray[i] = new serverAddress(splited[0], Integer.parseInt(splited[1]));
			}
			// Regist this server
			registServer();
		} catch (IOException e) {
			System.err.println("Config file not found");

			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}

	private void registServer()
			throws RemoteException, AlreadyBoundException, AccessException, MalformedURLException {
		Registry registry = LocateRegistry.createRegistry(id.getPort());
		Object server = UnicastRemoteObject.exportObject(this, 0);
		System.setProperty("java.rmi.server.hostname", "127.0.0.1");
		registry.bind(
				"rmi://" + id.getIpAddress() + ":" + id.getPort() + "/server",
				(Remote) server);
		Naming.rebind(
				"rmi://" + id.getIpAddress() + ":" + id.getPort() + "/server",
				(Remote) server);
		System.out.println(id.getIpAddress() + ":" + id.getPort() + " connected");
	}

	public ArrayList<String> invokeRPC(
			ArrayList<String> message,
			String newMsg,
			String label) {
		try {
			resetTimer();
			timeoutThread.interrupt();
			timeoutThread = new TimoutThread(this);
			timeoutThread.start();
			boolean flag = false;
			System.out.println(label);
			if (label.equals("GET")) {
				return wholeMessage;
			}
			if (label.equals("ADD")) {
				for (int i = 0; i < wholeMessage.size(); i++) {
					if (wholeMessage.get(i).equals(newMsg)) {
						flag = true;
					}
				}

				if (flag) {
					return wholeMessage;
				} else if (flag == false) {
					wholeMessage.add(newMsg);
					return wholeMessage;
				}
				return wholeMessage;
			}
			return wholeMessage;
		} catch (Exception e) {
			System.out.println(e);
			return wholeMessage;
		}
	}

	public String quorumInvokeRPC(
			serverAddress[] servers,
			String label,
			String data) throws RemoteException {
		BlockingQueue<ArrayList<String>> responsesQueue = new BlockingQueue<>(10);

		try {
			System.out.println(servers);

			for (int i = 0; i < servers.length; i++) {
				serverAddress serverAux = servers[i];
				new Thread(() -> {
					try {
						System.out.println(serverAux);
						ArrayList<String> responseAux = new ArrayList<>();
						ServerInterface server = (ServerInterface) Naming.lookup(
								"rmi://" +
										serverAux.getIpAddress() +
										":" +
										serverAux.getPort() +
										"/server");

						responseAux = server.invokeRPC(wholeMessage, data, label);
						responsesQueue.enqueue(responseAux);
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				})
						.start();
			}

			new Thread(() -> {
				try {
					int responsesCount = 0;
					ArrayList<String> entry = new ArrayList<>();
					while (true) {
						if (responsesCount > (servers.length / 2)) {
							System.out.println("Respostas recolhidas");
							Thread.interrupted();
						}
						entry = responsesQueue.dequeue();
						responsesCount++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			})
					.start();

			return "";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private void resetTimer() {
		timeout = randomGen.nextInt(15);
		while (timeout<10){
			timeout = randomGen.nextInt(15);
		}
	}
	

	public serverState getState() {
		return currentState;
	}
	public void setCurrentState(serverState currentState) {
		this.currentState = currentState;
	}

	public serverAddress[] getClusterArray() {
		return clusterArray;
	}

	public int getHeartBeatTimer(){
		return heartBeatTimer;
	}
	public int getTimeOut(){
		return timeout;
	}
}
