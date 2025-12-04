package MX.unison;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Simula la lectura de datos de un Arduino a través de comunicación serial.
 * Genera datos aleatorios cada 1 segundo.
 */
public class ArduinoSimulator extends Thread {

    private volatile boolean isRunning = false;
    private final Consumer<SensorData> dataConsumer;
    private final Random random = new Random();

    // Rango de valores (ejemplo)
    private static final int MIN_VAL = 50;
    private static final int MAX_VAL = 150;

    /**
     * @param dataConsumer Función a llamar cada vez que se genera un nuevo SensorData.
     */
    public ArduinoSimulator(Consumer<SensorData> dataConsumer) {
        this.dataConsumer = dataConsumer;
        setDaemon(true); // Permite que el programa se cierre si este hilo es el único restante
    }

    public void startSimulation() {
        if (!isRunning) {
            isRunning = true;
            // Solo llama a start() si el Thread no se ha iniciado antes.
            // Si el hilo ya terminó, se necesita una nueva instancia, pero para este caso
            // simplificaremos asumiendo que el usuario no lo para y vuelve a iniciar
            // inmediatamente después de cerrar.
            if (!this.isAlive()) {
                super.start();
            }
        }
    }

    public void stopSimulation() {
        isRunning = false;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (isRunning) {
                    // Generar datos aleatorios simulados
                    int x = random.nextInt(MAX_VAL - MIN_VAL) + MIN_VAL;
                    int y = random.nextInt(MAX_VAL - MIN_VAL) + MIN_VAL;
                    int z = random.nextInt(MAX_VAL - MIN_VAL) + MIN_VAL;

                    SensorData data = new SensorData(x, y, z);

                    // Enviar los datos al consumidor (que será la clase Cliente)
                    dataConsumer.accept(data);

                    // Esperar 1 segundo (requisito del proyecto)
                    Thread.sleep(1000);
                } else {
                    // Si no está corriendo, simplemente espera un poco para no consumir CPU
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Simulador de Arduino detenido.");
        }
    }
}