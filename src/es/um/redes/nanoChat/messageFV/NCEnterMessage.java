package es.um.redes.nanoChat.messageFV;

import es.um.redes.nanoChat.messageFV.encoding.InvalidFormat;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageEncoder;

public class NCEnterMessage implements NCMessage {
	static private final NCMessageOp MESSAGE_OP = NCMessageOp.ENTER;
	static private final String FIELD_NAME = "Room";
	private final String roomName;
	
	public NCEnterMessage(String roomName) {
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
	
	static NCEnterMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String roomName = dec.decodeField(FIELD_NAME);
		dec.assertNoMoreToRead();
		return new NCEnterMessage(roomName);
	}
	
	public String getRoomName() {
		return roomName;
	}
}
