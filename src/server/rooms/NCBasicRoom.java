package server.rooms;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;

import messageFV.NCMessageType;
import messageFV.messages.NCControlMessage;
import messageFV.messages.NCNotificationMessage;
import server.NCServerManager;
import server.NCServerThread;

public class NCBasicRoom extends NCGlobalRoom {
	private final HashSet<String> admins;
	private boolean deleted = false;
	
	public NCBasicRoom(NCServerManager serverManager, String name, String creator, NCServerThread creatorThread) throws IOException {
		super(serverManager, name);
		admins = new HashSet<String>();
		admins.add(creator);
		UserInfo creatorInfo = new UserInfo(new DataOutputStream(creatorThread.getSocket().getOutputStream()), creatorThread);
		members.put(creator, creatorInfo); // does not notify anyone
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
	public synchronized boolean enter(String user, NCServerThread th) throws IOException {
		if (deleted)
			return false;
		return super.enter(user, th);
	}
	
	@Override
	public synchronized void exit(String user) {
		super.exit(user);
		admins.remove(user);
		if (members.isEmpty()) {
			serverManager.deleteRoom(roomName);
			deleted = true;
		}
	}
	
	protected synchronized boolean hasRights(String user)  {
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
}
