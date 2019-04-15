package pruebas;

import java.io.IOException;
import java.net.InetSocketAddress;

import es.um.redes.nanoChat.directory.connector.DirectoryConnector;

public class Tester {

	private static final int PORT = 6969;
	private static final int PROTOCOL = 42;
	
	public static void main(String[] args) throws IOException {
		DirectoryConnector dc = new DirectoryConnector("localhost");
		//InetSocketAddress address = dc.getServerForProtocol(0);
		//System.out.println(address.toString());
		
		System.out.println(dc.registerServerForProtocol(PROTOCOL, PORT));
		InetSocketAddress addr = dc.getServerForProtocol(PROTOCOL);
		if (addr == null) {
			System.out.println("Servidor no encontrado");
		} else {
			System.out.println("Obtenido el servidor " + addr.toString());
		}
	}

}
