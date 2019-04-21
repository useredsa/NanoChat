package es.um.redes.nanoChat.messageFV.encoding;

import java.util.Collection;

public interface IFieldEncoder {
	
	public IFieldEncoder encodeField(String fieldName, String fieldValue);
	
	public IFieldEncoder encodeMultiField(String fieldName, Collection<String> fieldValues);
	
}
