
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Kindly note that some code implemented was gotten from the worksheet provided as well as some github sources such as : emiliyanhristov
 */

public class Index {

    /**
     * The files and their status.
     */
    public HashMap<String, String> fileStatus;

    public HashMap<String, Integer> fileNSize;

    /**
     * List of all the files in the system
     */
    public ArrayList<String> files;


    /**
     * stores a file and the dstores it's stored in
     */
  public HashMap<String, ArrayList<Integer>> fileandDstore;


    public Index (HashMap<String, String> fileStatus, ArrayList <String> files, HashMap<String,ArrayList<Integer>> fileandDstore, HashMap<String, Integer> fileAndSize) {
        this.fileNSize = fileAndSize;
        this.fileStatus = fileStatus;
        this.files = files;
        this.fileandDstore = fileandDstore;

    }

    /**
     * Gets files and their current status
     * @return current status of an already existing file
     */
    public HashMap getFileStatus() {
        return fileStatus;
    }


//    public void fileInfo (HashMap<String, String> status, ArrayList <String> file, int filesize, ArrayList<Integer> dStores, String filename) {
//        if (!fileStatus.containsKey(filename)) {
//            this.fileStatus = status;
//            this.files = file;
//            this.fileSize = filesize;
//            this.dstores = dStores;
//        }
//    }

    /**
     * Keeps track of the allocation of files to DStores, as well as the size of each stored file
//     * @param filename
//     * @param dStores
//     * @param fileSize
     */
//    public void addFileInfo (String filename, ArrayList<Integer> dStores, int fileSize) {
//        // using arraylist because a file should be replicated over many dStores hence its association with an arraylist
//        if (fileStatus.containsKey(filename)) {
//            this.fileandDstore.put(filename, dStores);
//            this.fileNSize.put(filename, fileSize);
//        }
//    }

    public void updateFileStatus(HashMap<String, String> fileAndStatus ) {
        this.fileStatus = fileAndStatus;
    }

    public void setFileNSize (HashMap<String, Integer> fileAndSize) {
        this.fileNSize = fileAndSize;
    }

    public HashMap<String, Integer> getFileNSize() {
        return fileNSize;
    }

    public void setFileandDstore (HashMap<String, ArrayList<Integer>> file_and_dstore) {
        this.fileandDstore = fileandDstore;
    }

    public HashMap<String, ArrayList<Integer>> getFileandDstore () {
        return fileandDstore;
    }

    public void updateFiles(ArrayList<String> newFiles ) {
        this.files = newFiles;
    }

    /**
     * Gets files in the system
     * @return files in the system
     */
    public ArrayList getFile() {
        return files;
    }
    /**
     * Removes all the mappings of the files to their status
     */
    public void clear() {
        this.fileStatus.clear();  // will be empty
        this.files.clear();   // will be empty
        this.fileandDstore.clear();
        this.fileNSize.clear();
    }

}

