package messageFV.messages;

import messageFV.NCMessageEncoder;
import messageFV.NCMessageType;

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
