package mainPackage;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RequestRejectionHandler implements RejectedExecutionHandler{

	//Called when a request is rejected
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		
		//this condition is just for safety
		if(r instanceof SearchHandler) {
			SearchHandler searchHandler = (SearchHandler) r;
			System.err.println("\tRequest " + searchHandler.getRequestNumber() + ": Request BLOCKED");
			searchHandler.reject(); //tells the client that they are rejected and closes the socket
		}
		
	}

	
}
