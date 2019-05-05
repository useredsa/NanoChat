package es.um.redes.nanoChat.messageFV;

import java.util.Collection;

public final class NCMessageEncoder implements IFieldEncoder {
	private static final char DELIMITER = ':';	//Define el delimitador
	private static final char SEPARATOR = '&';	// Defines the separator character (copied from NCMessageEncoder)
	private static final char END_LINE = '\n';	//Define el carácter de fin de línea
	private static final String OPCODE_FIELD = "Operation";
	private final StringBuffer sb = new StringBuffer();
	
	private NCMessageEncoder(NCMessageType op) {
		sb.append(OPCODE_FIELD + DELIMITER + op.getText() + END_LINE);
	}
	
	public static IFieldEncoder ofType(NCMessageType op) {
		return new NCMessageEncoder(op);
	}
	
	@Override
	public IFieldEncoder encodeField(String fieldName, String fieldValue) {
		sb.append(fieldName + DELIMITER + fieldValue + END_LINE); // Construimos el campo
		return this;
	}
	
	@Override
	public IFieldEncoder encodeMultiField(String fieldName, Collection<String> fieldValues) {
		sb.append(fieldName + DELIMITER);
		boolean first = true;
		for (String value : fieldValues) {
			if (first) first = false;
			else sb.append(SEPARATOR);
			sb.append(value);
		}
		sb.append(END_LINE);
		return this;
	}
	
	@Override
	public String toString() {
		sb.append(END_LINE);
		return sb.toString();
	}
}
