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
    private TextField IPGlavnogServera;

    @FXML
    public TextField fileNameTB;

    @FXML
    private Label fileName;

    @FXML
    private TextField IPPodservera;

    @FXML
    private Button ConnectToPodserver;

    @FXML
    public TextArea ClientText;

    @FXML
    public TextArea ClientLogs;

    @FXML
    private TextField podServerPort;

    @FXML
    void ConnectToPod(MouseEvent event) {
        System.out.println("clientapp.ClientAppController.ConnectToPod()");
        clientCommunicator = new ClientCommunicator(fileNameTB.getText(), IPPodservera.getText(), Integer.parseInt(podServerPort.getText()),
                ClientText, ClientLogs);
        clientCommunicator.start();
    }

    @FXML
    void GetPodserverIPFunction(MouseEvent event) {

    }

}
