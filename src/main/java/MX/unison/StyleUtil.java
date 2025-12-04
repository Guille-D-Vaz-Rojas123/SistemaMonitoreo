package MX.unison;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.EmptyBorder;

/**
 * Clase de utilidad para mantener la coherencia de estilos y colores de la Unison.
 */
public class StyleUtil {

    // Colores de la Universidad de Sonora
    public static final Color AZUL_UNISON = Color.decode("#00529e");
    public static final Color AZUL_OSCURO_UNISON = Color.decode("#015294");
    public static final Color DORADO_UNISON = Color.decode("#f8bb00");
    public static final Color DORADO_OSCURO_UNISON = Color.decode("#d99e30");

    // Colores de la GUI (para Monitor y Histórico)
    public static final Color COLOR_BG_LIGHT = Color.decode("#F4F7F9");
    public static final Color COLOR_TEXT_DARK = Color.decode("#333333");

    // Fuente obligatoria
    public static final String FONT_NAME = "Segoe UI";

    // Radio de borde redondeado obligatorio
    private static final int BORDER_RADIUS = 4;

    /**
     * Aplica el estilo base a botones y campos de texto (Fuente Segoe UI y redondeo).
     */
    public static void applyBaseStyle(JButton button) {
        button.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        // Usar un UI personalizado para manejar el redondeo
        button.setUI(new RoundedButtonUI());
        // Añadir relleno interno
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
    }

    public static void applyBaseStyle(JTextField textField) {
        textField.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        textField.setBorder(new RoundedBorder(AZUL_UNISON, 1, BORDER_RADIUS));
        textField.putClientProperty("JComponent.roundRect", true);
        textField.setBorder(new EmptyBorder(5, 10, 5, 10)); // Añadir padding interno
    }

    /**
     * Aplica el color Azul Unison al botón principal.
     */
    public static void applyPrimaryStyle(JButton button) {
        applyBaseStyle(button);
        button.setBackground(AZUL_UNISON);
        button.setForeground(Color.WHITE);
    }

    /**
     * Aplica el estilo para el botón "Monitor" (Verde simulado de la figura).
     */
    public static void applyMonitorStyle(JButton button) {
        applyBaseStyle(button);
        button.setBackground(Color.decode("#90EE90")); // Verde claro simulado
        button.setForeground(COLOR_TEXT_DARK);
    }

    /**
     * Aplica el estilo para el botón "Histórico" (Dorado simulado de la figura).
     */
    public static void applyHistoricalStyle(JButton button) {
        applyBaseStyle(button);
        button.setBackground(DORADO_UNISON);
        button.setForeground(COLOR_TEXT_DARK);
    }

    // --- Clases internas para soporte de redondeo ---

    /**
     * Borde redondeado personalizado para JTextFields.
     */
    private static class RoundedBorder implements javax.swing.border.Border {
        private final Color color;
        private final int thickness;
        private final int radius;

        RoundedBorder(Color c, int t, int r) {
            color = c;
            thickness = t;
            radius = r;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(radius + 1, radius + 1, radius + 2, radius);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    /**
     * Custom UI para JButton que soporta el redondeo.
     */
    private static class RoundedButtonUI extends BasicButtonUI {
        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton button = (AbstractButton) c;
            button.setOpaque(false);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            paintBackground(g, b, b.getModel().isRollover());
            super.paint(g, c);
        }

        private void paintBackground(Graphics g, JComponent c, boolean isRollover) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color color = c.getBackground();
            if (isRollover) {
                // Un ligero oscurecimiento al pasar el ratón
                color = color.darker();
            }

            g2.setColor(color);
            g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), BORDER_RADIUS, BORDER_RADIUS);
            g2.dispose();
        }
    }
}