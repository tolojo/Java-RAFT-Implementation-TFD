package com.raft.launcher;

import com.raft.Server;
/*
 *-path impraft/src/main/java/com/raft/server0
 */
public class serverLauncher {
	
	public static void main(String[] args) throws Exception{
		String root = "";
		boolean monitorMode = false;
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "-path": {
					root = args[i+1];
				}
			}
		}
		if(root.isBlank())
			throw new IllegalArgumentException("Server root path could not be found");
		else {
			System.out.println("Starting server "+root);
			new Server(root);
		}
	}
}