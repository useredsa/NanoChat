package es.um.redes.nanoChat.server.roomManager;

import java.io.IOException;

import es.um.redes.nanoChat.messageFV.messages.NCControlMessage;
import es.um.redes.nanoChat.server.NCServerThread;

public interface NCRoomManager {
	
	public abstract String getRoomName();
	//Método para registrar a un usuario u en una sala (se anota también su socket de comunicación)
	public abstract boolean enter(String user, NCServerThread userThread) throws IOException;
	//Método para eliminar un usuario de una sala
	public abstract void exit(String user);
	//Método para hacer llegar un mensaje enviado por un usuario u
	public abstract void broadcastMessage(String user, String message);
	//Método para devolver la descripción del estado actual de la sala
	public abstract NCRoomDescription getDescription();
	//Método para devolver el número de usuarios conectados a una sala
	public abstract int usersInRoom();
	
	
	/**
	 * Processes a request to rename the room. The function returns a control message of type:
	 * <ul>
	 * <li> OK if the operation is performed successfully.</li> 
	 * <li> IMPOSSIBLE if the room doesn't implement this functionality</li>
	 * <li> DENIED if the user can't perform this operation (because they don't have the necessary rights).</li>
	 * <li> REPEATED if there is another room with the new name </li>
	 * </ul>
	 * @param user The user that performs the operation.
	 * @param newName The new name of the room.
	 * @return 0 if the operation was successfully performed. For other return values, check the description.
	 */
	public abstract NCControlMessage rename(String user, String newName);
	/**
	 * Processes a request to promote another user to administrator. The function returns:
	 * <ul>
	 * <li> OK if the operation is performed successfully.</li>
	 * <li> IMPOSSIBLE if the room doesn't implement this functionality.</li>
	 * <li> DENIED if the user can't perform this operation (because they don't have the necessary rights).</li>
	 * <li> REPEATED if the user being promoted is already an administrator.</li>
	 * </ul>
	 * @param user The user that performs the operation.
	 * @param promoted The user being promoted
	 * @return 0 if the operation was successfully performed. For other return values, check the description.
	 */
	public abstract NCControlMessage promote(String user, String promoted);
	/**
	 * Processes a request to kick another user from the room.
	 * <ul>
	 * <li> OK if the operation is performed successfully.</li>
	 * <li> IMPOSSIBLE if the room doesn't implement this functionality</li>
	 * <li> DENIED if the user can't kick other users in this room.</li>
	 * <li> DENIED If the user being kicked can't be kicked from this room.</li>
	 * <li> IMPOSSIBLE if the user being kicked is not in the room.</li>
	 * </ul>
	 * @param user The user that performs the operation.
	 * @param kicked The user being kicked
	 * @return 0 if the operation was successfully performed. For other return values, check the description.
	 */
	public abstract NCControlMessage kick(String user, String kicked);
}
