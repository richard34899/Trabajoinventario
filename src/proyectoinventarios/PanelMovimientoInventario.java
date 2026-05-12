package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PanelMovimientoInventario extends JPanel {

    private final ControlandoInventario control = new ControlandoInventario();
    private final HashMap<String, String[]> productosPorClave = new HashMap<>();
    private final HashMap<String, Integer> numeroProductoPorClave = new HashMap<>();
    private final List<ControlandoInventario.DetalleMovimientoData> detallesPendientes = new ArrayList<>();

    private JComboBox<String> comboProducto;
    private JComboBox<String> comboTipo;
    private JTextField txtCantidad;
    private JTextField txtFecha;
    private JTextField txtNumeroMovimiento;
    private JTextField txtCodigoDetalle;
    private JTextArea txtMotivo;
    private JTable tablaDetalleMovimiento;
    private JTable tablaStockActual;
    private DefaultTableModel modeloDetalleMovimiento;
    private DefaultTableModel modeloStockActual;
    private JButton btnAgregar;
    private boolean actualizandoCodigo;
    private Runnable onMovimientoRegistrado;
    private String tipoNotaActual = "";
    private String motivoNotaActual = "";
    public PanelMovimientoInventario() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        initComponents();
        recargarDatos();
    }

    private void initComponents() {
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearPanelRegistro(), BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0, 51, 102));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Movimiento de inventario");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titulo);
        return panel;
    }

    private JPanel crearPanelRegistro() {
        JPanel card = crearCard();
        card.setLayout(new BorderLayout(14, 0));
        card.setPreferredSize(new Dimension(0, 560));

        JPanel acciones = new JPanel();
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.setOpaque(false);
        acciones.setPreferredSize(new Dimension(210, 0));

        JLabel tituloAcciones = new JLabel("Acciones");
        tituloAcciones.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tituloAcciones.setForeground(new Color(15, 23, 42));
        tituloAcciones.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnAgregar = crearBotonAccion("Agregar");
        JButton btnFinalizar = crearBotonAccion("Finalizar movimiento");
        JButton btnHistorial = crearBotonAccion("Historial");
        JButton btnStockActual = crearBotonAccion("Stock actual");
        JButton btnLimpiar = crearBotonAccion("Limpiar");
        btnAgregar.addActionListener(evt -> agregarDetalleMovimiento());
        btnFinalizar.addActionListener(evt -> finalizarMovimiento());
        btnHistorial.addActionListener(evt -> mostrarHistorial());
        btnStockActual.addActionListener(evt -> mostrarStockActual());
        btnLimpiar.addActionListener(evt -> confirmarLimpieza());

        acciones.add(tituloAcciones);
        acciones.add(Box.createRigidArea(new Dimension(0, 10)));
        acciones.add(btnFinalizar);
        acciones.add(Box.createRigidArea(new Dimension(0, 8)));
        acciones.add(btnHistorial);
        acciones.add(Box.createRigidArea(new Dimension(0, 8)));
        acciones.add(btnStockActual);
        acciones.add(Box.createRigidArea(new Dimension(0, 8)));
        acciones.add(btnLimpiar);
        acciones.add(Box.createVerticalGlue());

        comboProducto = new JComboBox<>();
        estilizarCombo(comboProducto);
        comboProducto.addActionListener(evt -> actualizarCodigoProducto());

        comboTipo = new JComboBox<>(new String[]{"Entrada", "Salida", "Ajuste"});
        estilizarCombo(comboTipo);

        txtCantidad = crearCampo();
        txtCantidad.setHorizontalAlignment(SwingConstants.RIGHT);
        txtCantidad.setText("1");

        txtFecha = crearCampo();
        txtFecha.setText(LocalDate.now().toString());

        txtNumeroMovimiento = crearCampo();
        txtNumeroMovimiento.setEditable(false);
        txtNumeroMovimiento.setHorizontalAlignment(SwingConstants.RIGHT);

        txtCodigoDetalle = crearCampo();
        txtCodigoDetalle.setHorizontalAlignment(SwingConstants.LEFT);
        txtCodigoDetalle.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                buscarProductoPorCodigo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                buscarProductoPorCodigo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                buscarProductoPorCodigo();
            }
        });

        txtMotivo = new JTextArea(1, 20);
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        txtMotivo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMotivo.setBackground(Color.WHITE);
        txtMotivo.setForeground(new Color(15, 23, 42));
        txtMotivo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane scrollMotivo = new JScrollPane(txtMotivo);
        scrollMotivo.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184)));
        scrollMotivo.setPreferredSize(new Dimension(220, 38));
        scrollMotivo.setMinimumSize(new Dimension(220, 38));
        estilizarScrollPane(scrollMotivo);

        configurarEnterCampos();

        JPanel documento = new JPanel(new BorderLayout(0, 12));
        documento.setOpaque(false);
        documento.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 18);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        agregarCampo(formulario, crearCampoPanel("Tipo", comboTipo), gbc, 0, 0);
        agregarCampo(formulario, crearCampoPanel("Fecha", txtFecha), gbc, 1, 0);
        agregarCampo(formulario, crearCampoPanel("Motivo", scrollMotivo), gbc, 2, 0);
        agregarCampo(formulario, crearCampoPanel("Codigo", txtCodigoDetalle), gbc, 0, 1);
        agregarCampo(formulario, crearCampoPanel("Cantidad", txtCantidad), gbc, 1, 1);
        agregarCampo(formulario, crearCampoPanel("No. movimiento", txtNumeroMovimiento), gbc, 2, 1);

        JPanel panelProducto = new JPanel(new BorderLayout(12, 0));
        panelProducto.setOpaque(false);
        panelProducto.add(comboProducto, BorderLayout.CENTER);

        JPanel contenedorBotonAgregar = new JPanel(new BorderLayout());
        contenedorBotonAgregar.setOpaque(false);
        contenedorBotonAgregar.setBorder(BorderFactory.createEmptyBorder(2, 14, 0, 0));
        contenedorBotonAgregar.add(btnAgregar, BorderLayout.CENTER);
        panelProducto.add(contenedorBotonAgregar, BorderLayout.EAST);

        GridBagConstraints gbcProducto = new GridBagConstraints();
        gbcProducto.gridx = 0;
        gbcProducto.gridy = 2;
        gbcProducto.gridwidth = 2;
        gbcProducto.weightx = 2.0;
        gbcProducto.fill = GridBagConstraints.BOTH;
        gbcProducto.insets = new Insets(0, 0, 12, 18);
        formulario.add(crearCampoPanel("Producto", panelProducto), gbcProducto);

        modeloDetalleMovimiento = new DefaultTableModel(new String[]{"Clave", "Cantidad", "Producto seleccionado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDetalleMovimiento = new JTable(modeloDetalleMovimiento);
        configurarTabla(tablaDetalleMovimiento);
        tablaDetalleMovimiento.getColumnModel().getColumn(0).setPreferredWidth(105);
        tablaDetalleMovimiento.getColumnModel().getColumn(0).setMaxWidth(125);
        tablaDetalleMovimiento.getColumnModel().getColumn(1).setPreferredWidth(145);
        tablaDetalleMovimiento.getColumnModel().getColumn(1).setMaxWidth(165);
        tablaDetalleMovimiento.getColumnModel().getColumn(2).setPreferredWidth(470);

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalleMovimiento);
        scrollDetalle.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        scrollDetalle.setPreferredSize(new Dimension(0, 340));
        scrollDetalle.setMinimumSize(new Dimension(0, 340));
        estilizarScrollPane(scrollDetalle);

        modeloStockActual = new DefaultTableModel(new String[]{"Clave", "Producto", "Stock", "Minimo", "Alerta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaStockActual = new JTable(modeloStockActual);
        configurarTabla(tablaStockActual);
        tablaStockActual.getColumnModel().getColumn(0).setPreferredWidth(82);
        tablaStockActual.getColumnModel().getColumn(0).setMaxWidth(96);
        tablaStockActual.getColumnModel().getColumn(2).setPreferredWidth(70);
        tablaStockActual.getColumnModel().getColumn(2).setMaxWidth(80);
        tablaStockActual.getColumnModel().getColumn(3).setPreferredWidth(75);
        tablaStockActual.getColumnModel().getColumn(3).setMaxWidth(90);
        tablaStockActual.getColumnModel().getColumn(4).setPreferredWidth(120);
        tablaStockActual.getColumnModel().getColumn(4).setMaxWidth(140);
        tablaStockActual.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String alerta = value == null ? "" : value.toString();
                    if ("Agotado".equalsIgnoreCase(alerta)) {
                        label.setForeground(new Color(185, 28, 28));
                    } else if ("Stock bajo".equalsIgnoreCase(alerta)) {
                        label.setForeground(new Color(180, 83, 9));
                    } else if ("Sobreinventario".equalsIgnoreCase(alerta)) {
                        label.setForeground(new Color(3, 105, 161));
                    } else {
                        label.setForeground(new Color(22, 101, 52));
                    }
                }
                return label;
            }
        });

        JPanel panelTablaNota = new JPanel(new BorderLayout(0, 8));
        panelTablaNota.setOpaque(false);
        panelTablaNota.add(crearTituloSeccion("Productos de la nota"), BorderLayout.NORTH);
        panelTablaNota.add(scrollDetalle, BorderLayout.CENTER);

        documento.add(formulario, BorderLayout.NORTH);
        documento.add(panelTablaNota, BorderLayout.CENTER);

        card.add(acciones, BorderLayout.WEST);
        card.add(documento, BorderLayout.CENTER);
        return card;
    }

    private void recargarDatos() {
        try {
            cargarProductos();
            cargarSiguienteNumeroMovimiento();
            limpiarFormulario();
        } catch (Exception e) {
            mostrarDialogo("Movimiento de inventario", "No fue posible cargar la informacion inicial.");
        }
    }

    private void cargarProductos() throws Exception {
        List<String[]> productos = control.leerProductos();
        productosPorClave.clear();
        numeroProductoPorClave.clear();
        comboProducto.removeAllItems();
        comboProducto.addItem("Seleccione un producto");

        int consecutivo = 1;
        for (String[] producto : productos) {
            productosPorClave.put(producto[0], producto);
            numeroProductoPorClave.put(producto[0], consecutivo++);
            comboProducto.addItem(producto[0] + " - " + producto[1]);
        }

        comboProducto.setSelectedIndex(0);
        actualizarCodigoProducto();
        cargarTablaStockActual(productos);
    }

    private boolean agregarDetalleMovimiento() {
        try {
            if (txtFecha.getText().trim().isEmpty()) {
                mostrarDialogo("Movimiento de inventario", "La fecha no puede ir vacia.");
                txtFecha.requestFocusInWindow();
                return false;
            }
            try {
                LocalDate.parse(txtFecha.getText().trim());
            } catch (DateTimeParseException ex) {
                mostrarDialogo("Movimiento de inventario", "La fecha debe estar en formato YYYY-MM-DD.");
                txtFecha.requestFocusInWindow();
                return false;
            }

            String codigoCapturado = txtCodigoDetalle.getText().trim();
            String cantidadCapturada = txtCantidad.getText().trim();
            if (codigoCapturado.isEmpty() || cantidadCapturada.isEmpty()) {
                mostrarDialogo("Movimiento de inventario", "Hay que llenar los campos vacios.");
                txtCodigoDetalle.requestFocusInWindow();
                txtCodigoDetalle.selectAll();
                return false;
            }

            String tipoMovimiento = obtenerTipoMovimientoSeleccionado();
            if (tipoMovimiento.isEmpty()) {
                mostrarDialogo("Movimiento de inventario", "Selecciona un tipo de movimiento.");
                comboTipo.requestFocusInWindow();
                return false;
            }
            String motivoCapturado = txtMotivo.getText().trim();
            if ("Ajuste".equalsIgnoreCase(tipoMovimiento) && motivoCapturado.isEmpty()) {
                mostrarDialogo("Movimiento de inventario", "No se permite ajuste sin comentario.");
                txtMotivo.requestFocusInWindow();
                return false;
            }

            String itemProducto = obtenerProductoSeleccionado();
            if (itemProducto.isBlank()) {
                mostrarDialogo("Movimiento de inventario", "Hay que llenar los campos vacios.");
                txtCodigoDetalle.requestFocusInWindow();
                txtCodigoDetalle.selectAll();
                return false;
            }

            int cantidad = Integer.parseInt(cantidadCapturada);
            if (cantidad < 0) {
                mostrarDialogo("Movimiento de inventario", "La cantidad no puede ser negativa.");
                txtCantidad.requestFocusInWindow();
                txtCantidad.selectAll();
                return false;
            }
            if (cantidad <= 0) {
                mostrarDialogo("Movimiento de inventario", "La cantidad debe ser mayor a 0.");
                txtCantidad.requestFocusInWindow();
                txtCantidad.selectAll();
                return false;
            }

            String claveProducto = itemProducto.split(" - ", 2)[0];
            String[] producto = productosPorClave.get(claveProducto);
            if (producto == null) {
                mostrarDialogo("Movimiento de inventario", "Selecciona un producto valido.");
                return false;
            }

            boolean productoActivo = Boolean.parseBoolean(producto[9]);
            if (("Entrada".equalsIgnoreCase(tipoMovimiento) || "Salida".equalsIgnoreCase(tipoMovimiento))
                    && !productoActivo) {
                mostrarDialogo("Movimiento de inventario", "No se permite entrada o salida para productos deshabilitados. Solo ajuste.");
                txtCodigoDetalle.requestFocusInWindow();
                txtCodigoDetalle.selectAll();
                return false;
            }

            if ("Salida".equalsIgnoreCase(tipoMovimiento)) {
                int stockActual = Integer.parseInt(producto[3]);
                int cantidadPendiente = 0;
                for (ControlandoInventario.DetalleMovimientoData detalle : detallesPendientes) {
                    if (detalle.getClaveProducto().equalsIgnoreCase(claveProducto)) {
                        cantidadPendiente = detalle.getCantidad();
                        break;
                    }
                }
                if ((cantidadPendiente + cantidad) > stockActual) {
                    mostrarDialogo("Movimiento de inventario", "No puede dar salida si el stock no es suficiente.");
                    txtCantidad.requestFocusInWindow();
                    txtCantidad.selectAll();
                    return false;
                }
            }

            boolean actualizado = false;
            for (int i = 0; i < detallesPendientes.size(); i++) {
                ControlandoInventario.DetalleMovimientoData detalle = detallesPendientes.get(i);
                if (detalle.getClaveProducto().equalsIgnoreCase(claveProducto)
                        && detalle.getTipoMovimiento().equalsIgnoreCase(tipoMovimiento)) {
                    int nuevaCantidad = "Ajuste".equalsIgnoreCase(tipoMovimiento)
                            ? cantidad
                            : detalle.getCantidad() + cantidad;
                    detallesPendientes.set(i, new ControlandoInventario.DetalleMovimientoData(
                            claveProducto,
                            producto[1],
                            nuevaCantidad,
                            tipoMovimiento,
                            motivoCapturado));
                    actualizado = true;
                    break;
                }
            }

            if (!actualizado) {
                detallesPendientes.add(new ControlandoInventario.DetalleMovimientoData(
                        claveProducto, producto[1], cantidad, tipoMovimiento, motivoCapturado));
            }

            if (detallesPendientes.size() == 1) {
                tipoNotaActual = tipoMovimiento;
                motivoNotaActual = motivoCapturado;
            }

            refrescarTablaDetalleConPendientes();
            limpiarDetallePendiente();
            return true;
        } catch (NumberFormatException ex) {
            mostrarDialogo("Movimiento de inventario", "La cantidad debe ser un numero valido.");
            txtCantidad.requestFocusInWindow();
            txtCantidad.selectAll();
            return false;
        }
    }

    private void finalizarMovimiento() {
        try {
            boolean confirmadoDesdeAdvertencia = false;
            String fechaMovimiento = txtFecha.getText().trim();
            String numeroMovimiento = txtNumeroMovimiento.getText().trim();

            if (detallesPendientes.isEmpty()) {
                mostrarDialogo("Movimiento de inventario", "Agrega al menos un producto al movimiento.");
                txtCodigoDetalle.requestFocusInWindow();
                txtCodigoDetalle.selectAll();
                return;
            }

            if (fechaMovimiento.isEmpty()) {
                mostrarDialogo("Movimiento de inventario", "La fecha no puede ir vacia.");
                txtFecha.requestFocusInWindow();
                return;
            }
            try {
                LocalDate.parse(fechaMovimiento);
            } catch (DateTimeParseException ex) {
                mostrarDialogo("Movimiento de inventario", "La fecha debe estar en formato YYYY-MM-DD.");
                txtFecha.requestFocusInWindow();
                return;
            }

            List<ControlandoInventario.DetalleMovimientoData> detallesARegistrar = new ArrayList<>(detallesPendientes);
            String tipoMovimiento = tipoNotaActual;
            String motivoMovimiento = motivoNotaActual;
            if (tipoMovimiento.isEmpty()) {
                mostrarDialogo("Movimiento de inventario", "Agrega al menos un producto al movimiento.");
                txtCodigoDetalle.requestFocusInWindow();
                return;
            }
            ControlandoInventario.ResultadoValidacion resultado = control.validarMovimientoCompleto(detallesARegistrar);
            if (!resultado.isValido()) {
                enfocarMovimiento(resultado.getCampo(), resultado.getMensaje());
                return;
            }

            if (resultado.isAdvertencia()) {
                int respuesta = mostrarConfirmacion("Advertencia", resultado.getMensaje() + "\nDesea continuar?");
                if (respuesta != JOptionPane.YES_OPTION) {
                    txtCantidad.requestFocusInWindow();
                    txtCantidad.selectAll();
                    return;
                }
                confirmadoDesdeAdvertencia = true;
            }

            control.aplicarMovimientoCompleto(
                    numeroMovimiento,
                    fechaMovimiento,
                    tipoMovimiento,
                    motivoMovimiento,
                    detallesARegistrar);

            cargarProductos();
            if (onMovimientoRegistrado != null) {
                onMovimientoRegistrado.run();
            }
            limpiarFormulario();
            cargarSiguienteNumeroMovimiento();
            if (!confirmadoDesdeAdvertencia) {
                mostrarDialogo("Movimiento de inventario", "Movimiento registrado correctamente.");
            }
        } catch (Exception ex) {
            mostrarDialogo("Movimiento de inventario", "No fue posible registrar el movimiento.");
        }
    }

    private List<ControlandoInventario.DetalleMovimientoData> construirDetallesParaRegistro() {
        List<ControlandoInventario.DetalleMovimientoData> detallesARegistrar = new ArrayList<>(detallesPendientes);
        String cantidadCapturada = txtCantidad.getText().trim();
        if (cantidadCapturada.isEmpty()) {
            return detallesARegistrar;
        }

        String itemProducto = obtenerProductoSeleccionado();
        String codigoCapturado = txtCodigoDetalle.getText().trim();
        if (itemProducto.isBlank()) {
            if (!detallesARegistrar.isEmpty() && codigoCapturado.isEmpty()) {
                return detallesARegistrar;
            }
            if (codigoCapturado.isEmpty()) {
                mostrarDialogo("Movimiento de inventario", "Agrega al menos un producto al movimiento.");
                txtCodigoDetalle.requestFocusInWindow();
                txtCodigoDetalle.selectAll();
                return null;
            }
            mostrarDialogo("Movimiento de inventario", "Selecciona un producto valido.");
            txtCodigoDetalle.requestFocusInWindow();
            txtCodigoDetalle.selectAll();
            return null;
        }

        try {
            int cantidad = Integer.parseInt(cantidadCapturada);
            if (cantidad < 0) {
                mostrarDialogo("Movimiento de inventario", "La cantidad no puede ser negativa.");
                txtCantidad.requestFocusInWindow();
                txtCantidad.selectAll();
                return null;
            }
            if (cantidad <= 0) {
                mostrarDialogo("Movimiento de inventario", "La cantidad debe ser mayor a 0.");
                txtCantidad.requestFocusInWindow();
                txtCantidad.selectAll();
                return null;
            }

            String claveProducto = itemProducto.split(" - ", 2)[0];
            String[] producto = productosPorClave.get(claveProducto);
            if (producto == null) {
                mostrarDialogo("Movimiento de inventario", "Selecciona un producto valido.");
                return null;
            }

            boolean actualizado = false;
            for (int i = 0; i < detallesARegistrar.size(); i++) {
                ControlandoInventario.DetalleMovimientoData detalle = detallesARegistrar.get(i);
                if (detalle.getClaveProducto().equalsIgnoreCase(claveProducto)
                        && detalle.getTipoMovimiento().equalsIgnoreCase(obtenerTipoMovimientoSeleccionado())) {
                    detallesARegistrar.set(i, new ControlandoInventario.DetalleMovimientoData(
                            claveProducto,
                            producto[1],
                            detalle.getCantidad() + cantidad,
                            obtenerTipoMovimientoSeleccionado(),
                            txtMotivo.getText().trim()));
                    actualizado = true;
                    break;
                }
            }

            if (!actualizado) {
                detallesARegistrar.add(new ControlandoInventario.DetalleMovimientoData(
                        claveProducto, producto[1], cantidad, obtenerTipoMovimientoSeleccionado(), txtMotivo.getText().trim()));
            }

            return detallesARegistrar;
        } catch (NumberFormatException ex) {
            mostrarDialogo("Movimiento de inventario", "La cantidad debe ser un numero valido.");
            txtCantidad.requestFocusInWindow();
            txtCantidad.selectAll();
            return null;
        }
    }

    private void refrescarTablaDetalleConPendientes() {
        modeloDetalleMovimiento.setRowCount(0);
        for (ControlandoInventario.DetalleMovimientoData detalle : detallesPendientes) {
            modeloDetalleMovimiento.addRow(new Object[]{
                detalle.getClaveProducto(),
                detalle.getCantidad(),
                detalle.getClaveProducto() + " - " + detalle.getNombreProducto()
            });
        }
    }

    private void limpiarDetallePendiente() {
        SwingUtilities.invokeLater(() -> {
            txtCantidad.setText("1");
            comboTipo.setSelectedIndex(0);
            txtMotivo.setText("");
            comboProducto.setSelectedIndex(0);
            actualizarCodigoProducto();
            txtMotivo.setCaretPosition(0);
            solicitarFocoInicial();
            revalidate();
            repaint();
        });
    }

    private void limpiarFormulario() {
        txtFecha.setText(LocalDate.now().toString());
        tipoNotaActual = "";
        motivoNotaActual = "";
        detallesPendientes.clear();
        modeloDetalleMovimiento.setRowCount(0);
        limpiarDetallePendiente();
    }

    private void cargarTablaStockActual(List<String[]> productos) {
        modeloStockActual.setRowCount(0);
        for (String[] producto : productos) {
            int stockActual = parseEnteroSeguro(producto[3]);
            int stockMinimo = parseEnteroSeguro(producto[4]);
            modeloStockActual.addRow(new Object[]{
                producto[0],
                producto[1],
                stockActual,
                stockMinimo,
                calcularIndicadorStock(stockActual, stockMinimo)
            });
        }
    }

    private int parseEnteroSeguro(String valor) {
        try {
            return Integer.parseInt(valor == null ? "0" : valor.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String calcularIndicadorStock(int stockActual, int stockMinimo) {
        if (stockActual == 0) {
            return "Agotado";
        }
        if (stockMinimo > stockActual) {
            return "Stock bajo";
        }
        if (stockMinimo > 0 && stockActual > (stockMinimo * 3)) {
            return "Sobreinventario";
        }
        return "Normal";
    }

    private void confirmarLimpieza() {
        int respuesta = mostrarConfirmacion("Advertencia", "Todo lo borrado no se guarda.\nDesea continuar?");
        if (respuesta == JOptionPane.YES_OPTION) {
            limpiarFormulario();
        }
    }

    public void solicitarFocoInicial() {
        SwingUtilities.invokeLater(() -> {
            txtCodigoDetalle.requestFocusInWindow();
            txtCodigoDetalle.selectAll();
        });
    }

    public void setOnMovimientoRegistrado(Runnable onMovimientoRegistrado) {
        this.onMovimientoRegistrado = onMovimientoRegistrado;
    }

    private void actualizarCodigoProducto() {
        if (actualizandoCodigo) {
            return;
        }
        String itemProducto = obtenerProductoSeleccionado();
        actualizandoCodigo = true;
        if (itemProducto.isBlank()) {
            txtCodigoDetalle.setText("");
            actualizandoCodigo = false;
            return;
        }

        String claveProducto = itemProducto.split(" - ", 2)[0];
        txtCodigoDetalle.setText(claveProducto);
        actualizandoCodigo = false;
    }

    private void buscarProductoPorCodigo() {
        if (actualizandoCodigo) {
            return;
        }

        String codigo = txtCodigoDetalle.getText().trim();
        if (codigo.isEmpty()) {
            actualizandoCodigo = true;
            comboProducto.setSelectedIndex(0);
            actualizandoCodigo = false;
            return;
        }

        int indiceCoincidencia = -1;
        String codigoMayus = codigo.toUpperCase();
        for (int i = 1; i < comboProducto.getItemCount(); i++) {
            String item = comboProducto.getItemAt(i);
            String clave = item.split(" - ", 2)[0];
            String claveMayus = clave.toUpperCase();
            if (claveMayus.equals(codigoMayus)) {
                indiceCoincidencia = i;
                break;
            }
            if (indiceCoincidencia == -1 && claveMayus.startsWith(codigoMayus)) {
                indiceCoincidencia = i;
            }
        }

        if (indiceCoincidencia >= 0 && comboProducto.getSelectedIndex() != indiceCoincidencia) {
            actualizandoCodigo = true;
            comboProducto.setSelectedIndex(indiceCoincidencia);
            actualizandoCodigo = false;
        }
    }

    private void mostrarHistorial() {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Historial", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);

        JPanel contenedor = new JPanel(new BorderLayout(0, 0));
        contenedor.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        contenedor.setBackground(Color.WHITE);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(0, 51, 102));
        barra.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblTitulo = new JLabel("Historial");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        barra.add(lblTitulo, BorderLayout.WEST);

        JTextField txtCodigoHistorial = crearCampo();
        txtCodigoHistorial.setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableModel modeloHistorialProducto = new DefaultTableModel(
                new String[]{"Fecha", "No. movimiento", "Tipo", "Cantidad", "Motivo", "Stock antes", "Stock despues"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaHistorialProducto = new JTable(modeloHistorialProducto);
        configurarTabla(tablaHistorialProducto);
        tablaHistorialProducto.getColumnModel().getColumn(0).setPreferredWidth(110);
        tablaHistorialProducto.getColumnModel().getColumn(1).setPreferredWidth(110);
        tablaHistorialProducto.getColumnModel().getColumn(1).setMaxWidth(130);
        tablaHistorialProducto.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaHistorialProducto.getColumnModel().getColumn(2).setMaxWidth(110);
        tablaHistorialProducto.getColumnModel().getColumn(3).setPreferredWidth(90);
        tablaHistorialProducto.getColumnModel().getColumn(3).setMaxWidth(100);
        tablaHistorialProducto.getColumnModel().getColumn(5).setPreferredWidth(95);
        tablaHistorialProducto.getColumnModel().getColumn(5).setMaxWidth(110);
        tablaHistorialProducto.getColumnModel().getColumn(6).setPreferredWidth(105);
        tablaHistorialProducto.getColumnModel().getColumn(6).setMaxWidth(120);

        JScrollPane scrollHistorial = new JScrollPane(tablaHistorialProducto);
        scrollHistorial.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        estilizarScrollPane(scrollHistorial);

        JPanel panelSuperior = new JPanel(new BorderLayout(0, 8));
        panelSuperior.setOpaque(false);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        panelSuperior.add(crearTituloSeccion("Codigo del producto"), BorderLayout.NORTH);
        panelSuperior.add(txtCodigoHistorial, BorderLayout.CENTER);

        JPanel panelTabla = new JPanel(new BorderLayout(0, 8));
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        panelTabla.add(crearTituloSeccion("Historial de movimientos"), BorderLayout.NORTH);
        panelTabla.add(scrollHistorial, BorderLayout.CENTER);

        txtCodigoHistorial.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                cargarHistorialPorCodigo(modeloHistorialProducto, txtCodigoHistorial.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                cargarHistorialPorCodigo(modeloHistorialProducto, txtCodigoHistorial.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                cargarHistorialPorCodigo(modeloHistorialProducto, txtCodigoHistorial.getText());
            }
        });

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFocusPainted(false);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setBackground(new Color(0, 51, 102));
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnCerrar.addActionListener(evt -> dialogo.dispose());

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(new Color(241, 245, 249));
        pie.setBorder(BorderFactory.createEmptyBorder(10, 16, 14, 16));
        pie.add(btnCerrar, BorderLayout.EAST);

        contenedor.add(barra, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 0));
        centro.setOpaque(false);
        centro.add(panelSuperior, BorderLayout.NORTH);
        centro.add(panelTabla, BorderLayout.CENTER);
        contenedor.add(centro, BorderLayout.CENTER);
        contenedor.add(pie, BorderLayout.SOUTH);

        dialogo.setContentPane(contenedor);
        dialogo.setSize(880, 620);
        dialogo.setLocationRelativeTo(this);
        SwingUtilities.invokeLater(() -> txtCodigoHistorial.requestFocusInWindow());
        dialogo.setVisible(true);
    }

    private void mostrarStockActual() {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Stock actual", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);

        JTable tablaStockDialogo = new JTable(modeloStockActual);
        configurarTabla(tablaStockDialogo);
        tablaStockDialogo.getColumnModel().getColumn(0).setPreferredWidth(95);
        tablaStockDialogo.getColumnModel().getColumn(0).setMaxWidth(110);
        tablaStockDialogo.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaStockDialogo.getColumnModel().getColumn(2).setMaxWidth(95);
        tablaStockDialogo.getColumnModel().getColumn(3).setPreferredWidth(85);
        tablaStockDialogo.getColumnModel().getColumn(3).setMaxWidth(100);
        tablaStockDialogo.getColumnModel().getColumn(4).setPreferredWidth(130);
        tablaStockDialogo.getColumnModel().getColumn(4).setMaxWidth(150);
        tablaStockDialogo.getColumnModel().getColumn(4).setCellRenderer(tablaStockActual.getColumnModel().getColumn(4).getCellRenderer());

        JScrollPane scrollStock = new JScrollPane(tablaStockDialogo);
        scrollStock.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        estilizarScrollPane(scrollStock);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(0, 51, 102));
        barra.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblTitulo = new JLabel("Stock actual de productos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        barra.add(lblTitulo, BorderLayout.WEST);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        centro.add(scrollStock, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFocusPainted(false);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setBackground(new Color(0, 51, 102));
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnCerrar.addActionListener(evt -> dialogo.dispose());

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(new Color(241, 245, 249));
        pie.setBorder(BorderFactory.createEmptyBorder(10, 16, 14, 16));
        pie.add(btnCerrar, BorderLayout.EAST);

        JPanel contenedor = new JPanel(new BorderLayout(0, 0));
        contenedor.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        contenedor.setBackground(Color.WHITE);
        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(centro, BorderLayout.CENTER);
        contenedor.add(pie, BorderLayout.SOUTH);

        dialogo.setContentPane(contenedor);
        dialogo.setSize(840, 560);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void cargarHistorialPorCodigo(DefaultTableModel modeloHistorialProducto, String codigoCapturado) {
        modeloHistorialProducto.setRowCount(0);

        String codigo = codigoCapturado == null ? "" : codigoCapturado.trim();
        if (codigo.isEmpty()) {
            return;
        }

        try {
            for (String[] movimiento : control.leerMovimientosPorPrefijoClave(codigo)) {
                modeloHistorialProducto.addRow(new Object[]{
                    movimiento[4],
                    movimiento[0],
                    movimiento[3],
                    movimiento[5],
                    movimiento[6],
                    movimiento[7],
                    movimiento[8]
                });
            }
        } catch (Exception ex) {
            modeloHistorialProducto.setRowCount(0);
        }
    }

    private JLabel crearTituloSeccion(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(15, 23, 42));
        return label;
    }

    private String obtenerProductoSeleccionado() {
        String itemProducto = comboProducto.getSelectedItem() != null ? comboProducto.getSelectedItem().toString() : "";
        if (itemProducto.isBlank() || "Seleccione un producto".equalsIgnoreCase(itemProducto)) {
            return "";
        }
        return itemProducto;
    }

    private String obtenerTipoMovimientoSeleccionado() {
        String tipoMovimiento = comboTipo.getSelectedItem() != null
                ? comboTipo.getSelectedItem().toString().trim() : "";
        if (tipoMovimiento.isEmpty() || "Seleccione un tipo".equalsIgnoreCase(tipoMovimiento)) {
            return "";
        }
        return tipoMovimiento;
    }

    private void enfocarMovimiento(String campo, String mensaje) {
        mostrarDialogo("Movimiento de inventario", mensaje);
        switch (campo) {
            case "producto" -> comboProducto.requestFocusInWindow();
            case "cantidad", "detalle" -> {
                txtCantidad.requestFocusInWindow();
                txtCantidad.selectAll();
            }
            case "motivo" -> txtMotivo.requestFocusInWindow();
            default -> txtCantidad.requestFocusInWindow();
        }
    }

    private void configurarTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(32);
        tabla.setGridColor(new Color(191, 219, 254));
        tabla.setSelectionBackground(new Color(219, 234, 254));
        tabla.setSelectionForeground(new Color(15, 23, 42));
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));
        tabla.setFillsViewportHeight(true);

        tabla.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(new Color(0, 51, 102));
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                return label;
            }
        });

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    label.setForeground(new Color(15, 23, 42));
                }
                label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return label;
            }
        });
    }

    private JPanel crearCampoPanel(String etiqueta, Component componente) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
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
        agregarCampo(contenedor, campo, base, x, y, 1);
    }

    private void agregarCampo(JPanel contenedor, JPanel campo, GridBagConstraints base, int x, int y, int width) {
        GridBagConstraints gbc = (GridBagConstraints) base.clone();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.weightx = width > 1 ? 2.0 : 1.0;
        if (x + width >= 3) {
            gbc.insets = new Insets(0, 0, 12, 0);
        }
        contenedor.add(campo, gbc);
    }

    private JTextField crearCampo() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184)),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        return campo;
    }

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(15, 23, 42));
        combo.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184)));
        combo.setMaximumRowCount(8);
        combo.setEditable(false);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton boton = new JButton("▾");
                boton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 13));
                boton.setForeground(new Color(0, 51, 102));
                boton.setBackground(Color.WHITE);
                boton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(219, 234, 254)),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                boton.setFocusPainted(false);
                return boton;
            }
        });
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                String texto = value == null ? "" : value.toString();
                label.setText(index == -1 ? limitarTextoVisual(texto, 30) : limitarTextoVisual(texto, 42));
                label.setToolTipText(texto);
                if (isSelected) {
                    label.setBackground(new Color(219, 234, 254));
                    label.setForeground(new Color(0, 51, 102));
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(15, 23, 42));
                }
                return label;
            }
        });
        SwingUtilities.invokeLater(() -> estilizarPopupCombo(combo));
    }

    private String limitarTextoVisual(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.trim();
        if (limpio.length() <= maximo) {
            return limpio;
        }
        return limpio.substring(0, Math.max(0, maximo - 3)) + "...";
    }

    private void estilizarPopupCombo(JComboBox<String> combo) {
        for (int i = 0; i < combo.getAccessibleContext().getAccessibleChildrenCount(); i++) {
            Object hijo = combo.getAccessibleContext().getAccessibleChild(i);
            if (hijo instanceof BasicComboPopup popup) {
                popup.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184)));
                for (Component componente : popup.getComponents()) {
                    if (componente instanceof JScrollPane scrollPopup) {
                        estilizarScrollPane(scrollPopup);
                    }
                }
                popup.getList().setFixedCellHeight(34);
                popup.getList().setSelectionBackground(new Color(219, 234, 254));
                popup.getList().setSelectionForeground(new Color(0, 51, 102));
            }
        }
    }

    private void estilizarScrollPane(JScrollPane scrollPane) {
        scrollPane.getViewport().setBackground(Color.WHITE);

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setPreferredSize(new Dimension(12, 12));
        vertical.setUnitIncrement(14);
        vertical.setOpaque(false);
        vertical.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(59, 130, 246);
                trackColor = new Color(226, 232, 240);
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(trackColor);
                g2.fillRoundRect(trackBounds.x + 2, trackBounds.y, trackBounds.width - 4, trackBounds.height, 10, 10);
                g2.dispose();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 1, thumbBounds.width - 4, thumbBounds.height - 2, 10, 10);
                g2.dispose();
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonScroll();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonScroll();
            }
        });

        JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
        if (horizontal != null) {
            horizontal.setPreferredSize(new Dimension(0, 0));
        }
    }

    private JButton crearBotonScroll() {
        JButton boton = new JButton();
        boton.setPreferredSize(new Dimension(0, 0));
        boton.setMinimumSize(new Dimension(0, 0));
        boton.setMaximumSize(new Dimension(0, 0));
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorder(BorderFactory.createEmptyBorder());
        boton.setFocusable(false);
        return boton;
    }

    private void cargarSiguienteNumeroMovimiento() {
        try {
            txtNumeroMovimiento.setText(control.generarNumeroMovimiento());
        } catch (Exception ex) {
            txtNumeroMovimiento.setText("1");
        }
    }

    private void configurarEnterCampos() {
        txtCodigoDetalle.addActionListener(evt -> {
            txtCantidad.requestFocusInWindow();
            txtCantidad.selectAll();
        });

        txtCantidad.addActionListener(evt -> btnAgregar.doClick());

        txtMotivo.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "irACodigo");
        txtMotivo.getActionMap().put("irACodigo", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtCodigoDetalle.requestFocusInWindow();
                txtCodigoDetalle.selectAll();
            }
        });
        comboProducto.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "irAAgregar");
        comboProducto.getActionMap().put("irAAgregar", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnAgregar.doClick();
            }
        });

        comboTipo.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "irAFecha");
        comboTipo.getActionMap().put("irAFecha", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtFecha.requestFocusInWindow();
                txtFecha.selectAll();
            }
        });

        txtFecha.addActionListener(evt -> {
            txtNumeroMovimiento.requestFocusInWindow();
            txtNumeroMovimiento.selectAll();
        });

        txtNumeroMovimiento.addActionListener(evt -> {
            txtMotivo.requestFocusInWindow();
            txtMotivo.selectAll();
        });
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

    private JPanel crearCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return panel;
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

    private int mostrarConfirmacion(String titulo, String mensaje) {
        final int[] respuesta = {JOptionPane.NO_OPTION};
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

        JButton btnNo = crearBotonDialogo("No", false);
        JButton btnSi = crearBotonDialogo("Si", true);
        btnNo.addActionListener(evt -> {
            respuesta[0] = JOptionPane.NO_OPTION;
            dialogo.dispose();
        });
        btnSi.addActionListener(evt -> {
            respuesta[0] = JOptionPane.YES_OPTION;
            dialogo.dispose();
        });

        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.X_AXIS));
        acciones.add(btnNo);
        acciones.add(Box.createRigidArea(new Dimension(10, 0)));
        acciones.add(btnSi);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(new Color(241, 245, 249));
        pie.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        pie.add(acciones, BorderLayout.EAST);

        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(lblMensaje, BorderLayout.CENTER);
        contenedor.add(pie, BorderLayout.SOUTH);

        dialogo.setContentPane(contenedor);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.getRootPane().setDefaultButton(btnSi);
        dialogo.setVisible(true);
        return respuesta[0];
    }

    private JButton crearBotonDialogo(String texto, boolean primario) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if (primario) {
            boton.setForeground(Color.WHITE);
            boton.setBackground(new Color(0, 51, 102));
            boton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        } else {
            boton.setForeground(new Color(0, 51, 102));
            boton.setBackground(Color.WHITE);
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 51, 102), 1),
                    BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        }
        return boton;
    }
}
