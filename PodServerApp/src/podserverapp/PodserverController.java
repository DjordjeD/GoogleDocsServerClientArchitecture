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

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
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
//            File dir = new File("c:\\kdp");
//            deleteAllDirsAndFiles(dir);
            podserverRequestHandler = new PodserverRequestHandler(Integer.parseInt(socketPort.getText()), PodserverLogs);
            podserverCommunicator = new PodserverCommunicator(PodserverLogs);

            podserverRequestHandler.start();
            podserverCommunicator.start();

        } catch (Exception ex) {
            Logger.getLogger(PodserverController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void deleteAllDirsAndFiles(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                deleteAllDirsAndFiles(new File(dir, children[i]));
            }
        }
        dir.delete();
    }

    private void setParameters(Application.Parameters params) {
        List<String> list = params.getRaw();
        //namesti ovo
    }

}
