package messageFV.messages;

import messageFV.InvalidFormat;
import messageFV.NCMessageDecoder;
import messageFV.NCMessageEncoder;
import messageFV.NCMessageType;

public class NCTextMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.NEW_MESSAGE;
	static private final String FIELD1_NAME = "User";
	static private final String FIELD2_NAME = "Text";
	private final String user;
	private final String text;
	
	public NCTextMessage(String username, String text) {
		this.user = username;
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
				.encodeField(FIELD1_NAME, user)
				.encodeField(FIELD2_NAME, text)
				.toString();
	}
	
	static NCTextMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String user = dec.decodeField(FIELD1_NAME);
		String text = dec.decodeField(FIELD2_NAME);
		dec.assertNoMoreToRead();
		return new NCTextMessage(user, text);
	}
	
	public String getUser() {
		return user;
	}
	
	public String getMessage() {
		return text;
	}
}