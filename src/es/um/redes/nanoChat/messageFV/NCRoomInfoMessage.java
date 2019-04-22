package es.um.redes.nanoChat.messageFV;

import java.util.Arrays;
import java.util.Collection;

import es.um.redes.nanoChat.messageFV.encoding.InvalidFormat;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageEncoder;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCRoomInfoMessage implements NCMessage {
	static private final NCMessageOp MESSAGE_OP = NCMessageOp.ROOM_INFO;
	static private final String ROOMS_FIELD = "Room";
	static private final String USERS_FIELD = "Users";
	static private final String LAST_MESSAGE_FIELD = "Last Message";
	private final NCRoomDescription description;
	
	public NCRoomInfoMessage(NCRoomDescription description) {
		this.description = description; 
	}

	@Override
	public NCMessageOp getOp() {
		return MESSAGE_OP;
	}

	@Override
	public String encode() {
		return NCMessageEncoder
				.ofType(MESSAGE_OP)
				.encodeField(ROOMS_FIELD, description.name)
				.encodeMultiField(USERS_FIELD, description.members)
				.encodeField(LAST_MESSAGE_FIELD, String.valueOf(description.timeLastMessage))
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
		return description; //TODO consider unmodifiable
	}
}
