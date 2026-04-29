package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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

        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);
        mostrarInicio();
    }

    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(270, 0));
        sidebar.setBackground(new Color(15, 23, 42));
        sidebar.setBorder(BorderFactory.createEmptyBorder(26, 20, 26, 20));

        JLabel titulo = new JLabel("Sistema de Inventario");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(titulo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 28)));

        sidebar.add(crearBotonMenu("Inicio / Dashboard", true, this::mostrarInicio));
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(crearBotonMenu("Catalogo de productos", true, this::mostrarCatalogo));
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(crearBotonMenu("Movimiento de inventario", false, null));
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(crearBotonMenu("Analisis de inventario", false, null));
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(crearBotonMenu("Reportes", false, null));
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(crearBotonMenu("Configuracion", false, null));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(crearBotonMenu("Salir", true, this::cerrarPrograma));
        return sidebar;
    }

    private JButton crearBotonMenu(String texto, boolean habilitado, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        boton.setPreferredSize(new Dimension(220, 48));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setCursor(habilitado ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        boton.setEnabled(habilitado);

        if (habilitado) {
            boton.setBackground(new Color(30, 41, 59));
            boton.setForeground(Color.WHITE);
            boton.addActionListener(evt -> accion.run());
        } else {
            boton.setBackground(new Color(51, 65, 85));
            boton.setForeground(new Color(148, 163, 184));
        }

        return boton;
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
            panelCatalogo.solicitarFocoEnClave();
        }
    }

    public void abrirCatalogo() {
        mostrarCatalogo();
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
