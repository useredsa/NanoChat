package es.um.redes.nanoChat.messageFV;

import es.um.redes.nanoChat.messageFV.encoding.InvalidFormat;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageEncoder;

public class NCSendMessage implements NCMessage {
	static private final NCMessageOp MESSAGE_OP = NCMessageOp.SEND;
	static private final String FIELD_NAME = "Text";
	private final String text;
	
	public NCSendMessage(String text) {
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
