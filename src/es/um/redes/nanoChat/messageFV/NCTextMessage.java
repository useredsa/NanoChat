package es.um.redes.nanoChat.messageFV;


/*
 *TEXT
 ------
 operation:<operation>
 user:<user>
 text:<text>
 
 En el campo user se pone el nombre del receptor
 o del transmisor; dependiendo de si se envia al servidor
 o es el servidor el que lo envia, espectivamente.
 
 Defined operations:
 DM
 New_Message
 New_DM
 */

public class NCTextMessage extends NCMessage {	
	private String user;
	private final String text;
	private final static String USER_FIELD = "User";
	private final static String MESSAGE_FIELD = "Message";
	
	public NCTextMessage(NCMessageOp op, String user, String text) {
		this.op = op;
		this.user = user;
		this.text = text;
	}
	
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		sb.append(OPCODE_FIELD + DELIMITER + op.getText() + END_LINE);
		sb.append(USER_FIELD + DELIMITER + user + END_LINE);
		sb.append(MESSAGE_FIELD + DELIMITER + text + END_LINE);
		sb.append(END_LINE);
		return sb.toString();
	}
	
	
	public static NCTextMessage readFromString(NCMessageOp op, String message) {
		String[] lines = message.split(System.getProperty("line.separator"));
		String user = null;
		String text = null;
		
		int idx = lines[1].indexOf(DELIMITER);
		String field = lines[1].substring(0, idx).toLowerCase();
		String value = lines[1].substring(idx+1).trim();
		if(field.equalsIgnoreCase(USER_FIELD))
			user = value;
		idx = lines[2].indexOf(DELIMITER);
		field = lines[2].substring(0, idx).toLowerCase();
		value = lines [2].substring(idx+1).trim();
		if(field.equalsIgnoreCase(MESSAGE_FIELD));
			text = value;
		return new NCTextMessage(op,user,text);
	}
	
	public String getText() {
		return text;
	}
	public String getUser() {
		return user;
	}
}
