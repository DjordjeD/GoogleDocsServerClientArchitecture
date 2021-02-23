/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package podserverapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
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
class PodserverCommunicator extends Thread {

    private static final int PORT_NUMBER = 17555;
    private static final String DONE = "DONE";
    private static Socket sock;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static ServerSocket servsock;
    private static String baseDir;
    private static String filename;

    @FXML
    public TextArea PodserverLogs;

    PodserverCommunicator(TextArea PodserverLogs) {
        this.PodserverLogs = PodserverLogs;
    }

    @Override
    public void run() {

        try {
            servsock = new ServerSocket(PORT_NUMBER);
        } catch (IOException ex) {
            Logger.getLogger(PodserverCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }

        //napravi sokete while (true) {
        while (true) {
            try {
                sock = servsock.accept();
                oos = new ObjectOutputStream(sock.getOutputStream());
                ois = new ObjectInputStream(sock.getInputStream());

                //dokle god ne izvuces request izvlaci
                while (PodserverRequestHandler.requestBuffer.isEmpty()) {
                }
                filename = PodserverRequestHandler.requestBuffer.remove().getDirname();

                ispis("Trenutno updateuje:" + filename + sock.getInetAddress().toString(), PodserverLogs);

                //proveri da li imas ovaj fajl na podserveru
                File root = new File("c:\\kdp");
                File podserverFile = null;
                String fileName = filename;
                try {
                    boolean recursive = true;

                    Collection files = FileUtils.listFiles(root, null, recursive);

                    for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                        File file = (File) iterator.next();
                        System.out.println(file.getPath());
                        if (file.getName().equals(fileName)) {
                            podserverFile = file;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Boolean fileOnPodserver = false;
                if (podserverFile.exists()) {
                    fileOnPodserver = true;
                }

                System.out.println("Directory: " + baseDir);

                //ako imas ili nemas vrati
                oos.writeObject(fileOnPodserver);
                oos.flush();

                //da cekas da vidis jel klijent ima taj fajl;
                Boolean clientHasFile = (Boolean) ois.readObject();

                //String clientFilePath = (String) ois.readObject();
                //oos.writeObject((String));
                //sad se ide u proveru
                Integer direction = 4; //do nothing, both have it, same modfied time
                //direction =0 podsever->client
                //direction = 1 client->podserver

                Vector<String> vec = (Vector<String>) ois.readObject();// read path and lastmodified
                //reinitConn();

                Long clientModifiedTime = new Long(vec.elementAt(0));

                if (clientHasFile && !fileOnPodserver) {

                    direction = 1; //take from client, you made the directory, he copies it

                    oos.writeObject(new Integer(direction));
                    oos.flush();
                    // ovde kopiraj taj fajl
                    // ako baci exception jebiga uzmi stari
                    receiveFile(podserverFile);

                    podserverFile.setLastModified(clientModifiedTime);

                    oos.writeObject(new Boolean(true));
                    oos.flush();

                    String clientfilename = (String) ois.readObject();
                    Path source = podserverFile.toPath();
                    Files.move(source, source.resolve(clientfilename));

                } else if (!clientHasFile && fileOnPodserver) {
                    direction = 0;

                    oos.writeObject(new Integer(direction));
                    oos.flush();

                    ois.readObject();

                    sendFile(podserverFile);

                    ois.readObject();

                    oos.writeObject(new Long(podserverFile.lastModified()));
                    oos.flush();

                    oos.writeObject(podserverFile.getName());
                    oos.flush();

                    ois.readObject();

                } else if (clientHasFile && fileOnPodserver) {
                    //they both have the file

                    if (podserverFile.lastModified() < clientModifiedTime) {
                        direction = 2; //uzmi fajl od klijenta
                        oos.writeObject(new Integer(direction));
                        oos.flush();

                        ois.readObject();

                        podserverFile.delete();

                        oos.writeObject(new Boolean(true)); // send back ok
                        oos.flush();

                        receiveFile(podserverFile);

                        oos.writeObject(new Boolean(true)); // send back ok
                        oos.flush();

                        Long newModifiedTime = (Long) ois.readObject();
                        podserverFile.setLastModified(newModifiedTime);

                    } else if (podserverFile.lastModified() > clientModifiedTime) {
                        direction = 3; // posalji na klijent
                        oos.writeObject(new Integer(direction));
                        oos.flush();

                        ois.readObject();

                        sendFile(podserverFile);

                        ois.readObject();

                        oos.writeObject(new Long(podserverFile.lastModified()));
                        oos.flush();

                    } else {

                        oos.writeObject(new Integer(direction));
                        oos.flush();
                    }
                }

                oos.writeObject(new String(DONE));
                oos.flush();

                System.out.print("Finished sync...");

                // loadFile(PodserverLogs);
                //PodserverController.PodserverLogs.appendText("finished sync");
                ois.readObject();

                oos.close();
                ois.close();
                sock.close();

            } catch (SocketException e) {
                System.out.println("Greska" + e.getMessage());
                try {
                    oos.close();
                    ois.close();
                    sock.close();
                } catch (IOException ex) {
                    Logger.getLogger(PodserverCommunicator.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException e) {

                System.out.println("Greska" + e.getMessage());

            } catch (Exception e) {
                System.out.println("Greska" + e.getMessage());
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
    }

    private static void reinitConn() throws Exception {
        oos.close();
        ois.close();
        sock.close();
        sock = servsock.accept();
        oos = new ObjectOutputStream(sock.getOutputStream());
        ois = new ObjectInputStream(sock.getInputStream());
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

    private static void loadFile(TextArea PodserverLogs) {
        Runnable r = () -> {

            try {
                File file
                        = new File("C:\\kdp\\opetnovagara\\zare.txt");
                Scanner sc = new Scanner(file);

                sc.useDelimiter("\\Z");
                String print;

                Platform.runLater(() -> PodserverLogs.appendText(sc.next()));

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
    /* private static void visitAllDirsAndFiles(File dir) throws Exception {
        oos.writeObject(new String(dir.getAbsolutePath().substring((dir.getAbsolutePath().indexOf(baseDir) + baseDir.length()))));
        oos.flush();

        ois.readObject();

        Boolean isDirectory = dir.isDirectory();
        oos.writeObject(new Boolean(isDirectory));
        oos.flush();

        if (isDirectory) {
            if (!(Boolean) ois.readObject()) {
                oos.writeObject(new Boolean(true));
                oos.flush();
            }
        } else {
            if (!(Boolean) ois.readObject()) {
                oos.writeObject(new Boolean(true));
                oos.flush();

                Integer delete = (Integer) ois.readObject();

                if (delete == 0) {
                    sendFile(dir);

                    ois.readObject();

                    oos.writeObject(new Long(dir.lastModified()));
                    oos.flush();

                    ois.readObject();
                } // ELSE DO NOTHING!
            }
        }

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(new File(dir, children[i]));
            }
        }
    }
     */
//	private static void deleteAllDirsAndFiles(File dir) {
//		if (dir.isDirectory()) {
//			String[] children = dir.list();
//			for (int i=0; i<children.length; i++) {
//				deleteAllDirsAndFiles(new File(dir, children[i]));
//			}
//		}
//		dir.delete();
//	}
}
