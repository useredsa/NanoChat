package es.um.redes.nanoChat.messageFV.messages;

import java.util.Arrays;
import java.util.Collection;

import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.NCMessageEncoder;
import es.um.redes.nanoChat.messageFV.NCMessageType;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCRoomInfoMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.ROOM_INFO;
	static private final String ROOMS_FIELD = "Room";
	static private final String USERS_FIELD = "Users";
	static private final String LAST_MESSAGE_FIELD = "Last Message";
	private final NCRoomDescription description;
	
	public NCRoomInfoMessage(NCRoomDescription description) {
		this.description = description; 
	}

	@Override
	public NCMessageType getType() {
		return MESSAGE_OP;
	}

	@Override
	public String encode() {
		return NCMessageEncoder
				.ofType(MESSAGE_OP)
				.encodeField(ROOMS_FIELD, description.getName())
				.encodeMultiField(USERS_FIELD, description.getMembers())
				.encodeField(LAST_MESSAGE_FIELD, String.valueOf(description.getTimeLastMessage()))
				.toString();
	}
	
	public static NCRoomInfoMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String roomName = dec.decodeField(ROOMS_FIELD);
		Collection<String> members = Arrays.asList(dec.decodeMultiField(USERS_FIELD));
		long lastMessageTime = Long.valueOf(dec.decodeField(LAST_MESSAGE_FIELD));
		dec.assertNoMoreToRead();
		return new NCRoomInfoMessage(new NCRoomDescription(roomName, members, lastMessageTime));
	}

	public NCRoomDescription getRoomDescription() {
		return description;
	}
}
