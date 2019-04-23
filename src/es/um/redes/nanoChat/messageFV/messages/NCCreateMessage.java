package es.um.redes.nanoChat.messageFV.messages;

import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.NCMessageEncoder;
import es.um.redes.nanoChat.messageFV.NCMessageType;

public class NCCreateMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.CREATE;
	static private final String FIELD_NAME = "Room name";
	private final String roomName;
	
	public NCCreateMessage(String roomName) {
		this.roomName = roomName;
	}
	
	@Override
	public NCMessageType getType() {
		return MESSAGE_OP;
	}
	
	@Override
	public String encode() {
		return NCMessageEncoder
				.ofType(MESSAGE_OP)
				.encodeField(FIELD_NAME, roomName)
				.toString();
	}
	
	static NCCreateMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String roomName = dec.decodeField(FIELD_NAME);
		dec.assertNoMoreToRead();
		return new NCCreateMessage(roomName);
	}
	
	public String getRoomName() {
		return roomName;
	}
}
