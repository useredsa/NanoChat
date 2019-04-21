package es.um.redes.nanoChat.messageFV;

import es.um.redes.nanoChat.messageFV.encoding.InvalidFormat;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.encoding.NCMessageEncoder;

public class NCRegisterMessage implements NCMessage {
	static private final NCMessageOp MESSAGE_OP = NCMessageOp.REGISTER;
	static private final String FIELD_NAME = "User";
	private final String name;
	
	public NCRegisterMessage(String name) {
		this.name = name;
	}
	
	@Override
	public NCMessageOp getOp() {
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
