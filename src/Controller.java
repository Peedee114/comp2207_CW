
import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kindly note that some code implemented was gotten from the worksheet provided as well as some github sources such as : emiliyanhristov
 */

public class Controller {

//    private ArrayList<String> folders = new ArrayList<>();
//    private HashMap<String, String> fileStatus = new HashMap<>();
    protected static  ArrayList<Integer> dStorePorts = new ArrayList<>();
    protected static Index index = new Index(new HashMap<>(), new ArrayList<>(), new HashMap<>(),new HashMap<>());

    protected static Map<Integer, Socket> socketMap = new HashMap<>();


    private static int repFactor = 0;

    private static int portCounter = 0;

    private static int deletesCompleted = 0;

    private static int storesCompleted = 0;






    // the above parameters will be given on the cli and can be referred to as args[0], args[1], args [2] and args[3]

    /**
     * Stores the files and send the dstore ports to the client.
     *
     * @param fileName file name
//     * @param r_factor  replication factor
     * @param printWriter sending messages to the client
     */
    private synchronized static void store(String fileName, PrintWriter printWriter, int filesize) {

        //checks if the file exists and if not adds it with an index and tells the
        //client to store its files to the dStores (ports)
        if (!index.fileStatus.containsKey(fileName) ) {

            index.fileandDstore.put(fileName, dStorePorts);
            index.fileStatus.put(fileName, "Store in Progress");
            System.out.println("Index updated to \"Store in progress\"!");
            index.fileNSize.put(fileName,filesize);

            String ports = "";
            for (int i = 0; i < dStorePorts.size(); i++) {
                ports = ports + " " + dStorePorts.get(i);
            }
            printWriter.println(Protocol.STORE_TO_TOKEN + ports);
            System.out.println("STORE_TO message sent to Client");
        }
        else {
            printWriter.println(Protocol.ERROR_FILE_ALREADY_EXISTS_TOKEN);
            System.out.println(Protocol.ERROR_FILE_ALREADY_EXISTS_TOKEN);
        }

    }


    private synchronized static void storeCompleted (String filename, PrintWriter clientPw, int currentDport) {

               if (!index.files.contains(filename)) {
                   index.files.add(filename);   //adding file to the list of files
                   System.out.println(filename + " now added to the index list of files");
                   if (index.fileandDstore.containsKey(filename) && dStorePorts.contains(currentDport)) {
                       index.fileandDstore.remove(filename);
                       index.fileandDstore.put(filename,dStorePorts);
                   }
                   index.fileStatus.remove(filename); // removes previous status of this file
                   index.fileStatus.put(filename, "Store Completed");  //adds the file back with a new status
                   System.out.println("Index updated to \"Store complete\"!");
                   clientPw.println(Protocol.STORE_COMPLETE_TOKEN);
                   System.out.println("\"STORE_COMPLETE\" message sent to Client");




           } else {
                       index.files.remove(filename);
                       index.fileandDstore.remove(filename);
                       index.fileStatus.remove(filename);
                       index.fileNSize.remove(filename);
                       System.out.println("File not sent to dstore");


               }

    }


