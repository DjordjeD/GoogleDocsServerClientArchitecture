/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientapp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class ClientAppController {

    ClientCommunicator clientCommunicator;

    private static Socket sock;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static ServerSocket servsock;

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
    private TextField GlavniServerPort;

    @FXML
    void ConnectToPod(MouseEvent event) {
        System.out.println("clientapp.ClientAppController.ConnectToPod()");
        clientCommunicator = new ClientCommunicator(fileNameTB.getText(), IPPodservera.getText(), Integer.parseInt(podServerPort.getText()),
                ClientText, ClientLogs);
        ConnectToPodserver.setDisable(true);
        clientCommunicator.start();
    }

    @FXML
    void GetPodserverIPFunction(MouseEvent event) {
        try {
            //tri puta pokusava
            sock = new Socket(IPGlavnogServera.getText(), Integer.parseInt(GlavniServerPort.getText()));
            oos = new ObjectOutputStream(sock.getOutputStream());
            ois = new ObjectInputStream(sock.getInputStream());

            oos.writeObject(fileNameTB.getText());
            String podserverIP = (String) ois.readObject();
            IPPodservera.setText(podserverIP);

            oos.close();
            ois.close();
            sock.close();

        } catch (Exception ex) {
            Logger.getLogger(ClientAppController.class.getName()).log(Level.SEVERE, null, ex);
            // ako 3 puta pokusa da kontaktira server i ovaj se ne javlja fatal error
        }
        getPodserverIp.setDisable(true);
    }

}
