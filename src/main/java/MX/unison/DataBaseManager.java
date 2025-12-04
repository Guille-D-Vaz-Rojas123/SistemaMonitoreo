package MX.unison;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseManager {

    // Nombre del archivo de la base de datos SQLite
    private static final String DB_NAME = "monitorBD.db";
    // URL de conexión a la base de datos SQLite
    private static final String URL = "jdbc:sqlite:" + DB_NAME;

    /**
     * Establece y retorna la conexión a la base de datos.
     * @return Objeto Connection si la conexión es exitosa, null en caso contrario.
     */
    public Connection connect() {
        Connection conn = null;
        try {
            // Carga el driver de SQLite (aunque con versiones recientes de JDBC esto puede no ser necesario)
            // Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);
            System.out.println("Conexión a SQLite establecida.");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Crea la tabla 'datos_sensor' si no existe, siguiendo el esquema del proyecto.
     * Esquema: id, x, y, z, fecha_de_captura, hora_de_captura.
     */
    public void createTable() {
        // SQL para crear la tabla
        String sql = "CREATE TABLE IF NOT EXISTS datos_sensor (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + " x INTEGER NOT NULL,\n"
                + " y INTEGER NOT NULL,\n"
                + " z INTEGER NOT NULL,\n"
                + " fecha_de_captura TEXT NOT NULL,\n" // Usaremos TEXT para almacenar la fecha y hora
                + " hora_de_captura TEXT NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // Ejecuta el SQL para crear la tabla
            stmt.execute(sql);
            System.out.println("Tabla 'datos_sensor' creada o ya existe.");

        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
        }
    }
}