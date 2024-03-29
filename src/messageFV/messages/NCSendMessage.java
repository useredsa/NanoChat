package messageFV.messages;

import messageFV.InvalidFormat;
import messageFV.NCMessageDecoder;
import messageFV.NCMessageEncoder;
import messageFV.NCMessageType;

public class NCSendMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.SEND;
	static private final String FIELD_NAME = "Text";
	private final String text;
	
	public NCSendMessage(String text) {
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
				.encodeField(FIELD_NAME, text)
				.toString();
	}
	
	static NCSendMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String name = dec.decodeField(FIELD_NAME);
		return new NCSendMessage(name);
	}
	
	public String getText() {
		return text;
	}
}
