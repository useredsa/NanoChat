package es.um.redes.nanoChat.messageFV;

import es.um.redes.nanoChat.messageFV.encoding.InvalidFormat;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageEncoder;

public class NCTextMessage implements NCMessage {
	static private final NCMessageOp MESSAGE_OP = NCMessageOp.NEW_MESSAGE;
	static private final String FIELD1_NAME = "User";
	static private final String FIELD2_NAME = "Text";
	private final String user;
	private final String text;
	
	public NCTextMessage(String user, String text) {
		this.user = user;
		this.text = text;
	}
	
	@Override
	public NCMessageOp getOp() {
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