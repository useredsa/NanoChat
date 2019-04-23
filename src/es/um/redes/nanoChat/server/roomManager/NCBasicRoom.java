package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import es.um.redes.nanoChat.messageFV.messages.NCTextMessage;

public class NCBasicRoom implements NCRoomManager {
	private String roomName;
	private final String creator;
	private final HashSet<String> admins;
	private final HashMap<String, Socket> members;
	private long timeLastMessage = 0; //TODO variable que sacaremos de la lista de mensajes
	
	public NCBasicRoom(String name, String creator, Socket s) {
		roomName = name;
		this.creator = creator;
		admins = new HashSet<String>();
		members = new HashMap<String, Socket>();
		registerUser(creator, s);
	}

	@Override
	public boolean registerUser(String u, Socket s) {
		if (members.containsKey(u)) return false;
		members.put(u, s);
		return true;
	}
	
	@Override
	public void removeUser(String u) {
		members.remove(u);
	}

	@Override 
	public void broadcastMessage(String u, String message) throws IOException {
		for(String e : members.keySet()) {
			if (!e.equals(u)) {
				DataOutputStream dos = new DataOutputStream(members.get(e).getOutputStream()); //TODO preguntar oscar (concurrencia?)
				dos.writeUTF(new NCTextMessage(u,message).encode());
			}
		}
		timeLastMessage = new Date().getTime();
	}
	
	private boolean hasRights(String user)  {
		return creator.equals(user) || admins.contains(user);
	}

	@Override
	public int rename(String user, String newName) {
		if (hasRights(user)) {
			this.roomName = newName;
			return 0; // The operation was successful
		} else return 2; // The user has no rights
	}
	
	@Override
	public int promote(String user, String promoted) {
		if (hasRights(user)) {
			if (!hasRights(promoted)) {
				admins.add(promoted);
				return 0; // The operation was successful
			} else return 3; // The target already has rights
		} else return 2; // The user has no rights
	}
	
	@Override
	public int kick(String user, String kicked) {
		if (hasRights(user) && !hasRights(kicked)) {
			if (!hasRights(kicked)) {
				if (members.containsKey(kicked)) {
					//TODO remover de la sala, notificar a su thread, etc
					return 0; // The operation was successful
				} else return 4; // The target is not in the room
			} else return 3; // The target cannot be kicked
		} else return 2; // The user has no rights
	}

	@Override
	public NCRoomDescription getDescription() {
		return new NCRoomDescription(roomName, members.keySet(), timeLastMessage);
	}

	@Override
	public int usersInRoom() {
		return members.size();
	}

	@Override
	public String getRoomName() {
		return roomName;
	}
}
