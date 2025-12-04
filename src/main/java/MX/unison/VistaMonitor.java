package MX.unison;

import javax.swing.*;
import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vista Monitor: Muestra una gráfica de líneas en tiempo real para x, y, z.
 * Incluye un botón para Iniciar/Detener la lectura de datos y el envío por Socket.
 */
public class VistaMonitor extends JPanel {

    private final MainFrame mainFrame;
    private final ClientConnection clientConnection;

    // Componentes de JFreeChart para el gráfico en tiempo real
    private final XYSeries seriesX;
    private final XYSeries seriesY;
    private final XYSeries seriesZ;
    private final XYSeriesCollection dataset;
    private final JFreeChart chart;

    private final JButton btnStartStop;
    private final ArduinoSimulator simulator;
    private final AtomicInteger timeTick = new AtomicInteger(0);

    private volatile boolean isMonitoring = false;

    public VistaMonitor(MainFrame mainFrame, ClientConnection clientConnection) {
        this.mainFrame = mainFrame;
        this.clientConnection = clientConnection;

        // 1. Inicializar el dataset para las series de tiempo
        seriesX = new XYSeries("Eje X");
        seriesY = new XYSeries("Eje Y");
        seriesZ = new XYSeries("Eje Z");
        dataset = new XYSeriesCollection();
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);

        // 2. Inicializar el gráfico
        chart = createChart();
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(650, 400));
        chartPanel.setBorder(BorderFactory.createLineBorder(StyleUtil.AZUL_UNISON, 1)); // Borde simulado (FIGURA 5)

        // 3. Inicializar el simulador
        // El callback se llama cuando el simulador genera un nuevo dato.
        simulator = new ArduinoSimulator(this::onSensorDataReceived);

        // 4. Configuración del layout
        setLayout(new BorderLayout(10, 10));
        setBackground(StyleUtil.COLOR_BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Contenedor Superior (Título y Puerto COM - Requisito) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Vista: Monitor - Datos en Tiempo Real");
        lblTitle.setFont(new Font(StyleUtil.FONT_NAME, Font.BOLD, 18));
        topPanel.add(lblTitle);

        // Menú de Puerto COM (Simulado por ahora, se añadiría la lógica real aquí)
        topPanel.add(new JLabel("Puerto COM:"));
        String[] ports = {"COM1 (Simulado)", "COM2", "COM3"};
        JComboBox<String> comSelect = new JComboBox<>(ports);
        comSelect.setFont(new Font(StyleUtil.FONT_NAME, Font.PLAIN, 14));
        topPanel.add(comSelect);

        add(topPanel, BorderLayout.NORTH);

        // --- Centro: Gráfico ---
        add(chartPanel, BorderLayout.CENTER);

        // --- Inferior: Botones ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setOpaque(false);

        btnStartStop = new JButton("Iniciar Lectura");
        StyleUtil.applyPrimaryStyle(btnStartStop);
        btnStartStop.setBackground(StyleUtil.AZUL_UNISON); // Color azul inicial
        btnStartStop.addActionListener(e -> toggleMonitoring());

        JButton btnBack = new JButton("Volver a Inicio");
        StyleUtil.applyBaseStyle(btnBack);
        btnBack.setBackground(StyleUtil.AZUL_OSCURO_UNISON.darker());
        btnBack.addActionListener(e -> mainFrame.showCard(MainFrame.getCardInicio()));

        bottomPanel.add(btnStartStop);
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea la configuración inicial del gráfico de líneas de JFreeChart.
     */
    private JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Lecturas de Sensores (X, Y, Z)", // Título del gráfico
                "Tiempo (s)",                      // Etiqueta del eje X
                "Valor",                           // Etiqueta del eje Y
                dataset,                           // Datos
                PlotOrientation.VERTICAL,
                true,                              // Leyenda
                false,                             // Tooltips
                false                              // URLs
        );

        chart.setBackgroundPaint(StyleUtil.COLOR_BG_LIGHT);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Asignar colores Unison a las series
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, StyleUtil.AZUL_UNISON);
        renderer.setSeriesPaint(1, StyleUtil.DORADO_UNISON);
        renderer.setSeriesPaint(2, StyleUtil.AZUL_OSCURO_UNISON);
        plot.setRenderer(renderer);

        return chart;
    }

    /**
     * Alterna entre iniciar y detener el monitoreo.
     */
    private void toggleMonitoring() {
        if (!clientConnection.isConnected()) {
            JOptionPane.showMessageDialog(this, "Debe estar conectado al servidor para iniciar el monitoreo.",
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isMonitoring) {
            // Detener
            simulator.stopSimulation();
            btnStartStop.setText("Iniciar Lectura");
            btnStartStop.setBackground(StyleUtil.AZUL_UNISON);
            isMonitoring = false;
        } else {
            // Iniciar
            // Limpiar la gráfica al iniciar una nueva sesión
            seriesX.clear();
            seriesY.clear();
            seriesZ.clear();
            timeTick.set(0);

            simulator.startSimulation();
            btnStartStop.setText("Detener Lectura");
            btnStartStop.setBackground(StyleUtil.AZUL_OSCURO_UNISON);
            isMonitoring = true;
        }
    }

    /**
     * Callback para recibir datos del simulador (Thread).
     * Se asegura de que la actualización de la gráfica ocurra en el EDT.
     */
    private void onSensorDataReceived(SensorData data) {
        // Enviar los datos al servidor (requisito del proyecto)
        clientConnection.sendSensorData(data);

        // Actualizar la gráfica en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            int currentTick = timeTick.incrementAndGet();
            seriesX.add(currentTick, data.x);
            seriesY.add(currentTick, data.y);
            seriesZ.add(currentTick, data.z);

            // Mantener solo los últimos 60 puntos para una vista en tiempo real
            if (seriesX.getItemCount() > 60) {
                seriesX.remove(0);
                seriesY.remove(0);
                seriesZ.remove(0);
            }
        });
    }
}