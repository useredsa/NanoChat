package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import es.um.redes.nanoChat.messageFV.NCMessageOp;
import es.um.redes.nanoChat.messageFV.NCTextMessage;

public class NCBasicRoom extends NCRoomManager {
	private final HashMap<String, Socket> users;
	private long timeLastMessage = 0; //TODO variable que sacaremos de la lista de mensajes
	
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
	public void broadcastMessage(String u, String message) throws IOException {//TODO revisar jm
		for(String e : users.keySet()) {
			DataOutputStream dos = new DataOutputStream(users.get(e).getOutputStream());
			dos.writeUTF(new NCTextMessage(NCMessageOp.NEW_MESSAGE,u,message).toEncodedString());
		}
		Date last = new Date();
		timeLastMessage = last.getTime();
	}
	
	@Override
	public void sendMessage(String u, String v, String message) throws IOException{//TODO revisar jm
		if(users.containsKey(v)) {
			DataOutputStream dos = new DataOutputStream(users.get(v).getOutputStream());
			dos.writeUTF(new NCTextMessage(NCMessageOp.NEW_DM, u, message).toEncodedString());
		}
	}
	
	@Override
	public void removeUser(String u) { //TODO revisar jm
		users.remove(u);
	}

	@Override
	public void setRoomName(String roomName) {// TODO revisar jm
		this.roomName = roomName;
	}

	@Override
	public NCRoomDescription getDescription() {
		ArrayList<String> usersArray = new ArrayList<String> (users.keySet()); //TODO considerar hacer array
		return new NCRoomDescription(roomName, usersArray, timeLastMessage);
	}

	@Override
	public int usersInRoom() {
		return users.size();
	}

}
