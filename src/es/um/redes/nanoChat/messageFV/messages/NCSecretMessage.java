package es.um.redes.nanoChat.messageFV.messages;

import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.NCMessageEncoder;
import es.um.redes.nanoChat.messageFV.NCMessageType;

public class NCSecretMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.NEW_DM;
	static private final String USER_FIELD = "User";
	static private final String TEXT_FIELD = "Text";
	private final String user;
	private final String text;
	
	
	public NCSecretMessage(String user, String text) {
		this.user = user;
		this.text = text;
	}
	
	@Override
	public NCMessageType getType() {
		return MESSAGE_OP;
	}
	
	@Override
	public String encode() {
		return NCMessageEncoder
				.ofType(MESSAGE_OP)
				.encodeField(USER_FIELD, user)
				.encodeField(TEXT_FIELD, text)
				.toString();
	}
	
	static NCSecretMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String user = dec.decodeField(USER_FIELD);
		String text = dec.decodeField(TEXT_FIELD);
		return new NCSecretMessage(user,text);
	}
	
	public String getText() {
		return text;
	}
	
	public String getUser() {
		return user;
	}

}
