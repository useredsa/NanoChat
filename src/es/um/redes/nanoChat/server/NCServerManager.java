package es.um.redes.nanoChat.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import es.um.redes.nanoChat.server.roomManager.NCBasicRoom;
import es.um.redes.nanoChat.server.roomManager.NCGlobalRoom;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

/**
 * This class manages the server's main resources, which are:
 * <ul>
 * <li> Users </li>
 * <li> Rooms </li>
 * </ul>
 * The creation or deletion of common resources is done using this class.
 * However, the interaction with the rooms can be done via their NCRoomManager.
 */
public class NCServerManager {
	// Prefer concurrent collections and atomic operations instead of synchronized methods
	// to reduce the server's load.
	/** Names of the global rooms in the server */
	private final static String[] GLOBAL_ROOMS = {"General", "PublicBar"};
	/** Registered names */
	private ConcurrentHashMap<String, Boolean> users; // used as set because concurrent set does not exist
	/** */
	private ConcurrentHashMap<String, NCServerThread> connections;
	/** Available Rooms (and their room managers) */
	private ConcurrentHashMap<String, NCRoomManager> rooms;	
	
	NCServerManager() {
		users = new ConcurrentHashMap<String, Boolean>();
		connections = new ConcurrentHashMap<String, NCServerThread>();
		rooms = new ConcurrentHashMap<String, NCRoomManager>();
		for (String roomName : GLOBAL_ROOMS) {
			rooms.put(roomName, new NCGlobalRoom(this, roomName));
		}
	}
	
	/**
	 * Attempts to register a client in the server. </br>
	 * This method should be called by NCServerThread when trying to
	 * register. If there is another user with the same username in
	 * the server the register will be unsuccessful and the function
	 * will return false.
	 * If successful, the function will return true.
	 * @param username the username 
	 * @param th the NCServerThread attending this client
	 * @return The new NCUser if the user was registered. null otherwise.
	 */
	public boolean addUser(String username, NCServerThread th) {
		// The following call is atomic, don't touch it unless you know what you are doing:
		//TODO yep i dont know what im doing, sorry but not sorry, im doing my f***** job
		if(users.putIfAbsent(username, true) == null) {
				connections.put(username,th);
			return true;
		}
		return false;	
	}
	
	/**
	 * Deletes a user from the server, freeing its username.
	 * This method should be called by NCServerThread when the user disconnects.
	 * @param user the disconnected user.
	 */
	public void removeUser(String user) {
		users.remove(user);
		connections.remove(user);	//TODO revisar jm
	}
	
	//Devuelve la descripción de las salas existentes
	public ArrayList<NCRoomDescription> getRoomList() {
		// Pregunta a cada RoomManager cuál es la descripción actual de su sala
		// Añade la información al ArrayList
		ArrayList<NCRoomDescription> info_rooms = new ArrayList<NCRoomDescription>();
		for(String s : rooms.keySet()) {
			info_rooms.add(rooms.get(s).getDescription());
		}
		return info_rooms;
	}
	
	/**
	 * Process request from a user to create room. </br>
	 * If a room with the same name already existed, the function
	 * returns null. Otherwise, the room is created and the function
	 * returns it's NCRoomManager.
	 * @param room the name of the new room
	 * @param user the creator of the room
	 * @return The new room's NCRoomManager or null if there was a room with
	 * the same name.
	 * @throws IOException
	 */
	public NCRoomManager createRoom(String room, String user, NCServerThread userThread) throws IOException {
		NCRoomManager rm = new NCBasicRoom(this, room, user, userThread);
		// This function is atomic, don't touch it unless you know what you are doing.
		NCRoomManager check = rooms.putIfAbsent(room, rm);
		if (check == null)
			return rm;
		return null;
	}
	
	public NCRoomManager getRoomManager(String room) {
		return rooms.get(room); 
	}
	
	/**
	 * This function is called by the NCRoomManager when it decides
	 * that the room must be deleted. In the future, this functionality
	 * may be added to users, but it would use a different function.
	 * @param room the room name
	 */
	public void deleteRoom(String room) {
		rooms.remove(room);
	}
	
	/**
	 * This function is called by the NCRoomManager when it tries
	 * to rename the room. </br>
	 * The method returns true when the server's manager updates it's references.
	 * If another room has the new name at the moment of the call, then the method
	 * will return false and the NCRoomManager must not change its name.
	 * @param oldName The previous name associated with the NCRoomManager.
	 * @param newName The new name.
	 * @return true if the NCRoomManager is allowed to change its name to newName.
	 */
	public boolean renameRoom(String oldName, String newName) {
		// The following call is atomic, don't touch it unless you know what you are doing.
		NCRoomManager check = rooms.putIfAbsent(newName, rooms.get(oldName));
		if (check == null) {
			rooms.remove(oldName);
			return true;
		}
		return false;
	}
	
	public DataOutputStream getConnectionWith(String user) {
		try {
			NCServerThread hilo = connections.get(user);
			if(hilo != null)
				return new DataOutputStream(hilo.getSocket().getOutputStream());
			return null;
		} catch (IOException e) {
			//e.printStackTrace();
			return null; //TODO solucionar esto de una manera mas agradable para el que pida la conexion
		}
	}
}
