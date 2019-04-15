package es.um.redes.nanoChat.server.roomManager;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NCBasicRoom extends NCRoomManager {
	
	private final HashMap<String, Socket> users;
	private final long lastMessagetime = 0; //TODO variable que sacaremos de la lista de mensajes
	
	public NCBasicRoom(String name) {
		roomName = name;
		users = new HashMap<String, Socket>();
	}
	
	public NCBasicRoom(String name, String creator, Socket s) {
		roomName = name;
		users = new HashMap<String, Socket>();
		registerUser(creator, s);
	}

	@Override
	public boolean registerUser(String u, Socket s) {
		users.put(u, s); //TODO manejar duplicados?
		return true;
	}

	@Override
	public void broadcastMessage(String u, String message) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeUser(String u) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRoomName(String roomName) {
		// TODO Auto-generated method stub

	}

	@Override
	public NCRoomDescription getDescription() {
		ArrayList<String> usersArray = new ArrayList<String> (users.keySet()); //TODO considerar hacer array
		return new NCRoomDescription(roomName, usersArray, lastMessagetime);
	}

	@Override
	public int usersInRoom() {
		// TODO Auto-generated method stub
		return 0;
	}

}
