package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class PanelConfiguracion extends JPanel {

    private final ConfiguracionService configuracionService = new ConfiguracionService();

    private JTextField txtCostoPedido;
    private JTextField txtCostoMantenimiento;
    private JTextField txtTiempoEntrega;
    private JButton btnRegistrar;
    private JButton btnEditar;
    private JButton btnGuardar;
    private Runnable onConfiguracionGuardada;
    private boolean modoEdicion;

    public PanelConfiguracion() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        initComponents();
        recargarDatos();
    }

    private void initComponents() {
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearPanelContenido(), BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0, 51, 102));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Configuracion");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titulo);
        return panel;
    }

    private JPanel crearPanelContenido() {
        JPanel card = crearCard();
        card.setLayout(new BorderLayout(14, 0));
        card.setPreferredSize(new Dimension(0, 360));

        JPanel acciones = new JPanel();
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.setOpaque(false);
        acciones.setPreferredSize(new Dimension(220, 0));

        JLabel tituloAcciones = new JLabel("Acciones");
        tituloAcciones.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tituloAcciones.setForeground(new Color(15, 23, 42));
        tituloAcciones.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnRegistrar = crearBotonAccion("Registrar");
        btnEditar = crearBotonAccion("Editar");
        btnGuardar = crearBotonAccion("Guardar");
        btnRegistrar.addActionListener(evt -> registrarConfiguracion());
        btnEditar.addActionListener(evt -> activarEdicion());
        btnGuardar.addActionListener(evt -> guardarEdicion());

        acciones.add(tituloAcciones);
        acciones.add(Box.createRigidArea(new Dimension(0, 10)));
        acciones.add(btnRegistrar);
        acciones.add(Box.createRigidArea(new Dimension(0, 8)));
        acciones.add(btnEditar);
        acciones.add(Box.createRigidArea(new Dimension(0, 8)));
        acciones.add(btnGuardar);
        acciones.add(Box.createVerticalGlue());

        txtCostoPedido = crearCampo();
        txtCostoMantenimiento = crearCampo();
        txtTiempoEntrega = crearCampo();
        configurarEnterCampos();

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 14, 18);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        agregarCampo(formulario, crearCampoPanel("Costo por pedido", txtCostoPedido), gbc, 0, 0);
        agregarCampo(formulario, crearCampoPanel("Costo de mantenimiento", txtCostoMantenimiento), gbc, 1, 0);
        agregarCampo(formulario, crearCampoPanel("Tiempo de entrega", txtTiempoEntrega), gbc, 0, 1);

        JPanel contenedor = new JPanel(new BorderLayout(0, 10));
        contenedor.setOpaque(false);
        contenedor.add(crearTituloSeccion("Registro global del inventario"), BorderLayout.NORTH);
        contenedor.add(formulario, BorderLayout.CENTER);

        card.add(acciones, BorderLayout.WEST);
        card.add(contenor(contenedor), BorderLayout.CENTER);
        return card;
    }

    private JPanel contenor(JPanel contenido) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(contenido, BorderLayout.NORTH);
        return panel;
    }

    private void configurarEnterCampos() {
        txtCostoPedido.addActionListener(evt -> {
            txtCostoMantenimiento.requestFocusInWindow();
            txtCostoMantenimiento.selectAll();
        });
        txtCostoMantenimiento.addActionListener(evt -> {
            txtTiempoEntrega.requestFocusInWindow();
            txtTiempoEntrega.selectAll();
        });
        txtTiempoEntrega.addActionListener(evt -> {
            if (modoEdicion) {
                btnGuardar.doClick();
            } else {
                btnRegistrar.doClick();
            }
        });
    }

    public void recargarDatos() {
        try {
            ConfiguracionService.EstadoConfiguracion estado = configuracionService.cargarEstado();
            if (!estado.isHayDatos()) {
                limpiarCampos();
                habilitarRegistro();
            } else {
                aplicarValores(estado.getCostoPedido(), estado.getCostoMantenimiento(), estado.getTiempoEntrega());
                bloquearCampos();
            }
        } catch (Exception ex) {
            limpiarCampos();
            habilitarRegistro();
            mostrarDialogo("Configuracion", "No fue posible cargar los parametros.");
        }
    }

    public void solicitarFocoInicial() {
        SwingUtilities.invokeLater(() -> {
            if (modoEdicion) {
                txtCostoPedido.requestFocusInWindow();
                txtCostoPedido.selectAll();
            } else if (btnEditar != null) {
                btnEditar.requestFocusInWindow();
            }
        });
    }

    public void setOnConfiguracionGuardada(Runnable onConfiguracionGuardada) {
        this.onConfiguracionGuardada = onConfiguracionGuardada;
    }

    private void activarEdicion() {
        modoEdicion = true;
        txtCostoPedido.setEditable(true);
        txtCostoMantenimiento.setEditable(true);
        txtTiempoEntrega.setEditable(true);
        btnRegistrar.setEnabled(false);
        btnEditar.setEnabled(false);
        btnGuardar.setEnabled(true);
        txtCostoPedido.requestFocusInWindow();
        txtCostoPedido.selectAll();
    }

    private void bloquearCampos() {
        modoEdicion = false;
        txtCostoPedido.setEditable(false);
        txtCostoMantenimiento.setEditable(false);
        txtTiempoEntrega.setEditable(false);
        btnRegistrar.setEnabled(false);
        btnEditar.setEnabled(true);
        btnGuardar.setEnabled(false);
    }

    private void habilitarRegistro() {
        modoEdicion = false;
        txtCostoPedido.setEditable(true);
        txtCostoMantenimiento.setEditable(true);
        txtTiempoEntrega.setEditable(true);
        btnRegistrar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnGuardar.setEnabled(false);
    }

    private void registrarConfiguracion() {
        guardarConfiguracion(true);
    }

    private void guardarEdicion() {
        guardarConfiguracion(false);
    }

    private void guardarConfiguracion(boolean esRegistroNuevo) {
        ConfiguracionService.ResultadoConfiguracion resultado = configuracionService.guardar(
                new ConfiguracionService.ConfiguracionFormularioData(
                        textoLimpio(txtCostoPedido),
                        textoLimpio(txtCostoMantenimiento),
                        textoLimpio(txtTiempoEntrega)),
                esRegistroNuevo);

        if (!resultado.isValido()) {
            mostrarDialogo("Configuracion", resultado.getMensaje());
            enfocarCampoConfiguracion(resultado.getCampo());
            return;
        }

        aplicarValores(
                resultado.getCostoPedidoFormateado(),
                resultado.getCostoMantenimientoFormateado(),
                resultado.getTiempoEntregaFormateado());
        bloquearCampos();

        if (onConfiguracionGuardada != null) {
            onConfiguracionGuardada.run();
        }

        mostrarDialogo("Configuracion", resultado.getMensaje());
        solicitarFocoInicial();
    }

    private String textoLimpio(JTextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void limpiarCampos() {
        aplicarValores("", "", "");
    }

    private void aplicarValores(String costoPedido, String costoMantenimiento, String tiempoEntrega) {
        txtCostoPedido.setText(costoPedido);
        txtCostoMantenimiento.setText(costoMantenimiento);
        txtTiempoEntrega.setText(tiempoEntrega);
    }

    private void enfocarCampoConfiguracion(String campo) {
        JTextField objetivo = switch (campo == null ? "" : campo) {
            case "costoMantenimiento" -> txtCostoMantenimiento;
            case "tiempoEntrega" -> txtTiempoEntrega;
            case "general" -> txtCostoPedido;
            default -> txtCostoPedido;
        };
        objetivo.requestFocusInWindow();
        objetivo.selectAll();
    }

    private JPanel crearCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    private JLabel crearTituloSeccion(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(15, 23, 42));
        return label;
    }

    private JTextField crearCampo() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184)),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        return campo;
    }

    private JPanel crearCampoPanel(String etiqueta, Component componente) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        label.setPreferredSize(new Dimension(220, 22));

        if (componente instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            jc.setPreferredSize(new Dimension(220, 38));
            jc.setMinimumSize(new Dimension(220, 38));
            jc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        }

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(componente);
        return panel;
    }

    private void agregarCampo(JPanel contenedor, JPanel campo, GridBagConstraints base, int x, int y) {
        GridBagConstraints gbc = (GridBagConstraints) base.clone();
        gbc.gridx = x;
        gbc.gridy = y;
        if (x >= 1) {
            gbc.insets = new Insets(0, 0, 14, 0);
        }
        contenedor.add(campo, gbc);
    }

    private JButton crearBotonAccion(String texto) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setHorizontalAlignment(JLabel.LEFT);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(0, 51, 102));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return boton;
    }

    private void mostrarDialogo(String titulo, String mensaje) {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        contenedor.setBackground(Color.WHITE);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(0, 51, 102));
        barra.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        barra.add(lblTitulo, BorderLayout.WEST);

        JLabel lblMensaje = new JLabel("<html><body style='width:300px;font-family:Segoe UI;font-size:11px;color:#0f172a;line-height:1.4;'>"
                + mensaje.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                + "</body></html>");
        lblMensaje.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        JButton btnOk = new JButton("OK");
        btnOk.setFocusPainted(false);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.setForeground(Color.WHITE);
        btnOk.setBackground(new Color(0, 51, 102));
        btnOk.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnOk.addActionListener(evt -> dialogo.dispose());

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(new Color(241, 245, 249));
        pie.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        pie.add(btnOk, BorderLayout.EAST);

        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(lblMensaje, BorderLayout.CENTER);
        contenedor.add(pie, BorderLayout.SOUTH);

        dialogo.setContentPane(contenedor);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.getRootPane().setDefaultButton(btnOk);
        dialogo.setVisible(true);
    }
}
