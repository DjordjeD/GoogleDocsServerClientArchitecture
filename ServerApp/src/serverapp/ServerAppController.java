/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverapp;

import java.util.Vector;
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
    private TextField numThreads;

    @FXML
    private Button addPodserver;
    @FXML
    private TextField socketClientPort;

    static Vector<String> podservers = null;

    @FXML
    public void initialize() {
        StartServerFunction.setDisable(true);

        podservers = new Vector<String>();
    }

    @FXML
    void StartServerFunction(MouseEvent event) {

        ServerLogs.appendText("Starting Server");
        //= args od kolko ima uzima ip adrese sve
        int numberOfThreads = Integer.parseInt(numThreads.getText()); // kolko niti puni request handler
        //serverRequestHandler = new ServerRequestHandler(Integer.parseInt(SocketPort.getText()), ServerLogs, ServerFilesText, Podservers);
        serverCommunicator = new ServerCommunicator(Integer.parseInt(SocketPort.getText()), ServerLogs, ServerFilesText, Podservers);

        //vise niti treba da se napravi;
        Thread[] threads = new Thread[1];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new ServerRequestHandler(Integer.parseInt(socketClientPort.getText()), ServerLogs, ServerFilesText, Podservers
            );
            threads[i].start();
        }

        serverCommunicator.start();
        serverRequestHandler.listenToClients(ServerLogs);

        StartServerFunction.setDisable(true);

    }

    @FXML
    void addNewPodserver(MouseEvent event) {

        Boolean flag = true;
        String newPodserver = IPAdress.getText();

        if (!podservers.isEmpty()) {
            for (String podserver : podservers) {

                if (podserver.equalsIgnoreCase(newPodserver)) {
                    flag = false;
                }
            }
        }
        //provera da li vec postoji, ne sme duplo
        if (flag) {
            podservers.add(IPAdress.getText());
            Podservers.appendText(IPAdress.getText());

        }
        if (!podservers.isEmpty()) {
            StartServerFunction.setDisable(false);
        }
    }

}
