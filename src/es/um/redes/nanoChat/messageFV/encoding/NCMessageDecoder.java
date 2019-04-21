package es.um.redes.nanoChat.messageFV.encoding;

import es.um.redes.nanoChat.messageFV.NCMessageOp;

public class NCMessageDecoder {
	private static final char DELIMITER = ':';    //Define el delimitador
	private static final char SEPARATOR = '&'; //TODO move apart for encoder and decoder
	private static final char END_LINE = '\n';    //Define el carácter de fin de línea
	private static final String OPCODE_FIELD = "Operation";
	private int currentLine;
	private final String[] messageLines;
	private final NCMessageOp op;
	
	public NCMessageDecoder(String message) throws InvalidFormat {
		assert message != null;
		currentLine = 0;
		messageLines = message.trim().split(System.getProperty("line.separator"), -1);
		if (messageLines.length == 0)
			throw new InvalidFormat("This message is empty");
		String operationText = decodeField(OPCODE_FIELD);
		op = NCMessageOp.fromString(operationText);
	}
	
	public String decodeField(String fieldName) throws InvalidFormat {
		if (currentLine >= messageLines.length)
			throw new InvalidFormat("Message is too short, expected more than " + messageLines.length + " lines\n");
		int idx = messageLines[currentLine].indexOf(DELIMITER);			// Posición del delimitador
		if (idx == -1)
			throw new InvalidFormat("Line " + messageLines[currentLine] + " has no delimiter");
		String field = messageLines[currentLine].substring(0, idx);
		String value = messageLines[currentLine].substring(idx + 1);
		if (!field.equalsIgnoreCase(fieldName))
			throw new InvalidFormat("Couldn't parse field " + fieldName + " from " + messageLines[currentLine]);
		currentLine++;
		return value;
	}
	
	public String[] decodeMultiField(String fieldName) throws InvalidFormat {
		String completeField = decodeField(fieldName);
		String[] values;
		if (completeField.equals(""))
			values = new String[0];
		else
			values = completeField.split(String.valueOf(SEPARATOR), -1);
		return values;
	}
	
	public void assertNoMoreToRead() throws InvalidFormat {
		if (!hasEnded())
			throw new InvalidFormat("Message was longer than expected. Next line: " + messageLines[currentLine]);
	}
	
	public boolean hasEnded() {
		return currentLine == messageLines.length;
	}
	
	public NCMessageOp getMessageOp() {
		return op;
	}
	
}
