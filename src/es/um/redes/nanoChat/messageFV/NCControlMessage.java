package es.um.redes.nanoChat.messageFV;

import es.um.redes.nanoChat.messageFV.encoding.NCMessageEncoder;

public class NCControlMessage implements NCMessage {
	private final NCMessageOp op;
	
	public NCControlMessage(NCMessageOp op) {
		this.op= op;
	}
	
	@Override
	public NCMessageOp getOp() {
		return op;
	}
	
	@Override
	public String encode() {
		return NCMessageEncoder.ofType(op).toString();
	}
}
