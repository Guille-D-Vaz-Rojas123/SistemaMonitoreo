package MX.unison;

import MX.unison.ClientHandler;
import MX.unison.DataBaseManager;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Clase principal del programa Servidor.
 * Se encarga de inicializar la DB y escuchar conexiones de clientes.
 */
public class Server {

    private static final int PORT = 12345; // Puerto fijo para la comunicación

    public static void main(String[] args) {
        System.out.println("Iniciando Sistema de Monitoreo - Servidor...");

        // 1. Preparar la Base de Datos
        DataBaseManager dbManager = new DataBaseManager();
        dbManager.createTable();

        // 2. Iniciar el Socket Servidor
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado en el puerto " + PORT + ". Esperando conexiones...");

            // Bucle principal para aceptar conexiones indefinidamente
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Bloquea hasta que un cliente se conecta

                // Un cliente se ha conectado, se le asigna un hilo
                System.out.println("Mensaje: Se conecta un cliente.");
                ClientHandler clientHandler = new ClientHandler(clientSocket, dbManager);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Error fatal en el Servidor Socket: " + e.getMessage());
        }
    }
}