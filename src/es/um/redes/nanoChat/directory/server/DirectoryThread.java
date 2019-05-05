package es.um.redes.nanoChat.directory.server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class DirectoryThread extends Thread {
	//Socket de comunicación UDP
	protected DatagramSocket socket = null;
	//Estructura para guardar las asociaciones ID_PROTOCOLO -> Dirección del servidor
	protected HashMap<Integer,InetSocketAddress> servers;
	//Probabilidad de descarte del mensaje
	protected double messageDiscardProbability;
	//Tamaño máximo del paquete UDP
	private static final int PACKET_MAX_SIZE  = 128;
	private static final byte REGISTER_OPCODE = 16;
	private static final byte QUERY_OPCODE	  = 8;
	private static final byte INFO_OPCODE	  = 4;
	private static final byte NOTFOUND_OPCODE = 2;
	private static final byte ACK_OPCODE	  = 0;

	public DirectoryThread(String name, int directoryPort, double corruptionProbability) throws SocketException {
		super(name);
		socket = new DatagramSocket(new InetSocketAddress(directoryPort));
		servers = new HashMap<Integer,InetSocketAddress>();
		messageDiscardProbability = corruptionProbability;
	}

	public void run() {
		byte[] buf = new byte[PACKET_MAX_SIZE];

		System.out.println("Directory starting...");
		boolean running = true;
		try {
			while (running) {
				DatagramPacket pckt = new DatagramPacket(buf, buf.length);
				// 1) Recibir la solicitud por el socket
				socket.receive(pckt);
				// 2) Extraer quién es el cliente (su dirección)
				InetSocketAddress clientAddress = (InetSocketAddress) pckt.getSocketAddress();
				// 3) Vemos si el mensaje debe ser descartado por la probabilidad de descarte
				if (Math.random() < messageDiscardProbability) {
					System.err.println("Directory DISCARDED corrupt request from " + clientAddress.getHostString());
					continue;
				}
				// 4) Analizar y procesar la solicitud (llamada a processRequestFromCLient)
				processRequestFromClient(pckt.getData(), clientAddress);
			}
		} catch (IOException e) {
			System.err.println(e.toString());
			e.printStackTrace();
		} finally {
			socket.close();			
		}
	}

	//Método para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		// 1) Extraemos el tipo de mensaje recibido
		ByteBuffer ret = ByteBuffer.wrap(data);
		byte opcode = ret.get();
		if (opcode == REGISTER_OPCODE) {
			// 2) Procesar el caso de que sea un registro y enviar mediante sendOK
			int port = ret.getInt();
			int protocolID = ret.get();
			servers.put(protocolID, new InetSocketAddress(clientAddr.getAddress(), port));
			sendOK(clientAddr);
		} else if (opcode == QUERY_OPCODE) {
			// 3) Procesar el caso de que sea una consulta
			int protocolID = ret.get();
			InetSocketAddress serverAddress = servers.get(protocolID);
			// 3.1) Devolver una dirección si existe un servidor (sendServerInfo)
			if (serverAddress != null) {
				System.err.println("server found, returning address");
				this.sendServerInfo(serverAddress, clientAddr);
			}
			// 3.2) Devolver una notificación si no existe un servidor (sendEmpty)
			else
				this.sendEmpty(clientAddr);
		}
	}

	//Método para enviar una respuesta vacía (no hay servidor)
	private void sendEmpty(InetSocketAddress clientAddr) throws IOException {
		ByteBuffer ret = ByteBuffer.allocate(1); // Hardcoded
		ret.put(NOTFOUND_OPCODE);
		DatagramPacket pckt = new DatagramPacket(ret.array(), ret.array().length, clientAddr);
		socket.send(pckt);
	}

	//Método para enviar la dirección del servidor al cliente
	private void sendServerInfo(InetSocketAddress serverAddress, InetSocketAddress clientAddr) throws IOException {
		ByteBuffer ret = ByteBuffer.allocate(9); // Hardcoded
		ret.put(INFO_OPCODE);
		ret.put(serverAddress.getAddress().getAddress()); // IPv4
		ret.putInt(serverAddress.getPort());
		DatagramPacket pckt = new DatagramPacket(ret.array(), ret.array().length, clientAddr);
		socket.send(pckt);
	}

	//Método para enviar la confirmación del registro
	private void sendOK(InetSocketAddress clientAddr) throws IOException {
		ByteBuffer ret = ByteBuffer.allocate(1); // HardCoded 
		ret.put(ACK_OPCODE);
		DatagramPacket pckt = new DatagramPacket(ret.array(), ret.array().length, clientAddr);
		socket.send(pckt);
	}
}
