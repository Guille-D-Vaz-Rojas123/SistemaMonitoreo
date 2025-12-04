package MX.unison;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Hilo (Thread) que maneja la comunicación con un cliente específico.
 * Se encarga de recibir, desencriptar y procesar los datos del cliente, y responder peticiones de histórico.
 */
public class ClientHandler extends Thread {
    private Socket clientSocket;
    private DataBaseManager dbManager;
    private PrintWriter out;
    private BufferedReader in;

    // Formato de fecha y hora para la DB
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Delimitador para enviar múltiples registros de datos históricos en una sola cadena
    private static final String DATA_DELIMITER = "|";

    public ClientHandler(Socket socket, DataBaseManager dbManager) {
        this.clientSocket = socket;
        this.dbManager = dbManager;
        System.out.println("Mensaje: Cliente conectado desde: " + socket.getInetAddress());
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            // El servidor lee los datos encriptados del cliente
            while ((inputLine = in.readLine()) != null) {
                // 1. Desencriptar el mensaje
                String decryptedMessage = EncryptionUtil.decrypt(inputLine);

                if (decryptedMessage == null) {
                    System.err.println("Mensaje recibido inválido o no se pudo desencriptar.");
                    continue;
                }

                System.out.println("Servidor recibió (Desencriptado): " + decryptedMessage);

                // 2. Procesar el mensaje
                if (decryptedMessage.startsWith("DATA:")) {
                    processSensorData(decryptedMessage.substring(5).trim());
                } else if (decryptedMessage.startsWith("HISTORICAL_REQUEST:")) {
                    System.out.println("Mensaje: Cliente solicita datos históricos.");
                    // En el futuro, aquí se procesarían los filtros (fecha/hora)
                    sendHistoricalData();
                }
            }

        } catch (Exception e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
            System.out.println("Cliente desconectado.");
        }
    }

    /**
     * Procesa y guarda los datos de los sensores en la base de datos.
     * Formato esperado: "x=#, y=#, z=#"
     * @param dataStr La cadena de datos del sensor.
     */
    private void processSensorData(String dataStr) {
        try {
            // Analizar la cadena para obtener x, y, z
            int x = 0, y = 0, z = 0;
            String[] parts = dataStr.split(", ");
            for (String part : parts) {
                if (part.startsWith("x:")) {
                    x = Integer.parseInt(part.substring(2));
                } else if (part.startsWith("y:")) {
                    y = Integer.parseInt(part.substring(2));
                } else if (part.startsWith("z:")) {
                    z = Integer.parseInt(part.substring(2));
                }
            }

            // Obtener fecha y hora actual
            LocalDateTime now = LocalDateTime.now();
            String fecha = now.format(DATE_FORMAT);
            String hora = now.format(TIME_FORMAT);

            // Insertar en la DB
            saveSensorData(x, y, z, fecha, hora);
            System.out.println("Mensaje: Cliente envía datos para guardar. Datos guardados: X=" + x + ", Y=" + y + ", Z=" + z);

        } catch (NumberFormatException e) {
            System.err.println("Error de formato en los datos del sensor: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error al procesar los datos: " + e.getMessage());
        }
    }

    /**
     * Realiza la inserción de datos en la tabla 'datos_sensor'.
     */
    private void saveSensorData(int x, int y, int z, String fecha, String hora) {
        String sql = "INSERT INTO datos_sensor(x, y, z, fecha_de_captura, hora_de_captura) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setInt(3, z);
            pstmt.setString(4, fecha);
            pstmt.setString(5, hora);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar en la DB: " + e.getMessage());
        }
    }

    /**
     * Consulta todos los datos históricos de la base de datos, los serializa y los envía al cliente.
     */
    private void sendHistoricalData() {
        String sql = "SELECT x, y, z, fecha_de_captura, hora_de_captura FROM datos_sensor ORDER BY id DESC";
        List<SensorData> historicalData = new ArrayList<>();

        try (Connection conn = dbManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                SensorData data = new SensorData(
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getString("fecha_de_captura"),
                        rs.getString("hora_de_captura")
                );
                historicalData.add(data);
            }

            // 1. Serializar la lista de datos a una sola cadena
            StringBuilder sb = new StringBuilder();
            for (SensorData data : historicalData) {
                sb.append(data.toHistoricalString()).append(DATA_DELIMITER);
            }

            String plainHistoricalData = sb.toString();

            // Si hay datos, eliminar el último delimitador
            if (plainHistoricalData.length() > 0) {
                plainHistoricalData = plainHistoricalData.substring(0, plainHistoricalData.length() - DATA_DELIMITER.length());
            } else {
                plainHistoricalData = "NO_DATA";
            }

            // 2. Encriptar y enviar
            String encryptedResponse = EncryptionUtil.encrypt(plainHistoricalData);

            if (encryptedResponse != null) {
                out.println(encryptedResponse);
                System.out.println("Mensaje: Se envían los datos solicitados. Total de registros: " + historicalData.size());
            } else {
                System.err.println("Error: Fallo al encriptar la respuesta histórica.");
            }

        } catch (SQLException e) {
            System.err.println("Error al leer datos históricos de la DB: " + e.getMessage());
            // Enviar un mensaje de error al cliente si falla
            String errorResponse = EncryptionUtil.encrypt("ERROR: Database read failed.");
            if (errorResponse != null) out.println(errorResponse);
        }
    }
}