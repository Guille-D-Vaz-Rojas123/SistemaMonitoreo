package MX.unison;

import javax.swing.*;
import java.awt.*;

/**
 * Contenedor principal de la aplicación Cliente.
 * Utiliza CardLayout para gestionar las 3 vistas.
 * Implementa la conexión y gestión de Sockets compartida.
 */
public class MainFrame extends JFrame {

    private static final String CARD_INICIO = "Inicio";
    private static final String CARD_MONITOR = "Monitor";
    private static final String CARD_HISTORICO = "Histórico";

    private final JPanel cards; // Panel que usa CardLayout
    private final CardLayout cardLayout = new CardLayout();

    // Instancias de comunicación y simulación
    private final ClientConnection clientConnection;

    public MainFrame() {
        super("Sistema de Monitoreo");

        // 1. Inicializar la conexión con el servidor
        this.clientConnection = new ClientConnection();
        boolean connected = this.clientConnection.connect();

        // Notificar el estado de la conexión
        String statusMessage = connected ? "Conexión con el servidor OK." : "ERROR: No se pudo conectar al servidor.";
        JOptionPane.showMessageDialog(this, statusMessage, "Estado de Conexión",
                connected ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        // 2. Configuración del JFrame principal
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600); // Tamaño base
        setLocationRelativeTo(null); // Centrar en pantalla

        cards = new JPanel(cardLayout);

        // 3. Crear las vistas
        VistaInicio vistaInicio = new VistaInicio(this);
        VistaMonitor vistaMonitor = new VistaMonitor(this, clientConnection);
        VistaHistorico vistaHistorico = new VistaHistorico(this, clientConnection);

        // 4. Añadir las vistas al CardLayout
        cards.add(vistaInicio, CARD_INICIO);
        cards.add(vistaMonitor, CARD_MONITOR);
        cards.add(vistaHistorico, CARD_HISTORICO);

        add(cards);

        // Asegurar que el socket se cierre al salir de la aplicación
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                clientConnection.disconnect();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    /**
     * Método para cambiar entre las vistas.
     * @param cardName Nombre de la tarjeta (CARD_INICIO, CARD_MONITOR, CARD_HISTORICO).
     */
    public void showCard(String cardName) {
        cardLayout.show(cards, cardName);
    }

    // Constantes públicas para las tarjetas
    public static String getCardInicio() { return CARD_INICIO; }
    public static String getCardMonitor() { return CARD_MONITOR; }
    public static String getCardHistorico() { return CARD_HISTORICO; }

    public static void main(String[] args) {
        // Asegurar que la inicialización de la GUI se ejecute en el Event Dispatch Thread
        SwingUtilities.invokeLater(MainFrame::new);
    }
}