package impRaft;

import java.net.*;
import java.util.Date;
import java.io.*;

public class Server {

	public static void main(String[] args) throws IOException {
		
		int port = Integer.parseInt(args[0]);
		
		ServerSocket serverSocket1 = new ServerSocket(port);
		ServerSocket serverSocket2 = new ServerSocket(port);
		ServerSocket serverSocket3 = new ServerSocket(port);
		 
        try {
 
            System.out.println("Server is listening on port " + port);
 
            while (true) {
                Socket socket = serverSocket1.accept();
 
                System.out.println("New client connected");
 
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
 
                writer.println(new Date().toString());
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
