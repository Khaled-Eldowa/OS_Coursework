package mainPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {
	
		public static void main(String[] args) {
			
			PrintWriter writer = null;
			BufferedReader reader = null;
			Socket thisClientSocket = null;
			Scanner in = null;
		
			try {
				thisClientSocket = new Socket("127.0.0.1", 1234);
				InputStream clientInputStream = thisClientSocket.getInputStream();
				OutputStream clientOutputStream = thisClientSocket.getOutputStream();
				writer = new PrintWriter(clientOutputStream, true);
				reader = new BufferedReader(new InputStreamReader(clientInputStream));
				in = new Scanner(System.in);
				
				System.out.println("Reached the server");
				System.out.println("Waiting for acceptance or rejection... (request may be queued)");
				String response = "";
				response = reader.readLine();
				System.out.println("Server: " + response);
				
				if(!response.equals("REQUEST BLOCKED")) {
					
					String searchKey;
					
					System.out.print("Enter Search Key: ");
					searchKey = in.nextLine();
					
					writer.println(searchKey);
					System.out.println(reader.readLine());
					
					String line = "";
					while( (line = reader.readLine()) != null) {
					       System.out.println(line);
					}
					
				}
				
				
			} catch (ConnectException e) {
				System.out.println("Could not connect to the server!!");
				System.exit(1);
			} catch (IOException e) {
				System.out.println("IO Exception");
				System.exit(2);
			} finally {
				
				try {
					thisClientSocket.close();
					reader.close();
					writer.close();
					in.close();
				} catch (IOException e) {
					System.out.println("Could not close resources");
					e.printStackTrace();
				}
			}
		}

	

	
}
