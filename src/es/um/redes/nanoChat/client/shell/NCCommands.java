package es.um.redes.nanoChat.client.shell;

public enum NCCommands {
	SOCKET_IN	(NCCommands.NON_USER, NCCommands.NON_USER),
	INVALID		(NCCommands.NON_USER, NCCommands.NON_USER),
	ROOMLIST	("roomlist","provides a list of available rooms to chat"),
	CREATE		("create",	"create a new <room>"),
	ENTER		("enter", 	"enter a particular <room>"),
	NICK		("nick",	"to set the <nickname> in the server"),
	SEND		("send",	"to send a <message> in the chat"),
	EXIT		("exit",	"to leave the current room"),
	INFO		("info",	"shows the information of the room"),
	QUIT		("quit",	"to quit the application"),
	HELP		("help",	"shows this information");
	
	
	private static final String NON_USER = "NON-USER";
	
	
	private final String name;			// Command name
	private final String helpMessage;	// Command help message shown with the help command
										// NON_USER means not to display
	
	private NCCommands (String name, String message) {
		this.name = name;
		helpMessage = message;
	}
		
	// Gets Command from string
	public static NCCommands stringToCommand(String comStr) {
		// Search within commands for the string
		for (NCCommands comm : NCCommands.values())
			if (comm.name.equals(comStr))
				return comm;
		// Or return invalid if no match was found
		return INVALID;
	}

	// Prints list of valid commands and the help message of each one.
	public static void printCommandsHelp() {
		System.out.println("List of commands:");
		for (NCCommands comm : NCCommands.values())
			if (!comm.helpMessage.equals(NON_USER))
				System.out.println(comm.name + " -- " + comm.helpMessage);
	}

	public String getName() {
		return name;
	}

	public String getHelpMessage() {
		return helpMessage;
	}
}	

