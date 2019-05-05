package messageFV.messages;

import messageFV.InvalidFormat;
import messageFV.NCMessageDecoder;
import messageFV.NCMessageEncoder;
import messageFV.NCMessageType;

public class NCEnterMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.ENTER;
	static private final String FIELD_NAME = "Room";
	private final String roomName;
	
	public NCEnterMessage(String roomName) {
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
	
	static NCEnterMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String roomName = dec.decodeField(FIELD_NAME);
		dec.assertNoMoreToRead();
		return new NCEnterMessage(roomName);
	}
	
	public String getRoomName() {
		return roomName;
	}
}
