package es.um.redes.nanoChat.messageFV.messages;

import es.um.redes.nanoChat.messageFV.NCMessageEncoder;
import es.um.redes.nanoChat.messageFV.NCMessageType;

public class NCControlMessage implements NCMessage {
	private final NCMessageType op;
	
	public NCControlMessage(NCMessageType op) {
		this.op= op;
	}
	
	@Override
	public NCMessageType getType() {
		return op;
	}
	
	@Override
	public String encode() {
		return NCMessageEncoder.ofType(op).toString();
	}
}
