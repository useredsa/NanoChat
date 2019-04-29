package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import es.um.redes.nanoChat.messageFV.NCMessageType;
import es.um.redes.nanoChat.messageFV.messages.NCControlMessage;
import es.um.redes.nanoChat.messageFV.messages.NCNotificationMessage;
import es.um.redes.nanoChat.messageFV.messages.NCTextMessage;
import es.um.redes.nanoChat.server.NCServerManager;
import es.um.redes.nanoChat.server.NCServerThread;

public class NCBasicRoom implements NCRoomManager {
	private class UserInfo {
		DataOutputStream dos;
		NCServerThread thread;
		
		UserInfo(DataOutputStream dos, NCServerThread thread) {
			this.dos = dos;
			this.thread = thread;
		}
	}
	private final NCServerManager serverManager;
	private String roomName;
	private final HashSet<String> admins;
	private final HashMap<String, UserInfo> members;
	private long timeLastMessage = 0;
	private boolean deleted = false;
	
	public NCBasicRoom(NCServerManager serverManager, String name, String creator, NCServerThread creatorThread) throws IOException {
		this.serverManager = serverManager;
		roomName = name;
		admins = new HashSet<String>();
		members = new HashMap<String, UserInfo>();
		admins.add(creator);
		UserInfo creatorInfo = new UserInfo(new DataOutputStream(creatorThread.getSocket().getOutputStream()), creatorThread);
		members.put(creator, creatorInfo); // does not notify anyone
	}
	
	private synchronized void sendNotification(String user, NCMessageType action) {
		sendNotification(user, action, null);
	}
	
	private synchronized void sendNotification(String user, NCMessageType action, String object)  {
		boolean rename = NCMessageType.RENAME == action;
		boolean promote = NCMessageType.PROMOTE == action;
		String notification = new NCNotificationMessage(user,action,object).encode();
		for(String e : members.keySet()) {
			if (!e.equals(user) && (!e.equals(object) || rename || promote)) {
				try {
					members.get(e).dos.writeUTF(notification); 
				} catch (IOException e1) {
					System.err.println("* An error ocurred while notifying the user " + e + " from room: " + roomName);
				}
			}
		}
	}
	
	@Override
	public synchronized boolean enter(String user, NCServerThread userThread) throws IOException {
		if (deleted || members.containsKey(user))
			return false;
		UserInfo userInfo = new UserInfo(new DataOutputStream(userThread.getSocket().getOutputStream()), userThread);
		members.put(user, userInfo);
		sendNotification(user, NCMessageType.ENTER);
		return true;
	}
	
	@Override
	public synchronized void exit(String user) {
		admins.remove(user);
		members.remove(user);
		sendNotification(user, NCMessageType.EXIT);
		if (members.isEmpty()) {
			serverManager.deleteRoom(roomName);
			deleted = true;
		}
	}

	@Override 
	public synchronized void broadcastMessage(String user, String message) {
		for(Map.Entry<String, UserInfo> entry : members.entrySet()) {
			if (!entry.getKey().equals(user)) {
				try {
					entry.getValue().dos.writeUTF(new NCTextMessage(user, message).encode());
				} catch (IOException e) {
					System.out.println("* Could not write to user " + entry.getKey() + " from room " + roomName); //TODO habría que informar al emisor?
				}
			}
		}
		timeLastMessage = new Date().getTime();
	}
	
	private synchronized boolean hasRights(String user)  {
		return admins.contains(user);
	}

	@Override
	public synchronized NCControlMessage rename(String user, String newName) {
		if (hasRights(user)) {
			if (serverManager.renameRoom(roomName, newName)) {
				// The operation can be performed
				this.roomName = newName;
				sendNotification(user, NCMessageType.RENAME, newName); 
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
	public synchronized NCControlMessage promote(String user, String promoted) {
		if (hasRights(user)) {
			if (members.containsKey(promoted)) {
				if (!hasRights(promoted)) {
					// The operation can be performed
					admins.add(promoted); 
					sendNotification(user, NCMessageType.PROMOTE, promoted);
					return new NCControlMessage(NCMessageType.OK);
				} else {
					// The target already has rights
					return new NCControlMessage(NCMessageType.REPEATED);
				}
			} else {
				// The target is not in the room
				return new NCControlMessage(NCMessageType.IMPOSSIBLE);
			}
		} else {
			// The user has no rights
			return new NCControlMessage(NCMessageType.DENIED);
		}
	}
	
	@Override
	public synchronized NCControlMessage kick(String user, String kicked) {
		if (hasRights(user) && !hasRights(kicked)) {
			if (!hasRights(kicked)) {
				if (members.containsKey(kicked)) {
					// The operation can be performed
					members.get(kicked).thread.forceExit(); // Notify the user thread to kick
					members.remove(kicked);	// Remove user from the room
					sendNotification(user, NCMessageType.KICK, kicked);
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
