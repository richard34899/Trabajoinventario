package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class PanelConfiguracion extends JPanel {

    private final ControlandoInventario control = new ControlandoInventario();
    private final DecimalFormat formatoMoneda = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private JTextField txtCostoPedido;
    private JTextField txtCostoMantenimiento;
    private JTextField txtTiempoEntrega;
    private JButton btnRegistrar;
    private JButton btnEditar;
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
        btnRegistrar.addActionListener(evt -> guardarConfiguracion());
        btnEditar.addActionListener(evt -> activarEdicion());

        acciones.add(tituloAcciones);
        acciones.add(Box.createRigidArea(new Dimension(0, 10)));
        acciones.add(btnRegistrar);
        acciones.add(Box.createRigidArea(new Dimension(0, 8)));
        acciones.add(btnEditar);
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
        txtTiempoEntrega.addActionListener(evt -> btnRegistrar.doClick());
    }

    public void recargarDatos() {
        try {
            ControlandoInventario.ParametrosAnalisis parametros = control.leerParametrosAnalisis();
            txtCostoPedido.setText(formatoMoneda.format(parametros.getCostoPedido()));
            txtCostoMantenimiento.setText(formatoMoneda.format(parametros.getH()));
            txtTiempoEntrega.setText(String.valueOf(parametros.getDiasEntregaGlobal()));
        } catch (Exception ex) {
            txtCostoPedido.setText("100.00");
            txtCostoMantenimiento.setText("10.00");
            txtTiempoEntrega.setText("1");
            mostrarDialogo("Configuracion", "No fue posible cargar los parametros.");
        }
        bloquearCampos();
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
        txtCostoPedido.requestFocusInWindow();
        txtCostoPedido.selectAll();
    }

    private void bloquearCampos() {
        modoEdicion = false;
        txtCostoPedido.setEditable(false);
        txtCostoMantenimiento.setEditable(false);
        txtTiempoEntrega.setEditable(false);
    }

    private void guardarConfiguracion() {
        try {
            String costoPedidoTexto = textoLimpio(txtCostoPedido);
            String costoMantenimientoTexto = textoLimpio(txtCostoMantenimiento);
            String tiempoEntregaTexto = textoLimpio(txtTiempoEntrega);

            if (costoPedidoTexto.isEmpty() || costoMantenimientoTexto.isEmpty() || tiempoEntregaTexto.isEmpty()) {
                mostrarDialogo("Configuracion", "Hay que llenar todos los campos.");
                txtCostoPedido.requestFocusInWindow();
                txtCostoPedido.selectAll();
                return;
            }

            double costoPedido = Double.parseDouble(costoPedidoTexto);
            if (costoPedido <= 0) {
                mostrarDialogo("Configuracion", "El costo por pedido debe ser numerico y mayor que 0.");
                txtCostoPedido.requestFocusInWindow();
                txtCostoPedido.selectAll();
                return;
            }

            double costoMantenimiento = Double.parseDouble(costoMantenimientoTexto);
            if (costoMantenimiento <= 0) {
                mostrarDialogo("Configuracion", "El costo de mantenimiento debe ser numerico y mayor que 0.");
                txtCostoMantenimiento.requestFocusInWindow();
                txtCostoMantenimiento.selectAll();
                return;
            }

            if (tiempoEntregaTexto.contains(".")) {
                mostrarDialogo("Configuracion", "El tiempo de entrega no debe permitir valores decimales.");
                txtTiempoEntrega.requestFocusInWindow();
                txtTiempoEntrega.selectAll();
                return;
            }

            int tiempoEntrega = Integer.parseInt(tiempoEntregaTexto);
            if (tiempoEntrega <= 0) {
                mostrarDialogo("Configuracion", "El tiempo de entrega debe ser numerico y mayor que 0.");
                txtTiempoEntrega.requestFocusInWindow();
                txtTiempoEntrega.selectAll();
                return;
            }

            control.guardarParametrosAnalisis(costoPedido, costoMantenimiento, tiempoEntrega);
            txtCostoPedido.setText(formatoMoneda.format(costoPedido));
            txtCostoMantenimiento.setText(formatoMoneda.format(costoMantenimiento));
            txtTiempoEntrega.setText(String.valueOf(tiempoEntrega));
            bloquearCampos();

            if (onConfiguracionGuardada != null) {
                onConfiguracionGuardada.run();
            }

            mostrarDialogo("Configuracion", "Los parametros se guardaron correctamente.");
            solicitarFocoInicial();
        } catch (NumberFormatException ex) {
            mostrarDialogo("Configuracion", "Los valores de datos deben ser numericos y mayores que 0.");
            txtCostoPedido.requestFocusInWindow();
            txtCostoPedido.selectAll();
        } catch (Exception ex) {
            mostrarDialogo("Configuracion", "No fue posible guardar los parametros.");
        }
    }

    private String textoLimpio(JTextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
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
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}
