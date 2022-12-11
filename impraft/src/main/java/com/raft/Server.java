package com.raft;

import com.raft.resources.serverAddress;
import java.io.*;
import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.json.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server implements ServerInterface, Remote, Serializable {

  public static final String CONFIG_INI = "config.ini";
  private String port, clusterString, clusterAux;
  private serverAddress[] clusterArray;
  private ExecutorService executor;
  private serverAddress id;
  private ExecutorService connectorService = Executors.newFixedThreadPool(1);
  private ExecutorService executorService = Executors.newFixedThreadPool(1);
  private String path;
  private ArrayList<String> exception = new ArrayList<String>();
  ArrayList<ArrayList> responses = new ArrayList<>();

  private serverState currentState;
  private int timeout = 0; // random timer, garantir que tomos têm um timer para se tornarem candidates
  private Random randomGen = new Random();
  private int heartBeatTimer = 3;

  private static final String Snap_FileTemp = "snapshot.tmp";

  private int counter = 0;
  private int commitCounter = 0;
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

  public BlockingQueue<Request> pedidos = new BlockingQueue<>(10);

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
    /*
     * election = new ElectionThread(this);
     * election.start();
     */
    ConsumeRequestsThread requestThread = new ConsumeRequestsThread(this);
    requestThread.start();
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

    System.out.println(this.getState());
  }

  private void init() {
    initFromSnapshot();
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
      int id,
      ArrayList<String> message,
      String newMsg,
      String label,
      int term,
      serverAddress leaderId,
      int lastLogIndex,
      boolean commit) {
    try {
      if (term >= currentTerm) {
        if (currentState != serverState.LEADER) {
          currentState = serverState.FOLLOWER;
          timeoutThread.stopElection();
        }
        this.currentTerm = term;
        this.leaderId = leaderId;

        int lastentry = this.lastLogIndex;
        votedFor = new serverAddress();
        System.out.println(wholeMessage);
        System.out.println("term: " + term);
        System.out.println("LeaderlastIndex: " + lastLogIndex);
        System.out.println("state machine state: " + counter);
        System.out.println("message: " + newMsg);
        resetTimer();
        timeoutThread.stop();
        timeoutThread = new TimoutThread(this);
        timeoutThread.start();
        boolean flag = false;
        if (label.equals("GET")) {
          return wholeMessage;
        }
        if (label.equals("ADD")) {
          if (commit) {
            System.out.println( wholeMessage.get(lastentry-1));
           System.out.println(message.get(lastentry-1));
            if (wholeMessage.get(lastentry-1).equals(message.get(lastentry-1))) {
              commitCounter++;
              counter = counter + newRequestValue;
              newRequestValue = 0;
              System.out.println("commited");
              System.out.println("state machine state: " + counter);

              if (commitCounter == 10) {
                char lastlog = wholeMessage
                    .get(lastLogIndex - 1)
                    .charAt(wholeMessage.get(lastLogIndex - 1).length() - 1);
                int logvalue = Integer.parseInt("" + lastlog);
                snapshot("0:" + logvalue);
              }

            } else {
              newRequestValue = Integer.parseInt(newMsg);
              wholeMessage.remove(lastLogIndex - 1);
              wholeMessage.add(id + ":" + message.get(lastLogIndex - 1));
              System.out.println("Last log term altered");
            }
          }
          if (newMsg.equals("")) {
            return wholeMessage;
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
            wholeMessage.add(id + ":" + newMsg);
            setLastLogIndex(this.lastLogIndex + 1);
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

  boolean majority = false;
  int faltosos = 0;

  public void receiveRequest(String label, String data) throws RemoteException{
    if (!(currentState == serverState.LEADER)){
      try {
        System.out.println("leader IP: "+ leaderId.getIpAddress());
        System.out.println("leader port: "+ leaderId.getPort());
        ServerInterface request;
        try {
          request = (ServerInterface) Naming.lookup(
                  "rmi://" +
                  leaderId.getIpAddress() +
                  ":" +
                  leaderId.getPort() +
                  "/server"
                );
              
              request.receiveRequest(
              label,data
              );
              System.out.println("redirected to leader");
              return;
        }
      catch (NotBoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      }
       catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } 
    }
    else{
    Request request = new Request(label, data);
    try {
      pedidos.enqueue(request);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}

  public String quorumInvokeRPC(String label, String data)
      throws RemoteException {
    BlockingQueue<ArrayList<String>> responsesQueue = new BlockingQueue<>(10);
    System.out.println(currentState);
    if (!(currentState == serverState.LEADER)) {
      try {
        System.out.println("leader IP: " + leaderId.getIpAddress());
        System.out.println("leader port: " + leaderId.getPort());
        ServerInterface request = (ServerInterface) Naming.lookup(
            "rmi://" +
                leaderId.getIpAddress() +
                ":" +
                leaderId.getPort() +
                "/server");

        String responseAux = request.quorumInvokeRPC(
            label, data);
        System.out.println("redirected to leader");
        return "redirected to leader";

      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (NotBoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    if (!data.equals("")) {
      requestQuorumId += 1;
    }
    if (!data.equals("")) {
      if (majority == false) {
        System.out.println("Nao existe maioria");
        requestQuorumId--;
        return "";
      }
    }
    try {
      System.out.println(data);
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
                    "/server");
            responseAux = server.invokeRPC(
                requestQuorumId,
                wholeMessage,
                data,
                label,
                currentTerm,
                id,
                lastLogIndex,
                commit);
            if (commit)
              commit = false;
            if (responseAux.size() < lastLogIndex) {
              System.out.println("resposne size: "+responseAux.size());
              System.out.println("lastlogIndex: "+lastLogIndex);
              int fLastIndex = responseAux.size();
              char[] mAux = wholeMessage.get(fLastIndex).toCharArray();
              System.out.println(mAux);
              String aux = "" + mAux[0];
              String messageAux = "" +
                  wholeMessage
                      .get(fLastIndex)
                      .charAt(wholeMessage.get(fLastIndex).length()-1 );
              int idAux = Integer.parseInt(aux);

              sendEntry(
                  idAux,
                  messageAux,
                  serverAux,
                  this,
                  lastLogIndex,
                  wholeMessage);
            }

            responsesQueue.enqueue(responseAux);
          } catch (RemoteException e) {
            faltosos++;
            // e.printStackTrace();
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
      if (faltosos >= 3) {
        majority = false;
        faltosos = 0;
      } else {
        majority = true;
        faltosos = 0;
      }
      new Thread(() -> {
        try {
          int responsesCount = 0;
          ArrayList<String> entry = new ArrayList<>();
          while (true) {
            if (responsesCount > (clusterArray.length / 2)) {
              System.out.println("Respostas recolhidas");
              if (!data.equals("")) {
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
      int lastTermIndex) {
    boolean responseF = false;
    boolean responseV = true;
    if (term <= termAux) {
      return responseF;
    }
    if (term < currentTerm) {
      return responseF;
    }
    if (votedFor.getPort() == 0 || votedFor == candidateId) {
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

  public void setLeaderId(serverAddress leaderId) {
    this.leaderId = leaderId;
  }

  public serverState getState() {
    return currentState;
  }

  public ArrayList<String> getWholeMessage() {
    return wholeMessage;
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

  public serverAddress getId() {
    return id;
  }

  public void setCurrentTerm(int currentTerm) {
    this.currentTerm = currentTerm;
  }

  public void setLastLogIndex(int lastLogIndex) {
    this.lastLogIndex = lastLogIndex;
  }

  public void sendEntry(
      int id,
      String msg,
      serverAddress followerId,
      Server server,
      int lastLogIndex,
      ArrayList<String> leaderLog) throws InterruptedException {
    try {
      
      ArrayList<String> aux;
      ServerInterface serverI = (ServerInterface) Naming.lookup(
          "rmi://" +
              followerId.getIpAddress() +
              ":" +
              followerId.getPort() +
              "/server");
      aux = serverI.invokeRPC(
          id,
          leaderLog,
          msg,
          "ADD",
          server.getCurrentTerm(),
          server.getId(),
          lastLogIndex,
          false);
      TimeUnit.SECONDS.sleep(2);
      aux = serverI.invokeRPC(
          id,
          leaderLog,
          "",
          "ADD",
          server.getCurrentTerm(),
          server.getId(),
          lastLogIndex,
          true);
      System.out.println("sending log: " + id + " with mesage: " + msg);
    
    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void snapshot(String lastIncludedTerm) {
    commitCounter = 0;
    requestQuorumId = 0;
    lastLogIndex = 1;
    wholeMessage = new ArrayList<>();
    wholeMessage.add(lastIncludedTerm);
    JSONObject checkpoint = new JSONObject();
    checkpoint.put("Last_Included_Log_Value", lastIncludedTerm);
    checkpoint.put("State_Machine_State", "" + counter);
    System.out.println("Snapshot criado");
    try {
      FileWriter file = new FileWriter(path + File.separator + Snap_FileTemp);
      file.write(checkpoint.toJSONString());
      file.close();
      Path oldFile = Paths.get(path + File.separator + Snap_FileTemp);
      File f = new File(path + File.separator + "snapshot");
      if (f.exists()) {
        f.delete();
      }
      Files.move(oldFile, oldFile.resolveSibling("snapshot"));
    } catch (FileNotFoundException e) {
      System.err.println("Snapshot file not found");
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void initFromSnapshot() {
    JSONParser parser = new JSONParser();
    File f = new File(path + File.separator + "snapshot");
    String data = "";
    if (f.exists()) {
      try (Scanner myReader = new Scanner(f)) {
        while (myReader.hasNextLine()) {
          data = myReader.nextLine();
        }

        Object obj = parser.parse(data);
        JSONObject jsonObject = (JSONObject) obj;

        String Last_Included_Log_Value = (String) jsonObject.get(
            "Last_Included_Log_Value");
        String State_Machine_State = (String) jsonObject.get(
            "State_Machine_State");
        System.out.println("Snapshotlog: " + Last_Included_Log_Value);
        System.out.println("State: " + State_Machine_State);
        wholeMessage.remove(0);
        wholeMessage.add(Last_Included_Log_Value);
        counter = Integer.parseInt(State_Machine_State);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("No snapshot file found");
    }
  }
}
