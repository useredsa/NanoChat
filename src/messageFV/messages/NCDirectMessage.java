package messageFV.messages;

import messageFV.InvalidFormat;
import messageFV.NCMessageDecoder;
import messageFV.NCMessageEncoder;
import messageFV.NCMessageType;

public class NCDirectMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.DM;
	static private final String USER_FIELD = "User";
	static private final String TEXT_FIELD = "Text";
	private final String user;
	private final String text;
	
	
	public NCDirectMessage(String user, String text) {
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
	
	static NCDirectMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String user = dec.decodeField(USER_FIELD);
		String text = dec.decodeField(TEXT_FIELD);
		return new NCDirectMessage(user,text);
	}
	
	public String getText() {
		return text;
	}
	
	public String getUser() {
		return user;
	}

}
