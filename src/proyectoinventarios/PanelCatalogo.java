package proyectoinventarios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JDialog;
import javax.swing.JScrollBar;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class PanelCatalogo extends JPanel {

    private final HashMap<String, String> mapaCategorias = new HashMap<>();
    private final HashMap<String, String> traductorCategorias = new HashMap<>();
    private final String archivoCategorias = "electronica.csv";
    private final String archivoProductos = "productos.csv";
    private final ControlandoInventario.ValidadorProducto validadorProducto = new ControlandoInventario.ValidadorProducto();

    private JTextField txtClave;
    private JTextField txtNombre;
    private JComboBox<String> comboCategorias;
    private JTextField txtStockActual;
    private JTextField txtStockMinimo;
    private JTextField txtPrecio;
    private JTextField txtCosto;
    private JTextField txtEntrega;
    private JTextField txtDemanda;
    private JTextField txtBuscar;
    private JTable tabla;
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnRegistrar;
    private JButton btnLimpiar;
    private JButton btnConsultar;
    private JButton btnEditar;
    private JButton btnDesactivar;
    private String claveEdicion;

    public PanelCatalogo() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        cargarCategorias();
        initComponents();
        cargarProductos();
        limpiarFormulario();
    }

    private void initComponents() {
        add(crearEncabezado(), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setOpaque(false);
        contenido.add(crearPanelRegistro(), BorderLayout.NORTH);
        contenido.add(crearPanelTabla(), BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(0, 51, 102));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Catalogo de productos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel("<html><body style='width:860px'>"
                + ""
                + "</body></html>");
        descripcion.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descripcion.setForeground(new Color(219, 234, 254));
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titulo);
        return panel;
    }

    private JPanel crearPanelRegistro() {
        JPanel card = crearCard();
        card.setLayout(new BorderLayout(18, 0));
        card.setPreferredSize(new Dimension(0, 320));

        JPanel acciones = new JPanel();
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.setOpaque(false);
        acciones.setPreferredSize(new Dimension(190, 0));

        JLabel tituloAcciones = new JLabel("Acciones");
        tituloAcciones.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tituloAcciones.setForeground(new Color(15, 23, 42));
        tituloAcciones.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnRegistrar = crearBotonAccion("Registrar");
        btnLimpiar = crearBotonAccion("Limpiar");
        btnRegistrar.addActionListener(evt -> guardarProducto());
        btnLimpiar.addActionListener(evt -> limpiarFormulario());

        acciones.add(tituloAcciones);
        acciones.add(Box.createRigidArea(new Dimension(0, 14)));
        acciones.add(btnRegistrar);
        acciones.add(Box.createRigidArea(new Dimension(0, 10)));
        acciones.add(btnLimpiar);
        acciones.add(Box.createVerticalGlue());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 18, 18);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtClave = crearCampo();
        txtNombre = crearCampo();
        comboCategorias = new JComboBox<>();
        estilizarCombo(comboCategorias);
        comboCategorias.addItem("- Seleccione una categoria -");
        for (String categoria : mapaCategorias.keySet().stream().sorted().toList()) {
            comboCategorias.addItem(categoria);
        }

        txtStockActual = crearCampo();
        txtStockMinimo = crearCampo();
        txtPrecio = crearCampo();
        txtCosto = crearCampo();
        txtEntrega = crearCampo();
        txtDemanda = crearCampo();
        alinearCampoNumerico(txtStockActual);
        alinearCampoNumerico(txtStockMinimo);
        alinearCampoNumerico(txtPrecio);
        alinearCampoNumerico(txtCosto);
        alinearCampoNumerico(txtEntrega);
        alinearCampoNumerico(txtDemanda);

        agregarCampo(form, crearCampoPanel("Clave o codigo", txtClave), gbc, 0, 0);
        agregarCampo(form, crearCampoPanel("Nombre o descripcion", txtNombre), gbc, 1, 0);
        agregarCampo(form, crearCampoPanel("Categoria", comboCategorias), gbc, 2, 0);
        agregarCampo(form, crearCampoPanel("Stock actual", txtStockActual), gbc, 0, 1);
        agregarCampo(form, crearCampoPanel("Stock minimo", txtStockMinimo), gbc, 1, 1);
        agregarCampo(form, crearCampoPanel("Costo", txtCosto), gbc, 2, 1);
        agregarCampo(form, crearCampoPanel("Precio", txtPrecio), gbc, 0, 2);
        agregarCampo(form, crearCampoPanel("Entrega en dias", txtEntrega), gbc, 1, 2);
        agregarCampo(form, crearCampoPanel("Demanda anual", txtDemanda), gbc, 2, 2);

        configurarNavegacionConEnter();

        card.add(acciones, BorderLayout.WEST);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearPanelTabla() {
        JPanel card = crearCard();
        card.setLayout(new BorderLayout(18, 0));

        JPanel acciones = new JPanel();
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.setOpaque(false);
        acciones.setPreferredSize(new Dimension(190, 0));

        JLabel titulo = new JLabel("Tabla y control");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(15, 23, 42));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnEditar = crearBotonAccion("Editar");
        btnConsultar = crearBotonAccion("Consultar");
        btnDesactivar = crearBotonAccion("Desactivar");

        btnEditar.addActionListener(evt -> cargarSeleccion());
        btnConsultar.addActionListener(evt -> consultarProductos());
        btnDesactivar.addActionListener(evt -> cambiarEstadoSeleccionado());

        acciones.add(titulo);
        acciones.add(Box.createRigidArea(new Dimension(0, 14)));
        acciones.add(btnEditar);
        acciones.add(Box.createRigidArea(new Dimension(0, 10)));
        acciones.add(btnConsultar);
        acciones.add(Box.createRigidArea(new Dimension(0, 10)));
        acciones.add(btnDesactivar);
        acciones.add(Box.createVerticalGlue());

        JPanel derecha = new JPanel(new BorderLayout(0, 14));
        derecha.setOpaque(false);

        JPanel filtros = new JPanel(new java.awt.GridLayout(1, 1, 18, 0));
        filtros.setOpaque(false);

        txtBuscar = crearCampo();
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

        filtros.add(crearCampoPanel("Buscar por clave, nombre o categoria", txtBuscar));

        modelo = new DefaultTableModel(new String[]{
            "Clave", "Nombre", "Categoria", "Stock A.", "Stock M.",
            "Precio", "Costo", "Entrega", "Demanda", "Estado"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(32);
        tabla.setGridColor(new Color(191, 219, 254));
        tabla.setSelectionBackground(new Color(191, 219, 254));
        tabla.setSelectionForeground(new Color(15, 23, 42));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setOpaque(false);
        tabla.getTableHeader().setBackground(new Color(0, 51, 102));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setShowVerticalLines(true);
        tabla.setShowHorizontalLines(true);
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
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(96, 165, 250)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
                return label;
            }
        });

        DefaultTableCellRenderer izquierdaRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    label.setForeground(new Color(15, 23, 42));
                }
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(191, 219, 254)),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)));
                return label;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            if (i <= 2 || i == 9) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(izquierdaRenderer);
            }
        }

        DefaultTableCellRenderer derechaEstilizada = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    label.setForeground(new Color(15, 23, 42));
                }
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(191, 219, 254)),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)));
                return label;
            }
        };

        for (int i = 3; i <= 8; i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(derechaEstilizada);
        }

        DefaultTableCellRenderer estadoRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    String estado = value != null ? value.toString() : "";
                    if ("Inactivo".equalsIgnoreCase(estado)) {
                        label.setForeground(new Color(185, 70, 70));
                    } else {
                        label.setForeground(new Color(15, 23, 42));
                    }
                }
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(191, 219, 254)),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)));
                return label;
            }
        };
        tabla.getColumnModel().getColumn(9).setCellRenderer(estadoRenderer);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(105);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(235);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(8).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(9).setPreferredWidth(110);

        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        estilizarScrollPane(scroll);

        derecha.add(filtros, BorderLayout.NORTH);
        derecha.add(scroll, BorderLayout.CENTER);

        card.add(acciones, BorderLayout.WEST);
        card.add(derecha, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return panel;
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
        label.setMinimumSize(new Dimension(120, 22));

        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        componente.setPreferredSize(new Dimension(220, 38));
        if (componente instanceof javax.swing.JComponent jComponent) {
            jComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        if (x == 2) {
            gbc.insets = new Insets(0, 0, 18, 0);
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

    private void alinearCampoNumerico(JTextField campo) {
        campo.setHorizontalAlignment(SwingConstants.RIGHT);
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
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
                boton.setFocusPainted(false);
                boton.setContentAreaFilled(true);
                return boton;
            }
        });
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (index == -1) {
                    label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 6));
                } else {
                    label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
                }
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
        combo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> estilizarScrollCombo(combo));
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            }
        });
    }

    private void estilizarScrollCombo(JComboBox<String> combo) {
        Object child = combo.getAccessibleContext().getAccessibleChild(0);
        if (!(child instanceof javax.swing.plaf.basic.BasicComboPopup popup)) {
            return;
        }

        JScrollPane scrollPane = (JScrollPane) popup.getComponents()[0];
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setPreferredSize(new Dimension(12, 0));
        vertical.setBackground(new Color(239, 246, 255));
        vertical.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(96, 165, 250);
                trackColor = new Color(239, 246, 255);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonScroll();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonScroll();
            }

            @Override
            protected void paintTrack(Graphics g, javax.swing.JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRoundRect(trackBounds.x + 2, trackBounds.y, trackBounds.width - 4, trackBounds.height, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintThumb(Graphics g, javax.swing.JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
                g2.dispose();
            }

            private JButton crearBotonScroll() {
                JButton boton = new JButton();
                boton.setPreferredSize(new Dimension(0, 0));
                boton.setMinimumSize(new Dimension(0, 0));
                boton.setMaximumSize(new Dimension(0, 0));
                boton.setBorder(BorderFactory.createEmptyBorder());
                boton.setOpaque(false);
                boton.setContentAreaFilled(false);
                return boton;
            }
        });
    }

    private void estilizarScrollPane(JScrollPane scrollPane) {
        scrollPane.getViewport().setBackground(Color.WHITE);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setPreferredSize(new Dimension(12, 0));
        vertical.setBackground(new Color(239, 246, 255));
        vertical.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(96, 165, 250);
                trackColor = new Color(239, 246, 255);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonScroll();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonScroll();
            }

            @Override
            protected void paintTrack(Graphics g, javax.swing.JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRoundRect(trackBounds.x + 2, trackBounds.y, trackBounds.width - 4, trackBounds.height, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintThumb(Graphics g, javax.swing.JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
                g2.dispose();
            }
        });
    }

    private JButton crearBotonScroll() {
        JButton boton = new JButton();
        boton.setPreferredSize(new Dimension(0, 0));
        boton.setMinimumSize(new Dimension(0, 0));
        boton.setMaximumSize(new Dimension(0, 0));
        boton.setBorder(BorderFactory.createEmptyBorder());
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        return boton;
    }

    private void configurarNavegacionConEnter() {
        txtClave.addActionListener(evt -> txtNombre.requestFocusInWindow());
        txtNombre.addActionListener(evt -> txtStockActual.requestFocusInWindow());
        txtStockActual.addActionListener(evt -> txtStockMinimo.requestFocusInWindow());
        txtStockMinimo.addActionListener(evt -> txtCosto.requestFocusInWindow());
        txtCosto.addActionListener(evt -> txtPrecio.requestFocusInWindow());
        txtPrecio.addActionListener(evt -> txtEntrega.requestFocusInWindow());
        txtEntrega.addActionListener(evt -> txtDemanda.requestFocusInWindow());
        txtDemanda.addActionListener(evt -> btnRegistrar.doClick());
    }

    private void cargarCategorias() {
        mapaCategorias.clear();
        traductorCategorias.clear();

        File archivo = new File(archivoCategorias);
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 2) {
                    String codigo = datos[0].trim();
                    String nombre = datos[1].trim();
                    mapaCategorias.put(nombre, codigo);
                    traductorCategorias.put(codigo, nombre);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se encontro el archivo de categorias.");
        }
    }

    private void cargarProductos() {
        modelo.setRowCount(0);
        File archivo = new File(archivoProductos);
        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] d = linea.split(",");
                if (d.length >= 10) {
                    modelo.addRow(new Object[]{
                        d[0],
                        d[1],
                        traductorCategorias.getOrDefault(d[2], d[2]),
                        d[3],
                        d[4],
                        d[5],
                        d[6],
                        d[7],
                        d[8],
                        Boolean.parseBoolean(d[9]) ? "Activo" : "Inactivo"
                    });
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los productos.");
        }
    }

    private void aplicarFiltros() {
        final String texto = txtBuscar.getText().trim().toLowerCase(Locale.ROOT);

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String clave = entry.getStringValue(0).toLowerCase(Locale.ROOT);
                String nombre = entry.getStringValue(1).toLowerCase(Locale.ROOT);
                String categoriaFila = entry.getStringValue(2).toLowerCase(Locale.ROOT);

                return texto.isEmpty()
                        || clave.startsWith(texto)
                        || nombre.startsWith(texto)
                        || categoriaFila.startsWith(texto);
            }
        });
    }

    private void consultarProductos() {
        aplicarFiltros();

        if (tabla.getRowCount() == 0) {
            mostrarDialogoInfo("Consulta de productos", "No se encontraron productos con ese criterio de busqueda.");
            return;
        }

        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0 || filaVista >= tabla.getRowCount()) {
            filaVista = 0;
            tabla.setRowSelectionInterval(0, 0);
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        String ficha = "📋 RESULTADO DE LA BUSQUEDA\n"
                + "================================\n"
                + "🔑 Clave: " + limitarTextoFicha(modelo.getValueAt(filaModelo, 0).toString(), 40) + "\n"
                + "📦 Nombre: " + limitarTextoFicha(modelo.getValueAt(filaModelo, 1).toString(), 55) + "\n"
                + "📁 Categoria: " + limitarTextoFicha(modelo.getValueAt(filaModelo, 2).toString(), 55) + "\n"
                + "📈 Stock Actual: " + modelo.getValueAt(filaModelo, 3) + "\n"
                + "📉 Stock Minimo: " + modelo.getValueAt(filaModelo, 4) + "\n"
                + "💰 Precio Venta: $" + modelo.getValueAt(filaModelo, 5) + "\n"
                + "💸 Costo: $" + modelo.getValueAt(filaModelo, 6) + "\n"
                + "🚚 Entrega: " + modelo.getValueAt(filaModelo, 7) + " dias\n"
                + "📊 Demanda: " + modelo.getValueAt(filaModelo, 8) + "\n"
                + "🔔 Estado: " + modelo.getValueAt(filaModelo, 9) + "\n"
                + "================================";

        mostrarDialogoInfo("Consulta de productos", ficha);
    }

    private String limitarTextoFicha(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.trim();
        if (limpio.length() <= maximo) {
            return limpio;
        }
        return limpio.substring(0, Math.max(0, maximo - 3)) + "...";
    }

    private void guardarProducto() {
        try {
            ControlandoInventario.DatosProductoFormulario datos = new ControlandoInventario.DatosProductoFormulario(
                    txtClave.getText().trim(),
                    txtNombre.getText().trim(),
                    comboCategorias.getSelectedIndex(),
                    comboCategorias.getSelectedItem() != null ? comboCategorias.getSelectedItem().toString() : "",
                    txtStockActual.getText().trim(),
                    txtStockMinimo.getText().trim(),
                    txtPrecio.getText().trim(),
                    txtCosto.getText().trim(),
                    txtEntrega.getText().trim(),
                    txtDemanda.getText().trim());

            ControlandoInventario.ResultadoValidacion resultado = validadorProducto.validar(datos);
            if (!resultado.isValido()) {
                enfocarCampo(resultado.getCampo(), resultado.getMensaje());
                return;
            }
            if (resultado.isAdvertencia()) {
                int respuesta = mostrarDialogoConfirmacion("Advertencia", resultado.getMensaje() + "\nDesea continuar?");
                if (respuesta != JOptionPane.YES_OPTION) {
                    enfocarCampoSinMensaje(resultado.getCampo());
                    return;
                }
            }

            String clave = datos.getClave();
            String nombre = datos.getNombre();
            String categoriaNombre = datos.getCategoriaNombre();
            int stockA = Integer.parseInt(datos.getStockActual());
            int stockM = Integer.parseInt(datos.getStockMinimo());
            double precio = Double.parseDouble(datos.getPrecio());
            double costo = Double.parseDouble(datos.getCosto());
            int entrega = Integer.parseInt(datos.getEntrega());
            int demanda = Integer.parseInt(datos.getDemanda());

            boolean activo = true;
            List<String[]> registros = leerRegistros();
            boolean actualizado = false;

            for (String[] registro : registros) {
                if (registro[0].equalsIgnoreCase(clave)) {
                    if (claveEdicion == null || !registro[0].equalsIgnoreCase(claveEdicion)) {
                        mostrarMensaje("La clave ya existe. Debe ser unica.");
                        txtClave.requestFocus();
                        return;
                    }
                    activo = Boolean.parseBoolean(registro[9]);
                }
            }

            String categoriaCodigo = mapaCategorias.get(categoriaNombre);
            String[] nuevo = new String[]{
                clave,
                nombre,
                categoriaCodigo,
                String.valueOf(stockA),
                String.valueOf(stockM),
                String.format(Locale.US, "%.2f", precio),
                String.format(Locale.US, "%.2f", costo),
                String.valueOf(entrega),
                String.valueOf(demanda),
                String.valueOf(activo)
            };

            for (int i = 0; i < registros.size(); i++) {
                if (claveEdicion != null && registros.get(i)[0].equalsIgnoreCase(claveEdicion)) {
                    registros.set(i, nuevo);
                    actualizado = true;
                    break;
                }
            }

            if (!actualizado) {
                registros.add(nuevo);
            }

            guardarRegistros(registros);
            cargarProductos();
            aplicarFiltros();
            mostrarDialogoInfo("Catalogo de productos", actualizado ? "Producto actualizado." : "Producto registrado.");
            limpiarFormulario();
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(ex.getMessage());
        } catch (IOException ex) {
            mostrarMensaje("Error al guardar el producto.");
        }
    }

    private List<String[]> leerRegistros() throws IOException {
        List<String[]> registros = new ArrayList<>();
        File archivo = new File(archivoProductos);
        if (!archivo.exists()) {
            return registros;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 10) {
                    registros.add(datos);
                }
            }
        }
        return registros;
    }

    private void guardarRegistros(List<String[]> registros) throws IOException {
        Collections.sort(registros, (a, b) -> a[0].compareToIgnoreCase(b[0]));
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivoProductos))) {
            for (String[] registro : registros) {
                pw.println(String.join(",", registro));
            }
        }
    }

    private void cargarSeleccion() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarMensaje("Selecciona un producto en la tabla para editar.");
            return;
        }

        int fila = tabla.convertRowIndexToModel(filaVista);
        txtClave.setText(modelo.getValueAt(fila, 0).toString());
        txtNombre.setText(modelo.getValueAt(fila, 1).toString());
        comboCategorias.setSelectedItem(modelo.getValueAt(fila, 2).toString());
        txtStockActual.setText(modelo.getValueAt(fila, 3).toString());
        txtStockMinimo.setText(modelo.getValueAt(fila, 4).toString());
        txtPrecio.setText(modelo.getValueAt(fila, 5).toString());
        txtCosto.setText(modelo.getValueAt(fila, 6).toString());
        txtEntrega.setText(modelo.getValueAt(fila, 7).toString());
        txtDemanda.setText(modelo.getValueAt(fila, 8).toString());

        claveEdicion = modelo.getValueAt(fila, 0).toString();
        txtClave.setEditable(false);
    }

    private void cambiarEstadoSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarMensaje("Selecciona un producto para activar o desactivar.");
            return;
        }

        int fila = tabla.convertRowIndexToModel(filaVista);
        String clave = modelo.getValueAt(fila, 0).toString();
        String estadoActual = modelo.getValueAt(fila, 9).toString();
        boolean activar = estadoActual.equalsIgnoreCase("Inactivo");

        int respuesta = mostrarDialogoConfirmacion(
                "Confirmacion",
                activar ? "Confirmar activacion del producto?" : "Confirmar desactivacion del producto?");
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            List<String[]> registros = leerRegistros();
            for (String[] registro : registros) {
                if (registro[0].equalsIgnoreCase(clave)) {
                    registro[9] = String.valueOf(activar);
                    break;
                }
            }
            guardarRegistros(registros);
            cargarProductos();
            aplicarFiltros();
        } catch (IOException ex) {
            mostrarMensaje("No fue posible actualizar el estado.");
        }
    }

    private void limpiarFormulario() {
        txtClave.setText("");
        txtNombre.setText("");
        comboCategorias.setSelectedIndex(0);
        txtStockActual.setText("");
        txtStockMinimo.setText("");
        txtPrecio.setText("");
        txtCosto.setText("");
        txtEntrega.setText("");
        txtDemanda.setText("");
        txtClave.setEditable(true);
        claveEdicion = null;
        txtClave.requestFocus();
    }

    public void limpiarFormularioPublico() {
        limpiarFormulario();
    }

    public void cargarProductoEnFormulario(String[] datos) {
        if (datos == null || datos.length < 9) {
            return;
        }
        txtClave.setText(datos[0]);
        txtNombre.setText(datos[1]);
        comboCategorias.setSelectedItem(datos[2]);
        txtStockActual.setText(datos[3]);
        txtStockMinimo.setText(datos[4]);
        txtPrecio.setText(datos[5]);
        txtCosto.setText(datos[6]);
        txtEntrega.setText(datos[7]);
        txtDemanda.setText(datos[8]);
        txtClave.setEditable(false);
    }

    public void solicitarFocoEnClave() {
        SwingUtilities.invokeLater(() -> {
            txtClave.requestFocusInWindow();
            txtClave.selectAll();
        });
    }

    private void mostrarMensaje(String mensaje) {
        if (mensaje != null && !mensaje.isEmpty()) {
            mostrarDialogoAdvertencia("Validacion", mensaje);
        }
    }

    private void enfocarCampo(JTextField campo, String mensaje) {
        mostrarDialogoAdvertencia("Validacion", mensaje);
        campo.requestFocusInWindow();
        campo.selectAll();
    }

    private void enfocarCampo(String campo, String mensaje) {
        switch (campo) {
            case "clave" -> enfocarCampo(txtClave, mensaje);
            case "nombre" -> enfocarCampo(txtNombre, mensaje);
            case "categoria" -> {
                if (mensaje != null && !mensaje.isEmpty()) {
                    mostrarDialogoAdvertencia("Validacion", mensaje);
                }
                comboCategorias.requestFocusInWindow();
            }
            case "stockActual" -> enfocarCampo(txtStockActual, mensaje);
            case "stockMinimo" -> enfocarCampo(txtStockMinimo, mensaje);
            case "precio" -> enfocarCampo(txtPrecio, mensaje);
            case "costo" -> enfocarCampo(txtCosto, mensaje);
            case "entrega" -> enfocarCampo(txtEntrega, mensaje);
            case "demanda" -> enfocarCampo(txtDemanda, mensaje);
            default -> {
                if (mensaje != null && !mensaje.isEmpty()) {
                    mostrarDialogoAdvertencia("Validacion", mensaje);
                }
            }
        }
    }

    private void enfocarCampoSinMensaje(String campo) {
        switch (campo) {
            case "clave" -> {
                txtClave.requestFocusInWindow();
                txtClave.selectAll();
            }
            case "nombre" -> {
                txtNombre.requestFocusInWindow();
                txtNombre.selectAll();
            }
            case "categoria" -> comboCategorias.requestFocusInWindow();
            case "stockActual" -> {
                txtStockActual.requestFocusInWindow();
                txtStockActual.selectAll();
            }
            case "stockMinimo" -> {
                txtStockMinimo.requestFocusInWindow();
                txtStockMinimo.selectAll();
            }
            case "precio" -> {
                txtPrecio.requestFocusInWindow();
                txtPrecio.selectAll();
            }
            case "costo" -> {
                txtCosto.requestFocusInWindow();
                txtCosto.selectAll();
            }
            case "entrega" -> {
                txtEntrega.requestFocusInWindow();
                txtEntrega.selectAll();
            }
            case "demanda" -> {
                txtDemanda.requestFocusInWindow();
                txtDemanda.selectAll();
            }
            default -> txtClave.requestFocusInWindow();
        }
    }

    private void mostrarDialogoInfo(String titulo, String mensaje) {
        mostrarDialogoPersonalizado(titulo, mensaje, new Color(0, 51, 102), new Color(241, 245, 249), true);
    }

    private void mostrarDialogoAdvertencia(String titulo, String mensaje) {
        mostrarDialogoPersonalizado(titulo, mensaje, new Color(0, 51, 102), new Color(241, 245, 249), false);
    }

    private int mostrarDialogoConfirmacion(String titulo, String mensaje) {
        final int[] respuesta = {JOptionPane.NO_OPTION};

        JDialog dialogo = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), titulo, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);

        Color colorTitulo = new Color(0, 51, 102);
        Color colorFondo = new Color(241, 245, 249);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        contenedor.setBackground(Color.WHITE);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(colorTitulo);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        barra.add(lblTitulo, BorderLayout.WEST);

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(colorFondo);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        JPanel textoPanel = new JPanel();
        textoPanel.setLayout(new BoxLayout(textoPanel, BoxLayout.Y_AXIS));
        textoPanel.setOpaque(false);

        JLabel subtitulo = new JLabel("Confirmacion requerida");
        subtitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        subtitulo.setForeground(colorTitulo);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMensaje = new JLabel("<html><body style='width:300px;font-family:Segoe UI;font-size:11px;color:#0f172a;line-height:1.4;'>"
                + mensaje.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                + "</body></html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMensaje.setForeground(new Color(15, 23, 42));
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);

        textoPanel.add(subtitulo);
        textoPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        textoPanel.add(lblMensaje);

        cuerpo.add(textoPanel, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(colorFondo);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));

        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.X_AXIS));

        JButton btnNo = new JButton("No");
        btnNo.setFocusPainted(false);
        btnNo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNo.setForeground(colorTitulo);
        btnNo.setBackground(Color.WHITE);
        btnNo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorTitulo, 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        btnNo.addActionListener(evt -> {
            respuesta[0] = JOptionPane.NO_OPTION;
            dialogo.dispose();
        });

        JButton btnSi = new JButton("Si");
        btnSi.setFocusPainted(false);
        btnSi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSi.setForeground(Color.WHITE);
        btnSi.setBackground(colorTitulo);
        btnSi.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnSi.addActionListener(evt -> {
            respuesta[0] = JOptionPane.YES_OPTION;
            dialogo.dispose();
        });

        acciones.add(btnNo);
        acciones.add(Box.createRigidArea(new Dimension(10, 0)));
        acciones.add(btnSi);
        pie.add(acciones, BorderLayout.EAST);

        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(cuerpo, BorderLayout.CENTER);
        contenedor.add(pie, BorderLayout.SOUTH);

        dialogo.setContentPane(contenedor);
        dialogo.pack();
        dialogo.setResizable(false);
        dialogo.setLocationRelativeTo(this);
        dialogo.getRootPane().setDefaultButton(btnSi);
        dialogo.setVisible(true);

        return respuesta[0];
    }

    private void mostrarDialogoPersonalizado(String titulo, String mensaje, Color colorTitulo, Color colorFondo, boolean mostrarDetalleConsulta) {
        JDialog dialogo = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), titulo, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        contenedor.setBackground(Color.WHITE);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(colorTitulo);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        barra.add(lblTitulo, BorderLayout.WEST);

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(colorFondo);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        JPanel textoPanel = new JPanel();
        textoPanel.setLayout(new BoxLayout(textoPanel, BoxLayout.Y_AXIS));
        textoPanel.setOpaque(false);

        JLabel subtitulo = new JLabel(mostrarDetalleConsulta ? "Detalle de consulta" : "Mensaje del sistema");
        subtitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        subtitulo.setForeground(colorTitulo);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        String html = "<html><body style='width:300px;font-family:Segoe UI;font-size:11px;color:#0f172a;line-height:1.4;'>"
                + formatearMensajeHtml(mensaje, mostrarDetalleConsulta)
                + "</body></html>";

        JLabel lblMensaje = new JLabel(html);
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMensaje.setForeground(new Color(15, 23, 42));
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);

        textoPanel.add(subtitulo);
        textoPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        textoPanel.add(lblMensaje);

        cuerpo.add(textoPanel, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(colorFondo);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));

        JButton btnOk = new JButton("OK");
        btnOk.setFocusPainted(false);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.setForeground(Color.WHITE);
        btnOk.setBackground(colorTitulo);
        btnOk.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnOk.addActionListener(evt -> dialogo.dispose());
        pie.add(btnOk, BorderLayout.EAST);

        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(cuerpo, BorderLayout.CENTER);
        contenedor.add(pie, BorderLayout.SOUTH);

        dialogo.setContentPane(contenedor);
        dialogo.pack();
        dialogo.setResizable(false);
        dialogo.setLocationRelativeTo(this);
        dialogo.getRootPane().setDefaultButton(btnOk);
        dialogo.setVisible(true);
    }

    private String formatearMensajeHtml(String mensaje, boolean mostrarDetalleConsulta) {
        String textoSeguro = mensaje.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String[] lineas = textoSeguro.split("\\n");
        StringBuilder html = new StringBuilder();

        for (String linea : lineas) {
            if (linea == null || linea.isBlank()) {
                html.append("<div style='height:5px;'></div>");
                continue;
            }

            if (mostrarDetalleConsulta && linea.contains("================================")) {
                html.append("<div style='margin:4px 0;color:#94a3b8;'>")
                        .append("&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;&#9472;")
                        .append("</div>");
            } else if (mostrarDetalleConsulta && linea.contains(":")) {
                String[] partes = linea.split(":", 2);
                html.append("<div style='margin:3px 0;'>")
                        .append("<span style='color:#0b3d75;font-weight:700;'>")
                        .append(partes[0])
                        .append(":</span> ")
                        .append(partes[1].trim())
                        .append("</div>");
            } else {
                html.append("<div style='margin:3px 0;font-weight:")
                        .append(mostrarDetalleConsulta ? "700" : "400")
                        .append(";'>")
                        .append(linea)
                        .append("</div>");
            }
        }

        return html.toString();
    }
}
