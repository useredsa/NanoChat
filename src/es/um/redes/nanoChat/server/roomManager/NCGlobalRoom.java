package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.um.redes.nanoChat.messageFV.NCMessageType;
import es.um.redes.nanoChat.messageFV.messages.NCControlMessage;
import es.um.redes.nanoChat.messageFV.messages.NCTextMessage;
import es.um.redes.nanoChat.server.NCServerManager;
import es.um.redes.nanoChat.server.NCServerThread;

public class NCGlobalRoom implements NCRoomManager {
	@SuppressWarnings("unused")
	private final NCServerManager serverManager;
	private String roomName;
	private final HashMap<String, DataOutputStream> members;
	private long timeLastMessage = 0;
	
	public NCGlobalRoom(NCServerManager serverManager, String name) {
		this.serverManager = serverManager;
		roomName = name;
		members = new HashMap<String, DataOutputStream>();
	}
	
	@Override
	public synchronized boolean enter(String user, NCServerThread userThread) throws IOException{
		if (members.containsKey(user))
			return false;
		members.put(user, new DataOutputStream(userThread.getSocket().getOutputStream()));
		return true;
	}
	
	@Override
	public synchronized void exit(String user) {
		DataOutputStream dos = members.remove(user);
		if (dos != null)
			dos.close();
	}

	@Override 
	public synchronized void broadcastMessage(String user, String message) {
		for(Map.Entry<String, DataOutputStream> entry : members.entrySet()) {
			if (!entry.getKey().equals(user)) {
				try {
					entry.getValue().writeUTF(new NCTextMessage(user, message).encode());
				} catch (IOException e) {
					System.out.println("Could not write to user " + entry.getKey() + " from room " + roomName);
				}
			}
		}
		timeLastMessage = new Date().getTime();
	}

	@Override
	public synchronized NCControlMessage rename(String user, String newName) {
		// This functionality is not supported
		return new NCControlMessage(NCMessageType.IMPOSSIBLE);
	}
	
	@Override
	public synchronized NCControlMessage promote(String user, String promoted) {
		// This functionality is not supported
		return new NCControlMessage(NCMessageType.IMPOSSIBLE);
	}
	
	@Override
	public synchronized NCControlMessage kick(String user, String kicked) {
		// This functionality is not supported
		return new NCControlMessage(NCMessageType.IMPOSSIBLE);
	}

	@Override
	public synchronized NCRoomDescription getDescription() {
		return new NCRoomDescription(roomName, members.keySet(), timeLastMessage);
	}

	@Override
	public synchronized int usersInRoom() {
		return members.size();
	}

	@Override
	public synchronized String getRoomName() {
		return roomName;
	}
}