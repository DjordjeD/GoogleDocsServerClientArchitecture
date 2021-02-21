/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * PODSERVER
 *
 * @author praksa
 */
package podserverapp;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class PodserverController {

    private String socketRequestPort;

    private PodserverRequestHandler podserverRequestHandler;

    private PodserverCommunicator podserverCommunicator;

    @FXML
    public TextArea PodserverLogs;

    @FXML
    public TextField socketPort;

    @FXML
    public Button startButton;

    @FXML
    public TextField ipAdresa;

    @FXML
    void startPodserver(MouseEvent event) {

        try {
            //deleteallfilesanddirs();
            podserverRequestHandler = new PodserverRequestHandler(Integer.parseInt(socketPort.getText()), PodserverLogs);
            podserverCommunicator = new PodserverCommunicator(PodserverLogs);

            podserverRequestHandler.start();
            podserverCommunicator.start();

//            Runnable r = () -> {\
//
//                try {
//                    File file
//                            = new File("C:\\kdp\\opetnovagara\\zare.txt");
//                    Scanner sc = new Scanner(file);
//
//                    sc.useDelimiter("\\Z");
//                    String print;
//
//                    Platform.runLater(() -> PodserverLogs.appendText(sc.next()));
//
//                    // System.out.println(sc.nextLine());
//                    // append the line on the application thread
//                } catch (IOException e) {
//                    //e.printStackTrace();
//                }
//            };
//            // run task on different thread
//            Thread t = new Thread(r);
//            t.start();
        } catch (Exception ex) {
            Logger.getLogger(PodserverController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
