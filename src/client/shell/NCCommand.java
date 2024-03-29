package client.shell;

public enum NCCommand {
	SOCKET_IN	(NCCommand.NON_USER, NCCommand.NON_USER),
	INVALID		(NCCommand.NON_USER, NCCommand.NON_USER),
	NICK		("nick",	"to set the <nickname> in the server"),
	ROOMLIST	("roomlist","provides a list of available rooms to chat"),
	ENTER		("enter", 	"enter a particular <room>"),
	EXIT		("exit",	"to leave the current room"),
	CREATE		("create",	"create a new <room>"),
	RENAME		("rename",	"changes the name of a <room> to <name>"),
	SEND		("send",	"to send a <message> in the chat"),
	DM			("dm",		"to send <user> a <message>"),
	INFO		("info",	"shows the information of the room"),
	PROMOTE		("promote",	"promote a <user> to administrator of the room"),
	KICK		("kick",	"kicks out the <user> from the room"),
	QUIT		("quit",	"to quit the application"),
	HELP		("help",	"shows this information");
	
	
	private static final String NON_USER = "NON-USER";
	private final String name;			// Command name
	private final String helpMessage;	// Command help message shown with the help command
										// NON_USER means not to display
	
	private NCCommand (String name, String message) {
		this.name = name;
		helpMessage = message;
	}
		
	// Gets Command from string
	public static NCCommand stringToCommand(String comStr) {
		// Search within commands for the string
		for (NCCommand comm : NCCommand.values())
			if (comm.name.equals(comStr))
				return comm;
		// Or return invalid if no match was found
		return INVALID;
	}

	// Prints list of valid commands and the help message of each one.
	public static void printCommandsHelp() {	//TODO poner mejor, de forma que se entiendan, sobre todo los que llevan dos campos y eso
		System.out.println("List of commands:");
		for (NCCommand comm : NCCommand.values())
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