    private synchronized static void remove (String filename, PrintWriter printWriter) {
        if (index.fileandDstore.containsKey(filename) && index.files.contains(filename)) {
            index.fileStatus.remove(filename);
            index.fileStatus.put(filename, "Remove in Progress");
            System.out.println("Index updated to \"Remove in progress\"!");


            for (int i = 0; i < dStorePorts.size(); i++) {
                try {

                    Socket dSoc = new Socket(InetAddress.getLocalHost(), dStorePorts.get(i));
                    PrintWriter dStoreOut = new PrintWriter(dSoc.getOutputStream(), true);
                    dStoreOut.println(Protocol.REMOVE_TOKEN + " " + filename);
                    System.out.println(Protocol.REMOVE_TOKEN + " sent to dstore on port " + dStorePorts.get(i));


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        } else {
            printWriter.println(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN);
            System.out.println(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN);

        }
    }
    private synchronized static void removeCompleted (String filename, PrintWriter clientPw) {

                if (index.files.contains(filename) && index.fileandDstore.containsKey(filename)) {
                    index.files.remove(filename);   //adding file to the list of files
                    System.out.println(filename + " has now been removed from the index list of files");
                    index.fileStatus.remove(filename);
                    index.fileStatus.put(filename, "Remove Completed");  //adds the file back with a new status
                    System.out.println("Index updated to \"Remove complete\"!");
                        index.fileandDstore.get(filename);
                        index.fileNSize.remove(filename);


                    clientPw.println(Protocol.REMOVE_COMPLETE_TOKEN);
                    System.out.println("\"REMOVE_COMPLETE\" message sent to Client");


                }

    }

    private synchronized static void list(PrintWriter printWriter) {


        String fileNames = " ";
        if (!index.files.isEmpty()) {
            for (String file: index.files) {
                fileNames = file + " " + fileNames;
            }

//            String[] filenames = fileNames.split(" ");
//            List<String>  listOfFilenames = Arrays.asList(filenames);
            printWriter.println(Protocol.LIST_TOKEN + " " + fileNames);
            System.out.println("List of Files: " + fileNames);
        } else {
            printWriter.println(Protocol.LIST_TOKEN);
        }
    }

    private synchronized static void load(String filename, PrintWriter printWriter, int portCounter) {
    if (index.files.contains(filename) && index.fileNSize.containsKey(filename)) {
        int fileSize = index.fileNSize.get(filename);
        int port = index.fileandDstore.get(filename).get(portCounter);

        printWriter.println(Protocol.LOAD_FROM_TOKEN + " " + port + " " + fileSize);
        System.out.println(Protocol.LOAD_FROM_TOKEN + " " + port + " " + fileSize );
    }
    else {
        printWriter.println(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN);
        System.out.println(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN);
   }

    }


    private static void addingDstores(String port) {
        dStorePorts.add(Integer.valueOf(port));
        System.out.println("Dstore on port "+ Integer.parseInt(port) + " has been added");
    }
    private static void setRepFactor (int rep_Factor) {
        repFactor = rep_Factor;
    }

    private static void setPortCounter(int port_counter) {
        portCounter = port_counter;
    }

    private static void setStoresCompleted (int storesCompleted) {
        Controller.storesCompleted = storesCompleted;
    }

    private static int getStoresCompleted() {
        return storesCompleted;
    }


        private static Boolean isDStoresEnough(PrintWriter printWriter) {
        if (dStorePorts.size() < repFactor) {
            printWriter.println(Protocol.ERROR_NOT_ENOUGH_DSTORES_TOKEN);
            System.out.println(Protocol.ERROR_NOT_ENOUGH_DSTORES_TOKEN);
            return false;
        }
            return true;
    }

    private synchronized static void removeFailedDstore (Integer port) {
        if (port == 0) return;

        dStorePorts.remove(port);

// removing from storage system when associated with a file
        for (String filename: index.files ) {
            if (index.fileandDstore.containsKey(filename)) {
                index.fileandDstore.get(filename).remove(port);
            } if (index.fileandDstore.size() == 1) {
                index.fileStatus.remove(filename);
                index.fileNSize.remove(filename);
                index.files.remove(filename);
            }

        }


    }


    public static void main(String[] args) {
            int cPort = Integer.parseInt(args[0]);
            int repFactor = Integer.parseInt(args[1]);
            //making replication factor to be same with the one from terminal
            setRepFactor(repFactor);
            int timeout =  Integer.parseInt(args[2]);
            int reBalancePeriod = Integer.parseInt(args[3]);




            CountDownLatch storeLatch = new CountDownLatch(dStorePorts.size());
            CountDownLatch removeLatch = new CountDownLatch(dStorePorts.size());

//            ClientHandler clientHandler = new ClientHandler()
            final int currentDstorePort;

            //clears the dStores and the files
            index.clear();
            dStorePorts.clear();

            try {
                ServerSocket ss = new ServerSocket(cPort);
                for(;;) {
                    try {
                        //client's stuff
                        System.out.println("Waiting for connection");
                        Socket client = ss.accept();

                        System.out.println("Connected!");



                      new Thread(new Runnable() {
                            @Override
                            public void run() {

                                int currentDstorePort = 0;
                                int storeAck = 0;
                                try {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                    PrintWriter clientPrintWriter = new PrintWriter(client.getOutputStream(),true);
//                                    boolean dStore = true;
                                    String line;

                                    while ((line = in.readLine()) != null) {
                                        String[] message = line.split(" ");

                                        if (message[0].equals(Protocol.JOIN_TOKEN)) {
                                            currentDstorePort  = Integer.valueOf(message[1]);
                                            addingDstores(message[1]);
//                                            Socket dsoc = new Socket(InetAddress.getLoopbackAddress(), Integer.parseInt(message[1]));
//                                            PrintWriter dOut = new PrintWriter(dsoc.getOutputStream());
                                            System.out.println(Protocol.JOIN_TOKEN + " TOKEN has been received");
//
                                        } else if (message[0].equals(Protocol.LIST_TOKEN)) {
                                            if (isDStoresEnough(clientPrintWriter)) {
                                                list(clientPrintWriter);
                                            }
//                                            else if (message[0].equals(Protocol.LIST_TOKEN) && dStore) {
//                                                //rebalance stuff
//                                            }

                                        } else if (message[0].equals(Protocol.STORE_TOKEN)) {
                                            if (isDStoresEnough(clientPrintWriter)) {
                                                store(message[1], clientPrintWriter,Integer.parseInt(message[2]));

                                                try {
                                                    storeLatch.await();
                                                    storeCompleted(message[1], clientPrintWriter, currentDstorePort);
                                                } catch (InterruptedException e) {
                                                    System.out.println("Waiting for acknowledgement interrupted " + e);
                                                }
                                            }
                                        } else if ((message[0].equals(Protocol.STORE_ACK_TOKEN))) {
                                            storeLatch.countDown();
                                        } else if(message[0].equals(Protocol.LOAD_TOKEN)) {
                                                if (isDStoresEnough(clientPrintWriter)) {
                                                   setPortCounter(0);
                                                   load(message[1],clientPrintWriter, portCounter);
                                                }
                                        } else if (message[0].equals(Protocol.RELOAD_TOKEN)) {
                                            //if for a particular file
                                            if (index.fileandDstore.get(message[1]).size() - 1 == portCounter) {
                                                clientPrintWriter.println(Protocol.ERROR_LOAD_TOKEN);
                                            }else if (isDStoresEnough(clientPrintWriter)) {
                                                setPortCounter(portCounter + 1);  //set the portCounter to be the most current one
                                                load(message[1], clientPrintWriter, portCounter);  //increments the port counter here
                                            }
                                        } else if (message[0].equals(Protocol.REMOVE_TOKEN)) {
                                            if (isDStoresEnough(clientPrintWriter)) {
                                                remove(message[1], clientPrintWriter);
                                                //dstores that are only associated with that filename
                                                try {
                                                    removeLatch.await();
                                                    removeCompleted(message[1],clientPrintWriter);
                                                } catch (InterruptedException e) {
                                                    System.out.println("Waiting for acknowledgement interrupted " + e);
                                                }
                                            }
                                        } else if ((message[0].equals(Protocol.REMOVE_ACK_TOKEN)) && (index.fileStatus.get(message[1]) == "Remove in Progress")) {
                                            removeLatch.countDown();
                                        } else if (message[0].equals(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN)) {
                                            System.out.println(line);
                                        }
                                        else {
                                            System.out.println("UNEXPECTED MESSAGE: " + line);
                                        }
                                    }
                                    client.close();
                                } catch (Exception e) {
                                    System.out.println("error1: " + e);
                                    removeFailedDstore(currentDstorePort);
                                    System.out.println("Dstore on " + currentDstorePort + " has been removed.");

                                }
                            }
                        }).start();

                        //in.close();
                       // printWriter.close();
                    } catch (Exception e) {
                        System.out.println("error: " + e);

                    }
                }
            } catch(Exception e) {
                System.out.println("error: " + e);
            }
        }

    /**
     * to get the ports the client
     */
//    class ClientHandler {
//
//        private Socket socket;
//
//        public int clientPort;
//
//        public ClientHandler(Socket socket) {
//            this.socket = socket;
//            this.clientPort = socket.getPort();
//        }
//
//        public int getPort() {
//            return this.clientPort;
//        }
//    }



}
