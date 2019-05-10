package mainPackage;


import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerApp {

	public static void main(String[] args) {
		
		ServerSocket server = null;
		Socket client;
		int requestNumber = 1; //unique number for each request (to be incremented)
		final int MAX_RUNNING_REQUESTS = 10;
		final int MAX_WAITING_REQUESTS = 10;
		final int MAX_RUNNING_THREADS_PER_REQUEST = 6;
		final String DIRECTORY_PATH = Paths.get("samples").toAbsolutePath().toString();
		File directory = new File(DIRECTORY_PATH);
		
		if(!(directory.isDirectory())) {
			System.out.println("The folder \"" + directory.getAbsolutePath() + "\" does not exist or is not a directory.");
			System.exit(1);
		}
		
		System.out.println("Search will be in directory: \"" + DIRECTORY_PATH + "\"");
		
		ArrayList<File> listOfFiles = getAllFiles(directory); //get all files from the directory (including files in sub-folders)
		
		if(listOfFiles.size() == 0) {
			System.out.println("No files found in the folder!");
			System.exit(2);
		}
		
		//Hash table that matches each file in the server with a pair of locks (read and write)
		Map<File, ReadWriteLock> locksMap = new HashMap<>();
		for (File file : listOfFiles) {
			locksMap.put(file, new ReentrantReadWriteLock());
		}
	
		try {
			server = new ServerSocket(1234);
	    } catch (IOException ie) {
	    	System.err.println("Cannot open server socket.");
	    	System.exit(3);
	    }
		
		//Create a thread pool with the parameters specified in the beginning 
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		ThreadPoolExecutor requestsPool = new ThreadPoolExecutor(MAX_RUNNING_REQUESTS, MAX_RUNNING_REQUESTS,
				10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(MAX_WAITING_REQUESTS), threadFactory,
				new RequestRejectionHandler());
		
		System.out.println("Unindented lines are from the server, singly indented lines are from the searchHandler threads, and doubly indented lines are from fileHandler threads");
		
		while(true) {
			
			System.out.println("Waiting for a client... (next request number: " + requestNumber + ")");
			try {
				
				client = server.accept();
				System.out.println("Request " + requestNumber + " has been received and delegated.");
				//pass the max number of file handler threads, the client socket, the files and their locks, and an identifier to a new searchHandler thread
				requestsPool.execute(new SearchHandler(MAX_RUNNING_THREADS_PER_REQUEST, client, listOfFiles, locksMap, requestNumber));
				//Either will execute or wait in the queue or get rejected
				
			} catch (IOException e) {
				System.err.println("IO Exception while trying to accept request " + requestNumber);
				e.printStackTrace();
			} finally {
				requestNumber++;
			}
			
		}
		
	}
	
	private static ArrayList<File> getAllFiles(File directory) {

		ArrayList<File> files = new ArrayList<File>(Arrays.asList(directory.listFiles()));
		ArrayList<File> folders = new ArrayList<File>();
		for (File file : files) {
			if(file.isDirectory()) {
				folders.add(file);
			}
		}
		files.removeAll(folders);
		for (File folder : folders) {
			files.addAll(getAllFiles(folder));
		}
		
		return files;
	}
	
}
