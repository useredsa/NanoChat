package es.um.redes.nanoChat.messageFV;

import es.um.redes.nanoChat.messageFV.encoding.InvalidFormat;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageEncoder;

public class NCCreateMessage implements NCMessage {
	static private final NCMessageOp MESSAGE_OP = NCMessageOp.CREATE;
	static private final String FIELD_NAME = "Room name";
	private final String roomName;
	
	public NCCreateMessage(String roomName) {
		this.roomName = roomName;
	}
	
	@Override
	public NCMessageOp getOp() {
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
