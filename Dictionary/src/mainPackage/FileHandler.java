package mainPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHandler implements Runnable{

	private String word;
	private int wordsFound;
	private File file;
	private ReadWriteLock lockPair;
	private SearchHandler searchHandler;
	
	public FileHandler(String word, File file, ReadWriteLock lockPair, SearchHandler searchHandler) {
		this.word = word;
		this.wordsFound = 0;
		this.file = file;
		this.lockPair = lockPair; //this is the lock pair (read and write) asscoiated with the passed file
		this.searchHandler = searchHandler;
	}
	
	@Override
	public void run() {
		
		System.out.println("\t\tRequest " + searchHandler.getRequestNumber() + ", " + 
				Thread.currentThread().getName() + ": STARTED");
		
		//acquire the file's read lock. If it was not available, block till it acquires it.
		Lock readLock = lockPair.readLock(); //gets the readlock object (not the lock itself)
		readLock.lock(); //may block here
		
		
		try (Scanner fileScanner = new Scanner(file)){
			
			while (fileScanner.hasNextLine()) {
		        String line = fileScanner.nextLine();
		        //Assuming we will only match whole words (e.g. "dog" will not be matched in "dogma")
		        //Searching is case sensitive
		        Matcher wordMatcher = Pattern.compile("\\b" + word + "\\b").matcher(line);
		        while(wordMatcher.find())
		        	wordsFound++;
		    }
			searchHandler.reportFileResults(file, wordsFound); //tells the searchHandler that its job is done
			System.out.println("\t\tRequest " + searchHandler.getRequestNumber() + ", " + 
					Thread.currentThread().getName() + ": DONE");
			
		} catch (FileNotFoundException e) {
			
			System.err.println("\t\tRequest " + searchHandler.getRequestNumber() + ", " + 
								Thread.currentThread().getName() + ": cannot open the file " + 
								file.getAbsolutePath());
			e.printStackTrace();
			
		} finally {
			
			readLock.unlock(); //unlock the read lock
			
		}
		
		
	}
}
