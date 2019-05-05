package messageFV.messages;

import messageFV.InvalidFormat;
import messageFV.NCMessageDecoder;
import messageFV.NCMessageEncoder;
import messageFV.NCMessageType;

public class NCPromoteMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.PROMOTE;
	static private final String FIELD_NAME = "User";
	private final String user;
	
	public NCPromoteMessage(String user) {
		this.user = user;
	}
	
	@Override
	public NCMessageType getType() {
		return MESSAGE_OP;
	}
	
	@Override
	public String encode() {
		return NCMessageEncoder
				.ofType(MESSAGE_OP)
				.encodeField(FIELD_NAME, user)
				.toString();
	}
	
	static NCPromoteMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String roomName = dec.decodeField(FIELD_NAME);
		dec.assertNoMoreToRead();
		return new NCPromoteMessage(roomName);
	}
	
	public String getUser() {
		return user;
	}
}
