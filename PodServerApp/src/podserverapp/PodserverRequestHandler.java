/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package podserverapp;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 *
 * @author praksa
 */
class PodserverRequestHandler extends Thread {

    public static PriorityBlockingQueue<SyncRequests> requestBuffer;
    public SyncRequests newsyncRequest;
    private static int requestPort;
    private final String DONE = "DONE";
    private static Socket sock;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static ServerSocket servsock;
    private static String baseDir;
    private static String filename;

    @FXML
    public TextArea PodserverLogs;

    PodserverRequestHandler(int port, TextArea PodserverLogs) {
        requestPort = port;
        requestBuffer = new PriorityBlockingQueue<SyncRequests>();
        this.PodserverLogs = PodserverLogs;

    }

    @Override
    public void run() {
        System.out.println("Starting File Sync Server!");
        try {

            servsock = new ServerSocket(requestPort);//10000

            while (true) {

                sock = servsock.accept();

                System.out.println("podserverapp.PodserverRequestHandler.run()");
                ois = new ObjectInputStream(sock.getInputStream());

                Vector<String> vec = new Vector<String>();

                vec = (Vector<String>) ois.readObject();
                //reinitConn();
                newsyncRequest = new SyncRequests(Integer.parseInt(vec.elementAt(1)), vec.elementAt(0));

                requestBuffer.add(newsyncRequest);

                System.out.println("podserverapp.PodserverRequestHandler.run()");
                ;

                ois.close();

                //PodserverController.PodserverLogs.appendText("dodat je zahtev od IP:" + sock.getInetAddress().toString() + "za fajl" + newsyncRequest.getDirname());
                //pisi u podserver log da je primio zahtev taj...
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void reinitConn() throws Exception {
        System.out.println("podserverapp.PodserverRequestHandler.reinitConn()");
        oos.close();
        ois.close();
        sock.close();
        sock = servsock.accept();
        oos = new ObjectOutputStream(sock.getOutputStream());
        ois = new ObjectInputStream(sock.getInputStream());
    }

    private static void ispis(String ispis, TextArea PodserverLogs) {
        Runnable r = () -> {

            Platform.runLater(() -> PodserverLogs.appendText(ispis));

            // System.out.println(sc.nextLine());
            // append the line on the application thread
        };
        // run task on different thread
        Thread t = new Thread(r);
        t.start();

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
}
