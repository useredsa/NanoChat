package es.um.redes.nanoChat.messageFV;

public enum NCMessageType {
	// Special opcodes
	INVALID_CODE("Invalid Op Code"),
	
	// User's opcodes
	REGISTER("Register"),
	LIST_ROOMS("Get room list"), 
	CREATE("Create"), 
	RENAME("Rename"), 
	ENTER("Enter"), 
	INFO("Get info"), 
	PROMOTE("Promote"),
	KICK("RICKROLL"),				// Un poquito de gracia :p
	SEND("Send"), 
	DM("Send DM"), 
	UPLOAD("Upload file"), 
	EXIT("Exit room"), 
	QUIT("Quit"),
	
	// Server's opcodes
	OK("Accepted"), 
	DENIED("Denied"),
	REPEATED("Duplicated"),
	IMPOSSIBLE("Impossible"),
	ROOMS_LIST("Rooms List"),
	ROOM_INFO("Room info"),
	KICKED("YOU GOT RICKROLLED"), 	// Un poquito de gracia :P 
	NEW_MESSAGE("New text message"), 
	NEW_DM("New DM"), 
	NEW_FILE("New file"),
	NOTIFICATION("New Notification");
	
	private final String messageText;	// Text that appears in the Field-Value encoding 
	
	private NCMessageType (String messageText) {
		this.messageText = messageText;
	}
	
	/**
	 * Returns the message type given its operation text.
	 * If none matches, then the value INVALID_CODE is returned.
	 * @param text The operation text.
	 * @return The message type or INVALID_CODE if there isn't a message with such text.
	 */
	public static NCMessageType fromString(String text) {
		for (NCMessageType op : NCMessageType.values())
			if (text.equalsIgnoreCase(op.getText()))
				return op;

		return NCMessageType.INVALID_CODE;
	}

	public String getText() {
		return messageText;
	}
}
