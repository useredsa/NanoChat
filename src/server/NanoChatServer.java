package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import directory.connector.DirectoryConnector;


public class NanoChatServer implements Runnable {

	public static final int PORT = 6969;
	private static final int MYPROTOCOL = 163;
	
    private InetSocketAddress socketAddress;	// Server's communication address
    private ServerSocket serverSocket = null;	// Server's communication socket
    private NCServerManager manager;			// Server's manager (shared between threads)
    
    private DirectoryConnector directory;		// Class to communicate with the directory
    private static String directoryHostname;	// Directory's address

    public static NanoChatServer create(int port) throws IOException {
    	return new NanoChatServer(new InetSocketAddress(port));
    }
    
    // Private Constructor. Objects are meant to be created using the method create(port)
    private NanoChatServer(InetSocketAddress a) {
    	this.socketAddress = a;
    	manager = new NCServerManager();
    }


    // Main code
	public void run()
	{
   		try {
   			// The server main thread loop.
   			while (true)
   			{
   				// Wait for new connections and obtain their socket
   				Socket s = serverSocket.accept();
   				System.out.println("New client connected from " + s.getInetAddress().toString() + ":" + s.getPort());

   				// Create a Thread to manage the new connection. The thread receives a link to the manager and the user's socket
   				new NCServerThread(manager, s).start();
   			}
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
	}
    
    // Server's start
    public void init() {
    	// Create server's socket and bind the socketAddress
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(socketAddress);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + socketAddress.getPort() + ".");
            System.exit(-1);
        }

        // Register server in the directory (to allow users to find it)
		try {
			directory = new DirectoryConnector(directoryHostname);
			boolean registered = directory.registerServerForProtocol(MYPROTOCOL, PORT);
			if (!registered) {
				System.err.println("Could not register the server in the Directory: " + directoryHostname + ".");
				throw new IOException();
			}
   			directory.close();
		} catch (IOException e) {
            System.err.println("Could not communicate with the Directory: " + directoryHostname + ".");
            System.exit(-1);
		}
        
        // If everything went well, start the server
    	new Thread(this).start();
    	System.out.println("Server running on port " + socketAddress.getPort() + ".");
    }

    public static void main(String[] args) throws IOException
    {
    	// Verification of the input data. Must receive a the directory's address
       	if (args.length != 1) {
    		System.out.println("* Correct use: java NanoChatServer <DirectoryServer>");
    		return;
    	} else 
    		directoryHostname = args[0];
       	NanoChatServer server = NanoChatServer.create(PORT);
     	server.init();
    }
}
