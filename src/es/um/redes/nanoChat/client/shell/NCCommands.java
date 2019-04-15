package es.um.redes.nanoChat.client.shell;

public enum NCCommands {
	/**
	 * Códigos para todos los comandos soportados por el shell
	 */
	public static final byte COM_INVALID = 0;
	public static final byte COM_ROOMLIST = 1;
	public static final byte COM_ENTER = 2;
	public static final byte COM_NICK = 3;
	public static final byte COM_SEND = 4;
	public static final byte COM_EXIT = 5;
	public static final byte COM_ROOMINFO = 7;
	public static final byte COM_QUIT = 8;
	public static final byte COM_HELP = 9;
	public static final byte COM_SOCKET_IN = 101;
	
	/**
	 * Códigos de los comandos válidos que puede
	 * introducir el usuario del shell. El orden
	 * es importante para relacionarlos con la cadena
	 * que debe introducir el usuario y con la ayuda
	 */
	private static final Byte[] _valid_user_commands = { 
		COM_ROOMLIST, 
		COM_ENTER,
		COM_NICK,
		COM_SEND,
		COM_EXIT, 
		COM_ROOMINFO,
		COM_QUIT,
		COM_HELP
		};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_user_commands_str = {
		"roomlist",
		"enter",
		"nick",
		"send",
		"exit",
		"info",
		"quit",
		"help" };

	/**
	 * Mensaje de ayuda para cada orden
	 */
	private static final String[] _valid_user_commands_help = {
		"provides a list of available rooms to chat",
		"enter a particular <room>",
		"to set the <nickname> in the server",
		"to send a <message> in the chat",
		"to leave the current room", 
		"shows the information of the room",
		"to quit the application",
		"shows this information"};

	/**
	 * Transforma una cadena introducida en el código de comando correspondiente
	 */
	public static byte stringToCommand(String comStr) {
		//Busca entre los comandos si es válido y devuelve su código
		for (int i = 0;
		i < _valid_user_commands_str.length; i++) {
			if (_valid_user_commands_str[i].equalsIgnoreCase(comStr)) {
				return _valid_user_commands[i];
			}
		}
		//Si no se corresponde con ninguna cadena entonces devuelve el código de comando no válido
		return COM_INVALID;
	}

	/**
	 * Imprime la lista de comandos y la ayuda de cada uno
	 */
	public static void printCommandsHelp() {
		System.out.println("List of commands:");
		for (int i = 0; i < _valid_user_commands_str.length; i++) {
			System.out.println(_valid_user_commands_str[i] + " -- "
					+ _valid_user_commands_help[i]);
		}		
	}
}	

