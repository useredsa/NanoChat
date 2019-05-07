package client.application;


public class NanoChat {

	public static void main(String[] args) {

		//Comprobamos que nos pasan el parámetro relativo al directorio al que conectar
		if (args.length != 1) {
			System.out.println("Usage: java NanoChat <directory_hostname>");
			return;
		}

		// Creamos el controlador que aceptará y procesará los comandos
		NCController controller;
		controller = new NCController();

		// Comenzamos la conversación con el servidor de Chats si hemos podido contactar con él
		if (controller.getServerFromDirectory(args[0])) {
			if (controller.connectToChatServer()) {
				controller.startProcessing();
			}
			else
				System.out.println("ERROR: there is no connection to the chat server");
		}
		else 
			System.out.println("ERROR: there is no connection to the directory server");
		System.out.println("Bye.");		
	}
}