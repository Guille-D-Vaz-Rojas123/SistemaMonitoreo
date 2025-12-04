package MX.unison;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gestiona la conexión del cliente con el servidor vía Socket.
 */
public class ClientConnection {

    private static final String SERVER_IP = "127.0.0.1"; // IP del servidor (localhost)
    private static final int SERVER_PORT = 12345;
    private static final String DATA_DELIMITER = "\\|"; // Delimitador para separar registros en la respuesta

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean isConnected = false;

    /**
     * Intenta establecer la conexión con el servidor.
     * @return true si la conexión fue exitosa.
     */
    public boolean connect() {
        try {
            System.out.println("Intentando conectar con el servidor en " + SERVER_IP + ":" + SERVER_PORT);
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Añadido BufferedReader
            isConnected = true;
            System.out.println("Conexión con el servidor establecida.");
            return true;
        } catch (Exception e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            isConnected = false;
            return false;
        }
    }

    /**
     * Envía los datos del sensor al servidor, cifrados.
     * @param data Los datos del sensor en formato SensorData.
     */
    public void sendSensorData(SensorData data) {
        if (!isConnected) {
            System.err.println("No conectado al servidor. No se pueden enviar datos.");
            return;
        }

        // 1. Formatear el mensaje: DATA:x:#, y:#, z:#
        String plainMessage = "DATA:" + data.toString();

        // 2. Encriptar el mensaje
        String encryptedMessage = EncryptionUtil.encrypt(plainMessage);

        if (encryptedMessage != null) {
            // 3. Enviar el mensaje cifrado al servidor
            out.println(encryptedMessage);
            // System.out.println("Cliente envió (Encriptado): " + encryptedMessage);
        } else {
            System.err.println("Fallo al encriptar el mensaje.");
        }
    }

    /**
     * Solicita datos históricos al servidor y espera la respuesta cifrada.
     * NOTA: Actualmente sin filtros (se añadirá en la GUI).
     * @return Una lista de objetos SensorData con la información histórica.
     */
    public List<SensorData> requestHistoricalData() {
        List<SensorData> historicalList = new ArrayList<>();
        if (!isConnected) {
            System.err.println("No conectado al servidor. No se puede solicitar el histórico.");
            return historicalList;
        }

        try {
            // 1. Enviar la solicitud cifrada
            String request = EncryptionUtil.encrypt("HISTORICAL_REQUEST:ALL");
            if (request == null) {
                System.err.println("Fallo al encriptar la solicitud histórica.");
                return historicalList;
            }
            out.println(request);

            // 2. Esperar y recibir la respuesta cifrada del servidor
            System.out.println("Esperando respuesta histórica del servidor...");
            String encryptedResponse = in.readLine();

            if (encryptedResponse == null) {
                System.err.println("Respuesta histórica vacía.");
                return historicalList;
            }

            // 3. Desencriptar la respuesta
            String decryptedResponse = EncryptionUtil.decrypt(encryptedResponse);

            if (decryptedResponse == null || decryptedResponse.startsWith("ERROR:")) {
                System.err.println("Error al desencriptar la respuesta o error del servidor: " + decryptedResponse);
                return historicalList;
            }

            if (decryptedResponse.equals("NO_DATA")) {
                System.out.println("El servidor no tiene datos históricos.");
                return historicalList;
            }

            // 4. Procesar la cadena delimitada
            String[] records = decryptedResponse.split(DATA_DELIMITER);
            for (String record : records) {
                try {
                    if (!record.trim().isEmpty()) {
                        historicalList.add(SensorData.fromHistoricalString(record.trim()));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear registro histórico: " + record);
                }
            }

            System.out.println("Datos históricos recibidos y procesados: " + historicalList.size() + " registros.");
            return historicalList;

        } catch (Exception e) {
            System.err.println("Error al solicitar/recibir datos históricos: " + e.getMessage());
            return historicalList;
        }
    }

    /**
     * Cierra la conexión.
     */
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            isConnected = false;
            System.out.println("Conexión con el servidor cerrada.");
        } catch (Exception e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
