package es.um.redes.nanoChat.messageFV.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import es.um.redes.nanoChat.messageFV.IFieldEncoder;
import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.NCMessageEncoder;
import es.um.redes.nanoChat.messageFV.NCMessageType;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCRoomListMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.ROOMS_LIST;
	static private final String ROOMS_FIELD = "Room";
	static private final String USERS_FIELD = "Users";
	static private final String LAST_MESSAGE_FIELD = "Last Message";
	private final Collection<NCRoomDescription> rooms;
	
	public NCRoomListMessage(Collection<NCRoomDescription> rooms) {
		this.rooms = rooms; 
	}

	@Override
	public NCMessageType getType() {
		return MESSAGE_OP;
	}

	@Override
	public String encode() {
		IFieldEncoder enc =
				NCMessageEncoder
				.ofType(MESSAGE_OP);
		for (NCRoomDescription des : rooms) {
			enc.encodeField(ROOMS_FIELD, des.name);
			enc.encodeMultiField(USERS_FIELD, des.members);
			enc.encodeField(LAST_MESSAGE_FIELD, String.valueOf(des.timeLastMessage));
		}
		return enc.toString();
	}
	
	public static NCRoomListMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		LinkedList<NCRoomDescription> rooms = new LinkedList<NCRoomDescription>();
		while (!dec.hasEnded()) {
			String roomName = dec.decodeField(ROOMS_FIELD);
			String[] users = dec.decodeMultiField(USERS_FIELD);
			long lastMessageTime = Long.parseLong(dec.decodeField(LAST_MESSAGE_FIELD));
			rooms.add(new NCRoomDescription(roomName, Arrays.asList(users), lastMessageTime));
		}
		return new NCRoomListMessage(rooms);
	}

	public Collection<NCRoomDescription> getRoomsInfo() {
		return rooms; //TODO consider unmodifiable
	}
}
