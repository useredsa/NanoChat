package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

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
	public void broadcastMessage(String u, String message) throws IOException {
		for(String e : users.keySet()) { //TODO probablemente no debería enviarse el mensaje a la persona que lo envió (opino igual)
			if (!e.equals(u)) {
				DataOutputStream dos = new DataOutputStream(users.get(e).getOutputStream()); //TODO preguntar oscar (concurrencia?)
				dos.writeUTF(new NCTextMessage(u,message).encode());
			}
		}
		timeLastMessage = new Date().getTime();
	}
	
	@Override
	public void sendMessage(String u, String v, String message) throws IOException{//TODO revisar jm
		if(users.containsKey(v)) {
			//DataOutputStream dos = new DataOutputStream(users.get(v).getOutputStream());
			//dos.writeUTF(new NCTextMessage(u, message).encode()); //TODO con mensaje DM
		}
	}
	
	@Override
	public void removeUser(String u) { //TODO revisar jm
		users.remove(u);
	}

	@Override
	public void setRoomName(String roomName) { // TODO revisar jm
		this.roomName = roomName;
	}

	@Override
	public NCRoomDescription getDescription() {
		return new NCRoomDescription(roomName, users.keySet(), timeLastMessage);
	}

	@Override
	public int usersInRoom() {
		return users.size();
	}

}
