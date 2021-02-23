/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author praksa
 */
class ServerCommunicator extends Thread {

    private static int failedConnections = 0;
    private static final int PORT_NUMBER = 17555;
    private static String filename;
    private static String podserverIP;
    private static String fullDirName;
    private static final String DONE = "DONE";
    private static Socket sock;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;
    private static Vector<String> nextRequest;
    @FXML
    private TextArea ServerLogs;

    @FXML
    private TextArea ServerFilesText;

    @FXML
    private TextArea Podservers;

    ServerCommunicator(TextArea ServerLogs, TextArea ServerFilesText, TextArea Podservers) {

        this.Podservers = Podservers;
        this.ServerFilesText = ServerFilesText;
        this.ServerLogs = ServerLogs;

    }

    @Override
    public void run() {

        while (true) {
            try {

                while (ServerRequestHandler.requestQueue.isEmpty()) {
                }
                nextRequest = ServerRequestHandler.requestQueue.remove();
                filename = nextRequest.remove(0);
                podserverIP = nextRequest.remove(1);

                sock = new Socket(podserverIP, PORT_NUMBER);
                ois = new ObjectInputStream(sock.getInputStream());
                oos = new ObjectOutputStream(sock.getOutputStream()); // send directory name to server
                Vector<String> requestVec = new Vector<String>();

                requestVec.add(filename);
                requestVec.add(Integer.valueOf(2).toString());

                //SyncRequests newRequest = new SyncRequests(1, filename);
                ispis("Trenutno updateuje:" + filename + "na podserveru" + podserverIP, ServerLogs);
                oos.writeObject(requestVec);
                oos.flush();

///--------------------------------------------------------------------------------------------------
                //ClientAppController.ClientLogs.appendText("Ubacen zahtev od");
                //ceka da primi da li fajl postoji na serveru
                //oos.writeObject(new Boolean(true));
                Boolean existsOnPodserver = (Boolean) ois.readObject();

                // skipping the base dir as it already should be set up on the server
                //String[] children = baseDir.list();
                File root = new File("c:\\kdp");// ovo se cita sa konzole, moze da se dodaje fajl
                File serverFile = null;

                if (!root.exists()) {
                    root.mkdir();
                }

                Boolean existsOnClient = false;
                String fileName = filename;
                try {
                    boolean recursive = true;

                    Collection files = FileUtils.listFiles(root, null, recursive);

                    for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                        File file = (File) iterator.next();
                        // System.out.println(file.getPath());
                        if (file.getName().equals(fileName)) {
                            serverFile = new File(file.getPath());
                            existsOnClient = true;
                            System.out.println(file.getPath());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                oos.writeObject(existsOnClient);
                oos.flush();

                Vector<String> vec = new Vector<String>();

                if (existsOnClient) {

                    Long l1 = serverFile.lastModified();
                    //upisi u fajl

                    vec.add(l1.toString());
                    oos.writeObject(vec);
                    oos.flush();
                    // reinitConn();
                } else {
                    serverFile = new File(root, "novi.txt");
                    vec.add("-1");
                    oos.writeObject(vec);
                    oos.flush();
                    // reinitConn();
                }

                Integer direction = (Integer) ois.readObject();

                if (direction == 1) { // send file to server
                    sendFile(serverFile);

                    ois.readObject(); // make sure server got the file

                    oos.writeObject(serverFile.getName());
                    oos.flush();

                } else if (direction == 0) { // update file from server.

                    oos.writeObject(new Boolean(true)); // send "Ready"
                    oos.flush();
                    //rollback
                    receiveFile(serverFile);

                    oos.writeObject(new Boolean(true)); // send back ok
                    oos.flush();

                    Long updateLastModified = (Long) ois.readObject(); // update the last modified date for this file from
                    // the server
                    serverFile.setLastModified(updateLastModified);

                    String newName = (String) ois.readObject();
                    Path source = serverFile.toPath();
                    Files.move(source, source.resolveSibling(newName));
                    oos.writeObject(new Boolean(true));

                } else if (direction == 2) { //send to server

                    oos.writeObject(new Boolean(true));
                    oos.flush();

                    ois.readObject();

                    sendFile(serverFile);

                    ois.readObject();

                    oos.writeObject(new Long(serverFile.lastModified()));
                    oos.flush();

                } else if (direction == 3) { // receive from server

                    serverFile.delete(); // first delete the current file

                    oos.writeObject(new Boolean(true)); // send "Ready"
                    oos.flush();

                    receiveFile(serverFile);

                    oos.writeObject(new Boolean(true)); // send back ok
                    oos.flush();

                    Long updateLastModified = (Long) ois.readObject(); // update the last modified date for this file from
                    serverFile.setLastModified(updateLastModified);

                }
                //on prvi ceka posle sinhronizacije
                String done = (String) ois.readObject();// ceka da se zavrsi

                System.out.println();
                System.out.println("Finished sync");
                ispis("Uspesna sinhronizacija sa " + sock.getInetAddress().toString() + "za faj: " + filename + "\n", ServerLogs);
                //gui update ovde

                oos.writeObject(new Boolean(true));
                oos.close();
                ois.close();
                sock.close();

                failedConnections = 0;

                sleep(3000);

            } catch (ConnectException e) {
                failedConnections++;

                if (failedConnections == 3) {

                    for (Iterator<String> iterator = ServerRequestHandler.podserverList.iterator(); iterator.hasNext();) {
                        String next = iterator.next();
                        if (next.equals(sock.getInetAddress().toString())) {
                            ServerRequestHandler.podserverList.remove(next);// proveri jel radi ovo
                            Vector<String> tempVector = new Vector<String>();
                            tempVector.add(filename);
                            tempVector.add(next);
                            ServerRequestHandler.podserverFilePairsUpdate.remove(tempVector);
                        }
                    }

                }
                try {
                    sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (SocketException e) {

                System.out.println(e.getMessage());

            } catch (IOException e) {
                System.out.println(e.getMessage());

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private static void sendFile(File dir) throws Exception {
        byte[] buff = new byte[sock.getSendBufferSize()];
        int bytesRead = 0;

        InputStream in = new FileInputStream(dir);

        while ((bytesRead = in.read(buff)) > 0) {
            oos.write(buff, 0, bytesRead);
        }
        in.close();
        // after sending a file you need to close the socket and reopen one.
        oos.flush();
        reinitConn();

        // printDebug(true, dir);
    }

    private static void receiveFile(File dir) throws Exception {
        FileOutputStream wr = new FileOutputStream(dir);
        byte[] outBuffer = new byte[sock.getReceiveBufferSize()];
        int bytesReceived = 0;
        while ((bytesReceived = ois.read(outBuffer)) > 0) {
            wr.write(outBuffer, 0, bytesReceived);
        }
        wr.flush();
        wr.close();

        reinitConn();

        // printDebug(false, dir);
    }

    private static void printDebug(Boolean sending, File dir) {
        if (sending) {
            System.out.println("SEND=Name: " + dir.getName() + " Dir: " + dir.isDirectory() + " Modified: "
                    + dir.lastModified() + " Size: " + dir.length());
        } else {
            System.out.println("RECV=Name: " + dir.getName() + " Dir: " + dir.isDirectory() + " Modified: "
                    + dir.lastModified() + " Size: " + dir.length());
        }
    }

    private static void reinitConn() throws Exception {
        System.out.println("clientapp.ClientCommunicator.reinitConn()");

        ois.close();
        oos.close();
        sock.close();
        sock = new Socket(podserverIP, PORT_NUMBER);

        ois = new ObjectInputStream(sock.getInputStream());
        oos = new ObjectOutputStream(sock.getOutputStream());
    }

    private static void reinitConn2() throws Exception {
        System.out.println("clientapp.ClientCommunicator.reinitConn()");

        ois.close();
        oos.close();
        sock.close();
        sock = new Socket(podserverIP, 10000);

        ois = new ObjectInputStream(sock.getInputStream());
        oos = new ObjectOutputStream(sock.getOutputStream());
    }

    private static void ispis(String ispis, TextArea PodserverLogs) {
        Runnable r = () -> {

            Platform.runLater(() -> PodserverLogs.appendText(ispis + "\n"));

            // System.out.println(sc.nextLine());
            // append the line on the application thread
        };
        // run task on different thread
        Thread t = new Thread(r);
        t.start();

    }

    private static void loadFile(TextArea PodserverLogs, File file) {
        Runnable r = () -> {

            try {

                Scanner sc = new Scanner(file);

                sc.useDelimiter("\\Z");
                String print;

                Platform.runLater(() -> PodserverLogs.setText(sc.next()));

                // System.out.println(sc.nextLine());
                // append the line on the application thread
            } catch (IOException e) {
                //e.printStackTrace();
            }
        };
        // run task on different thread
        Thread t = new Thread(r);
        t.start();

    }

}
