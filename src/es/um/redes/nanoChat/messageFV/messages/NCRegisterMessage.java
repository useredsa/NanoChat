package es.um.redes.nanoChat.messageFV.messages;

import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.NCMessageEncoder;
import es.um.redes.nanoChat.messageFV.NCMessageType;

public class NCRegisterMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.REGISTER;
	static private final String FIELD_NAME = "User";
	private final String name;
	
	public NCRegisterMessage(String name) {
		this.name = name;
	}
	
	@Override
	public NCMessageType getType() {
		return MESSAGE_OP;
	}
	
	@Override
	public String encode() {
		return NCMessageEncoder
				.ofType(MESSAGE_OP)
				.encodeField(FIELD_NAME, name)
				.toString();
	}
	
	static NCRegisterMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String name = dec.decodeField(FIELD_NAME);
		dec.assertNoMoreToRead();
		return new NCRegisterMessage(name);
	}
	
	public String getName() {
		return name;
	}
}
