package es.um.redes.nanoChat.messageFV.messages;

import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.NCMessageEncoder;
import es.um.redes.nanoChat.messageFV.NCMessageType;

public class NCKickMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.KICK;
	static private final String FIELD_NAME = "User";
	private final String user;
	
	public NCKickMessage(String user) {
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
	
	static NCKickMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String roomName = dec.decodeField(FIELD_NAME);
		dec.assertNoMoreToRead();
		return new NCKickMessage(roomName);
	}
	
	public String getUser() {
		return user;
	}
}
