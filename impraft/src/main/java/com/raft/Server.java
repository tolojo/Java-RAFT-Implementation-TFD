package com.raft;

import com.raft.resources.serverAddress;
import java.io.*;
import java.lang.Thread.State;
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

public class Server implements ServerInterface, Remote, Serializable {

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
  private int heartBeatTimer = 3;

  private int counter = 0;
  private int newRequestValue = 0;
  private int requestQuorumId = 0;

  private ArrayList<String> wholeMessage = new ArrayList<String>();

  private long serverId;
  TimoutThread timeoutThread;
  ElectionThread election;
  HeartBeatThread heartbeat;
  private serverAddress leaderId;

  private int currentTerm;
  private int lastLogIndex;
  private boolean commit = false;

  enum serverState {
    FOLLOWER,
    CANDIDATE,
    LEADER,
  }

  public Server(String path) {
    serverId = 1L;
    currentTerm = 0;
    lastLogIndex = 1;
    currentState = serverState.FOLLOWER;
    String placeholder = "";
    wholeMessage.add(placeholder);
    this.path = path;
    init();
    resetTimer();
    timeoutThread = new TimoutThread(this);
    timeoutThread.start();
    heartbeat = new HeartBeatThread(this);
    heartbeat.start();
    /*election = new ElectionThread(this);
    election.start(); */
    System.out.println(this.getState());
  }

  public Server(String path, int currentTerm, int lastLogIndex) {
    serverId = 1L;
    this.path = path;
    this.currentTerm = currentTerm;
    this.lastLogIndex = lastLogIndex;
    currentState = serverState.FOLLOWER;
    String placeholder = "";
    wholeMessage.add(placeholder);
    init();
    resetTimer();
    timeoutThread = new TimoutThread(this);
    timeoutThread.start();
    heartbeat = new HeartBeatThread(this);
    heartbeat.start();
    election = new ElectionThread(this);
    election.start();
    System.out.println(this.getState());
  }

