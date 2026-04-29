package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Catalogo extends JFrame {

    private final PanelCatalogo panelCatalogo;

    public Catalogo() {
        this(null);
    }

    public Catalogo(String[] datosEdicion) {
        panelCatalogo = new PanelCatalogo();
        configurarVentana();
        construirInterfaz();
        if (datosEdicion != null) {
            panelCatalogo.cargarProductoEnFormulario(datosEdicion);
        } else {
            panelCatalogo.solicitarFocoEnClave();
        }
    }

    private void configurarVentana() {
        setTitle("Catalogo de productos");
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

    public void mostrarRegistroNuevo() {
        panelCatalogo.limpiarFormularioPublico();
        panelCatalogo.solicitarFocoEnClave();
    }

    public static void main(String args[]) {
        InicioDashboard.aplicarLookAndFeel();
        java.awt.EventQueue.invokeLater(() -> new Catalogo().setVisible(true));
    }
}
