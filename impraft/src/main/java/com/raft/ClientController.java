package com.raft;

import java.net.URL;
import java.util.ResourceBundle;

import com.raft.resources.serverAddress;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ClientController {

    	//FXML attributes
        @FXML private Text ipClient;
        @FXML private Text ipLeader;
        @FXML private Text portLeader;
        @FXML private TextArea textArea;
        @FXML private TextField textField;
    
        private Client client;
        private static ClientController instance;
        
        
        
        public void initialize(URL location, ResourceBundle resources) {
            instance = this;
            client = new Client();
            ipClient.setText(serverAddress.getLocalIp());
            
        
            textArea.setEditable(false);
        }
     
        
        
        public void start(Stage window) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
            window.setScene(new Scene(loader.load()));
            window.show();
        }
    
      
        
        
        
        public static void main(String[] args) {
           // launch(args);
        }
    
    
        /**
         * @return the instance
         */
        public static ClientController getInstance() {
            return instance;
        }
}
