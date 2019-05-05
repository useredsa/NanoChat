package server.rooms;

import java.util.Collection;
import java.util.Date;

public class NCRoomDescription {
	//Campos de los que, al menos, se compone una descripción de una sala	
	private final String name;
	private final Collection<String> members;
	private final long timeLastMessage;
	
	//Constructor a partir de los valores para los campos
	public NCRoomDescription(String name, Collection<String> members, long timeLastMessage) {
		this.name = name;
		this.members = members;
		this.timeLastMessage = timeLastMessage;
	}
		
	//Método que devuelve una representación de la Descripción lista para ser impresa por pantalla
	public String toPrintableString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Room Name: "+ name + "\t Members (" + members.size() + "): ");
		for (String member: members) {
			sb.append(member+" ");
		}
		if (timeLastMessage != 0)
			sb.append("\tLast message: "+new Date(timeLastMessage).toString());
		else 
			sb.append("\tLast message: not yet");
		return sb.toString();
	}

	public String getName() {
		return name;
	}

	public Collection<String> getMembers() {
		return members;
	}

	public long getTimeLastMessage() {
		return timeLastMessage;
	}
}
