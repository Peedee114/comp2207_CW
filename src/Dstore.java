import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
//import org.apache.logging.log4j.Logger;

/**
 * Kindly note that some code implemented was gotten from the worksheet provided as well as some github sources such as : emiliyanhristov
 */

public class Dstore {

    static String file_folder;

    protected static void setFileFolder (String fileFolder) {
        file_folder = fileFolder;
    }

    private synchronized static void receiveAndStoreFileContent(InputStream inputStream, String currentFileName , int filesize) {

        try {
            File file = new File(file_folder + File.separator + currentFileName);
            OutputStream fileOutputStream = new FileOutputStream(file);
            byte[] content = new byte[filesize];
             // gets the number of bytes read from the buffer

            while((inputStream.readNBytes(content, 0, content.length)) > 0) {
                fileOutputStream.write(content,0,content.length);
            }
            fileOutputStream.close();
            System.out.println("File " + currentFileName + " is stored");

        } catch (Exception e) {
            System.out.println("error: " + e);
        }
    }

    private synchronized static void remove(PrintWriter pw, String filename) {
        File file = new File(file_folder + File.separator + filename);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println(filename + " has been deleted");
                            }
        } else {
            pw.println(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN + " " + filename);
            System.out.println(Protocol.ERROR_FILE_DOES_NOT_EXIST_TOKEN + " " + filename);
        }
    }

    public synchronized static void list (PrintWriter pw) {
        String fileNames= " ";
        File fileFolder = new File(file_folder);

            for (String file : fileFolder.list()) {
                fileNames = file + " " + fileNames;
            }

            //String[] filenames = fileNames.split(" ");
//            List<String> listOfFilenames = Arrays.asList(fileNames);
            pw.println(Protocol.LIST_TOKEN + " " + fileNames);
            System.out.println("List of Files: " + fileNames);

    }
    public synchronized static void loadFileContent(String filename, OutputStream outputStream) {
        File file = new File(file_folder + File.separator + filename);
        try {
            InputStream inputStream = new FileInputStream(file);
            int fileContent;
            while ((fileContent = inputStream.read()) != -1) {
                outputStream.write(fileContent);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // gets the number of bytes read from the buffer

//        while((inputStream.readNBytes(content, 0, content.length)) > 0) {
//            fileOutputStream.write(content,0,content.length
//        }

        //use input stream to do load
    }

    public static void main(String[] args) {

        int dPort = Integer.parseInt(args[0]);   //port the dstore listens on
        int cPort = Integer.parseInt(args[1]);  // the controller's port
        int timeout = Integer.parseInt(args[2]);
        String fileFolder = args[3];
        setFileFolder(fileFolder);


        try {
            ServerSocket serverSocket = new ServerSocket(dPort);    // listens for connections
            System.out.println("Dstore is listening on " + dPort);
                try {
                    //handling controller messages and receiving
                    Socket controller = new Socket(InetAddress.getLoopbackAddress(), cPort);   // talks to controller
                    // for sending messages to the controller
                    PrintWriter controllerOut = new PrintWriter(controller.getOutputStream(), true);
                    controllerOut.println(Protocol.JOIN_TOKEN + " " + dPort);


                    for (;;) {
                        System.out.println("Connected to controller");
                        // handling and serving client messages
                        System.out.println("Waiting to connect to client");
                        Socket client = serverSocket.accept();   // how it sends messages to the Dstore
                        System.out.println("Client connected!");


                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    BufferedReader clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                    OutputStream clientOutputStream = client.getOutputStream();
                                    InputStream clientInputStream = client.getInputStream();
                                    PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
                                    String line;

                                    while ((line = clientIn.readLine()) != null) {
                                        String[] messageContent = line.split(" ");
                                        if (messageContent[0].equals(Protocol.LIST_TOKEN)) {
                                            list(controllerOut);
                                        } else if (messageContent[0].equals(Protocol.STORE_TOKEN)) {
                                            clientOut.println(Protocol.ACK_TOKEN);
                                            client.setSoTimeout(timeout);
                                            receiveAndStoreFileContent(clientInputStream, messageContent[1], Integer.valueOf(messageContent[2]));
                                            //sending ACK to the controller
                                            controllerOut.println(Protocol.STORE_ACK_TOKEN + " " + messageContent[1]);
                                            System.out.println(Protocol.STORE_ACK_TOKEN + " has been sent to Controller");
                                        } else if (messageContent[0].equals(Protocol.LOAD_DATA_TOKEN)) {
                                            client.setSoTimeout(timeout);
                                            loadFileContent(messageContent[1], clientOutputStream);

                                        } else if (messageContent[0].equals(Protocol.REMOVE_TOKEN)) {
                                            remove(controllerOut, messageContent[1]);
                                            //sending ACK to the controller
                                            controllerOut.println(Protocol.REMOVE_ACK_TOKEN + " " + messageContent[1]);
                                            System.out.println(Protocol.REMOVE_ACK_TOKEN + " has been sent to Controller");
                                        } else {
                                            System.out.println("INVALID MESSAGE: " + line);
                                        }

                                    }
                                    client.close();

                                } catch (Exception e) {
                                    System.out.println("error: " + e);
                                }
                            }
                        }).start();
                    }
                }catch(Exception e){
                    System.out.println("error: " + e);
                }
            } catch(Exception e){
                System.out.println("error: " + e);
            }
    }
}
