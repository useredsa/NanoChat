package es.um.redes.nanoChat.messageFV.messages;

import es.um.redes.nanoChat.messageFV.InvalidFormat;
import es.um.redes.nanoChat.messageFV.NCMessageDecoder;
import es.um.redes.nanoChat.messageFV.NCMessageEncoder;
import es.um.redes.nanoChat.messageFV.NCMessageType;

public class NCRenameMessage implements NCMessage {
	static private final NCMessageType MESSAGE_OP = NCMessageType.RENAME;
	static private final String FIELD_NAME = "New name";
	private final String newName;
	
	public NCRenameMessage(String name) {
		this.newName = name;
	}
	
	@Override
	public NCMessageType getType() {
		return MESSAGE_OP;
	}
	
	@Override
	public String encode() {
		return NCMessageEncoder
				.ofType(MESSAGE_OP)
				.encodeField(FIELD_NAME, newName)
				.toString();
	}
	
	static NCRenameMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String roomName = dec.decodeField(FIELD_NAME);
		dec.assertNoMoreToRead();
		return new NCRenameMessage(roomName);
	}
	
	public String getNewName() {
		return newName;
	}
}
