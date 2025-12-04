package MX.unison;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

/**
 * Vista Histórico: Muestra una gráfica con los datos guardados en la base de datos.
 * Incluye filtros y un botón para cargar los datos desde el servidor.
 */
public class VistaHistorico extends JPanel {

    private final MainFrame mainFrame;
    private final ClientConnection clientConnection;

    private final JLabel lblStatus;
    private final JButton btnLoadData;
    private final JPanel chartContainer;

    private XYSeries seriesX;
    private XYSeries seriesY;
    private XYSeries seriesZ;
    private XYSeriesCollection dataset;
    private JFreeChart chart;

    public VistaHistorico(MainFrame mainFrame, ClientConnection clientConnection) {
        this.mainFrame = mainFrame;
        this.clientConnection = clientConnection;

        setLayout(new BorderLayout(15, 15));
        setBackground(StyleUtil.COLOR_BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- 1. Contenedor de Filtros (FIGURA 7) ---
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // --- 2. Contenedor Central del Gráfico (FIGURA 7) ---
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createLineBorder(StyleUtil.AZUL_UNISON.darker(), 2));
        chartContainer.setBackground(Color.WHITE);

        lblStatus = new JLabel("Presione 'Cargar Datos' para mostrar el histórico.", SwingConstants.CENTER);
        lblStatus.setFont(new Font(StyleUtil.FONT_NAME, Font.ITALIC, 16));
        lblStatus.setForeground(StyleUtil.AZUL_OSCURO_UNISON);

        chartContainer.add(lblStatus, BorderLayout.CENTER);
        add(chartContainer, BorderLayout.CENTER);

        // --- 3. Inferior: Botones ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setOpaque(false);

        btnLoadData = new JButton("Cargar Datos Históricos");
        StyleUtil.applyPrimaryStyle(btnLoadData);
        btnLoadData.addActionListener(e -> loadHistoricalData());

        JButton btnBack = new JButton("Volver a Inicio");
        StyleUtil.applyBaseStyle(btnBack);
        btnBack.setBackground(StyleUtil.AZUL_OSCURO_UNISON.darker());
        btnBack.addActionListener(e -> mainFrame.showCard(MainFrame.getCardInicio()));

        bottomPanel.add(btnLoadData);
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        initializeChartComponents();
    }

    /**
     * Crea la sección de filtros (Fecha/Hora).
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(StyleUtil.DORADO_UNISON.brighter()); // Color dorado simulado (FIGURA 7)
        panel.setBorder(BorderFactory.createLineBorder(StyleUtil.DORADO_OSCURO_UNISON, 1));

        JLabel lblTitle = new JLabel("Filtros de Búsqueda");
        lblTitle.setFont(new Font(StyleUtil.FONT_NAME, Font.BOLD, 16));

        // Requisitos: Incluir filtros por fecha y hora
        panel.add(lblTitle);
        panel.add(new JLabel("Fecha Inicio:"));
        JTextField txtDateStart = new JTextField(8);
        StyleUtil.applyBaseStyle(txtDateStart);
        panel.add(txtDateStart);

        panel.add(new JLabel("Hora Fin:"));
        JTextField txtTimeEnd = new JTextField(6);
        StyleUtil.applyBaseStyle(txtTimeEnd);
        panel.add(txtTimeEnd);

        return panel;
    }

    /**
     * Inicializa los componentes de la gráfica.
     */
    private void initializeChartComponents() {
        seriesX = new XYSeries("Eje X");
        seriesY = new XYSeries("Eje Y");
        seriesZ = new XYSeries("Eje Z");
        dataset = new XYSeriesCollection();
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);

        chart = ChartFactory.createXYLineChart(
                "Datos Históricos de Sensores",
                "Índice de Registro", "Valor",
                dataset, PlotOrientation.VERTICAL, true, false, false);

        chart.setBackgroundPaint(Color.WHITE);
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
    }

    /**
     * Carga los datos históricos del servidor en un Thread (requisito).
     */
    private void loadHistoricalData() {
        if (!clientConnection.isConnected()) {
            JOptionPane.showMessageDialog(this, "Debe estar conectado al servidor para cargar datos históricos.",
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnLoadData.setEnabled(false);
        chartContainer.removeAll();
        lblStatus.setText("Cargando datos desde la base de datos..."); // Mensaje de carga (requisito)
        chartContainer.add(lblStatus, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();

        // Cargar los datos desde un Thread (requisito)
        new Thread(() -> {
            try {
                // 1. Solicitar los datos al servidor (comunicación por Socket, datos encriptados)
                List<SensorData> dataList = clientConnection.requestHistoricalData();

                SwingUtilities.invokeLater(() -> {
                    if (!dataList.isEmpty()) {
                        // 2. Actualizar la gráfica con los nuevos datos
                        updateChart(dataList);
                        lblStatus.setText("Datos cargados: " + dataList.size() + " registros.");

                        ChartPanel chartPanel = new ChartPanel(chart);
                        chartContainer.removeAll();
                        chartContainer.add(chartPanel, BorderLayout.CENTER);
                    } else {
                        lblStatus.setText("No se encontraron datos históricos para mostrar.");
                        chartContainer.add(lblStatus, BorderLayout.CENTER);
                    }
                    chartContainer.revalidate();
                    chartContainer.repaint();
                    btnLoadData.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Error al cargar los datos: " + e.getMessage());
                    chartContainer.add(lblStatus, BorderLayout.CENTER);
                    chartContainer.revalidate();
                    chartContainer.repaint();
                    btnLoadData.setEnabled(true);
                });
            }
        }).start();
    }

    /**
     * Rellena las series de JFreeChart con la lista de datos históricos.
     */
    private void updateChart(List<SensorData> dataList) {
        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();

        // Usamos el índice como el eje X, ya que el gráfico de tiempo real ya usa el tiempo.
        for (int i = 0; i < dataList.size(); i++) {
            SensorData data = dataList.get(i);
            // Usamos el índice 'i' como el punto del eje X
            seriesX.add(i + 1, data.x);
            seriesY.add(i + 1, data.y);
            seriesZ.add(i + 1, data.z);
        }
    }
}
