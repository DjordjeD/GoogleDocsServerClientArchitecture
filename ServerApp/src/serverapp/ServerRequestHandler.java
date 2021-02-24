/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverapp;

import com.sun.jmx.remote.internal.ArrayQueue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
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
    // public static LinkedBlockingQueue<String> podserverList;
    public static Queue<Vector<String>> podserverFilePairsUpdate = new LinkedList<Vector<String>>(); // prvo ide ime fajla, pa ip podservera
    public static LinkedBlockingQueue<Vector<String>> requestQueue = new LinkedBlockingQueue<Vector<String>>();

    //private static final int PORT_NUMBER = 10001;
    private static Socket sock;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static ServerSocket servsock;
    public static Boolean flag = false;
    public static Vector<String> temp = new Vector<>();
    @FXML
    private TextArea ServerLogs;

    @FXML
    private TextArea ServerFilesText;

    @FXML
    private TextArea Podservers;
    static Semaphore semaphore = new Semaphore(1);
    static Semaphore waitSem = new Semaphore(0);

    ServerRequestHandler(int port1, TextArea ServerLogs, TextArea ServerFilesText, TextArea Podservers) {
        port = port1;
        this.Podservers = Podservers;
        this.ServerFilesText = ServerFilesText;
        this.ServerLogs = ServerLogs;

    }

    @Override
    public void run() {
        //napravi tred koji samo osluskuje da vidi jel neki klijent hoce da pristupi
        //ako klijent posalje help me
        // on mu vraca iz podserverFilePairs jedan par (vektor), tj daje neki server

        while (true) {
            try {
                semaphore.acquireUninterruptibly();

                if (!podserverFilePairsUpdate.isEmpty()) {

                    temp = podserverFilePairsUpdate.remove();
                    podserverFilePairsUpdate.add(temp);
                    //System.out.println(temp.size());
                    Vector addVec = new Vector<String>();
                    addVec.add(temp.elementAt(0));
                    addVec.add(temp.elementAt(1));

                    requestQueue.add(addVec);
                }
                semaphore.release();

                sleep(3000 + (int) Math.random() * 1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void listenToClients(TextArea serverlogs) {

        Runnable r = () -> {

            try {
                servsock = new ServerSocket(port);

            } catch (IOException ex) {
                Logger.getLogger(ServerRequestHandler.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

            // mozda ne terba while
            while (true) {
                try {
                    // Vector<String> podserverFilePair;

                    //napravi sokete
                    sock = servsock.accept();
                    ois = new ObjectInputStream(sock.getInputStream());
                    oos = new ObjectOutputStream(sock.getOutputStream());

                    String fileName = (String) ois.readObject();

                    if (fileName.equalsIgnoreCase("HELP")) {
                        if (!podserverFilePairsUpdate.isEmpty()) {

                            try {

                                ServerCommunicator.ispis("klijentu crkao podserver, traiz novi", serverlogs);
                                oos.writeObject(podserverFilePairsUpdate.remove());
                                //oos.writeObject(new String("novi IP"));

                            } catch (Exception e) {
                                ServerCommunicator.ispis("i klijent je crkao", serverlogs);
                            }
                        }
                        // else fatal error

                    } else if (fileName.equalsIgnoreCase("Podserver")) {

                        String ipadress = fileName;// ubacuje adresu podservera koji se sam pokrenuo ponovo
                        ServerCommunicator.ispis("dodajem novi podserver " + ipadress, serverlogs);
                        ServerAppController.podservers.add(ipadress); // dodaje ip adresu u listu podservera (aktivan)

                    } else {
                        //trazi da li postoji podserver sa tim fajlom
                        semaphore.acquireUninterruptibly();
                        if (!podserverFilePairsUpdate.isEmpty()) {
                            for (Iterator<Vector<String>> iterator = podserverFilePairsUpdate.iterator(); iterator.hasNext();) {
                                Vector<String> next = iterator.next();
                                if (next.elementAt(0).equalsIgnoreCase(fileName)) {
                                    oos.writeObject(next.elementAt(1));
                                    oos.flush();

                                }
                            }
                        }
                        semaphore.release();

                        // ako ne postoji daj mu neki podserver da se prikaci
                        if (!ServerAppController.podservers.isEmpty()) {
                            String next = ServerAppController.podservers.get(0);
                            Vector<String> temp = new Vector<String>();
                            temp.add(fileName);
                            temp.add(next);

                            oos.writeObject(next);
                            oos.flush();

                            semaphore.acquireUninterruptibly();

                            podserverFilePairsUpdate.add(temp);
                            //dodao si u listu parova
                            semaphore.release();
                            //refreshuj ispis podserver files       +

                        }
                        // else fatal error
                        ois.close();
                        oos.close();

                        sock.close();
                    }

                } catch (Exception ex) {
                    ServerCommunicator.ispis("greska server klijent", serverlogs);
                    System.out.println("serverapp.ServerRequestHandler.listenToClients()");
                }
            }

        };
        // run task on different thread
        Thread t = new Thread(r);
        t.start();
    }

}
