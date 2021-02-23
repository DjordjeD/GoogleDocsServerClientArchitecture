/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientapp;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ClientAppController {

    ClientCommunicator clientCommunicator;

    private static Socket sock;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static ServerSocket servsock;
    public static String ipGlavnog;

    public static Stage stage;

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
    public static Button addFile;

    @FXML
    void ConnectToPod(MouseEvent event) {

        if (!IPPodservera.getText().isEmpty() && !podServerPort.getText().isEmpty()) {

            System.out.println("clientapp.ClientAppController.ConnectToPod()");
            clientCommunicator = new ClientCommunicator(fileNameTB.getText(), IPPodservera.getText(), Integer.parseInt(podServerPort.getText()),
                    ClientText, ClientLogs);
            ConnectToPodserver.setDisable(true);
            clientCommunicator.start();
        }
    }

    @FXML
    void GetPodserverIPFunction(MouseEvent event) {

        if (!IPGlavnogServera.getText().isEmpty() && !GlavniServerPort.getText().isEmpty()) {

            for (int i = 0; i < 4; i++) {
                try {
                    //4 puta pokusava
                    ipGlavnog = IPGlavnogServera.getText();
                    sock = new Socket(ipGlavnog, Integer.parseInt(GlavniServerPort.getText()));
                    oos = new ObjectOutputStream(sock.getOutputStream());
                    ois = new ObjectInputStream(sock.getInputStream());

                    oos.writeObject(fileNameTB.getText());
                    String podserverIP = (String) ois.readObject();

                    ispis(podserverIP, IPPodservera);
                    oos.close();
                    ois.close();
                    sock.close();
                    break;
                } catch (Exception ex) {
                    Logger.getLogger(ClientAppController.class.getName()).log(Level.SEVERE, null, ex);

                    try {
                        //labela

                        sleep(2000);

                        // ako 3 puta pokusa da kontaktira server i ovaj se ne javlja fatal error
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(ClientAppController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

            }

            getPodserverIp.setDisable(true);
        }
    }

    private static void ispis(String ispis, TextField PodserverLogs) {
        Runnable r = () -> {

            Platform.runLater(() -> PodserverLogs.setText(ispis + "\n"));

            // System.out.println(sc.nextLine());
            // append the line on the application thread
        };
        // run task on different thread
        Thread t = new Thread(r);
        t.start();

    }

    void setStage(Stage stage1) {
        stage = stage1;
    }

    @FXML
    void addNewFile(MouseEvent event) {

        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            ispis(file.getName(), fileNameTB);
        }

    }
}
