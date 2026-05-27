package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class InicioDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private PanelCatalogo panelCatalogo;
    private PanelMovimientoInventario panelMovimientoInventario;
    private PanelAnalisisInventario panelAnalisisInventario;
    private PanelConfiguracion panelConfiguracion;

    public InicioDashboard() {
        configurarVentana();
        initComponents();
        setLocationRelativeTo(null);
    }

    private void configurarVentana() {
        setTitle("Proyecto Inventarios");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(1360, 860);
        setMinimumSize(new Dimension(1240, 760));
        setUndecorated(true);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(241, 245, 249));

        JPanel sidebar = crearSidebar();
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        contentPanel.add(crearPanelInicio(), "inicio");
        panelCatalogo = new PanelCatalogo();
        contentPanel.add(panelCatalogo, "catalogo");
        panelMovimientoInventario = new PanelMovimientoInventario();
        panelMovimientoInventario.setOnMovimientoRegistrado(() -> {
            if (panelCatalogo != null) {
                panelCatalogo.recargarProductosDesdeArchivo();
            }
            if (panelAnalisisInventario != null) {
                panelAnalisisInventario.recargarDatos();
            }
        });
        contentPanel.add(panelMovimientoInventario, "movimientos");
        panelAnalisisInventario = new PanelAnalisisInventario();
        contentPanel.add(panelAnalisisInventario, "analisis");
        panelConfiguracion = new PanelConfiguracion();
        panelConfiguracion.setOnConfiguracionGuardada(() -> {
            if (panelAnalisisInventario != null) {
                panelAnalisisInventario.recargarDatos();
            }
        });
        contentPanel.add(panelConfiguracion, "configuracion");

        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);
        mostrarInicio();
    }

    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(304, 0));
        sidebar.setBackground(new Color(187, 195, 206));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 22, 22, 22));

        sidebar.add(crearEncabezadoSimple());
        sidebar.add(Box.createRigidArea(new Dimension(0, 22)));

        JPanel bloqueMenu = new JPanel(new GridLayout(0, 1, 0, 10));
        bloqueMenu.setOpaque(false);
        bloqueMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloqueMenu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        bloqueMenu.add(crearBotonMenu("Catalogo de productos", true, this::mostrarCatalogo));
        bloqueMenu.add(crearBotonMenu("Movimiento de inventario", true, this::mostrarMovimientoInventario));
        bloqueMenu.add(crearBotonMenu("Analisis de inventario", true, this::mostrarAnalisisInventario));
        bloqueMenu.add(crearBotonMenu("Configuracion", true, this::mostrarConfiguracion));
        sidebar.add(bloqueMenu);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(crearPanelSalida());
        return sidebar;
    }

    private JPanel crearEncabezadoSimple() {
        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);
        encabezado.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("<html>Sistema de<br>Inventario</html>");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 25));
        titulo.setForeground(new Color(0, 51, 102));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel linea = new JPanel();
        linea.setBackground(new Color(59, 130, 246));
        linea.setMaximumSize(new Dimension(88, 4));
        linea.setPreferredSize(new Dimension(88, 4));
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);

        encabezado.add(titulo);
        encabezado.add(Box.createRigidArea(new Dimension(0, 14)));
        encabezado.add(linea);
        return encabezado;
    }

    private JButton crearBotonMenu(String texto, boolean habilitado, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        boton.setPreferredSize(new Dimension(252, 50));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setCursor(habilitado ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(habilitado ? new Color(71, 85, 105) : new Color(173, 181, 191)),
                BorderFactory.createEmptyBorder(12, 16, 12, 12)));
        boton.setMargin(new Insets(12, 16, 12, 12));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setEnabled(habilitado);

        if (habilitado) {
            boton.setBackground(new Color(23, 35, 58));
            boton.setForeground(Color.WHITE);
            boton.addActionListener(evt -> accion.run());
        } else {
            boton.setBackground(new Color(227, 232, 239));
            boton.setForeground(new Color(120, 136, 158));
        }

        return boton;
    }

    private JPanel crearPanelSalida() {
        JPanel panelSalida = new JPanel();
        panelSalida.setLayout(new BoxLayout(panelSalida, BoxLayout.Y_AXIS));
        panelSalida.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSalida.setOpaque(false);
        panelSalida.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        panelSalida.add(crearBotonMenu("Salir", true, this::cerrarPrograma));
        return panelSalida;
    }

    private JPanel crearPanelInicio() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        return panel;
    }

    private void mostrarInicio() {
        cardLayout.show(contentPanel, "inicio");
    }

    private void mostrarCatalogo() {
        cardLayout.show(contentPanel, "catalogo");
        if (panelCatalogo != null) {
            panelCatalogo.recargarProductosDesdeArchivo();
            panelCatalogo.solicitarFocoEnClave();
        }
    }

    public void abrirCatalogo() {
        mostrarCatalogo();
    }

    private void mostrarMovimientoInventario() {
        cardLayout.show(contentPanel, "movimientos");
        if (panelMovimientoInventario != null) {
            panelMovimientoInventario.solicitarFocoInicial();
        }
    }

    private void mostrarAnalisisInventario() {
        cardLayout.show(contentPanel, "analisis");
        if (panelAnalisisInventario != null) {
            panelAnalisisInventario.recargarDatos();
            panelAnalisisInventario.solicitarFocoEnBuscador();
        }
    }

    private void mostrarConfiguracion() {
        cardLayout.show(contentPanel, "configuracion");
        if (panelConfiguracion != null) {
            panelConfiguracion.recargarDatos();
            panelConfiguracion.solicitarFocoInicial();
        }
    }

    private void cerrarPrograma() {
        dispose();
        System.exit(0);
    }

    public static void aplicarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(InicioDashboard.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
