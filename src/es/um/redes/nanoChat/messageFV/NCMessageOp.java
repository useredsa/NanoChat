package es.um.redes.nanoChat.messageFV;

public enum NCMessageOp {
	// Special opcodes
	INVALID_CODE((byte) 0, "Invalid Op Code"),
	
	// Users opcodes
	REGISTER((byte) 1, "Register"),
	LIST_ROOMS((byte) 2, "Get room list"), 
	CREATE((byte) 3, "Create"), 
	RENAME((byte) 4, "Rename"), 
	ENTER((byte) 5, "Enter"), 
	INFO((byte) 102, "Get info"), 
	KICK((byte) 6, "RICKROLL"),
	SEND((byte) 7, "Send text message"), 
	DM((byte) 8, "Send DM"), 
	UPLOAD((byte) 9, "Upload file"), 
	EXIT((byte) 10, "Exit room"), 
	QUIT((byte) 11, "Quit"), //TODO revisar jm
	
	// Servers opcodes
	OK((byte) 100, "Accepted"), 
	DENIED((byte) 103, "Denied"), 
	REPEATED((byte) 101, "Duplicated"),
	ROOMS_LIST((byte) 98, "Rooms List"),
	ROOM_INFO((byte) 74, "Room info"),
	KICKED((byte) 107, "YOU GOT RICKROLLED"), 
	NEW_MESSAGE((byte) 104, "New text message"), 
	NEW_DM((byte) 105, "New DM"), 
	NEW_FILE((byte) 106, "New file");
	
	private final byte code;
	private final String text;
	
	private NCMessageOp (byte code, String text) {
		this.code = code;
		this.text = text;
	}
	
	// Transforma una cadena en el opcode correspondiente
	public static NCMessageOp fromString(String opStr) {
		//Busca entre los opcodes si es válido y devuelve su código
		for (NCMessageOp op : NCMessageOp.values())
			if (op.getText().equalsIgnoreCase(opStr))
				return op;
		
		//Si no se corresponde con ninguna cadena entonces devuelve el código de código no válido
		return NCMessageOp.INVALID_CODE;
	}

	public byte getCode() {
		return code;
	}

	public String getText() {
		return text;
	}
}
