/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientapp;



import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class ClientAppController {

    ClientCommunicator clientCommunicator;
    
    @FXML
    private Button getPodserverIp;

    @FXML
    private Label label;

    @FXML
    public static TextField IPGlavnogServera;

    @FXML
    public static TextField fileDirTB;

    @FXML
    private Label fileName;

    @FXML
    private TextField IPPodservera;

    @FXML
    private Button ConnectToPodserver;

    @FXML
    public static TextArea ClientText;

    @FXML
    public static TextArea ClientLogs;

    @FXML
    void ConnectToPod(MouseEvent event) {
        clientCommunicator= new ClientCommunicator();
        clientCommunicator.start();
    }

    @FXML
    void GetPodserverIPFunction(MouseEvent event) {

    }

 

}
