package MX.unison;

public class SensorData {
    public final int x;
    public final int y;
    public final int z;
    public final String fecha; // Se usa para datos históricos
    public final String hora;  // Se usa para datos históricos

    // Constructor para la simulación serial (tiempo real)
    public SensorData(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fecha = null; // No necesario en la simulación
        this.hora = null;  // No necesario en la simulación
    }

    // Constructor para datos históricos (leídos de la DB)
    public SensorData(int x, int y, int z, String fecha, String hora) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fecha = fecha;
        this.hora = hora;
    }

    /**
     * Retorna el formato de cadena serial requerido para el envío.
     * Formato: "x:#, y:#, z:#"
     */
    @Override
    public String toString() {
        return "x:" + x + ", y:" + y + ", z:" + z;
    }

    /**
     * Formato de cadena usado para enviar datos históricos.
     * Formato: "x:#, y:#, z:#, fecha:YYYY-MM-DD, hora:HH:MM:SS"
     */
    public String toHistoricalString() {
        return "x:" + x + ", y:" + y + ", z:" + z +
                ", fecha:" + fecha + ", hora:" + hora;
    }

    /**
     * Método estático para parsear una cadena histórica de vuelta a SensorData.
     */
    public static SensorData fromHistoricalString(String dataStr) throws NumberFormatException {
        int x = 0, y = 0, z = 0;
        String fecha = null;
        String hora = null;

        String[] parts = dataStr.split(", ");
        for (String part : parts) {
            if (part.startsWith("x:")) {
                x = Integer.parseInt(part.substring(2));
            } else if (part.startsWith("y:")) {
                y = Integer.parseInt(part.substring(2));
            } else if (part.startsWith("z:")) {
                z = Integer.parseInt(part.substring(2));
            } else if (part.startsWith("fecha:")) {
                fecha = part.substring(6);
            } else if (part.startsWith("hora:")) {
                hora = part.substring(5);
            }
        }

        if (fecha == null || hora == null) {
            throw new NumberFormatException("Faltan campos de fecha/hora en la cadena histórica.");
        }

        return new SensorData(x, y, z, fecha, hora);
    }
}