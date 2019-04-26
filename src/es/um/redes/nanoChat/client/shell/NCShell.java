package es.um.redes.nanoChat.client.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import es.um.redes.nanoChat.client.comm.NCConnector;

public class NCShell {
	/**
	 * Scanner para leer comandos de usuario de la entrada estándar
	 */
	private Scanner reader;

	NCCommand command = NCCommand.INVALID;
	String[] commandArgs = new String[0];

	public NCShell() {
		reader = new Scanner(System.in); 

		System.out.println("NanoChat shell");
		System.out.println("For help, type 'help'");
	}

	//Devuelve los parámetros proporcionados por el usuario para el comando actual
	public String[] getCommandArguments() {
		return commandArgs;	
	}

	//Espera hasta obtener un comando válido entre los comandos existentes
	public NCCommand readGeneralCommand() {
		boolean validArgs;
		do {
			commandArgs = readGeneralCommandFromStdIn();
			//si el comando tiene parámetros hay que validarlos
			validArgs = validateCommandArguments(commandArgs);
		} while(!validArgs);
		return command;
	}

	//Usa la entrada estándar para leer comandos y procesarlos
	private String[] readGeneralCommandFromStdIn() {
		String[] args = new String[0];
		Vector<String> vargs = new Vector<String>();
		while (true) {
			System.out.print("(nanoChat) ");
			//obtenemos la línea tecleada por el usuario
			String input = reader.nextLine();
			StringTokenizer st = new StringTokenizer(input);
			//si no hay ni comando entonces volvemos a empezar
			if (st.hasMoreTokens() == false) {
				continue;
			}
			//traducimos la cadena del usuario en el código de comando correspondiente
			command = NCCommand.stringToCommand(st.nextToken());
			//Dependiendo del comando...
			switch (command) {
			case INVALID:
				//El comando no es válido
				System.out.println("Invalid command");
				continue;
			case HELP:
				//Mostramos la ayuda
				NCCommand.printCommandsHelp();
				continue;
				
			//Estos comandos son válidos sin parámetros
			case QUIT:
			case ROOMLIST:
				break;

			case CREATE:
			case ENTER:
			case NICK:
				//Estos requieren un parámetro
				while (st.hasMoreTokens()) {
					vargs.add(st.nextToken());
				}
				break;
			default:
				System.out.println("That command is not valid outisde of a room");
				continue;
			}
			break;
		}
		return vargs.toArray(args);
	}

	//Espera a que haya un comando válido de sala o llegue un mensaje entrante
	public NCCommand readChatCommand(NCConnector ngclient) {
		boolean validArgs;
		do {
			commandArgs = readChatCommandFromStdIn(ngclient);
			//si hay parámetros se validan
			validArgs = validateCommandArguments(commandArgs);
		} while(!validArgs);
		return command;
	}

	//Utiliza la entrada estándar para leer comandos y comprueba si hay datos en el flujo de entrada del conector
	private String[] readChatCommandFromStdIn(NCConnector ncclient) {
		String[] args = new String[0];
		Vector<String> vargs = new Vector<String>();
		while (true) {
			System.out.print("(nanoChat-room) ");
			//Utilizamos un BufferedReader en lugar de un Scanner porque no podemos bloquear la entrada
			BufferedReader standardInput = new BufferedReader(new InputStreamReader(System.in));
			boolean blocked = true; 
			String input ="";
			//Estamos esperando comando o mensaje entrante
			while (blocked) {
				try {
					if (ncclient.isDataAvailable()) {
						//Si el flujo de entrada tiene datos entonces el comando actual es SOCKET_IN y debemos salir
						System.out.println("* Message received from server...");
						command = NCCommand.SOCKET_IN;
						return null;
					}
					else
						//Analizamos si hay datos en la entrada estándar (el usuario tecleó INTRO)
					if (standardInput.ready()) {
						input = standardInput.readLine();			
						blocked = false;
					}
					//Puesto que estamos sondeando las dos entradas de forma continua, esperamos para evitar un consumo alto de CPU
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (IOException | InterruptedException e) {
					command = NCCommand.INVALID;
					return null;
				}				
			}
			//Si el usuario tecleó un comando entonces procedemos de igual forma que hicimos antes para los comandos generales
			StringTokenizer st = new StringTokenizer(input);
			if (st.hasMoreTokens() == false) {
				continue;
			}
			command = NCCommand.stringToCommand(st.nextToken());
			switch (command) {
			// Special commands:
			case INVALID:
				System.out.println("Invalid command ("+input+")");
				continue;
			case HELP:
				NCCommand.printCommandsHelp();
				continue;
			// This commands don't require parameters:
			case INFO:
			case EXIT:
				break;
			// This commands require parameters:
			case RENAME:
			case PROMOTE:
			case KICK:
				while (st.hasMoreTokens()) {
					vargs.add(st.nextToken());
				}
				break;
			// This command requires an special parameter:
			case SEND:
				StringBuffer message = new StringBuffer();
				while (st.hasMoreTokens()) {
					message.append(st.nextToken()+" "); //TODO no me gustan este tipo de cosas, pero es mucha paliza cambiar esta clase
				}
				vargs.add(message.toString());
				break;
			default:
				System.out.println("That command is not valid inside a room");
				continue;
			}
			break;
		}
		return vargs.toArray(args);
	}


	// Algunos comandos requieren un parámetro
	// Este método comprueba si se proporciona parámetro para los comandos
	//TODO parsear comandos en la clase enum, y hacer para que salga automaticamente getName() y con el numero de comandos, etc
	private boolean validateCommandArguments(String[] args) {
		switch(this.command) {
		// CREATE/ENTER require the room name
		case CREATE:
		case ENTER:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use: " + command.getName() + " <room name>");
				return false;
			}
			break;
		// RENAME requires the new name
		case RENAME:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use: " + command.getName() + " <new room name>");
				return false;
			}
			break;
		// NICK/PROMOTE/KICK require the username
		case NICK:
		case PROMOTE:
		case KICK:
			if (args.length == 0 || args.length > 1) {
				System.out.println("Correct use: " + command.getName() + " <username>");
				return false;
			}		
			break;
		//send requiere el parámetro <message>
		case SEND:
			if (args.length == 0) {
				System.out
						.println("Correct use: send <message>");
				return false;
			}
			break;
		default:
		}
		//El resto no requieren parámetro
		return true;
	}
}
