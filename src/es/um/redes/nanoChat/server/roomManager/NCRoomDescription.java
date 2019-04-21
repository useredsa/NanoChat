package es.um.redes.nanoChat.server.roomManager;

import java.util.Collection;
import java.util.Date;

public class NCRoomDescription {
	//Campos de los que, al menos, se compone una descripción de una sala 
	//TODO make private
	public String name;
	public Collection<String> members;
	public long timeLastMessage;
	
	//Constructor a partir de los valores para los campos
	public NCRoomDescription(String name, Collection<String> members, long timeLastMessage) {
		this.name = name;
		this.members = members;
		this.timeLastMessage = timeLastMessage;
	}
		
	//Método que devuelve una representación de la Descripción lista para ser impresa por pantalla
	public String toPrintableString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Room Name: "+ name + "\t Members (" + members.size() + ") : ");
		for (String member: members) {
			sb.append(member+" ");
		}
		if (timeLastMessage != 0)
			sb.append("\tLast message: "+new Date(timeLastMessage).toString());
		else 
			sb.append("\tLast message: not yet");
		return sb.toString();
	}
}
