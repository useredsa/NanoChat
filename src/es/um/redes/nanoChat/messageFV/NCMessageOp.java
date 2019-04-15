package es.um.redes.nanoChat.messageFV;

public enum NCMessageOp {
	REGISTER((byte) 1, "Register"),
	ROOMS_LIST((byte) 2, "Room List"), 
	CREATE((byte) 3, "Create"), 
	RENAME((byte) 4, "Rename"), 
	ENTER((byte) 5, "Enter"), 
	INFO((byte) 102, "Info"), 
	KICK((byte) 6, "RICKROLL"),
	SEND((byte) 7, "Send"), 
	DM((byte) 8, "Direct Message"), 
	UPLOAD((byte) 9, "Upload"), 
	EXIT((byte) 10, "Exit"), 
	
	INVALID_CODE((byte) 0, "Invalid Op Code"), 
	OK((byte) 100, "Ok"), 
	DENIED((byte) 103, "Denied"), 
	REPEATED((byte) 101, "Repeated"),
	ROOMS_INFO((byte) 98, "Rooms Info"),
	KICKED((byte) 107, "YOU GOT RICKROLLED"), 
	NEW_MESSAGE((byte) 104, "Chat Message"), 
	NEW_DM((byte) 105, "Direct Message"), 
	NEW_FILE((byte) 106, "File");
	
	private final byte code;
	private final String text;
	
	private NCMessageOp (byte code, String text) {
		this.code = code;
		this.text = text;
	}

	public byte getCode() {
		return code;
	}

	public String getText() {
		return text;
	}
}
