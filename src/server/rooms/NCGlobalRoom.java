package server.rooms;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import messageFV.NCMessageType;
import messageFV.messages.NCControlMessage;
import messageFV.messages.NCNotificationMessage;
import messageFV.messages.NCTextMessage;
import server.NCServerManager;
import server.NCServerThread;

public class NCGlobalRoom implements NCRoomManager {
	
	protected class UserInfo {
		DataOutputStream dos;
		NCServerThread thread;
		
		UserInfo(DataOutputStream dos, NCServerThread thread) {
			this.dos = dos;
			this.thread = thread;
		}
	}
	
	protected final NCServerManager serverManager;
	protected String roomName;
	protected final HashMap<String, UserInfo> members;
	protected long timeLastMessage = 0;
	
	public NCGlobalRoom(NCServerManager serverManager, String name) {
		this.serverManager = serverManager;
		roomName = name;
		members = new HashMap<String, UserInfo>();
	}
	
	private synchronized void sendNotification(String user, NCMessageType action)  {
		for(String e : members.keySet()) {
			if (e != user) {
				try {
					members.get(e).dos.writeUTF(new NCNotificationMessage(user, action, null).encode());
				} catch (IOException e1) {
					System.err.println("* An error ocurred while notifying in room: " + roomName);
				}
			}
		}
	}
	
	@Override
	public synchronized boolean enter(String user, NCServerThread userThread) throws IOException {
		if (members.containsKey(user))
			return false;
		DataOutputStream userDos = new DataOutputStream(userThread.getSocket().getOutputStream()); 
		members.put(user, new UserInfo(userDos, userThread));
		sendNotification(user, NCMessageType.ENTER);
		return true;
	}
	
	@Override
	public synchronized void exit(String user) {
		members.remove(user);
		sendNotification(user, NCMessageType.EXIT);
	}

	@Override 
	public synchronized void broadcastMessage(String user, String message) {
		for(Map.Entry<String, UserInfo> entry : members.entrySet()) {
			if (!entry.getKey().equals(user)) {
				try {
					entry.getValue().dos.writeUTF(new NCTextMessage(user, message).encode());
				} catch (IOException e) {
					System.out.println("* Could not write to user " + entry.getKey() + " from room " + roomName);
				}
			}
		}
		timeLastMessage = new Date().getTime();
	}

	@Override
	public synchronized int usersInRoom() {
		return members.size();
	}
	
	@Override
	public synchronized String getRoomName() {
		return roomName;
	}
	
	@Override
	public synchronized NCRoomDescription getDescription() {
		return new NCRoomDescription(roomName, members.keySet(), timeLastMessage);
	}
	
	// This following functionality is not supported by global rooms
	@Override
	public synchronized NCControlMessage rename(String user, String newName) {
		return new NCControlMessage(NCMessageType.IMPOSSIBLE);
	}
	
	@Override
	public synchronized NCControlMessage promote(String user, String promoted) {
		return new NCControlMessage(NCMessageType.IMPOSSIBLE);
	}
	
	@Override
	public synchronized NCControlMessage kick(String user, String kicked) {
		return new NCControlMessage(NCMessageType.IMPOSSIBLE);
	}
}
