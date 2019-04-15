package es.um.redes.nanoChat.directory.connector;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Cliente con metodos de consulta y actualizacon especificos del directorio
 */
public class DirectoryConnector {
	//Tamano maximo del paquete UDP (los mensajes intercambiados son muy cortos)
	private static final int PACKET_MAX_SIZE = 128;
	//Puerto en el que atienden los servidores de directorio
	private static final int DEFAULT_PORT = 6868;
	//Valor del TIMEOUT
	private static final int TIMEOUT = 1000;
	private static final int MAX_TRIES_AFTER_TIMEOUT = 7;
	private static final byte REGISTER_OPCODE = 16;
	private static final byte QUERY_OPCODE	  = 8;
	private static final byte INFO_OPCODE	  = 4;
	private static final byte NOTFOUND_OPCODE = 2;
	private static final byte ACK_OPCODE	  = 0;
	
	private DatagramSocket socket; // socket UDP
	private InetSocketAddress directoryAddress; // direccion del servidor de directorio

	public DirectoryConnector(String agentAddress) throws IOException {
		//A partir de la direccion y del puerto genera la direccion de conexion para el Socket
		directoryAddress = new InetSocketAddress(InetAddress.getByName(agentAddress), DEFAULT_PORT);		
		socket = new DatagramSocket();	//Crea el socket UDP
	}

	/**
	 * Envia una solicitud para obtener el servidor de chat asociado a un determinado protocolo
	 * 
	 */
	public InetSocketAddress getServerForProtocol(int protocol) throws IOException {
		//Genera el mensaje de consulta llamando a buildQuery()
		byte[] message = buildQuery(protocol);
		//no sabemos tamano
		byte[] answer = new byte[PACKET_MAX_SIZE]; //TODO change
		//Construye el datagrama con la consulta
		DatagramPacket sentPckt = new DatagramPacket(message, message.length, directoryAddress);
		DatagramPacket ansPckt = new DatagramPacket(answer, answer.length);
		socket.setSoTimeout(TIMEOUT);		//Establece el temporizador para el caso en que no haya respuesta
		int failures = 0;
		do {
			socket.send(sentPckt);					//Envia datagrama por el socket	
						
			try {									//Recibe la respuesta
				socket.receive(ansPckt);
				break;
			} catch (java.net.SocketTimeoutException e) {
				System.err.println("Got Timeout");
				failures++;
			}
		} while (failures < MAX_TRIES_AFTER_TIMEOUT);
		if (failures == MAX_TRIES_AFTER_TIMEOUT)
			return null;
		try {
			//Procesamos la respuesta para devolver la direccion que hay en ella
			return getAddressFromResponse(ansPckt);
		} catch (UnknownHostException e) {
			System.err.println("Host desconocido");
			return null;
		}		
	}


	//Metodo para generar el mensaje de consulta (para obtener el servidor asociado a un protocolo)
	private byte[] buildQuery(int protocol) {
		ByteBuffer consulta = ByteBuffer.allocate(2);	//Devolvemos el mensaje codificado en binario segun el formato acordado
		consulta.put(QUERY_OPCODE);
		consulta.put((byte) protocol);
		byte[] ret = consulta.array();
		return ret;
	}

	//Metodo para obtener la direccion de internet a partir del mensaje UDP de respuesta
	private InetSocketAddress getAddressFromResponse(DatagramPacket packet) throws UnknownHostException {
		//Analiza si la respuesta no contiene direccion (devolver null)
		ByteBuffer ret = ByteBuffer.wrap(packet.getData());
		// Si la respuesta no esta vacia, devolver la direccion (extraerla del mensaje)
		if(ret.get() == INFO_OPCODE) {			
			byte[] ip = new byte[4]; 
			ret.get(ip);
			int port = ret.getInt();
			return new InetSocketAddress(InetAddress.getByAddress(ip), port);
		} else return null;
	}
	
	/**
	 * Envia una solicitud para registrar el servidor de chat asociado a un determinado protocolo
	 * 
	 */
	public boolean registerServerForProtocol(int protocol, int port) throws IOException {
		//Construir solicitud de registro (buildRegistration)
		byte[] registration = this.buildRegistration((byte) protocol, port);
		
		//Enviar solicitud
		DatagramPacket sentPckt = new DatagramPacket(registration, registration.length, directoryAddress);
		
		//Recibe respuesta
		byte[] answer = new byte[PACKET_MAX_SIZE];
		DatagramPacket answPckt = new DatagramPacket(answer, answer.length);
		socket.setSoTimeout(TIMEOUT);
		int failures = 0;
		do {
			socket.send(sentPckt);
			try {									
				socket.receive(answPckt);
				break;
			} catch (java.net.SocketTimeoutException e) {
				System.err.println("Got Timeout");
				failures++;
			}
		} while(failures<MAX_TRIES_AFTER_TIMEOUT);
		
		if (failures == MAX_TRIES_AFTER_TIMEOUT)
			return false;
		
		//Procesamos la respuesta para ver si se ha podido registrar correctamente
		byte[] data = answPckt.getData();
		ByteBuffer answ = ByteBuffer.wrap(data);
		return answ.get() == ACK_OPCODE;

	}


	//Metodo para construir una solicitud de registro de servidor
	//OJO: No hace falta proporcionar la direccion porque se toma la misma desde la que se envio el mensaje
	private byte[] buildRegistration(int protocol, int port) {
		//Devolvemos el mensaje codificado en binario segun el formato acordado
		ByteBuffer reg = ByteBuffer.allocate(6);
		reg.put(REGISTER_OPCODE);
		reg.putInt(port);
		reg.put((byte) protocol);
		return reg.array();
	}

	public void close() {
		socket.close();
	}
}