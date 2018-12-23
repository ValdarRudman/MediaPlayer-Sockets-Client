package socketConnection;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import gui.MainController;

/**
 * Socket connection to connect to server
 * @author valdar
 *
 */
public class SocketConnection {
	
	//IP address to connect to server
	static final String HOSTNAME = "localhost";
	//port number for socket
	static final int PORT_NUMBER = 5555;
	
	//client socket
	static Socket clientSocket = null;
	
	//input, output streams
	static ObjectOutputStream out;
	static ObjectInputStream in;
	
	//gui controller
	MainController mainController;
	
	//connection status
	boolean closed = true;
	
	/*
	 * Setup connection, pass through gui controller in to update gui
	 */
	public SocketConnection(MainController mainController) {
		
		this.mainController = mainController;
		
		try {
		
			//Creating socket and opening connections
			clientSocket = new Socket(HOSTNAME, PORT_NUMBER);

			out = new ObjectOutputStream(clientSocket.getOutputStream());

			in = new ObjectInputStream(clientSocket.getInputStream());

			closed = false;
			
			/*
			 * thread to check if connection is still active
			 */
		/*	new Thread(new Runnable() {
				
				@Override
				public void run() {

					//if closed close connections
					if(closed) {
						
						try {
							
							in.close();
							out.close();
							clientSocket.close();
							
							//end thread
							return;
							
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						
					}
					
				}
			}).start();*/

			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					monitorServer();
					
				}
			}).start();				
			
		}	
		catch(UnknownHostException e) {
		
			System.exit(1);
			
		}
		catch(IOException e) {
			
			System.out.println("Could not connect to Server");
			System.exit(1);
			
		}
		
	}
	
	/*
	 * get MainController
	 */
	public MainController getMainController() {
		
		return this.mainController;
		
	}
	
	/**
	 * Listen to server for any information
	 */
	private void monitorServer() {

		while(true) {

			Object[] serverSent;
		
			try {
			
				serverSent = (Object[]) in.readObject();
			
				// if bye received from server, set closed to true. Close connections on this end
				if(serverSent[0].equals("bye")) {
					
					in.close();
					out.close();
					clientSocket.close();
					
					break;
					
				}	
				//get servers list of files
				else if(serverSent[0].equals("list")) {
						
					File[] files = (File[])serverSent[1];
					this.mainController.setServerList(files);
						
				}
				//download song from sever
				else if(serverSent[0].equals("media")) {
						
					this.mainController.downloadMedia((String)serverSent[1], (byte[])serverSent[2]);
						
				}
						
					
			} catch (IOException | ClassNotFoundException e1) {
				
				e1.printStackTrace();
					
			}	
		}
			
	}
	
	/**
	 * upload song to sever
	 * @param name
	 * @param bytes
	 */
	public void upload(String name, byte[] fileBytes) {
		
		
		Object[] send = {"upload", name, fileBytes};
		
		try {
			
			out.writeObject(send);
		
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Send request to download song to server
	 * @param file
	 */
	public void download(String name) {
		
		Object[] send = {"download", name};

		try {
			
			out.writeObject(send);
		
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Inform server you are disconnecting 
	 */
	public void leave() {
		
		try {
			
			Object[] send = {"leave"};
			
			out.writeObject(send);
			System.out.println("ddsddssdsd");
		} catch (IOException  e) {
			
			e.printStackTrace();
		}
		
	}

}
