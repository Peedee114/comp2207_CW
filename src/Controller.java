
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Kindly note that some code implemented was gotten from the worksheet provided as well as some github sources such as : emiliyanhristov
 */

public class Controller {

//    private ArrayList<String> folders = new ArrayList<>();
//    private HashMap<String, String> fileStatus = new HashMap<>();
    protected static  ArrayList<Integer> dStorePorts = new ArrayList<>();
    protected static Index index = new Index(new HashMap<>(), new ArrayList<>(), new HashMap<>(),new HashMap<>());


    private static int repFactor = 0;

    private static int portCounter = 0;






    // the above parameters will be given on the cli and can be referred to as args[0], args[1], args [2] and args[3]

    /**
     * Stores the files and send the dstore ports to the client.
     *
     * @param fileName file name
     * @param r_factor  replication factor
     * @param printWriter sending messages to the client
     */
    public synchronized static void store(String fileName, int r_factor, PrintWriter printWriter, int filesize) {

        //checks if the file exists and if not adds it with an index and tells the
        //client to store its files to the dStores (ports)
        if (!index.getFileStatus().containsKey(fileName)) {
            index.fileStatus.put(fileName, "Store in Progress");
            System.out.println("Index updated to \"Store in progress\"!");
            index.fileNSize.put(fileName,filesize);

            String ports = "";
            for (int i = 0; i < r_factor; i++) {
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


    public synchronized static void storeCompleted (String filename, CountDownLatch latch, PrintWriter pw) {
        if (!index.files.contains(filename)) {
                   index.files.add(filename);   //adding file to the list of files
                    System.out.println(filename + " now added to the index list of files");
                    index.fileStatus.remove(filename); // removes previous status of this file
                    index.fileStatus.put(filename, "Store Completed");  //adds the file back with a new status
                    index.fileandDstore.put(filename, dStorePorts);

                    latch.countDown();
                }

    }
    public synchronized static void remove (String filename, PrintWriter printWriter) {
        if (index.files.contains(filename) && index.fileandDstore.containsKey(filename)){
            index.fileStatus.put(filename,"Remove in progress");
            System.out.println("Index updated to \"Remove in progress\"!");

                  //dstores that are only associated with that filename
                ArrayList<Integer> dStores = index.fileandDstore.get(filename);
                for (int i = 0; i < dStores.size(); i++) {
                    try {
                        Socket dSoc = new Socket(InetAddress.getLocalHost(), dStores.get(i));
                        PrintWriter dStoreOut = new PrintWriter(dSoc.getOutputStream(), true);
                        dStoreOut.println(Protocol.REMOVE_TOKEN + " " + filename);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }

        } else {
            printWriter.println(Protocol.ERROR_FILE_ALREADY_EXISTS_TOKEN);
            System.out.println(Protocol.ERROR_FILE_ALREADY_EXISTS_TOKEN);

        }
    }
    public synchronized static void removeCompleted (CountDownLatch latch, String filename) {
        if (index.files.contains(filename)) {
            index.files.remove(filename);   //adding file to the list of files
            System.out.println(filename + " has now been removed from the index list of files");
            index.fileStatus.put(filename, "Remove Completed");  //adds the file back with a new status
            index.fileandDstore.remove(filename);
            index.fileNSize.remove(filename);

            latch.countDown();
        }
    }

    public synchronized static void list(PrintWriter printWriter) {


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

    public synchronized static void load(String filename, PrintWriter printWriter, int portCounter) {
    if (index.files.contains(filename) && index.fileNSize.containsKey(filename)) {
        int fileSize = index.fileNSize.get(filename);
        int port = dStorePorts.get(portCounter);

        printWriter.println(Protocol.LOAD_FROM_TOKEN + " " + port + " " + fileSize);
        System.out.println(Protocol.LOAD_FROM_TOKEN + " " + port + " " + fileSize );
    }
    else {
        printWriter.println(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN);
        System.out.println(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN);
   }

    }


    public static void addingDstores(String port) {
        dStorePorts.add(Integer.valueOf(port));
        System.out.println("Dstore on port "+ Integer.parseInt(port) + " has been added");
    }
    public static void setRepFactor (int rep_Factor) {
        repFactor = rep_Factor;
    }

    public static void setPortCounter(int port_counter) {
        portCounter = port_counter;
    }

        public static Boolean isDStoresEnough(PrintWriter printWriter) {
        if (dStorePorts.size() >= repFactor) {
            return true;
        } else {
            printWriter.println(Protocol.ERROR_NOT_ENOUGH_DSTORES_TOKEN);
            System.out.println(Protocol.ERROR_NOT_ENOUGH_DSTORES_TOKEN);

            return false;
        }

    }

    public synchronized static void removeFailedDstore (Integer port) {
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
            int cPort =  Integer.parseInt(args[0]);
            int repFactor = Integer.parseInt(args[1]);
            //making replication factor to be same with the one from terminal
            setRepFactor(repFactor);
            int timeout = Integer.parseInt(args[2]);
            int reBalancePeriod = Integer.parseInt(args[3]);




            CountDownLatch countDownLatch = new CountDownLatch(dStorePorts.size());
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
                                                store(message[1], repFactor, clientPrintWriter,Integer.parseInt(message[2]));
                                            }

                                        } else if (message[0].equals(Protocol.STORE_ACK_TOKEN)) {
                                            if (isDStoresEnough(clientPrintWriter)) {
                                                storeCompleted(message[1], countDownLatch, clientPrintWriter);
                                            }
                                            //waits for all acks
                                            if (countDownLatch.await(timeout,TimeUnit.MILLISECONDS)) {
                                                System.out.println("All STORE ACKs received");

                                                clientPrintWriter.println(Protocol.STORE_COMPLETE_TOKEN);
                                                System.out.println("Stored " + message[1] + " completed!");
                                            }

                                        } else if(message[0].equals(Protocol.LOAD_TOKEN)) {
                                                if (isDStoresEnough(clientPrintWriter)) {
                                                   setPortCounter(0);
                                                   load(message[1],clientPrintWriter, portCounter);
                                                }
                                        } else if (message[0].equals(Protocol.RELOAD_TOKEN)) {
                                            if (index.fileNSize.size() == portCounter) {
                                                clientPrintWriter.println(Protocol.ERROR_LOAD_TOKEN);
                                            }else if (isDStoresEnough(clientPrintWriter)) {
                                                setPortCounter(portCounter);  //the portcounter to be the most current one
                                                load(message[1], clientPrintWriter, portCounter + 1);  //increments the port counter here
                                            }
                                        } else if (message[0].equals(Protocol.REMOVE_TOKEN)) {
                                            if (isDStoresEnough(clientPrintWriter)) {
                                                remove(message[1], clientPrintWriter);
                                            }
                                        } else if (message[0].equals(Protocol.REMOVE_ACK_TOKEN)) {
                                           if (isDStoresEnough(clientPrintWriter)) {
                                                removeCompleted(countDownLatch,message[1]);

                                            } if (countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                                                System.out.println("All REMOVE ACKs received");

                                                clientPrintWriter.println(Protocol.REMOVE_COMPLETE_TOKEN);
                                                System.out.println("Removed " + message[1] + " completed!");
                                            }
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
