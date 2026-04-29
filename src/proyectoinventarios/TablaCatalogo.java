package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TablaCatalogo extends JFrame {

    private final PanelCatalogo panelCatalogo;

    public TablaCatalogo() {
        panelCatalogo = new PanelCatalogo();
        configurarVentana();
        construirInterfaz();
    }

    private void configurarVentana() {
        setTitle("Tabla de productos");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1240, 760));
        setSize(1320, 820);
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(new Color(241, 245, 249));
        contenedor.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        contenedor.add(panelCatalogo, BorderLayout.CENTER);
        setContentPane(contenedor);
    }

    public void mostrarEdicion(String[] datosEdicion) {
        panelCatalogo.cargarProductoEnFormulario(datosEdicion);
    }

    public static void main(String args[]) {
        InicioDashboard.aplicarLookAndFeel();
        java.awt.EventQueue.invokeLater(() -> new TablaCatalogo().setVisible(true));
    }
}
