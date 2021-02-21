/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author praksa
 */
public class ClientCommunicator extends Thread {

    private static final int PORT_NUMBER = 17555;
    private int port;
    private static String filename;
    private static String serverIP;
    private static String fullDirName;
    private static final String DONE = "DONE";
    private static Socket sock;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;
    private static int fileCount = 0;
    public static Boolean update = false;

    @FXML
    public TextArea ClientText;

    @FXML
    public TextArea ClientLogs;

    ClientCommunicator(String filename, String serverip, int serverport, TextArea ClientText, TextArea ClientLogs) {
        this.filename = filename;
        port = serverport;
        serverIP = serverip;
        this.ClientLogs = ClientLogs;
        this.ClientText = ClientText;
    }

    @Override
    public void run() {
        try {
            listenToUpdate(ClientText);
            System.out.println("clientapp.ClientCommunicator.run()");
            while (true) {
                //fali ogroman slucaj sta ako klijent nema taj fajl
                // nece biti da mu nudi da napravi fajl sam, nego ce morati java t oda uradi.
                // bice nesto hardkodovano fazon kako da on zna gde da napravi taj fajl, bas na tom mestu

                sock = new Socket(serverIP, port);
                oos = new ObjectOutputStream(sock.getOutputStream()); // send directory name to server
                Vector<String> requestVec = new Vector<String>();
                requestVec.add(filename);
                requestVec.add(Integer.valueOf(1).toString());
                //SyncRequests newRequest = new SyncRequests(1, filename);
                oos.writeObject(requestVec);
                oos.flush();
                //reinitConn2();
                // prvi ceka na portu koji je korisnik definisao
                // sluzi

///--------------------------------------------------------------------------------------------------
                //ClientAppController.ClientLogs.appendText("Ubacen zahtev od");
                sock = new Socket(serverIP, PORT_NUMBER);
                ois = new ObjectInputStream(sock.getInputStream());
                oos = new ObjectOutputStream(sock.getOutputStream());
                //ceka da primi da li fajl postoji na serveru

                //oos.writeObject(new Boolean(true));
                Boolean existsOnPodserver = (Boolean) ois.readObject();

                // skipping the base dir as it already should be set up on the server
                //String[] children = baseDir.list();
                File root = new File("c:\\kdp");// ovo se cita sa konzole, moze da se dodaje fajl
                File clientFile = null;

//                if (!root.exists()) {
//                    root.mkdir();
//                }
                Boolean existsOnClient = false;
                String fileName = filename;
                try {
                    boolean recursive = true;

                    Collection files = FileUtils.listFiles(root, null, recursive);

                    for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                        File file = (File) iterator.next();
                        // System.out.println(file.getPath());
                        if (file.getName().equals(fileName)) {
                            clientFile = new File(file.getPath());
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

                    Long l1 = clientFile.lastModified();
                    //upisi u fajl
                    if (!ClientText.getText().isEmpty() && update) {
                        try {

                            FileOutputStream outputStream = new FileOutputStream(clientFile);
                            System.out.println(ClientText.getText());
                            byte[] strToBytes = ClientText.getText().getBytes();
                            outputStream.write(strToBytes);

                            outputStream.close();

//                            BufferedWriter writer = new BufferedWriter(new FileWriter(clientFile));
//                            System.out.println(ClientText.getText());
//                            writer.write(ClientText.getText());
                            Date date = new Date();
                            //This method returns the time in millis
                            long timeMilli = date.getTime();
                            l1 = timeMilli;
                            clientFile.setLastModified(l1);

                        } catch (Exception e) {
                            System.out.println("greska kod upisa");
                        }
                    }

                    vec.add(l1.toString());
                    oos.writeObject(vec);
                    oos.flush();
                    // reinitConn();
                } else {
                    clientFile = new File(root, "novi.txt");
                    vec.add("-1");
                    oos.writeObject(vec);
                    oos.flush();
                    // reinitConn();
                }

                Integer direction = (Integer) ois.readObject();

                if (direction == 1) { // send file to server
                    sendFile(clientFile);

                    ois.readObject(); // make sure server got the file

                    oos.writeObject(clientFile.getName());
                    oos.flush();

                } else if (direction == 0) { // update file from server.

                    oos.writeObject(new Boolean(true)); // send "Ready"
                    oos.flush();

                    receiveFile(clientFile);

                    oos.writeObject(new Boolean(true)); // send back ok
                    oos.flush();

                    Long updateLastModified = (Long) ois.readObject(); // update the last modified date for this file from
                    // the server
                    clientFile.setLastModified(updateLastModified);

                    String newName = (String) ois.readObject();
                    Path source = clientFile.toPath();
                    Files.move(source, source.resolveSibling(newName));
                    oos.writeObject(new Boolean(true));

                } else if (direction == 2) { //send to server

                    oos.writeObject(new Boolean(true));
                    oos.flush();

                    ois.readObject();

                    sendFile(clientFile);

                    ois.readObject();

                    oos.writeObject(new Long(clientFile.lastModified()));
                    oos.flush();

                } else if (direction == 3) { // receive from server

                    clientFile.delete(); // first delete the current file

                    oos.writeObject(new Boolean(true)); // send "Ready"
                    oos.flush();

                    receiveFile(clientFile);

                    oos.writeObject(new Boolean(true)); // send back ok
                    oos.flush();

                    Long updateLastModified = (Long) ois.readObject(); // update the last modified date for this file from
                    clientFile.setLastModified(updateLastModified);

                }
                //on prvi ceka posle sinhronizacije
                String done = (String) ois.readObject();// ceka da se zavrsi

                update = false;

                System.out.println();
                System.out.println("Finished sync");
                ispis("Synced", ClientLogs);
                //gui update ovde
                // ovde fali da se ponovo loaduje fajl u gui.
                loadFile(ClientText, clientFile);

                oos.writeObject(new Boolean(true));
                oos.close();
                ois.close();
                sock.close();
                sleep(3000);
            }

        } catch (Exception e) {
            System.out.println("NESTO SE SJEBALO" + e.getMessage());
        }
        //To change body of generated methods, choose Tools | Templates.
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
        sock = new Socket(serverIP, PORT_NUMBER);

        ois = new ObjectInputStream(sock.getInputStream());
        oos = new ObjectOutputStream(sock.getOutputStream());
    }

    private static void reinitConn2() throws Exception {
        System.out.println("clientapp.ClientCommunicator.reinitConn()");

        ois.close();
        oos.close();
        sock.close();
        sock = new Socket(serverIP, 10000);

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

    public static void listenToUpdate(TextArea textArea) {
        Runnable r = () -> {

            textArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                    // this will run whenever text is changed
                    update = true;
                    System.out.println("KeyPressed");
                }
            });

        };
        // run task on different thread
        Thread t = new Thread(r);
        t.start();

    }
    /* private static File visitAllDirsAndFiles(File dir, String filename) throws Exception {

//		if (fileCount % 20 == 0) {
//			System.out.print(".");
//			fileCount = 0;
//		}
//		fileCount++;
        Vector<String> vec = new Vector<String>();
        vec.add(dir.getName());
        vec.add(dir.getAbsolutePath().substring((dir.getAbsolutePath().indexOf(fullDirName) + fullDirName.length())));

        if (dir.isDirectory()) {
            oos.writeObject(vec);
            oos.flush();
            reinitConn();

            ois.readObject();
        } else {
            if (dir.getName().equalsIgnoreCase(filename)) {
                vec.add(new Long(dir.lastModified()).toString());
                oos.writeObject(vec);
                oos.flush();
                reinitConn();
                // receive SEND or RECEIVE
                Integer updateToServer = (Integer) ois.readObject(); // if true update server, else update from server

                if (updateToServer == 1) { // send file to server
                    sendFile(dir);

                    ois.readObject(); // make sure server got the file

                } else if (updateToServer == 0) { // update file from server.
                    dir.delete(); // first delete the current file

                    oos.writeObject(new Boolean(true)); // send "Ready"
                    oos.flush();

                    receiveFile(dir);

                    oos.writeObject(new Boolean(true)); // send back ok
                    oos.flush();

                    Long updateLastModified = (Long) ois.readObject(); // update the last modified date for this file from
                    // the server
                    dir.setLastModified(updateLastModified);

                } // no need to check if update to server == 2 because we do nothing here

                Vector<String> vecDONE = new Vector<String>();
                vecDONE.add(DONE);
                oos.writeObject(vecDONE);
                oos.flush();
                reinitConn();
            }

        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(new File(dir, children[i]), filename);
            }
        }

        return null;
    }*/
 /*  private static void updateFromServer() throws Exception {
		Boolean isDone = false;
		Boolean nAll = false;
		while(!isDone) {
			if (fileCount % 20 == 0) {
				System.out.print(".");
				fileCount = 0;
			}
			fileCount++;
			String path = (String) ois.readObject();//takes the path
                     //from server

			if(path.equals(DONE)) {
				isDone = true;
				break;
			}

			oos.writeObject(new Boolean(true));
			oos.flush();

			File newFile = new File(fullDirName + path); //da li sada
                        //ovaj fajl postoji na klijentu
			Boolean isDirectory = (Boolean) ois.readObject();

                        if(!newFile.exists()){
                        //napravi ga
                        }

			oos.writeObject(new Boolean(newFile.exists()));
			oos.flush();

			ois.readObject();

			if (isDirectory) {
                            newFile.mkdir();
                            oos.writeObject(new Boolean(false));
                            oos.flush();
                    } else {
                            oos.writeObject(new Integer(0));
                            oos.flush();
                            receiveFile(newFile);

                            oos.writeObject(new Boolean(true));
                            oos.flush();

                            Long lastModified = (Long) ois.readObject();
                            newFile.setLastModified(lastModified);

                            oos.writeObject(new Boolean(true));
                            oos.flush();
                    }
                    if (isDirectory) {
                            oos.writeObject(new Boolean(false));
                            oos.flush();
                    } else {
                            oos.writeObject(new Integer(2));
                            oos.flush();
                    }







		}
	}
     */
}
