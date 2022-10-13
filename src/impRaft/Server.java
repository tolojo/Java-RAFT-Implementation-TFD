package impRaft;

/*
 * /usr/bin/env sudo /usr/lib/jvm/java-11-openjdk-amd64/bin/java -cp /home/tomas/.config/Code/User/workspaceStorage/c76998ce14ccf60bc3262454dbc704ef/redhat.java/jdt_ws/jdt.ls-java-project/bin impRaft.Server ARG[]
 */

import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.util.Date;
import java.io.*;
import impRaft.*;

public class Server {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String data = "";
        int serverId = Integer.parseInt(args[0]);
        int aux = 0;

        try {
            File file = new File("serverConfigs/ports.config");
            Scanner myReader = new Scanner(file);
            while (aux < serverId) {
                data = myReader.nextLine();
                aux++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        System.out.println(data);
        ServerSocket server = new ServerSocket(Integer.parseInt(data));

        while (true) {
            Socket newSocket = null;
            try {
                newSocket = server.accept();
                System.out.println("Server has started on 127.0.0.1:" + data + ".\r\nWaiting for a connection…");
                System.out.println("A client connected.");
                DataInputStream in = new DataInputStream(newSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(newSocket.getOutputStream());
                //Scanner s = new Scanner(in, "UTF-8");
                Thread receiveMessage = new ClientHandler(newSocket, in, out);
                receiveMessage.start();

            } catch (Exception err) {
                newSocket.close();
				err.printStackTrace();
            }
        }
    }
}
