package MX.unison;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Vista inicial del programa Cliente.
 * Contiene el logo, título y botones para navegar a Monitor e Histórico.
 */
public class VistaInicio extends JPanel {

    private final MainFrame mainFrame;

    public VistaInicio(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(StyleUtil.COLOR_BG_LIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        // Centralizar el componente dentro de su celda (horizontal y vertical)
        gbc.anchor = GridBagConstraints.CENTER;

        // --- 1. Logo de la Universidad de Sonora ---
        JLabel logoLabel = createLogoLabel();
        add(logoLabel, gbc);

        // --- 2. Título del Sistema ---
        gbc.gridy++;
        JLabel titleLabel = new JLabel("SISTEMA DE MONITOREO", SwingConstants.CENTER);
        titleLabel.setFont(new Font(StyleUtil.FONT_NAME, Font.BOLD, 28));
        titleLabel.setForeground(StyleUtil.AZUL_OSCURO_UNISON);
        add(titleLabel, gbc);

        // --- 3. Nombre del Autor ---
        gbc.gridy++;
        JLabel authorLabel = new JLabel("Autor: Guillermo Vazquez Rojas", SwingConstants.CENTER);
        authorLabel.setFont(new Font(StyleUtil.FONT_NAME, Font.PLAIN, 16));
        authorLabel.setForeground(StyleUtil.COLOR_TEXT_DARK);
        add(authorLabel, gbc);

        // --- 4. Contenedor de Botones (Monitor y Histórico) ---
        gbc.gridy++;
        gbc.insets = new Insets(40, 15, 15, 15); // Más espacio arriba de los botones
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, gbc);
    }

    /**
     * Crea el JLabel para el logo, implementando una búsqueda exhaustiva de recursos.
     */
    private JLabel createLogoLabel() {
        JLabel logoLabel = new JLabel();

        // Rutas comunes donde el IDE podría colocar el recurso dentro del classpath/JAR
        String[] possiblePaths = {
                "/resources/unison_logo.gif", // Ruta absoluta esperada (la más común)
                "resources/unison_logo.gif",  // Ruta relativa al paquete/ClassLoader
                "/unison_logo.gif"            // Si el IDE o Maven copian el contenido de 'resources' a la raíz
        };

        URL imageUrl = null;

        // Intentar todas las rutas hasta que una funcione
        for (String path : possiblePaths) {
            // Usamos getClass().getResource(path) ya que puede manejar rutas absolutas y relativas
            imageUrl = getClass().getResource(path);
            if (imageUrl != null) {
                System.out.println("Éxito: Logo encontrado en la ruta: " + path);
                break; // Detener en el primer intento exitoso
            }
        }

        if (imageUrl != null) {
            try {
                ImageIcon icon = new ImageIcon(imageUrl);
                // Escalar la imagen a un tamaño razonable
                Image image = icon.getImage();
                Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaledImage));
                logoLabel.setText(null);
                // Asegurar que el contenido del JLabel esté centrado si hay espacio extra
                logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            } catch (Exception e) {
                System.err.println("Error al procesar la imagen del logo: " + e.getMessage());
                createPlaceholder(logoLabel);
            }
        } else {
            // Si después de todos los intentos, la imagen no se encuentra
            System.err.println("¡CRÍTICO! El logo unison_logo.gif no se encontró en ninguna de las rutas de recursos comunes. Asegúrese de que el archivo existe y que la carpeta 'resources' está marcada como Resource Root.");
            createPlaceholder(logoLabel);
        }

        return logoLabel;
    }

    /**
     * Configura un placeholder si la imagen del logo no se encuentra.
     */
    private void createPlaceholder(JLabel logoLabel) {
        logoLabel.setText("LOGO UNISON");
        logoLabel.setFont(new Font(StyleUtil.FONT_NAME, Font.BOLD, 20));
        logoLabel.setPreferredSize(new Dimension(150, 150));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setVerticalAlignment(SwingConstants.CENTER);
        // Simular la forma circular con un borde más llamativo
        logoLabel.setBorder(BorderFactory.createLineBorder(StyleUtil.AZUL_UNISON, 3));
        logoLabel.setForeground(StyleUtil.AZUL_OSCURO_UNISON);
    }

    /**
     * Crea el panel que contiene los botones Monitor e Histórico.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        panel.setOpaque(false); // Transparente para usar el fondo del padre

        JButton btnMonitor = new JButton("Monitor");
        StyleUtil.applyMonitorStyle(btnMonitor);
        btnMonitor.addActionListener(e -> mainFrame.showCard(MainFrame.getCardMonitor()));

        JButton btnHistorico = new JButton("Histórico");
        StyleUtil.applyHistoricalStyle(btnHistorico);
        btnHistorico.addActionListener(e -> mainFrame.showCard(MainFrame.getCardHistorico()));

        panel.add(btnMonitor);
        panel.add(btnHistorico);
        return panel;
    }
}