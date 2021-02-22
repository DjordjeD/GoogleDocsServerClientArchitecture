/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverapp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 *
 * @author praksa
 */
class ServerRequestHandler extends Thread {

    private static int port;
    public static LinkedBlockingQueue<String> podserverList;
    public static LinkedBlockingQueue<Vector<String>> podserverFilePairsUpdate; // prvo ide ime fajla, pa ip podservera
    public static LinkedBlockingQueue<Vector<String>> requestQueue;

    private static final int PORT_NUMBER = 10001;

    private static Socket sock;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static ServerSocket servsock;

    @FXML
    private TextArea ServerLogs;

    @FXML
    private TextArea ServerFilesText;

    @FXML
    private TextArea Podservers;

    ServerRequestHandler(int parseInt, TextArea ServerLogs, TextArea ServerFilesText, TextArea Podservers) {
        this.port = parseInt;
        this.Podservers = Podservers;
        this.ServerFilesText = ServerFilesText;
        this.ServerLogs = ServerLogs;

    }

    @Override
    public void run() {
        //napravi tred koji samo osluskuje da vidi jel neki klijent hoce da pristupi
        //ako klijent posalje help me
        // on mu vraca iz podserverFilePairs jedan par (vektor), tj daje neki server
        listenToClients();

        try {
            while (true) {

                requestQueue.add(podserverFilePairsUpdate.remove());
                sleep(5000);

            }

        } catch (Exception e) {
        }

    }

    void listenToClients() {
        Runnable r = () -> {

            try {
                // Vector<String> podserverFilePair;
                servsock = new ServerSocket(PORT_NUMBER);
                //napravi sokete
                sock = servsock.accept();
                ois = new ObjectInputStream(sock.getInputStream());
                oos = new ObjectOutputStream(sock.getOutputStream());

                String fileName = (String) ois.readObject();

                if (fileName.equalsIgnoreCase("HELP")) {
                    if (!podserverFilePairsUpdate.isEmpty()) {
                        oos.writeObject(podserverFilePairsUpdate.remove().elementAt(1));
                    }
                    // else fatal error

                } else if (fileName.equalsIgnoreCase("Podserver")) {

                    podserverList.add(fileName); // dodaje ip adresu u listu podservera (aktivan)
                    // mozda ora nesto da se pokrene nzm ni ja

                } else {
                    //trazi da li postoji podserver sa tim fajlom
                    for (Iterator<Vector<String>> iterator = podserverFilePairsUpdate.iterator(); iterator.hasNext();) {
                        Vector<String> next = iterator.next();
                        if (next.elementAt(0).equalsIgnoreCase(fileName)) {
                            oos.writeObject(next.elementAt(1));
                            oos.flush();

                        }
                    }

                    if (!podserverList.isEmpty()) {
                        String next = podserverList.remove();
                        Vector<String> temp = new Vector<String>();
                        temp.add(fileName);
                        temp.add(next);

                        oos.writeObject(next);
                        oos.flush();

                        podserverFilePairsUpdate.add(temp);//dodao si u listu parova
                        //refreshuj ispis podserver files

                    }
                    // else fatal error
                    ois.close();
                    oos.close();

                    sock.close();
                }

            } catch (Exception ex) {
                Logger.getLogger(ServerRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
        // run task on different thread
        Thread t = new Thread(r);
        t.start();

    }

}
