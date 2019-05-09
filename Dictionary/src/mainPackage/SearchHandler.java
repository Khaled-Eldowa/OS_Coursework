package mainPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchHandler implements Runnable{

	private Socket client;
	private int maxThreads; //max number of threads (for this request) running at the same time
	private int requestNumber; //just for reference
	private String word;
	private ArrayList<File> files;
	private int totalWordsFound;
	private Map<File, Integer> wordsFoundInEachFile;  //hash table that maps each file to the number of matches found in it
	
	public SearchHandler(int maxThreads, Socket client, ArrayList<File> files, int requestNumber) {
		this.maxThreads = maxThreads;
		this.client = client;
		this.files = files;
		this.requestNumber = requestNumber;
		this.totalWordsFound = 0;
		this.wordsFoundInEachFile = new HashMap<File, Integer>();
	}
	
	//to be called by the file handlers
	public void reportFileResults(File file, int wordsFound) {
		wordsFoundInEachFile.put(file, wordsFound); //update the hashmap
		totalWordsFound += wordsFound; //update the total count
	}
	
	@Override
	public void run() {
		
		try {
			
			System.out.println("\tRequest " + requestNumber + ": Request STARTED");
			
			
			OutputStream clientOutputStream = client.getOutputStream();
			PrintWriter writer = new PrintWriter(clientOutputStream, true);
	        writer.println("REQUEST ACCEPTED"); //sent to the client
			InputStream clientInputStream = client.getInputStream();   
	        BufferedReader reader = new BufferedReader(new InputStreamReader(clientInputStream));
	        
	        System.out.println("\tRequest " + requestNumber + ": Waiting for client's input...");
	        word = reader.readLine();
	        System.out.println("\tRequest " + requestNumber + ": Search key is \"" + word + "\"");
	        
	        //make a thread pool to manage the threads for this request
	        //if the pool capacity has been reached, a waiting thread will wait and be automatically executed once an executing thread finishes
	        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
	        for (File file : files) {
				executor.execute(new FileHandler(word, file, this));
			}
	        
	        //shutdown the executor once all threads are done executing
	        executor.shutdown();
	        while (!executor.isTerminated()) { //wait here since the previous call does not block
	        }
	        
	        //print the results on the screen and send them to the client
	        StringBuilder sBuilder = new StringBuilder();
	        writer.println("Found the word \"" + word + "\" " + totalWordsFound + " times:");
	        sBuilder.append("\tFound the word \"" + word + "\" " + totalWordsFound + " times:\n");
	        for (File file : wordsFoundInEachFile.keySet()) {
	        	writer.println(wordsFoundInEachFile.get(file) + " times in " + file.getAbsolutePath());
	        	sBuilder.append("\t" + wordsFoundInEachFile.get(file) + " times in " + file.getAbsolutePath() + "\n");
			}
	        
	        
	        System.out.println("\tRequest " + requestNumber + ": DONE. Sent this report to the user:");
	        System.out.print(sBuilder.toString());
			
			
	        
		} catch (Exception e) {
			
			System.err.println("\tRequest " + requestNumber + ": IO exception while handling client's socket.");
			e.printStackTrace();
			
		} finally {
			
			try {
				if(client != null)
					client.close();
				System.out.println("\tRequest " + requestNumber + ": closed the client's socket.");
			} catch (IOException e) {
				System.err.println("\tRequest " + requestNumber + ": Could not close client socket.");
				e.printStackTrace();
			}
			
		}
	}
	
	//Only called by the RequestRejectionHandler which is called when a process is rejected
	public void reject() {
		
		OutputStream clientOutputStream;
		try {
			clientOutputStream = client.getOutputStream();
			PrintWriter writer = new PrintWriter(clientOutputStream, true);
			writer.println("REQUEST BLOCKED");
		} catch (IOException e) {
			System.err.println("\tRequest " + requestNumber + ": IO exception while rejecting client's socket.");
			e.printStackTrace();
		}
		
		
		try {
			client.close();
		} catch (IOException e) {
			System.err.println("\tRequest " + requestNumber + ": Could not close client socket.");
			e.printStackTrace();
		}
        
		
	}

	public int getRequestNumber() {
		return requestNumber;
	}
	
		
	
}
