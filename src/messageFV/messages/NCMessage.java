package messageFV.messages;

import java.io.DataInputStream;
import java.io.IOException;

import messageFV.InvalidFormat;
import messageFV.NCMessageDecoder;
import messageFV.NCMessageType;


public interface NCMessage {
	
	// Extrae la operación del mensaje entrante y usa la subclase para parsear el resto del mensaje
	public static NCMessage readFromSocket(DataInputStream dis) throws IOException, InvalidFormat {
		String message = dis.readUTF();
		NCMessageDecoder dec = new NCMessageDecoder(message);
		NCMessageType op = dec.getMessageOp();
		NCMessage decodedMessage = null;
		switch (op) {
		case CREATE:
			decodedMessage = NCCreateMessage.decode(dec);
			break;
		case DM:
			decodedMessage = NCDirectMessage.decode(dec);
			break;
		case ENTER:
			decodedMessage = NCEnterMessage.decode(dec);
			break;
		case KICK:
			decodedMessage = NCKickMessage.decode(dec);
			break;
		case NOTIFICATION:
			decodedMessage = NCNotificationMessage.decode(dec);
			break;
		case NEW_DM:
			decodedMessage = NCNewDirectMessage.decode(dec);
			break;
		case NEW_MESSAGE:
			decodedMessage = NCNewTextMessage.decode(dec);
			break;
		case PROMOTE:
			decodedMessage = NCPromoteMessage.decode(dec);
			break;
		case REGISTER:
			decodedMessage = NCRegisterMessage.decode(dec);
			break;
		case RENAME:
			decodedMessage = NCRenameMessage.decode(dec);
			break;
		case ROOM_INFO:
			decodedMessage = NCRoomInfoMessage.decode(dec);
			break;
		case ROOMS_LIST:
			decodedMessage = NCRoomListMessage.decode(dec);
			break;
		case SEND:
			decodedMessage = NCSendMessage.decode(dec);
			break;
		case OK:
		case DENIED:
		case IMPOSSIBLE:
		case REPEATED:
		case EXIT:
		case INFO:
		case KICKED:
		case QUIT:
		case LIST_ROOMS:
			decodedMessage = new NCControlMessage(op);
			break;
		case INVALID_CODE:
			break;
		default: System.err.println("In class NCMessage: Yet unimplemented message decodification (You fool)");
		}
		return decodedMessage;
	}
	
	public static NCMessage readFromSocketNoChecks(DataInputStream dis) throws IOException {
		try {
			return readFromSocket(dis);
		} catch (InvalidFormat e) {
			System.err.println("While reading without format checks, an invalid format was encountered. Terminating application...");
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}
	
	public abstract NCMessageType getType();
	public abstract String encode();
}
