package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import es.um.redes.nanoChat.messageFV.NCMessageType;
import es.um.redes.nanoChat.messageFV.messages.NCControlMessage;
import es.um.redes.nanoChat.messageFV.messages.NCTextMessage;
import es.um.redes.nanoChat.server.NCServerManager;
import es.um.redes.nanoChat.server.NCServerThread;

public class NCBasicRoom implements NCRoomManager {
	private final NCServerManager serverManager;
	private String roomName;
	private final String creator;
	private final HashSet<String> admins;
	private final HashMap<String, NCServerThread> members;
	private long timeLastMessage = 0; //TODO variable que sacaremos de la lista de mensajes
	
	public NCBasicRoom(NCServerManager serverManager, String name, String creator, NCServerThread th) {
		this.serverManager = serverManager;
		roomName = name;
		this.creator = creator;
		admins = new HashSet<String>();
		members = new HashMap<String, NCServerThread>();
		enter(creator, th);
	}

	@Override
	public boolean enter(String u, NCServerThread th) {
		if (members.containsKey(u)) return false; //TODO notificar entradas y salidas (y entonces cambiar el constructor para que no llame a esto)
		members.put(u, th);
		return true;
	}
	
	@Override
	public void exit(String u) {
		members.remove(u);
	}

	@Override 
	public void broadcastMessage(String u, String message) throws IOException {
		for(String e : members.keySet()) {
			if (!e.equals(u)) {
				DataOutputStream dos = new DataOutputStream(members.get(e).getSocket().getOutputStream()); //TODO preguntar oscar (concurrencia?)
				dos.writeUTF(new NCTextMessage(u,message).encode());
			}
		}
		timeLastMessage = new Date().getTime();
	}
	
	private boolean hasRights(String user)  {
		return creator.equals(user) || admins.contains(user);
	}

	@Override
	public NCControlMessage rename(String user, String newName) {
		if (hasRights(user)) {
			if (serverManager.renameRoom(roomName, newName)) {
				// The operation can be performed
				this.roomName = newName; //TODO notificar al resto
				return new NCControlMessage(NCMessageType.OK);				
			} else {
				// There's another room with the newName
				return new NCControlMessage(NCMessageType.REPEATED);
			}
		} else {
			// The user has no rights
			return new NCControlMessage(NCMessageType.DENIED);
		}
	}
	
	@Override
	public NCControlMessage promote(String user, String promoted) {
		if (hasRights(user)) {
			if (!hasRights(promoted)) {
				// The operation can be performed
				admins.add(promoted); //TODO notificar a la otra persona
				return new NCControlMessage(NCMessageType.OK);
			} else {
				// The target already has rights
				return new NCControlMessage(NCMessageType.REPEATED);
			}
		} else {
			// The user has no rights
			return new NCControlMessage(NCMessageType.DENIED);
		}
	}
	
	@Override
	public NCControlMessage kick(String user, String kicked) {
		if (hasRights(user) && !hasRights(kicked)) {
			if (!hasRights(kicked)) {
				if (members.containsKey(kicked)) {
					// The operation can be performed
					NCServerThread th = members.get(kicked);
					th.forceExit(); // Notify the user being kicked
					exit(kicked);	// Remove user from the room
					return new NCControlMessage(NCMessageType.OK);
				} else {
					// The target is not in the room
					return new NCControlMessage(NCMessageType.IMPOSSIBLE);
				}
			} else {
				// The target cannot be kicked
				return new NCControlMessage(NCMessageType.IMPOSSIBLE);
			}
		} else {
			// The user has no rights
			return new NCControlMessage(NCMessageType.DENIED);
		}
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
