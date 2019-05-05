package messageFV.messages;

import messageFV.InvalidFormat;
import messageFV.NCMessageDecoder;
import messageFV.NCMessageEncoder;
import messageFV.NCMessageType;

public class NCNotificationMessage implements NCMessage{
	static private final NCMessageType MESSAGE_OP = NCMessageType.NOTIFICATION;
	static private final String ACTION_FIELD = "Action";
	static private final String USER_FIELD = "User";
	static private final String OBJECT_FIELD = "Object";
	private final NCMessageType action;
	private final String user;
	private final String object;
	
	public NCNotificationMessage(String user, NCMessageType action, String object) {
		this.user = user;
		this.action = action;
		this.object = object;
	}
	
	@Override
	public NCMessageType getType() {
		return MESSAGE_OP;
	}

	@Override
	public String encode() {
		return NCMessageEncoder
				.ofType(MESSAGE_OP)
				.encodeField(USER_FIELD, user)
				.encodeField(ACTION_FIELD, action.getText())
				.encodeField(OBJECT_FIELD, object)
				.toString();
	}
	
	static NCNotificationMessage decode(NCMessageDecoder dec) throws InvalidFormat {
		String user = dec.decodeField(USER_FIELD);
		String action = dec.decodeField(ACTION_FIELD);
		String object = dec.decodeField(OBJECT_FIELD);
		dec.assertNoMoreToRead();
		return new NCNotificationMessage(user, NCMessageType.fromString(action), object);
	}
	
	public NCMessageType getAction() {
		return action;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getObject() {
		return object;
	}

}
