package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class PanelAnalisisInventario extends JPanel {

    private final ControlandoInventario control = new ControlandoInventario();
    private final DecimalFormat formatoDecimal = new DecimalFormat("#,##0.00");
    private final Map<String, String> categorias = new HashMap<>();
    private final List<String[]> analisisActual = new ArrayList<>();

    private JLabel lblConsumoTotal;
    private JLabel lblEstadoConsumo;
    private JTextField txtBuscar;
    private JComboBox<String> comboCategoria;
    private JTable tablaAnalisis;
    private JTable tablaDetalle;
    private DefaultTableModel modeloAnalisis;
    private DefaultTableModel modeloDetalle;
    private TableRowSorter<DefaultTableModel> sorterAnalisis;

    public PanelAnalisisInventario() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        cargarCategorias();
        initComponents();
        recargarDatos();
    }

    private void initComponents() {
        add(crearEncabezado(), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setOpaque(false);
        contenido.add(crearPanelResumen(), BorderLayout.NORTH);
        contenido.add(crearPanelTablas(), BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0, 51, 102));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Analisis de inventario");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titulo);
        return panel;
    }

    private JPanel crearPanelResumen() {
        JPanel contenedor = crearCard();
        contenedor.setLayout(new BorderLayout(18, 0));
        contenedor.setPreferredSize(new Dimension(0, 122));

        JPanel parametros = new JPanel();
        parametros.setLayout(new BoxLayout(parametros, BoxLayout.Y_AXIS));
        parametros.setOpaque(false);
        parametros.setPreferredSize(new Dimension(340, 0));

        JLabel titulo = new JLabel("Parametros");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(15, 23, 42));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblConsumoTotal = new JLabel("$0.00");
        lblConsumoTotal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblConsumoTotal.setForeground(new Color(0, 51, 102));
        lblConsumoTotal.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Consumo total del inventario");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(71, 85, 105));
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblEstadoConsumo = new JLabel(" ");
        lblEstadoConsumo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstadoConsumo.setForeground(new Color(180, 83, 9));
        lblEstadoConsumo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblEstadoConsumo.setVisible(false);

        parametros.add(titulo);
        parametros.add(Box.createRigidArea(new Dimension(0, 10)));
        parametros.add(lblConsumoTotal);
        parametros.add(Box.createRigidArea(new Dimension(0, 6)));
        parametros.add(subtitulo);
        parametros.add(Box.createRigidArea(new Dimension(0, 6)));
        parametros.add(lblEstadoConsumo);
        parametros.add(Box.createVerticalGlue());

        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 16);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtBuscar = crearCampo();
        comboCategoria = new JComboBox<>();
        estilizarCombo(comboCategoria);
        comboCategoria.addItem("Todas las categorias");
        for (String categoria : categorias.values().stream().sorted().toList()) {
            comboCategoria.addItem(categoria);
        }

        agregarCampo(filtros, crearCampoPanel("Buscar por codigo o nombre", txtBuscar), gbc, 0, 0);
        agregarCampo(filtros, crearCampoPanel("Filtrar por categoria", comboCategoria), gbc, 1, 0);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltros();
            }
        });

        comboCategoria.addActionListener(evt -> aplicarFiltros());

        contenedor.add(parametros, BorderLayout.WEST);
        contenedor.add(filtros, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearPanelTablas() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 18));
        contenedor.setOpaque(false);

        modeloAnalisis = new DefaultTableModel(new String[]{
            "Grupo", "Codigo", "Nombre", "Categoria", "Precio", "Stock actual", "Punto de reorden", "EQQ", "Indicadores"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaAnalisis = new JTable(modeloAnalisis);
        configurarTabla(tablaAnalisis);
        tablaAnalisis.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaAnalisis.getColumnModel().getColumn(0).setMaxWidth(80);
        tablaAnalisis.getColumnModel().getColumn(1).setPreferredWidth(95);
        tablaAnalisis.getColumnModel().getColumn(1).setMaxWidth(110);
        tablaAnalisis.getColumnModel().getColumn(4).setPreferredWidth(95);
        tablaAnalisis.getColumnModel().getColumn(4).setMaxWidth(110);
        tablaAnalisis.getColumnModel().getColumn(5).setPreferredWidth(95);
        tablaAnalisis.getColumnModel().getColumn(5).setMaxWidth(105);
        tablaAnalisis.getColumnModel().getColumn(6).setPreferredWidth(125);
        tablaAnalisis.getColumnModel().getColumn(6).setMaxWidth(145);
        tablaAnalisis.getColumnModel().getColumn(7).setPreferredWidth(95);
        tablaAnalisis.getColumnModel().getColumn(7).setMaxWidth(110);

        sorterAnalisis = new TableRowSorter<>(modeloAnalisis);
        tablaAnalisis.setRowSorter(sorterAnalisis);
        tablaAnalisis.getSelectionModel().addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                cargarDetalleConsumo();
            }
        });

        JScrollPane scrollAnalisis = new JScrollPane(tablaAnalisis);
        scrollAnalisis.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        estilizarScrollPane(scrollAnalisis);

        JPanel panelAnalisis = crearCard();
        panelAnalisis.setLayout(new BorderLayout(0, 8));
        panelAnalisis.add(crearTituloSeccion("Productos y analisis ABC"), BorderLayout.NORTH);
        panelAnalisis.add(scrollAnalisis, BorderLayout.CENTER);

        modeloDetalle = new DefaultTableModel(new String[]{
            "Fecha", "No. movimiento", "Cantidad", "Motivo", "Stock antes", "Stock despues"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDetalle = new JTable(modeloDetalle);
        configurarTabla(tablaDetalle);
        tablaDetalle.getColumnModel().getColumn(0).setPreferredWidth(110);
        tablaDetalle.getColumnModel().getColumn(0).setMaxWidth(125);
        tablaDetalle.getColumnModel().getColumn(1).setPreferredWidth(105);
        tablaDetalle.getColumnModel().getColumn(1).setMaxWidth(120);
        tablaDetalle.getColumnModel().getColumn(2).setPreferredWidth(85);
        tablaDetalle.getColumnModel().getColumn(2).setMaxWidth(95);
        tablaDetalle.getColumnModel().getColumn(4).setPreferredWidth(95);
        tablaDetalle.getColumnModel().getColumn(4).setMaxWidth(110);
        tablaDetalle.getColumnModel().getColumn(5).setPreferredWidth(105);
        tablaDetalle.getColumnModel().getColumn(5).setMaxWidth(120);

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        scrollDetalle.setPreferredSize(new Dimension(0, 210));
        estilizarScrollPane(scrollDetalle);

        JPanel panelDetalle = crearCard();
        panelDetalle.setLayout(new BorderLayout(0, 8));
        panelDetalle.add(crearTituloSeccion("Detalle de consumo (solo salidas)"), BorderLayout.NORTH);
        panelDetalle.add(scrollDetalle, BorderLayout.CENTER);

        contenedor.add(panelAnalisis, BorderLayout.CENTER);
        contenedor.add(panelDetalle, BorderLayout.SOUTH);
        return contenedor;
    }

    public void recargarDatos() {
        try {
            cargarAnalisis();
        } catch (Exception ex) {
            lblConsumoTotal.setText("$0.00");
            lblEstadoConsumo.setText("No fue posible cargar el analisis.");
            modeloAnalisis.setRowCount(0);
            modeloDetalle.setRowCount(0);
        }
    }

    public void solicitarFocoEnBuscador() {
        SwingUtilities.invokeLater(() -> {
            if (txtBuscar != null) {
                txtBuscar.requestFocusInWindow();
                txtBuscar.selectAll();
            }
        });
    }

    private void cargarAnalisis() throws Exception {
        List<String[]> productos = control.leerProductos();
        List<String[]> movimientos = control.leerMovimientos();
        ControlandoInventario.ParametrosAnalisis parametros = control.leerParametrosAnalisis();

        Map<String, Double> consumoPorProducto = new HashMap<>();
        double consumoTotal = 0.0;

        for (String[] movimiento : movimientos) {
            if (movimiento.length < 9) {
                continue;
            }
            if (!"Salida".equalsIgnoreCase(movimiento[3])) {
                continue;
            }
            String clave = movimiento[1];
            String[] producto = buscarProducto(productos, clave);
            if (producto == null) {
                continue;
            }
            int cantidad = parseEntero(movimiento[5]);
            double costoActual = parseDecimal(producto[6]);
            double consumoProducto = cantidad * costoActual;
            consumoPorProducto.put(clave, consumoPorProducto.getOrDefault(clave, 0.0) + consumoProducto);
            consumoTotal += consumoProducto;
        }

        lblConsumoTotal.setText("$" + formatoDecimal.format(consumoTotal));
        if (consumoTotal <= 0.0) {
            lblEstadoConsumo.setText("Sin consumos registrados: la clasificacion ABC se muestra como '-' hasta que existan salidas.");
            lblEstadoConsumo.setVisible(true);
        } else {
            lblEstadoConsumo.setText(" ");
            lblEstadoConsumo.setVisible(false);
        }

        Map<String, String> grupoABC = calcularClasificacionABC(productos, consumoPorProducto, consumoTotal);

        analisisActual.clear();
        modeloAnalisis.setRowCount(0);
        for (String[] producto : productos) {
            String clave = producto[0];
            String nombre = producto[1];
            String categoria = traducirCategoria(producto[2]);
            double precio = parseDecimal(producto[5]);
            int stockActual = parseEntero(producto[3]);
            int stockMinimo = parseEntero(producto[4]);
            int demandaAnual = parseEntero(producto[8]);
            double puntoReorden = calcularPuntoReorden(demandaAnual, parametros.getDiasEntregaGlobal());
            double eqq = calcularEqq(demandaAnual, parametros.getCostoPedido(), parametros.getH());
            String indicadores = construirIndicadores(stockActual, stockMinimo, puntoReorden);
            String grupo = consumoTotal <= 0.0 ? "-" : grupoABC.getOrDefault(clave, "C");

            String[] fila = new String[]{
                grupo,
                clave,
                nombre,
                categoria,
                formatoDecimal.format(precio),
                String.valueOf(stockActual),
                formatoDecimal.format(puntoReorden),
                formatoDecimal.format(eqq),
                indicadores
            };
            analisisActual.add(fila);
            modeloAnalisis.addRow(fila);
        }

        aplicarFiltros();
        if (tablaAnalisis.getRowCount() > 0) {
            tablaAnalisis.setRowSelectionInterval(0, 0);
            cargarDetalleConsumo();
        } else {
            modeloDetalle.setRowCount(0);
        }
    }

    private Map<String, String> calcularClasificacionABC(List<String[]> productos, Map<String, Double> consumoPorProducto, double consumoTotal) {
        Map<String, String> clasificacion = new HashMap<>();
        if (consumoTotal <= 0.0) {
            return clasificacion;
        }

        List<String[]> productosOrdenados = new ArrayList<>(productos);
        productosOrdenados.sort(Comparator.comparingDouble((String[] p) -> consumoPorProducto.getOrDefault(p[0], 0.0)).reversed());

        double acumulado = 0.0;
        for (String[] producto : productosOrdenados) {
            String clave = producto[0];
            double consumoProducto = consumoPorProducto.getOrDefault(clave, 0.0);
            acumulado += consumoProducto / consumoTotal;
            if (acumulado < 0.80) {
                clasificacion.put(clave, "A");
            } else if (acumulado < 0.95) {
                clasificacion.put(clave, "B");
            } else {
                clasificacion.put(clave, "C");
            }
        }
        return clasificacion;
    }

    private void aplicarFiltros() {
        if (sorterAnalisis == null) {
            return;
        }
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        String categoria = comboCategoria.getSelectedItem() == null ? "Todas las categorias" : comboCategoria.getSelectedItem().toString();

        sorterAnalisis.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String codigo = entry.getStringValue(1).toLowerCase(Locale.ROOT);
                String nombre = entry.getStringValue(2).toLowerCase(Locale.ROOT);
                String categoriaFila = entry.getStringValue(3);

                boolean coincideTexto = texto.isEmpty() || codigo.contains(texto) || nombre.contains(texto);
                boolean coincideCategoria = "Todas las categorias".equalsIgnoreCase(categoria) || categoria.equalsIgnoreCase(categoriaFila);
                return coincideTexto && coincideCategoria;
            }
        });

        if (tablaAnalisis.getRowCount() > 0) {
            tablaAnalisis.setRowSelectionInterval(0, 0);
            cargarDetalleConsumo();
        } else {
            modeloDetalle.setRowCount(0);
        }
    }

    private void cargarDetalleConsumo() {
        modeloDetalle.setRowCount(0);
        int filaVista = tablaAnalisis.getSelectedRow();
        if (filaVista < 0) {
            return;
        }
        int filaModelo = tablaAnalisis.convertRowIndexToModel(filaVista);
        String clave = modeloAnalisis.getValueAt(filaModelo, 1).toString();

        try {
            List<String[]> movimientos = control.leerMovimientosPorClave(clave);
            for (String[] movimiento : movimientos) {
                if (!"Salida".equalsIgnoreCase(movimiento[3])) {
                    continue;
                }
                modeloDetalle.addRow(new Object[]{
                    movimiento[4],
                    movimiento[0],
                    movimiento[5],
                    movimiento[6],
                    movimiento[7],
                    movimiento[8]
                });
            }
        } catch (Exception ex) {
            modeloDetalle.setRowCount(0);
        }
    }

    private double calcularPuntoReorden(int demandaAnual, int entregaDiasGlobal) {
        double demandaDiaria = demandaAnual / 365.0;
        return demandaDiaria * Math.max(1, entregaDiasGlobal);
    }

    private double calcularEqq(int demandaAnual, double costoPedidoGlobal, double hGlobal) {
        double numerador = 2.0 * Math.max(0, demandaAnual) * Math.max(0.0, costoPedidoGlobal);
        double denominador = Math.max(1.0, hGlobal);
        return Math.sqrt(numerador / denominador);
    }

    private String construirIndicadores(int stockActual, int stockMinimo, double puntoReorden) {
        Set<String> indicadores = new HashSet<>();
        if (stockActual < stockMinimo) {
            indicadores.add("Stock bajo");
        }
        if (stockMinimo > 0 && stockActual > (stockMinimo * 3)) {
            indicadores.add("Sobreinventario");
        }
        if (puntoReorden >= stockActual) {
            indicadores.add("Punto de reorden");
        }
        if (indicadores.isEmpty()) {
            return "Normal";
        }
        return String.join(" | ", indicadores);
    }

    private String[] buscarProducto(List<String[]> productos, String clave) {
        for (String[] producto : productos) {
            if (producto[0].equalsIgnoreCase(clave)) {
                return producto;
            }
        }
        return null;
    }

    private int parseEntero(String valor) {
        try {
            return Integer.parseInt(valor == null ? "0" : valor.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private double parseDecimal(String valor) {
        try {
            return Double.parseDouble(valor == null ? "0" : valor.trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private String traducirCategoria(String codigo) {
        return categorias.getOrDefault(codigo, codigo);
    }

    private void cargarCategorias() {
        File archivo = new File("electronica.csv");
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", 2);
                if (datos.length >= 2) {
                    categorias.put(datos[0].trim(), datos[1].trim());
                }
            }
        } catch (Exception ex) {
            categorias.clear();
        }
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
            gbc.insets = new Insets(0, 0, 12, 0);
        }
        contenedor.add(campo, gbc);
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

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(15, 23, 42));
        combo.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184)));
        combo.setMaximumRowCount(8);
        combo.setEditable(false);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected javax.swing.JButton createArrowButton() {
                javax.swing.JButton boton = new javax.swing.JButton("v");
                boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 12));
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(59, 130, 246);
                trackColor = new Color(226, 232, 240);
            }

            @Override
            protected javax.swing.JButton createDecreaseButton(int orientation) {
                return crearBotonScroll();
            }

            @Override
            protected javax.swing.JButton createIncreaseButton(int orientation) {
                return crearBotonScroll();
            }

            private javax.swing.JButton crearBotonScroll() {
                javax.swing.JButton boton = new javax.swing.JButton();
                boton.setPreferredSize(new Dimension(0, 0));
                boton.setMinimumSize(new Dimension(0, 0));
                boton.setMaximumSize(new Dimension(0, 0));
                return boton;
            }
        });
    }
}
