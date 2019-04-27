package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import es.um.redes.nanoChat.client.application.NCController;
import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageType;
import es.um.redes.nanoChat.messageFV.messages.*;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

/**
 * A new thread runs for each connected client
 */
public class NCServerThread extends Thread {
	/** Server's manager (controls server's main resources, like users, rooms...) */
	private NCServerManager serverManager = null;
	/** User name of the client (once registered) */
	String user = null;
	/** Client's socket */
	private final Socket socket;
	/** Input Stream */
	private DataInputStream dis;
	/** Output Stream */
	private DataOutputStream dos;
	/** Current RoomManager (if the user is in any room) */
	NCRoomManager roomManager = null;
	/** Boolean that expresses if the user must exit the current room */
	private boolean mustExitRoom = false;
	/** Boolean that expresses if the user was kicked from the current room.
	 *  Must be atomic because other threads modify it via forceExit(). */
	private AtomicBoolean mustBeKicked = new AtomicBoolean(false);

	// Inicialización de la sala
	public NCServerThread(NCServerManager manager, Socket socket) throws IOException {
		super("NCServerThread");
		this.serverManager = manager;
		this.socket = socket;
	} 

	//Main loop
	public void run() {
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			// We start in the PRE-REGISTER state
			receiveAndVerifyNickname();
			// If we get here, we are in the OUTROOM state. OUTROOM loop:
			while (true) {
				NCMessage message = NCMessage.readFromSocket(dis);
				if (!processOutRoomMessage(message)) {
					// If the user sent a message that is not valid OUTROOM, we answer with INVALID_CODE 
					dos.writeUTF(new NCControlMessage(NCMessageType.INVALID_CODE).encode());
				}
			}
		} catch (InvalidFormat e) {
			System.out.println("* User " + user + " sent an invalid format message. Reason: " + e.getMessage());
			System.out.println("* Disconnecting " + user);
		} catch (Exception e) {
			System.out.println("* User "+ user + " disconnected. " + e.getMessage());
			
		} finally {
			// If an error occurs with the communications the user is removed from all the managers and the connection is closed
			if (user != null) {
				if (roomManager != null) {
					try {
						roomManager.exit(user);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				serverManager.removeUser(user);
			}
			if (!socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e1) {
					System.err.println("* Important: Could not close the socket!");
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Automaton's PRE-REGISTER state procedure.
	 * This procedure will communicate with the client to register it in the server with a
	 * nickname.
	 * The procedure ends when the user has a valid nickname or an error occurred.  
	 * @throws IOException the stream dis has been closed and the contained input stream does
	 * not support reading after close, or another I/O error occurs.
	 * @throws InvalidFormat if a wrong formatted message is received.
	 */
	private void receiveAndVerifyNickname() throws IOException, InvalidFormat {
		// We enter a loop until receiving a nickname which the server's manager accepts
		while (true) {
			NCMessage message = NCMessage.readFromSocket(dis);
			if (message.getType() == NCMessageType.REGISTER) {
				String receivedNick = ((NCRegisterMessage) message).getName();
				if (serverManager.addUser(receivedNick, this)) {
					user = receivedNick;
					dos.writeUTF(new NCControlMessage(NCMessageType.OK).encode());
					break;
				}
				dos.writeUTF(new NCControlMessage(NCMessageType.REPEATED).encode());
			} else {
				dos.writeUTF(new NCControlMessage(NCMessageType.INVALID_CODE).encode());
			}
		}
	}

	/**
	 * Automaton's INROOM state procedure.
	 * This procedure will communicate with the client while it is inside a room.
	 * @throws IOException the stream dis has been closed and the contained input stream does
	 * not support reading after close, or another I/O error occurs.
	 * @throws InvalidFormat if a wrong formatted message is received.
	 */
	private void attendInRoom() throws IOException, InvalidFormat  { //TODO acabar //TODO quitar invalid format con metodo, as above
		mustExitRoom = false;
		mustBeKicked.set(false);
		boolean unProcessedMessage = false;
		NCMessage message = new NCControlMessage(NCMessageType.INVALID_CODE);
		// INROOM loop:
		while (!mustExitRoom && !mustBeKicked.get()) {
			message = NCMessage.readFromSocket(dis);
			// If we were notified that we got kicked while reading a message,
			// we must exit this room. However, the client was also notified that
			// it was kicked. As the thread was blocked waiting for a message, 
			// this message may be an interrupted INROOM message/query, which we can
			// ignore, or an OUTROOM message, which we must process.
			if (mustBeKicked.get()) {
				unProcessedMessage = true;
				break;
			}
			// Otherwise, we proceed as expected.
			if (!processInRoomMessage(message)) {
				dos.writeUTF(new NCControlMessage(NCMessageType.INVALID_CODE).encode());
			}
			
		}
		// Exit protocol
		roomManager.exit(user);
		roomManager = null;
		if (unProcessedMessage) {
			processOutRoomMessage(message);
		}
	}
	
	/**
	 * This method processes an OUTROOM message.
	 * If the message does not corresponds to an OUTROOM message, then the method returns false.
	 * Otherwise, it processes it and returns true. 
	 * 
	 * @param message the message to be processed.
	 * @return If the message was processed.
	 * @throws IOException the stream dis has been closed and the contained input stream does
	 * not support reading after close, or another I/O error occurs.
	 * @throws InvalidFormat if a wrong formatted message is received.
	 */
	private boolean processOutRoomMessage(NCMessage message) throws IOException, InvalidFormat {
		switch (message.getType()) {
		case ROOMS_LIST:
			ArrayList<NCRoomDescription> rooms = serverManager.getRoomList();
			NCRoomListMessage answer = new NCRoomListMessage(rooms);
			dos.writeUTF(answer.encode());
			break;
		case DM:
			String receiver = ((NCDirectMessage)message).getUser();
			if(!receiver.equals(user)) {
				String text = ((NCDirectMessage)message).getText();
				DataOutputStream connection = serverManager.getConnectionWith(receiver);
				if(connection != null) {
					connection.writeUTF(new NCSecretMessage(user,text).encode());
					dos.writeUTF(new NCControlMessage(NCMessageType.OK).encode());
				}else dos.writeUTF(new NCControlMessage(NCMessageType.DENIED).encode());
			}else dos.writeUTF(new NCControlMessage(NCMessageType.IMPOSSIBLE).encode());
			break;
		case CREATE:
			String roomName = ((NCCreateMessage) message).getRoomName();
			roomManager = serverManager.createRoom(roomName, user, this);
			if (roomManager != null) {
				dos.writeUTF(new NCControlMessage(NCMessageType.OK).encode());
				attendInRoom();
			} else dos.writeUTF(new NCControlMessage(NCMessageType.REPEATED).encode());
			break;
		
		case ENTER:
			String room = ((NCEnterMessage) message).getRoomName();
			roomManager = serverManager.getRoomManager(room);
			if (roomManager != null) {
				if (roomManager.enter(user, this)) {
					dos.writeUTF(new NCControlMessage(NCMessageType.OK).encode());
					
					attendInRoom();
				} else dos.writeUTF(new NCControlMessage(NCMessageType.DENIED).encode());
			} else dos.writeUTF(new NCControlMessage(NCMessageType.IMPOSSIBLE).encode());
			break;
		// If do not recognize the message as any that could be received OUTROOM,
		// we inform the calling method.
		default:
			return false;
		}
		return true;
	}
	
	/**
	 * This method processes an INROOM message.
	 * If the message does not corresponds to an INROOM message, then the method returns false.
	 * Otherwise, it processes it and returns true. 
	 * 
	 * @param message the message to be processed.
	 * @return If the message was processed.
	 * @throws IOException the stream dis has been closed and the contained input stream does
	 * not support reading after close, or another I/O error occurs.
	 * @throws InvalidFormat if a wrong formatted message is received.
	 */
	private boolean processInRoomMessage(NCMessage message) throws IOException, InvalidFormat {
		switch (message.getType()) {
		case INFO:
			NCRoomInfoMessage infoMessage = new NCRoomInfoMessage(roomManager.getDescription());
			dos.writeUTF(infoMessage.encode());
			break;
		case SEND:
			roomManager.broadcastMessage(user, ((NCSendMessage) message).getText()); 
			break;			
		case DM: //TODO
			String receiver = ((NCDirectMessage)message).getUser();
			if(!receiver.equals(user)) {
				String text = ((NCDirectMessage)message).getText();
				DataOutputStream connection = serverManager.getConnectionWith(receiver);
				if(connection != null) {
					connection.writeUTF(new NCSecretMessage(user,text).encode());
					dos.writeUTF(new NCControlMessage(NCMessageType.OK).encode());
				}else dos.writeUTF(new NCControlMessage(NCMessageType.DENIED).encode());
			}else dos.writeUTF(new NCControlMessage(NCMessageType.IMPOSSIBLE).encode());
			break;
		case EXIT:
			mustExitRoom = true;
			break;
		case PROMOTE:
			NCControlMessage promoteAnswer = roomManager.promote(user, ((NCPromoteMessage) message).getUser());
			dos.writeUTF(promoteAnswer.encode());
			break;
		case KICK:
			NCControlMessage kickAnswer = roomManager.kick(user, ((NCKickMessage) message).getUser());  
			dos.writeUTF(kickAnswer.encode());
			break;
		case RENAME:
			NCControlMessage renAnswer = roomManager.rename(user, ((NCRenameMessage) message).getNewName());
			dos.writeUTF(renAnswer.encode());
			break;
		// If do not recognize the message as any that could be received INROOM,
		// we inform the calling method.
		default:
			return false;
		}
		return true;
	}
	
	
	/**
	 * Method called by other server's threads when this user is kicked from a room.
	 */
	public void forceExit() {
		mustBeKicked.set(true);
		NCControlMessage kicked = new NCControlMessage(NCMessageType.KICKED);
		try {
			dos.writeUTF(kicked.encode());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Socket getSocket() {
		return socket;
	}
}
