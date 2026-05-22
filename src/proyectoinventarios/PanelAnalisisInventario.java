package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Rectangle;
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
import javax.swing.JButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

public class PanelAnalisisInventario extends JPanel {

    private final ControlandoInventario control = new ControlandoInventario();
    private final DecimalFormat formatoDecimal = new DecimalFormat("#,##0.00");
    private final Map<String, String> categorias = new HashMap<>();
    private final List<String[]> analisisActual = new ArrayList<>();

    private JLabel lblConsumoTotal;
    private JLabel lblEstadoConsumo;
    private JTextField txtBuscar;
    private JComboBox<String> comboCategoria;
    private JButton btnGrafica;
    private JTable tablaAnalisis;
    private JTable tablaDetalle;
    private DefaultTableModel modeloAnalisis;
    private DefaultTableModel modeloDetalle;
    private TableRowSorter<DefaultTableModel> sorterAnalisis;
    private String mensajeDetalle = "";

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
        btnGrafica = crearBotonAccion("Grafica");
        comboCategoria = new JComboBox<>();
        estilizarCombo(comboCategoria);
        comboCategoria.addItem("Todas las categorias");
        for (String categoria : categorias.values().stream().sorted().toList()) {
            comboCategoria.addItem(categoria);
        }

        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);
        panelBusqueda.add(btnGrafica, BorderLayout.EAST);

        agregarCampo(filtros, crearCampoPanel("Buscar por codigo o nombre", panelBusqueda), gbc, 0, 0);
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
        btnGrafica.addActionListener(evt -> mostrarGraficaGrupos());

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
        tablaAnalisis.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaAnalisis.getTableHeader().setReorderingAllowed(false);
        tablaAnalisis.getTableHeader().setResizingAllowed(false);
        tablaAnalisis.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaAnalisis.getColumnModel().getColumn(1).setPreferredWidth(95);
        tablaAnalisis.getColumnModel().getColumn(2).setPreferredWidth(220);
        tablaAnalisis.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaAnalisis.getColumnModel().getColumn(4).setPreferredWidth(95);
        tablaAnalisis.getColumnModel().getColumn(5).setPreferredWidth(105);
        tablaAnalisis.getColumnModel().getColumn(6).setPreferredWidth(135);
        tablaAnalisis.getColumnModel().getColumn(7).setPreferredWidth(95);
        tablaAnalisis.getColumnModel().getColumn(8).setPreferredWidth(180);

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
        panelAnalisis.add(crearTituloSeccion("Productos"), BorderLayout.NORTH);
        panelAnalisis.add(scrollAnalisis, BorderLayout.CENTER);

        modeloDetalle = new DefaultTableModel(new String[]{
            "Fecha", "No. movimiento", "Cantidad", "Tipo de movimiento", "Stock antes", "Stock despues"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDetalle = new JTable(modeloDetalle) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0 && mensajeDetalle != null && !mensajeDetalle.isBlank()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int ancho = Math.min(getWidth() - 40, 360);
                    int alto = 42;
                    int x = Math.max(20, (getWidth() - ancho) / 2);
                    int y = Math.max(16, (getHeight() - alto) / 2);

                    g2.setColor(new Color(255, 247, 237));
                    g2.fillRoundRect(x, y, ancho, alto, 14, 14);
                    g2.setColor(new Color(253, 230, 138));
                    g2.drawRoundRect(x, y, ancho, alto, 14, 14);
                    g2.setColor(new Color(146, 64, 14));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int textoX = x + 12;
                    int textoY = y + ((alto - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(mensajeDetalle, textoX, textoY);
                    g2.dispose();
                }
            }
        };
        configurarTabla(tablaDetalle);
        tablaDetalle.getTableHeader().setReorderingAllowed(false);
        tablaDetalle.getTableHeader().setResizingAllowed(false);
        tablaDetalle.getColumnModel().getColumn(0).setPreferredWidth(135);
        tablaDetalle.getColumnModel().getColumn(1).setPreferredWidth(145);
        tablaDetalle.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaDetalle.getColumnModel().getColumn(3).setPreferredWidth(185);
        tablaDetalle.getColumnModel().getColumn(4).setPreferredWidth(130);
        tablaDetalle.getColumnModel().getColumn(5).setPreferredWidth(140);

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        scrollDetalle.setPreferredSize(new Dimension(0, 210));
        estilizarScrollPane(scrollDetalle);

        JPanel panelDetalle = crearCard();
        panelDetalle.setLayout(new BorderLayout(0, 8));
        panelDetalle.add(crearTituloSeccion("Detalle de consumo"), BorderLayout.NORTH);
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
            ocultarEstadoDetalle();
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
            double acumuladoPrevio = acumulado;
            if (acumuladoPrevio < 0.80) {
                clasificacion.put(clave, "A");
            } else if (acumuladoPrevio < 0.95) {
                clasificacion.put(clave, "B");
            } else {
                clasificacion.put(clave, "C");
            }
            acumulado += consumoProducto / consumoTotal;
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

                boolean coincideTexto = texto.isEmpty()
                        || codigo.startsWith(texto)
                        || nombre.startsWith(texto);
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
        ocultarEstadoDetalle();
        int filaVista = tablaAnalisis.getSelectedRow();
        if (filaVista < 0) {
            return;
        }
        int filaModelo = tablaAnalisis.convertRowIndexToModel(filaVista);
        String clave = modeloAnalisis.getValueAt(filaModelo, 1).toString();

        try {
            List<String[]> movimientos = control.leerMovimientosPorClave(clave);
            boolean haySalidas = false;
            for (String[] movimiento : movimientos) {
                if (!"Salida".equalsIgnoreCase(movimiento[3])) {
                    continue;
                }
                haySalidas = true;
                modeloDetalle.addRow(new Object[]{
                    movimiento[4],
                    movimiento[0],
                    movimiento[5],
                    movimiento[3],
                    movimiento[7],
                    movimiento[8]
                });
            }
            if (!haySalidas) {
                mostrarEstadoDetalle("Este producto no tiene salidas registradas.");
            }
        } catch (Exception ex) {
            modeloDetalle.setRowCount(0);
            mostrarEstadoDetalle("No fue posible cargar el historial de consumo.");
        }
    }

    private void mostrarEstadoDetalle(String mensaje) {
        mensajeDetalle = mensaje;
        if (tablaDetalle != null) {
            tablaDetalle.repaint();
        }
    }

    private void ocultarEstadoDetalle() {
        mensajeDetalle = "";
        if (tablaDetalle != null) {
            tablaDetalle.repaint();
        }
    }

    private void mostrarGraficaGrupos() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int grupoA = 0;
        int grupoB = 0;
        int grupoC = 0;

        for (String[] fila : analisisActual) {
            if (fila.length == 0) {
                continue;
            }
            String grupo = fila[0];
            if ("A".equalsIgnoreCase(grupo)) {
                grupoA++;
            } else if ("B".equalsIgnoreCase(grupo)) {
                grupoB++;
            } else if ("C".equalsIgnoreCase(grupo)) {
                grupoC++;
            }
        }

        dataset.addValue(grupoA, "Productos", "A");
        dataset.addValue(grupoB, "Productos", "B");
        dataset.addValue(grupoC, "Productos", "C");

        JFreeChart chart = ChartFactory.createBarChart(
                "Grupos ABC",
                "Grupo",
                "Productos",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        chart.getTitle().setPaint(new Color(15, 23, 42));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(new Color(203, 213, 225));
        plot.setRangeGridlinePaint(new Color(203, 213, 225));
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 51, 102));
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 12));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(false);
        chartPanel.setOpaque(true);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(760, 420));

        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Grafica ABC", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        contenedor.setBackground(Color.WHITE);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(0, 51, 102));
        barra.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblTitulo = new JLabel("Grafica ABC");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        barra.add(lblTitulo, BorderLayout.WEST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Color.WHITE);
        centro.setBorder(BorderFactory.createEmptyBorder(16, 16, 10, 16));
        centro.add(chartPanel, BorderLayout.CENTER);

        JButton btnCerrar = crearBotonDialogo("Cerrar", true);
        btnCerrar.addActionListener(evt -> dialogo.dispose());

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(new Color(241, 245, 249));
        pie.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        pie.add(btnCerrar, BorderLayout.EAST);

        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(centro, BorderLayout.CENTER);
        contenedor.add(pie, BorderLayout.SOUTH);

        dialogo.setContentPane(contenedor);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
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
        if (stockActual > 0 && stockActual < stockMinimo) {
            indicadores.add("Stock menor al minimo");
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

    private JButton crearBotonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setForeground(new Color(0, 51, 102));
        boton.setBackground(Color.WHITE);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 51, 102), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        return boton;
    }

    private JButton crearBotonAccion(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(0, 51, 102));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return boton;
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
            horizontal.setPreferredSize(new Dimension(12, 12));
            horizontal.setUnitIncrement(14);
            horizontal.setOpaque(false);
            horizontal.setUI(new BasicScrollBarUI() {
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
                    g2.fillRoundRect(trackBounds.x, trackBounds.y + 2, trackBounds.width, trackBounds.height - 4, 10, 10);
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
                    g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 2, thumbBounds.width - 2, thumbBounds.height - 4, 10, 10);
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

        JButton btnOk = crearBotonDialogo("OK", true);
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
