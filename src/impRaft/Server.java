package impRaft;

import java.net.*;
import java.io.*;

public class Server {

	public static void main(String[] args) {
		
		try {
			
			ServerSocket ss1 = new ServerSocket(1000);
			ServerSocket ss2 = new ServerSocket(1001);
			ServerSocket ss3 = new ServerSocket(1002);
			
			Socket soc1 = ss1.accept();
			Socket soc2 = ss2.accept();
			Socket soc3 = ss3.accept();
			
			 DataInputStream dis1 = new DataInputStream(soc1.getInputStream());
			 DataInputStream dis2 = new DataInputStream(soc2.getInputStream());
			 DataInputStream dis3 = new DataInputStream(soc3.getInputStream());
			 
			 
			 ss1.close();
			 ss2.close();
			 ss3.close();
		}
		 catch (Exception e) {
			 
	            System.out.println(e);
	        }
		
	}

}