  private void init() {
    port = "";
    clusterString = "";
    try {
      Properties p = new Properties();
      p.load(new FileInputStream(path + File.separator + CONFIG_INI));
      String[] clusterAux = p.getProperty("cluster").split(";");
      clusterArray = new serverAddress[clusterAux.length];
      id =
        new serverAddress(
          p.getProperty("ip"),
          Integer.parseInt(p.getProperty("port"))
        );
      executor = Executors.newFixedThreadPool(clusterString.split(";").length);

      for (int i = 0; i < clusterAux.length; i++) {
        String[] splited = clusterAux[i].split(":");
        clusterArray[i] =
          new serverAddress(splited[0], Integer.parseInt(splited[1]));
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
      (Remote) server
    );
    Naming.rebind(
      "rmi://" + id.getIpAddress() + ":" + id.getPort() + "/server",
      (Remote) server
    );
    System.out.println(id.getIpAddress() + ":" + id.getPort() + " connected");
  }

  public ArrayList<String> invokeRPC(
    int id,
    ArrayList<String> message,
    String newMsg,
    String label,
    int term,
    serverAddress leaderId,
    int lastLogIndex,
    boolean commit
  ) {
    try {
      if (term >= currentTerm) {
        if(currentState == serverState.LEADER){
          if (leaderId != this.leaderId){
            currentState = serverState.FOLLOWER;
            this.leaderId = leaderId;
          }
        }
        //deve retornar o seu id(IP:PORT) juntamente com a resposta (wholeMessage)
        this.currentTerm = term;
        this.leaderId = leaderId;
        this.lastLogIndex = lastLogIndex;

        votedFor = new serverAddress();
        System.out.println("term: " + term);
        System.out.println("lasIndex: " + lastLogIndex);
        System.out.println("state machine state: " + counter);
        System.out.println("message: " + newMsg);
        resetTimer();
        timeoutThread.stop();
        timeoutThread = new TimoutThread(this);
        timeoutThread.start();
        boolean flag = false;
        System.out.println(label);
        if (label.equals("GET")) {
          return wholeMessage;
        }
        if (label.equals("ADD")) {
          if (commit){
            System.out.println(wholeMessage.size());
            System.out.println(message.size());
            if (wholeMessage.get(lastLogIndex).equals(message.get(lastLogIndex))){
              counter = counter + newRequestValue;
              this.commit = false;
              newRequestValue = 0;
              System.out.println("commited");
              System.out.println("state machine state: " + counter);
              setLastLogIndex(lastLogIndex + 1);
              return message;
            } else{
              newRequestValue = Integer.parseInt(newMsg);
              wholeMessage.remove(lastLogIndex-1);
              wholeMessage.add(id+":"+message.get(lastLogIndex-1));
              System.out.println("Last log term altered");
            }
          }
          if (newMsg.equals("")) {
            return message;
          }
          for (int i = 0; i < wholeMessage.size(); i++) {
            if (wholeMessage.get(i).equals(newMsg)) {
              flag = true;
            }
          }
          if (flag) {
            return wholeMessage;
          } else {
            commit = true;
            newRequestValue = Integer.parseInt(newMsg);
            wholeMessage.add(id+":"+newMsg);
            return wholeMessage;
          }
        }
      }
      return wholeMessage;
    } catch (Exception e) {
      System.out.println(e);
      return wholeMessage;
    }
  }

  public String quorumInvokeRPC(String label, String data)
    throws RemoteException {
    BlockingQueue<ArrayList<String>> responsesQueue = new BlockingQueue<>(10);
    requestQuorumId += 1;
    try {
      
      System.out.println(data);

      if (!data.equals("")) {
        for (int j = 0; j < wholeMessage.size(); j++) {
          if (data.equals(wholeMessage.get(j))) {}
        }
        
      }

      for (int i = 0; i < clusterArray.length; i++) {
        serverAddress serverAux = clusterArray[i];
        new Thread(() -> {
          try {
            ArrayList<String> responseAux = new ArrayList<>();
            ServerInterface server = (ServerInterface) Naming.lookup(
              "rmi://" +
              serverAux.getIpAddress() +
              ":" +
              serverAux.getPort() +
              "/server"
            );
            responseAux =
              server.invokeRPC(
                requestQuorumId,
                wholeMessage,
                data,
                label,
                currentTerm,
                leaderId,
                lastLogIndex,
                commit
              );
            //nesta posição verificar se o log retornado pelo follower é igual ao log do leader, se nao for inicar uma thread para incrementar as logs em falta

            responsesQueue.enqueue(responseAux);
          } catch (RemoteException e) {
            //e.printStackTrace();
          } catch (MalformedURLException e) {
            e.printStackTrace();
          } catch (NotBoundException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
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
            if (responsesCount > (clusterArray.length / 2)) {
              System.out.println("Respostas recolhidas");
              if(!data.equals("")){
                commit = true;
              }
              
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

  private serverAddress votedFor = new serverAddress();
  private int termAux = 0;

  public boolean requestVoteRPC(
    int term,
    serverAddress candidateId,
    int clastLogIndex,
    int lastTermIndex
  ) {
    boolean responseF = false;
    boolean responseV = true;
    if (term < currentTerm) {
      return responseF;
    } else if (votedFor.getPort() == 0 || votedFor == candidateId) {
      if (clastLogIndex == lastLogIndex) {
        votedFor = candidateId;
        termAux = term;
        System.out.println("vote granted");
        return responseV;
      }
      return responseF;
    }
    return responseF;
  }

  private void resetTimer() {
    timeout = randomGen.nextInt(25);
    while (timeout < 10) {
      timeout = randomGen.nextInt(25);
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

  public int getHeartBeatTimer() {
    return heartBeatTimer;
  }

  public int getTimeOut() {
    return timeout;
  }

  public serverAddress getLeaderId() {
    return leaderId;
  }

  public int getCurrentTerm() {
    return currentTerm;
  }

  public int getLastLogIndex() {
    return lastLogIndex;
  }

  public long getServerId() {
    return serverId;
  }

  public serverAddress getId(){
    return id;
  }

  public void setCurrentTerm(int currentTerm) {
    this.currentTerm = currentTerm;
  }

  public void setLastLogIndex(int lastLogIndex) {
    this.lastLogIndex = lastLogIndex;
  }
}
