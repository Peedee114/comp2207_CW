

import java.io.*;
import java.util.Random;

/**
 *  This is just an example of how to use the provided client library. You are expected to customise this class and/or 
 *  develop other client classes to test your Controller and Dstores thoroughly. You are not expected to include client 
 *  code in your submission.
 */
public class ClientMain {
	
	public static void main(String[] args) throws Exception{

		// change to integer.getArgs[] for testing on linux
		final int cPort = 12345;
		int timeout = 5000;

		
		// this client expects a 'downloads' folder in the current directory; all files loaded from the store will be stored in this folder
		File downloadFolder = new File("downloads");
		if (!downloadFolder.exists())
		if (!downloadFolder.mkdir()) throw new RuntimeException("Cannot create download folder (folder absolute path: " + downloadFolder.getAbsolutePath() + ")");
		
		// this client expects a 'to_store' folder in the current directory; all files to be stored in the store will be collected from this folder
		File uploadFolder = new File("to_store");
		if (!uploadFolder.exists())
		if (!uploadFolder.mkdir())throw new RuntimeException("to_store folder does not exist");
		
		// launch a single client
		testClient(cPort, timeout, downloadFolder, uploadFolder);


		// launch a number of concurrent clients, each doing the same operations
//		for (int i = 0; i < 10; i++) {
//			new Thread() {
//				public void run() {
//					test2Client(cPort, timeout, downloadFolder, uploadFolder);
//				}
//			}.start();
//		}


	}
	
	public static void test2Client(int cport, int timeout, File downloadFolder, File uploadFolder) {
		Client client = null;
		
		try {
			client = new Client(cport, timeout, Logger.LoggingType.ON_FILE_AND_TERMINAL);
			client.connect();
			Random random = new Random(System.currentTimeMillis() * System.nanoTime());
			
			File fileList[] = uploadFolder.listFiles();
			for (int i=0; i<fileList.length/2; i++) {
				File fileToStore = fileList[random.nextInt(fileList.length)];
				try {					
					client.store(fileToStore);
				} catch (Exception e) {
					System.out.println("Error storing file " + fileToStore);
					e.printStackTrace();
				}
			}
			
			String list[] = null;
			try { list = list(client); } catch(IOException e) { e.printStackTrace(); }
			
			for (int i = 0; i < list.length/4; i++) {
				String fileToRemove = list[random.nextInt(list.length)];
				try {
					client.remove(fileToRemove);
				} catch (Exception e) {
					System.out.println("Error remove file " + fileToRemove);
					e.printStackTrace();
				}
			}
			
			try { list = list(client); } catch(IOException e) { e.printStackTrace(); }
			
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if (client != null)
				try { client.disconnect(); } catch(Exception e) { e.printStackTrace(); }
		}
	}
	
	public static void testClient(int cport, int timeout, File downloadFolder, File uploadFolder) {
		Client client = null;
		
		try {
			
			client = new Client(cport, timeout, Logger.LoggingType.ON_FILE_AND_TERMINAL);
		
			try { client.connect(); } catch(IOException e) { e.printStackTrace(); return; }
			
			//try { list(client); } catch(IOException e) { e.printStackTrace(); }

			try {client.list();} catch (IOException e) {e.printStackTrace();}

			try {client.store(new File("SomeFile1"));} catch (IOException e) {e.printStackTrace();}
      		try {client.store(new File("File2"));} catch (IOException e) {e.printStackTrace();}
			try {client.store(new File("File3"));} catch (IOException e) {e.printStackTrace();}

			try {client.load("File2",new File("src\\downloads"));} catch (IOException e) {e.printStackTrace();}

			try {client.remove("File3");} catch (IOException e) {e.printStackTrace();}








			// store first file in the to_store folder twice, then store second file in the to_store folder once
			File fileList[] = uploadFolder.listFiles();
			if (fileList.length > 0) {
				try { client.store(fileList[0]); } catch(IOException e) { e.printStackTrace(); }				
				try { client.store(fileList[0]); } catch(IOException e) { e.printStackTrace(); }
				System.out.println("File 1 stored");
			}
			if (fileList.length > 1) {
				try { client.store(fileList[1]); } catch(IOException e) { e.printStackTrace(); }
				System.out.println("File 2 stored!");
			}



			String list[] = null;
			try { list = list(client); } catch(IOException e) { e.printStackTrace(); }

			if (list != null)
				for (String filename : list)
					try { client.load(filename, downloadFolder); } catch(IOException e) { e.printStackTrace(); }

			if (list != null)
				for (String filename : list)
					try { client.remove(filename); } catch(IOException e) { e.printStackTrace(); }
			if (list != null && list.length > 0)
				try { client.remove(list[0]); } catch(IOException e) { e.printStackTrace(); }

			try { list(client); } catch(IOException e) { e.printStackTrace(); }
			
		} finally {
			if (client != null)
				try { client.disconnect(); } catch(Exception e) { e.printStackTrace(); }
		}
	}

	public static String[] list(Client client) throws IOException, NotEnoughDstoresException {
		System.out.println("Retrieving list of files...");
		String list[] = client.list();
		
		System.out.println("Ok, " + list.length + " files:");
		int i = 0; 
		for (String filename : list)
			System.out.println("[" + i++ + "] " + filename);
		
		return list;
	}
	
}
