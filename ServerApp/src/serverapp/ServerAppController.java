/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverapp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class ServerAppController {

    private ServerRequestHandler serverRequestHandler;
    private ServerCommunicator serverCommunicator;
    
    @FXML
    private Label label;

    @FXML
    private TextArea ServerLogs;

    @FXML
    private TextArea ServerFilesText;

    @FXML
    private TextArea Podservers;

    @FXML
    private TextField SocketPort;

    @FXML
    private TextField IPAdress;

    @FXML
    private Button StartServerFunction;

    @FXML
    void StartServerFunction(MouseEvent event) {
        
        serverRequestHandler= new ServerRequestHandler(Integer.parseInt(SocketPort.getText()));
        serverCommunicator = new ServerCommunicator();
        
        //vise niti treba da se napravi;
        serverRequestHandler.start();
        serverCommunicator.start();
        

    }

}
