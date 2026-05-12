/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectoinventarios;

/**
 *
 * @author Romero
 */
import java.io.*;
import java.util.*;
import java.nio.file.Files;
import javax.swing.JOptionPane;
public class ControlandoInventario {
    
    private final String ARCHIVO = "productos.csv";
    private final String ARCHIVO_MOV_ENC = "mov_enc.csv";
    private final String ARCHIVO_MOV_DET = "mov_det.csv";
    private final String ARCHIVO_MOV_HIST = "mov_hist.csv";
    private final String ARCHIVO_CONFIG = "configuracion.csv";
    private static final String ENCABEZADO_MOV_ENC = "No. movimiento,Fecha,Tipo,Motivo";
    private static final String ENCABEZADO_MOV_DET = "No. movimiento,Cantidad,Clave producto";
    private static final String ENCABEZADO_MOV_HIST = "No. movimiento,Clave,Producto,Tipo,Fecha,Cantidad,Motivo,Stock antes,Stock despues";
    private static final String ENCABEZADO_CONFIG = "Costo pedido,H,Dias entrega global";
    private static final String CONFIG_DEFAULT = "100.0,10.0,1";

    public ControlandoInventario() {
        try {
            normalizarArchivosMovimiento();
            normalizarArchivoConfiguracion();
        } catch (IOException ex) {
            // Se intenta continuar con los datos actuales.
        }
    }

    // --- AQUÍ SE MUDARON TODAS TUS VALIDACIONES ---
    public boolean validarTodo(String clave, String nombre, int catIndex, String stockA, 
                               String stockM, String precio, String costo, String entrega, String demanda) {
        
        // 1. Validación Global
        if (clave.isEmpty() && nombre.isEmpty() && catIndex == 0) {
            JOptionPane.showMessageDialog(null, "Debe llenar todos los campos");
            return false;
        }

        // 2. Validaciones Individuales (Tus mismos mensajes)
        if (clave.isEmpty()) { JOptionPane.showMessageDialog(null, "Por favor, ingresa la clave"); return false; }
        if (nombre.isEmpty()) { JOptionPane.showMessageDialog(null, "El campo nombre está vacío."); return false; }
        if (catIndex == 0) { JOptionPane.showMessageDialog(null, "Selecciona una categoria válida."); return false; }
        if (stockA.isEmpty()) { JOptionPane.showMessageDialog(null, "Falta el stock actual"); return false; }
        if (stockM.isEmpty()) { JOptionPane.showMessageDialog(null, "Falta el stock minimo"); return false; }
        if (precio.isEmpty()) { JOptionPane.showMessageDialog(null, "Ingresa el precio"); return false; }
        if (costo.isEmpty()) { JOptionPane.showMessageDialog(null, "Ingresa el costo"); return false; }
        if (entrega.isEmpty()) { JOptionPane.showMessageDialog(null, "Falta el tiempo de entrega"); return false; }
        if (demanda.isEmpty()) { JOptionPane.showMessageDialog(null, "Ingresa la demanda actual."); return false; }

        // 3. Validaciones Numéricas (Tu bloque try-catch)
        try {
            double c = Double.parseDouble(costo);
            double p = Double.parseDouble(precio);
            int sM = Integer.parseInt(stockM);

            if (c <= 0) { JOptionPane.showMessageDialog(null, "El costo debe ser mayor a 0."); return false; }
            if (p <= 0) { JOptionPane.showMessageDialog(null, "El precio debe ser mayor a 0."); return false; }
            if (c >= p) { JOptionPane.showMessageDialog(null, "¡Advertencia! El costo no puede ser mayor o igual al precio."); return false; }
            if (sM <= 0) { JOptionPane.showMessageDialog(null, "El stock mínimo debe ser al menos 1."); return false; }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error: Costo, Precio y Stock deben ser números válidos.");
            return false;
        }
        return true;
    }

    // --- AQUÍ SE MUDÓ TU LÓGICA DE ARCHIVO Y ORDENAMIENTO ---
    public boolean guardarEnArchivo(Producto pNuevo) {
        File archivoOriginal = new File(ARCHIVO);
        File archivoTemp = new File("temp.csv");
        boolean esEdicion = false;

        try {
            if (!archivoOriginal.exists()) archivoOriginal.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(archivoOriginal));
            PrintWriter pw = new PrintWriter(new FileWriter(archivoTemp));
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length > 0 && datos[0].equals(pNuevo.getClave())) {
                    pw.println(pNuevo.toString()); // Usamos el objeto
                    esEdicion = true;
                } else {
                    pw.println(linea);
                }
            }

            if (!esEdicion) pw.println(pNuevo.toString());

            br.close();
            pw.close();
            archivoOriginal.delete();
            archivoTemp.renameTo(archivoOriginal);

            // El truco del ordenamiento que ya teníamos
            ordenarArchivo();
            
            return esEdicion; 
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            return false;
        }
    }

    private void ordenarArchivo() throws IOException {
        File f = new File(ARCHIVO);
        List<String> lineas = Files.readAllLines(f.toPath());
        Collections.sort(lineas, (a, b) -> {
            if (a.trim().isEmpty() || b.trim().isEmpty()) return 0;
            return a.split(",")[0].compareToIgnoreCase(b.split(",")[0]);
        });
        Files.write(f.toPath(), lineas);
    }
    
    // Método para checar si existe la clave (POO)
    public boolean existeClave(String clave) throws IOException {
        File f = new File(ARCHIVO);
        if(!f.exists()) return false;
        return Files.lines(f.toPath()).anyMatch(l -> l.split(",")[0].equalsIgnoreCase(clave));
    }

    public List<String[]> leerProductos() throws IOException {
        List<String[]> productos = new ArrayList<>();
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            return productos;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 10) {
                    productos.add(datos);
                }
            }
        }
        return productos;
    }

    public ParametrosAnalisis leerParametrosAnalisis() throws IOException {
        normalizarArchivoConfiguracion();
        File archivo = new File(ARCHIVO_CONFIG);
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea == null || linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = linea.split(",", -1);
                if (esEncabezadoConfiguracion(datos)) {
                    continue;
                }
                if (datos.length >= 3) {
                    double costoPedido = parsearDecimalSeguro(datos[0], 100.0);
                    double h = parsearDecimalSeguro(datos[1], 10.0);
                    int diasEntrega = parsearEnteroSeguro(datos[2], 1);
                    return new ParametrosAnalisis(costoPedido, h, diasEntrega);
                }
            }
        }
        return new ParametrosAnalisis(100.0, 10.0, 1);
    }

    public void guardarParametrosAnalisis(double costoPedido, double h, int diasEntrega) throws IOException {
        normalizarArchivoConfiguracion();
        List<String> lineas = new ArrayList<>();
        lineas.add(ENCABEZADO_CONFIG);
        lineas.add(String.format(Locale.US, "%.2f,%.2f,%d", costoPedido, h, diasEntrega));
        Files.write(new File(ARCHIVO_CONFIG).toPath(), lineas);
    }

    public List<String[]> leerMovimientos() throws IOException {
        File archivoHist = new File(ARCHIVO_MOV_HIST);
        if (archivoHist.exists()) {
            List<String[]> movimientosHistorial = leerMovimientosDesdeHistorial();
            if (!movimientosHistorial.isEmpty()) {
                return movimientosHistorial;
            }
        }

        return leerMovimientosReconstruidos();
    }

    private List<String[]> leerMovimientosReconstruidos() throws IOException {
        File archivoEnc = new File(ARCHIVO_MOV_ENC);
        if (!archivoEnc.exists()) {
            return new ArrayList<>();
        }

        Map<String, String[]> encabezados = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivoEnc))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", -1);
                if (esEncabezadoMovimientoEnc(datos)) {
                    continue;
                }
                if (datos.length >= 7) {
                    encabezados.put(datos[0], datos);
                } else if (datos.length >= 4) {
                    encabezados.put(datos[0], datos);
                }
            }
        }

        List<String[]> detalles = leerDetallesMovimientoInterno();
        List<String[]> movimientos = new ArrayList<>();
        Map<String, Integer> stockActualPorClave = new HashMap<>();
        for (String[] producto : leerProductos()) {
            stockActualPorClave.put(producto[0], Integer.parseInt(producto[3]));
        }
        Set<String> clavesConAjuste = new HashSet<>();

        for (String[] detalle : detalles) {
            String[] encabezado = encabezados.get(detalle[0]);
            if (encabezado == null) {
                continue;
            }

            String clave = detalle[3];
            String nombre = detalle[2];
            String cantidad = detalle[1];
            String tipo;
            String fecha;
            String motivo;
            if (encabezado.length >= 4) {
                tipo = encabezado[2];
                fecha = encabezado[1];
                motivo = encabezado[3];
            } else {
                continue;
            }
            String stockAntes = detalle[4];
            String stockDespues = detalle[5];
            String motivoDetalle = detalle[6];

            if (stockAntes.isBlank() && stockDespues.isBlank()) {
                Integer stockDespuesActual = stockActualPorClave.get(clave);
                if (stockDespuesActual != null && !clavesConAjuste.contains(clave)) {
                    int cantidadNumero = Integer.parseInt(cantidad);
                    if ("Entrada".equalsIgnoreCase(tipo)) {
                        stockDespues = String.valueOf(stockDespuesActual);
                        stockAntes = String.valueOf(stockDespuesActual - cantidadNumero);
                        stockActualPorClave.put(clave, stockDespuesActual - cantidadNumero);
                    } else if ("Salida".equalsIgnoreCase(tipo)) {
                        stockDespues = String.valueOf(stockDespuesActual);
                        stockAntes = String.valueOf(stockDespuesActual + cantidadNumero);
                        stockActualPorClave.put(clave, stockDespuesActual + cantidadNumero);
                    } else {
                        stockDespues = String.valueOf(stockDespuesActual);
                        stockAntes = "";
                        clavesConAjuste.add(clave);
                    }
                }
            }

            movimientos.add(new String[]{
                detalle[0], // numero movimiento
                clave,
                nombre,
                tipo,
                fecha,
                cantidad,
                motivoDetalle.isBlank() ? motivo : motivoDetalle,
                stockAntes,
                stockDespues
            });
        }
        return movimientos;
    }

    private List<String[]> leerMovimientosDesdeHistorial() throws IOException {
        List<String[]> movimientos = new ArrayList<>();
        File archivoHist = new File(ARCHIVO_MOV_HIST);
        if (!archivoHist.exists()) {
            return movimientos;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivoHist))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", -1);
                if (esEncabezadoMovimientoHist(datos)) {
                    continue;
                }
                if (datos.length >= 9) {
                    movimientos.add(new String[]{
                        datos[0], // no
                        datos[1], // clave
                        datos[2], // producto
                        datos[3], // tipo
                        datos[4], // fecha
                        datos[5], // cantidad
                        datos[6], // motivo
                        datos[7], // stock antes
                        datos[8]  // stock despues
                    });
                }
            }
        }

        movimientos.sort((a, b) -> {
            int comparacionFecha = b[4].compareToIgnoreCase(a[4]);
            if (comparacionFecha != 0) {
                return comparacionFecha;
            }
            return compararNumeroMovimientoDesc(a[0], b[0]);
        });
        return movimientos;
    }

    public List<String[]> leerDetallesMovimiento() throws IOException {
        List<String[]> detalles = new ArrayList<>();
        for (String[] detalle : leerDetallesMovimientoInterno()) {
            detalles.add(new String[]{detalle[0], detalle[1], detalle[2], detalle[3]});
        }
        return detalles;
    }

    public List<String[]> leerHistorialNotas() throws IOException {
        List<String[]> notas = new ArrayList<>();
        File archivoEnc = new File(ARCHIVO_MOV_ENC);
        if (!archivoEnc.exists()) {
            return notas;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivoEnc))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", -1);
                if (esEncabezadoMovimientoEnc(datos)) {
                    continue;
                }
                if (datos.length >= 4) {
                    notas.add(new String[]{datos[0], datos[1], datos[2], datos[3], "", "", ""});
                }
            }
        }

        notas.sort((a, b) -> compararNumeroMovimientoDesc(a[0], b[0]));
        return notas;
    }

    public List<String[]> leerDetallePorMovimiento(String numeroMovimiento) throws IOException {
        List<String[]> detalleNota = new ArrayList<>();
        for (String[] detalle : leerDetallesMovimientoInterno()) {
            if (numeroMovimiento.equalsIgnoreCase(detalle[0])) {
                detalleNota.add(new String[]{detalle[1], detalle[2]});
            }
        }
        return detalleNota;
    }

    private List<String[]> leerDetallesMovimientoInterno() throws IOException {
        List<String[]> detalles = new ArrayList<>();
        File archivoDet = new File(ARCHIVO_MOV_DET);
        if (!archivoDet.exists()) {
            return detalles;
        }
        List<String[]> productos = leerProductos();

        try (BufferedReader br = new BufferedReader(new FileReader(archivoDet))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", -1);
                if (esEncabezadoMovimientoDet(datos)) {
                    continue;
                }
                if (datos.length >= 6) {
                    // Formato anterior: no, clave, nombre, cantidad, stockAntes, stockDespues
                    detalles.add(new String[]{
                        datos[0], // numero movimiento
                        datos[3], // cantidad
                        datos[2], // nombre
                        datos[1], // clave
                        datos[4], // stock antes
                        datos[5], // stock despues
                        datos.length > 6 ? datos[6] : ""
                    });
                } else if (datos.length >= 4) {
                    // Formato intermedio: no, cantidad, producto, motivo
                    String claveReconstruida = esClaveProducto(productos, datos[2]) ? datos[2] : buscarClavePorNombre(productos, datos[2]);
                    String nombreReconstruido = buscarNombrePorClave(productos, claveReconstruida, datos[2]);
                    detalles.add(new String[]{
                        datos[0],
                        datos[1],
                        nombreReconstruido,
                        claveReconstruida,
                        "",
                        "",
                        datos[3]
                    });
                } else if (datos.length >= 3) {
                    // Formato nuevo de detalle: no, cantidad, clave o producto
                    String claveReconstruida = esClaveProducto(productos, datos[2]) ? datos[2] : buscarClavePorNombre(productos, datos[2]);
                    String nombreReconstruido = buscarNombrePorClave(productos, claveReconstruida, datos[2]);
                    detalles.add(new String[]{
                        datos[0],
                        datos[1],
                        nombreReconstruido,
                        claveReconstruida,
                        "",
                        "",
                        ""
                    });
                } else if (datos.length >= 2) {
                    // Formato intermedio de detalle: cantidad, producto
                    String claveReconstruida = buscarClavePorNombre(productos, datos[1]);
                    detalles.add(new String[]{
                        "",
                        datos[0],
                        datos[1],
                        claveReconstruida,
                        "",
                        "",
                        ""
                    });
                }
            }
        }

        detalles.sort((a, b) -> compararNumeroMovimientoDesc(a[0], b[0]));
        return detalles;
    }

    public List<String[]> leerMovimientosPorClave(String claveProducto) throws IOException {
        List<String[]> movimientos = leerMovimientos();
        movimientos.removeIf(mov -> !mov[1].equalsIgnoreCase(claveProducto));
        movimientos.sort((a, b) -> {
            int comparacionFecha = b[4].compareToIgnoreCase(a[4]);
            if (comparacionFecha != 0) {
                return comparacionFecha;
            }
            return compararNumeroMovimientoDesc(a[0], b[0]);
        });
        return movimientos;
    }

    public List<String[]> leerMovimientosPorPrefijoClave(String prefijoClave) throws IOException {
        List<String[]> movimientos = leerMovimientos();
        String prefijo = prefijoClave == null ? "" : prefijoClave.trim().toUpperCase();
        movimientos.removeIf(mov -> mov[1] == null || !mov[1].toUpperCase().startsWith(prefijo));
        movimientos.sort((a, b) -> {
            int comparacionFecha = b[4].compareToIgnoreCase(a[4]);
            if (comparacionFecha != 0) {
                return comparacionFecha;
            }
            return compararNumeroMovimientoDesc(a[0], b[0]);
        });
        return movimientos;
    }

    public ResultadoValidacion registrarMovimiento(MovimientoData movimiento) {
        return validarMovimientoCompleto(Collections.singletonList(new DetalleMovimientoData(
                movimiento.getClaveProducto(),
                movimiento.getNombreProducto(),
                movimiento.getCantidad(),
                movimiento.getTipoMovimiento(),
                movimiento.getMotivo())));
    }

    public ResultadoValidacion validarMovimientoCompleto(List<DetalleMovimientoData> detalles) {
        try {
            if (detalles == null || detalles.isEmpty()) {
                return ResultadoValidacion.error("detalle", "Agrega al menos un producto al movimiento.");
            }

            List<String[]> productos = leerProductos();
            List<String> productosConAdvertencia = new ArrayList<>();

            for (DetalleMovimientoData detalle : detalles) {
                String tipoMovimiento = detalle.getTipoMovimiento();
                String[] producto = buscarProducto(productos, detalle.getClaveProducto());
                if (producto == null) {
                    return ResultadoValidacion.error("producto", "Selecciona un producto valido.");
                }

                int stockActual = Integer.parseInt(producto[3]);
                int stockMinimo = Integer.parseInt(producto[4]);
                boolean activo = Boolean.parseBoolean(producto[9]);
                int cantidad = detalle.getCantidad();

                if (cantidad < 0) {
                    return ResultadoValidacion.error("cantidad", "La cantidad no puede ser negativa.");
                }

                if ("Ajuste".equalsIgnoreCase(tipoMovimiento) && detalle.getMotivo().trim().isEmpty()) {
                    return ResultadoValidacion.error("motivo", "No se permite ajuste sin comentario.");
                }

                if (("Entrada".equalsIgnoreCase(tipoMovimiento) || "Salida".equalsIgnoreCase(tipoMovimiento)) && !activo) {
                    return ResultadoValidacion.error("producto", "No se permite entrada o salida para productos deshabilitados. Solo ajuste.");
                }

                if ("Salida".equalsIgnoreCase(tipoMovimiento) && cantidad > stockActual) {
                    return ResultadoValidacion.error("cantidad", "No puede dar salida si el stock no es suficiente.");
                }

                if ("Entrada".equalsIgnoreCase(tipoMovimiento) && stockMinimo > 0 && (stockActual + cantidad) > (stockMinimo * 3)) {
                    productosConAdvertencia.add(detalle.getNombreProducto());
                }
            }

            if (!productosConAdvertencia.isEmpty()) {
                return ResultadoValidacion.advertencia("detalle",
                        "La entrada generara sobreinventario en: " + String.join(", ", productosConAdvertencia) + ".");
            }

            return ResultadoValidacion.ok();
        } catch (IOException e) {
            return ResultadoValidacion.error("producto", "No fue posible validar el movimiento.");
        }
    }

    public void aplicarMovimiento(MovimientoData movimiento) throws IOException {
        aplicarMovimientoCompleto(
                movimiento.getNumeroMovimiento(),
                movimiento.getFechaMovimiento(),
                movimiento.getTipoMovimiento(),
                movimiento.getMotivo(),
                Collections.singletonList(new DetalleMovimientoData(
                        movimiento.getClaveProducto(),
                        movimiento.getNombreProducto(),
                        movimiento.getCantidad(),
                        movimiento.getTipoMovimiento(),
                        movimiento.getMotivo())));
    }

    public void aplicarMovimientoCompleto(String numeroMovimiento, String fechaMovimiento, String tipoMovimiento,
            String motivo, List<DetalleMovimientoData> detalles) throws IOException {
        List<String[]> productos = leerProductos();
        guardarEncabezadoMovimiento(numeroMovimiento, fechaMovimiento, tipoMovimiento, motivo);

        for (DetalleMovimientoData detalle : detalles) {
            String[] producto = buscarProducto(productos, detalle.getClaveProducto());
            if (producto == null) {
                throw new IOException("Producto no encontrado.");
            }

            int stockActual = Integer.parseInt(producto[3]);
            int nuevoStock = calcularNuevoStock(stockActual, detalle.getTipoMovimiento(), detalle.getCantidad());
            producto[3] = String.valueOf(nuevoStock);
            guardarDetalleMovimiento(numeroMovimiento, detalle, stockActual, nuevoStock);
            guardarMovimientoHistorial(numeroMovimiento, fechaMovimiento, detalle, stockActual, nuevoStock);
        }

        guardarProductos(productos);
    }

    private int calcularNuevoStock(int stockActual, String tipoMovimiento, int cantidad) {
        if ("Entrada".equalsIgnoreCase(tipoMovimiento)) {
            return stockActual + cantidad;
        }
        if ("Salida".equalsIgnoreCase(tipoMovimiento)) {
            return stockActual - cantidad;
        }
        return cantidad;
    }

    private void guardarProductos(List<String[]> productos) throws IOException {
        Collections.sort(productos, (a, b) -> a[0].compareToIgnoreCase(b[0]));
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {
            for (String[] producto : productos) {
                pw.println(String.join(",", producto));
            }
        }
    }

    private void guardarEncabezadoMovimiento(String numeroMovimiento, String fechaMovimiento, String tipoMovimiento,
            String motivo) throws IOException {
        File archivoEnc = new File(ARCHIVO_MOV_ENC);
        asegurarArchivoConEncabezado(archivoEnc, ENCABEZADO_MOV_ENC);

        try (PrintWriter pwEnc = new PrintWriter(new FileWriter(archivoEnc, true))) {
            pwEnc.println(String.join(",",
                    numeroMovimiento,
                    fechaMovimiento,
                    tipoMovimiento,
                    escaparCampo(motivo)));
        }
    }

    private void guardarDetalleMovimiento(String numeroMovimiento, DetalleMovimientoData detalle,
            int stockAnterior, int nuevoStock) throws IOException {
        File archivoDet = new File(ARCHIVO_MOV_DET);
        asegurarArchivoConEncabezado(archivoDet, ENCABEZADO_MOV_DET);

        try (PrintWriter pwDet = new PrintWriter(new FileWriter(archivoDet, true))) {
            pwDet.println(String.join(",",
                    numeroMovimiento,
                    String.valueOf(detalle.getCantidad()),
                    escaparCampo(detalle.getClaveProducto())));
        }
    }

    private void guardarMovimientoHistorial(String numeroMovimiento, String fechaMovimiento,
            DetalleMovimientoData detalle, int stockAnterior, int nuevoStock) throws IOException {
        File archivoHist = new File(ARCHIVO_MOV_HIST);
        asegurarArchivoConEncabezado(archivoHist, ENCABEZADO_MOV_HIST);

        try (PrintWriter pwHist = new PrintWriter(new FileWriter(archivoHist, true))) {
            pwHist.println(String.join(",",
                    numeroMovimiento,
                    detalle.getClaveProducto(),
                    escaparCampo(detalle.getNombreProducto()),
                    escaparCampo(detalle.getTipoMovimiento()),
                    fechaMovimiento,
                    String.valueOf(detalle.getCantidad()),
                    escaparCampo(detalle.getMotivo()),
                    String.valueOf(stockAnterior),
                    String.valueOf(nuevoStock)));
        }
    }

    public String generarNumeroMovimiento() throws IOException {
        File archivoEnc = new File(ARCHIVO_MOV_ENC);
        if (!archivoEnc.exists()) {
            return "1";
        }
        int ultimo = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(archivoEnc))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", -1);
                if (esEncabezadoMovimientoEnc(datos)) {
                    continue;
                }
                if (datos.length > 0) {
                    try {
                        if (datos.length >= 7) {
                            ultimo = Math.max(ultimo, Integer.parseInt(datos[0]));
                        } else {
                            ultimo = Math.max(ultimo, Integer.parseInt(datos[0]));
                        }
                    } catch (NumberFormatException ex) {
                        // Ignorar valores dañados
                    }
                }
            }
        }
        return String.valueOf(ultimo + 1);
    }

    public String[] obtenerResumenProducto(String claveProducto) throws IOException {
        List<String[]> productos = leerProductos();
        String[] producto = buscarProducto(productos, claveProducto);
        if (producto == null) {
            return null;
        }
        return new String[]{producto[0], producto[1], producto[3], producto[4], Boolean.parseBoolean(producto[9]) ? "Activo" : "Inactivo"};
    }

    public List<String[]> leerUltimosMovimientos() throws IOException {
        List<String[]> movimientos = leerMovimientos();
        movimientos.sort((a, b) -> compararNumeroMovimientoDesc(a[0], b[0]));
        return movimientos;
    }

    private int compararNumeroMovimientoDesc(String numeroA, String numeroB) {
        try {
            return Integer.compare(Integer.parseInt(numeroB), Integer.parseInt(numeroA));
        } catch (NumberFormatException ex) {
            return numeroB.compareToIgnoreCase(numeroA);
        }
    }

    private int compararNumeroMovimientoAsc(String numeroA, String numeroB) {
        try {
            return Integer.compare(Integer.parseInt(numeroA), Integer.parseInt(numeroB));
        } catch (NumberFormatException ex) {
            return numeroA.compareToIgnoreCase(numeroB);
        }
    }

    private void normalizarArchivosMovimiento() throws IOException {
        normalizarArchivoEncabezado();
        normalizarArchivoDetalle();
        normalizarArchivoHistorial();
    }

    private void normalizarArchivoConfiguracion() throws IOException {
        File archivo = new File(ARCHIVO_CONFIG);
        if (!archivo.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
                pw.println(ENCABEZADO_CONFIG);
                pw.println(CONFIG_DEFAULT);
            }
            return;
        }

        List<String> lineas = Files.readAllLines(archivo.toPath());
        List<String> resultado = new ArrayList<>();
        resultado.add(ENCABEZADO_CONFIG);

        boolean encontroDatos = false;
        for (String linea : lineas) {
            if (linea == null || linea.trim().isEmpty()) {
                continue;
            }
            String[] datos = linea.split(",", -1);
            if (esEncabezadoConfiguracion(datos)) {
                continue;
            }
            if (datos.length >= 3 && !encontroDatos) {
                resultado.add(String.join(",",
                        String.valueOf(parsearDecimalSeguro(datos[0], 100.0)),
                        String.valueOf(parsearDecimalSeguro(datos[1], 10.0)),
                        String.valueOf(parsearEnteroSeguro(datos[2], 1))));
                encontroDatos = true;
            }
        }

        if (!encontroDatos) {
            resultado.add(CONFIG_DEFAULT);
        }

        Files.write(archivo.toPath(), resultado);
    }

    private void normalizarArchivoEncabezado() throws IOException {
        File archivo = new File(ARCHIVO_MOV_ENC);
        if (!archivo.exists()) {
            asegurarArchivoConEncabezado(archivo, ENCABEZADO_MOV_ENC);
            return;
        }

        List<String> lineasOriginales = Files.readAllLines(archivo.toPath());
        List<String> lineasNormalizadas = new ArrayList<>();
        lineasNormalizadas.add(ENCABEZADO_MOV_ENC);

        for (String linea : lineasOriginales) {
            if (linea == null || linea.trim().isEmpty()) {
                continue;
            }
            String[] datos = linea.split(",", -1);
            if (esEncabezadoMovimientoEnc(datos)) {
                continue;
            }
            if (datos.length >= 7) {
                if (esFilaEncabezadoOrdenViejo(datos)) {
                    lineasNormalizadas.add(String.join(",",
                            datos[3],
                            datos[0],
                            datos[1],
                            datos[4]));
                } else if (esFilaEncabezadoOrdenActual(datos)) {
                    lineasNormalizadas.add(String.join(",",
                            datos[0],
                            datos[2],
                            datos[3],
                            datos[4]));
                } else {
                    lineasNormalizadas.add(String.join(",",
                            datos[0],
                            datos[1],
                            datos[2],
                            datos[3]));
                }
            } else if (datos.length >= 4) {
                lineasNormalizadas.add(String.join(",",
                        datos[0],
                        datos[1],
                        datos[2],
                        datos[3]));
            }
        }

        LinkedHashMap<String, String> movimientosUnicos = new LinkedHashMap<>();
        for (int i = 1; i < lineasNormalizadas.size(); i++) {
            String linea = lineasNormalizadas.get(i);
            String[] datos = linea.split(",", -1);
            if (datos.length >= 4 && !movimientosUnicos.containsKey(datos[0])) {
                movimientosUnicos.put(datos[0], String.join(",",
                        datos[0],
                        datos[1],
                        datos[2],
                        datos[3]));
            }
        }

        List<String> resultado = new ArrayList<>();
        resultado.add(ENCABEZADO_MOV_ENC);
        resultado.addAll(movimientosUnicos.values());
        Files.write(archivo.toPath(), resultado);
    }

    private void normalizarArchivoDetalle() throws IOException {
        File archivo = new File(ARCHIVO_MOV_DET);
        if (!archivo.exists()) {
            asegurarArchivoConEncabezado(archivo, ENCABEZADO_MOV_DET);
            return;
        }

        List<String> lineasOriginales = Files.readAllLines(archivo.toPath());
        List<String> lineasNormalizadas = new ArrayList<>();
        lineasNormalizadas.add(ENCABEZADO_MOV_DET);

        for (String linea : lineasOriginales) {
            if (linea == null || linea.trim().isEmpty()) {
                continue;
            }
            String[] datos = linea.split(",", -1);
            if (esEncabezadoMovimientoDet(datos)) {
                continue;
            }
            if (datos.length >= 6) {
                lineasNormalizadas.add(String.join(",",
                        datos[0],
                        datos[3],
                        escaparCampo(datos[1])));
            } else if (datos.length >= 4) {
                String claveReconstruida = esClaveProducto(leerProductos(), datos[2]) ? datos[2] : buscarClavePorNombre(leerProductos(), datos[2]);
                lineasNormalizadas.add(String.join(",",
                        datos[0],
                        datos[1],
                        escaparCampo(claveReconstruida)));
            } else if (datos.length >= 3) {
                String claveReconstruida = esClaveProducto(leerProductos(), datos[2]) ? datos[2] : buscarClavePorNombre(leerProductos(), datos[2]);
                lineasNormalizadas.add(String.join(",",
                        datos[0],
                        datos[1],
                        escaparCampo(claveReconstruida)));
            } else if (datos.length >= 2) {
                String claveReconstruida = esClaveProducto(leerProductos(), datos[1]) ? datos[1] : buscarClavePorNombre(leerProductos(), datos[1]);
                lineasNormalizadas.add(String.join(",",
                        "",
                        datos[0],
                        escaparCampo(claveReconstruida)));
            }
        }

        Files.write(archivo.toPath(), lineasNormalizadas);
    }

    private void normalizarArchivoHistorial() throws IOException {
        File archivo = new File(ARCHIVO_MOV_HIST);
        if (!archivo.exists()) {
            asegurarArchivoConEncabezado(archivo, ENCABEZADO_MOV_HIST);
        } else {
            asegurarArchivoConEncabezado(archivo, ENCABEZADO_MOV_HIST);
        }

        List<String[]> movimientosReconstruidos = leerMovimientosReconstruidos();
        List<String> lineasExistentes = Files.readAllLines(archivo.toPath());
        LinkedHashMap<String, String> filasHistorial = new LinkedHashMap<>();

        List<String[]> movimientosOrdenados = new ArrayList<>(movimientosReconstruidos);
        movimientosOrdenados.sort((a, b) -> {
            int comparacionNumero = compararNumeroMovimientoAsc(a[0], b[0]);
            if (comparacionNumero != 0) {
                return comparacionNumero;
            }
            return a[1].compareToIgnoreCase(b[1]);
        });

        for (String[] movimiento : movimientosOrdenados) {
            if (movimiento.length < 9) {
                continue;
            }
            String fila = String.join(",",
                    movimiento[0],
                    escaparCampo(movimiento[1]),
                    escaparCampo(movimiento[2]),
                    escaparCampo(movimiento[3]),
                    movimiento[4],
                    movimiento[5],
                    escaparCampo(movimiento[6]),
                    movimiento[7],
                    movimiento[8]);
            filasHistorial.put(generarLlaveHistorial(movimiento), fila);
        }

        for (String linea : lineasExistentes) {
            if (linea == null || linea.trim().isEmpty()) {
                continue;
            }
            String[] datos = linea.split(",", -1);
            if (esEncabezadoMovimientoHist(datos) || datos.length < 9) {
                continue;
            }
            filasHistorial.putIfAbsent(generarLlaveHistorial(datos), String.join(",",
                    datos[0],
                    escaparCampo(datos[1]),
                    escaparCampo(datos[2]),
                    escaparCampo(datos[3]),
                    datos[4],
                    datos[5],
                    escaparCampo(datos[6]),
                    datos[7],
                    datos[8]));
        }

        List<String> resultado = new ArrayList<>();
        resultado.add(ENCABEZADO_MOV_HIST);
        resultado.addAll(filasHistorial.values());
        Files.write(archivo.toPath(), resultado);
    }

    private void asegurarArchivoConEncabezado(File archivo, String encabezado) throws IOException {
        if (!archivo.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
                pw.println(encabezado);
            }
            return;
        }

        List<String> lineas = Files.readAllLines(archivo.toPath());
        if (lineas.isEmpty()) {
            Files.write(archivo.toPath(), Collections.singletonList(encabezado));
            return;
        }

        List<String> nuevasLineas = new ArrayList<>();
        nuevasLineas.add(encabezado);

        for (String linea : lineas) {
            if (linea == null || linea.trim().isEmpty()) {
                continue;
            }
            if (linea.trim().equalsIgnoreCase(encabezado.trim())) {
                continue;
            }
            nuevasLineas.add(linea);
        }

        Files.write(archivo.toPath(), nuevasLineas);
    }

    private boolean esEncabezadoMovimientoEnc(String[] datos) {
        if (datos.length == 0) {
            return false;
        }
        String primeraColumna = limpiarCeldaEncabezado(datos[0]);
        return "No. movimiento".equalsIgnoreCase(primeraColumna)
                || "Codigo".equalsIgnoreCase(primeraColumna)
                || "Fecha".equalsIgnoreCase(primeraColumna);
    }

    private boolean esEncabezadoMovimientoDet(String[] datos) {
        if (datos.length == 0) {
            return false;
        }
        String primeraColumna = limpiarCeldaEncabezado(datos[0]);
        return "Cantidad".equalsIgnoreCase(primeraColumna)
                || "No. movimiento".equalsIgnoreCase(primeraColumna);
    }

    private boolean esEncabezadoMovimientoHist(String[] datos) {
        if (datos.length == 0) {
            return false;
        }
        String primeraColumna = limpiarCeldaEncabezado(datos[0]);
        return "No. movimiento".equalsIgnoreCase(primeraColumna)
                || "Clave".equalsIgnoreCase(primeraColumna);
    }

    private boolean esEncabezadoConfiguracion(String[] datos) {
        if (datos.length == 0) {
            return false;
        }
        String primeraColumna = limpiarCeldaEncabezado(datos[0]);
        return "Costo pedido".equalsIgnoreCase(primeraColumna);
    }

    private boolean esFilaEncabezadoOrdenViejo(String[] datos) {
        return datos.length >= 7
                && datos[0].matches("\\d{4}-\\d{2}-\\d{2}")
                && ("Entrada".equalsIgnoreCase(datos[1])
                || "Salida".equalsIgnoreCase(datos[1])
                || "Ajuste".equalsIgnoreCase(datos[1]));
    }

    private boolean esFilaEncabezadoOrdenActual(String[] datos) {
        return datos.length >= 7
                && datos[0].trim().matches("\\d+")
                && datos[2].matches("\\d{4}-\\d{2}-\\d{2}")
                && ("Entrada".equalsIgnoreCase(datos[3])
                || "Salida".equalsIgnoreCase(datos[3])
                || "Ajuste".equalsIgnoreCase(datos[3]));
    }

    private String limpiarCeldaEncabezado(String valor) {
        return valor == null ? "" : valor.replace("\uFEFF", "").trim();
    }

    private double parsearDecimalSeguro(String valor, double porDefecto) {
        try {
            return Double.parseDouble((valor == null ? "" : valor.trim()).replace(",", "."));
        } catch (NumberFormatException ex) {
            return porDefecto;
        }
    }

    private int parsearEnteroSeguro(String valor, int porDefecto) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException ex) {
            return porDefecto;
        }
    }

    private String generarLlaveHistorial(String[] datos) {
        return String.join("||",
                datos.length > 0 ? datos[0].trim() : "",
                datos.length > 1 ? datos[1].trim() : "",
                datos.length > 2 ? datos[2].trim() : "",
                datos.length > 3 ? datos[3].trim() : "",
                datos.length > 4 ? datos[4].trim() : "",
                datos.length > 5 ? datos[5].trim() : "",
                datos.length > 6 ? datos[6].trim() : "",
                datos.length > 7 ? datos[7].trim() : "",
                datos.length > 8 ? datos[8].trim() : "");
    }

    private String obtenerCodigoProducto(String claveProducto, List<String[]> productos) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i)[0].equalsIgnoreCase(claveProducto)) {
                return String.valueOf(i + 1);
            }
        }
        return "";
    }

    private void guardarMovimientoPlanoCompatibilidad(MovimientoData movimiento, int stockAnterior, int nuevoStock) throws IOException {
        File archivo = new File("movimientos_inventario.csv");
        if (!archivo.exists()) {
            archivo.createNewFile();
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, true))) {
            pw.println(String.join(",",
                    movimiento.getClaveProducto(),
                    movimiento.getNombreProducto(),
                    movimiento.getTipoMovimiento(),
                    movimiento.getFechaMovimiento(),
                    String.valueOf(movimiento.getCantidad()),
                    escaparCampo(movimiento.getMotivo()),
                    String.valueOf(stockAnterior),
                    String.valueOf(nuevoStock)));
        }
    }

    private String escaparCampo(String valor) {
        return valor.replace(",", " ");
    }

    private String[] buscarProducto(List<String[]> productos, String claveProducto) {
        for (String[] producto : productos) {
            if (producto[0].equalsIgnoreCase(claveProducto)) {
                return producto;
            }
        }
        return null;
    }

    private String buscarClavePorNombre(List<String[]> productos, String nombreProducto) {
        if (nombreProducto == null) {
            return "";
        }
        for (String[] producto : productos) {
            if (producto[1].equalsIgnoreCase(nombreProducto.trim())) {
                return producto[0];
            }
        }
        return "";
    }

    private String buscarNombrePorClave(List<String[]> productos, String claveProducto, String valorAlterno) {
        if (claveProducto != null && !claveProducto.isBlank()) {
            String[] producto = buscarProducto(productos, claveProducto);
            if (producto != null) {
                return producto[1];
            }
        }
        return valorAlterno == null ? "" : valorAlterno;
    }

    private boolean esClaveProducto(List<String[]> productos, String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        return buscarProducto(productos, valor.trim()) != null;
    }

    public static class DatosProductoFormulario {

        private final String clave;
        private final String nombre;
        private final int indiceCategoria;
        private final String categoriaNombre;
        private final String stockActual;
        private final String stockMinimo;
        private final String precio;
        private final String costo;
        private final String entrega;
        private final String demanda;

        public DatosProductoFormulario(String clave, String nombre, int indiceCategoria, String categoriaNombre,
                String stockActual, String stockMinimo, String precio, String costo, String entrega, String demanda) {
            this.clave = clave;
            this.nombre = nombre;
            this.indiceCategoria = indiceCategoria;
            this.categoriaNombre = categoriaNombre;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
            this.precio = precio;
            this.costo = costo;
            this.entrega = entrega;
            this.demanda = demanda;
        }

        public String getClave() { return clave; }
        public String getNombre() { return nombre; }
        public int getIndiceCategoria() { return indiceCategoria; }
        public String getCategoriaNombre() { return categoriaNombre; }
        public String getStockActual() { return stockActual; }
        public String getStockMinimo() { return stockMinimo; }
        public String getPrecio() { return precio; }
        public String getCosto() { return costo; }
        public String getEntrega() { return entrega; }
        public String getDemanda() { return demanda; }

        public boolean estanTodosLosCamposBaseVacios() {
            return clave.isEmpty()
                    && nombre.isEmpty()
                    && indiceCategoria <= 0
                    && stockActual.isEmpty()
                    && stockMinimo.isEmpty()
                    && precio.isEmpty()
                    && costo.isEmpty()
                    && entrega.isEmpty()
                    && demanda.isEmpty();
        }
    }

    public static class ResultadoValidacion {

        private final boolean valido;
        private final boolean advertencia;
        private final String campo;
        private final String mensaje;

        private ResultadoValidacion(boolean valido, boolean advertencia, String campo, String mensaje) {
            this.valido = valido;
            this.advertencia = advertencia;
            this.campo = campo;
            this.mensaje = mensaje;
        }

        public static ResultadoValidacion ok() {
            return new ResultadoValidacion(true, false, null, null);
        }

        public static ResultadoValidacion error(String campo, String mensaje) {
            return new ResultadoValidacion(false, false, campo, mensaje);
        }

        public static ResultadoValidacion advertencia(String campo, String mensaje) {
            return new ResultadoValidacion(true, true, campo, mensaje);
        }

        public boolean isValido() { return valido; }
        public boolean isAdvertencia() { return advertencia; }
        public String getCampo() { return campo; }
        public String getMensaje() { return mensaje; }
    }

    public static class ValidadorProducto {

        public ResultadoValidacion validar(DatosProductoFormulario datos) {
            if (datos.estanTodosLosCamposBaseVacios()) {
                return ResultadoValidacion.error("clave", "Debe llenar todos los campos.");
            }

            if (datos.getClave().isEmpty()) {
                return ResultadoValidacion.error("clave", "La clave no puede ir vacia.");
            }
            if (datos.getNombre().isEmpty()) {
                return ResultadoValidacion.error("nombre", "El nombre de producto no puede ir vacio.");
            }
            if (datos.getIndiceCategoria() <= 0) {
                return ResultadoValidacion.error("categoria", "La categoria no puede ir vacia.");
            }

            ResultadoValidacion stockActualNumero = validarEntero(datos.getStockActual(), "stockActual", "El stock actual debe ser un numero valido.");
            if (!stockActualNumero.isValido()) return stockActualNumero;

            ResultadoValidacion stockMinimoNumero = validarEntero(datos.getStockMinimo(), "stockMinimo", "El stock minimo debe ser un numero valido.");
            if (!stockMinimoNumero.isValido()) return stockMinimoNumero;

            ResultadoValidacion precioNumero = validarDecimal(datos.getPrecio(), "precio", "El precio debe ser un numero valido.");
            if (!precioNumero.isValido()) return precioNumero;

            ResultadoValidacion costoNumero = validarDecimal(datos.getCosto(), "costo", "El costo debe ser un numero valido.");
            if (!costoNumero.isValido()) return costoNumero;

            ResultadoValidacion entregaNumero = validarEntero(datos.getEntrega(), "entrega", "Los dias de entrega deben ser un numero valido.");
            if (!entregaNumero.isValido()) return entregaNumero;

            ResultadoValidacion demandaNumero = validarEntero(datos.getDemanda(), "demanda", "La demanda anual debe ser un numero valido.");
            if (!demandaNumero.isValido()) return demandaNumero;

            int stockActual = Integer.parseInt(datos.getStockActual());
            int stockMinimo = Integer.parseInt(datos.getStockMinimo());
            double precio = Double.parseDouble(datos.getPrecio());
            double costo = Double.parseDouble(datos.getCosto());
            int entrega = Integer.parseInt(datos.getEntrega());
            int demanda = Integer.parseInt(datos.getDemanda());

            if (stockActual < 0) {
                return ResultadoValidacion.error("stockActual", "El stock actual no puede ser negativo.");
            }
            if (stockMinimo == 0 && precio == 0 && costo == 0 && entrega == 0 && demanda == 0) {
                return ResultadoValidacion.error("stockActual", "Los valores no pueden ser cero o menor.");
            }
            if (stockMinimo <= 0) {
                return ResultadoValidacion.error("stockMinimo", "El stock minimo no puede ser 0 o menor.");
            }
            if (precio <= 0) {
                return ResultadoValidacion.error("precio", "Precio no puede ser 0 o menor.");
            }
            if (costo <= 0) {
                return ResultadoValidacion.error("costo", "Costo no puede ser 0 o menor.");
            }
            if (entrega < 1) {
                return ResultadoValidacion.error("entrega", "Los dias de entrega deben ser 1 o mayores.");
            }
            if (demanda < 1) {
                return ResultadoValidacion.error("demanda", "La estimacion de demanda anual debe ser 1 o mayor.");
            }
            if (costo >= precio) {
                return ResultadoValidacion.advertencia("precio", "El costo es mayor o igual que el precio.");
            }

            return ResultadoValidacion.ok();
        }

        private ResultadoValidacion validarEntero(String valor, String campo, String mensaje) {
            try {
                Integer.parseInt(valor);
                return ResultadoValidacion.ok();
            } catch (NumberFormatException ex) {
                return ResultadoValidacion.error(campo, mensaje);
            }
        }

        private ResultadoValidacion validarDecimal(String valor, String campo, String mensaje) {
            try {
                Double.parseDouble(valor);
                return ResultadoValidacion.ok();
            } catch (NumberFormatException ex) {
                return ResultadoValidacion.error(campo, mensaje);
            }
        }
    }

    public static class MovimientoData {

        private final String claveProducto;
        private final String nombreProducto;
        private final String tipoMovimiento;
        private final int cantidad;
        private final String fechaMovimiento;
        private final String motivo;
        private final String numeroMovimiento;

        public MovimientoData(String claveProducto, String nombreProducto, String tipoMovimiento, int cantidad,
                String fechaMovimiento, String motivo, String numeroMovimiento) {
            this.claveProducto = claveProducto;
            this.nombreProducto = nombreProducto;
            this.tipoMovimiento = tipoMovimiento;
            this.cantidad = cantidad;
            this.fechaMovimiento = fechaMovimiento;
            this.motivo = motivo;
            this.numeroMovimiento = numeroMovimiento;
        }

        public String getClaveProducto() { return claveProducto; }
        public String getNombreProducto() { return nombreProducto; }
        public String getTipoMovimiento() { return tipoMovimiento; }
        public int getCantidad() { return cantidad; }
        public String getFechaMovimiento() { return fechaMovimiento; }
        public String getMotivo() { return motivo; }
        public String getNumeroMovimiento() { return numeroMovimiento; }
    }

    public static class DetalleMovimientoData {

        private final String claveProducto;
        private final String nombreProducto;
        private final int cantidad;
        private final String tipoMovimiento;
        private final String motivo;

        public DetalleMovimientoData(String claveProducto, String nombreProducto, int cantidad, String tipoMovimiento, String motivo) {
            this.claveProducto = claveProducto;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.tipoMovimiento = tipoMovimiento == null ? "" : tipoMovimiento;
            this.motivo = motivo == null ? "" : motivo;
        }

        public String getClaveProducto() { return claveProducto; }
        public String getNombreProducto() { return nombreProducto; }
        public int getCantidad() { return cantidad; }
        public String getTipoMovimiento() { return tipoMovimiento; }
        public String getMotivo() { return motivo; }
    }

    public static class ParametrosAnalisis {

        private final double costoPedido;
        private final double h;
        private final int diasEntregaGlobal;

        public ParametrosAnalisis(double costoPedido, double h, int diasEntregaGlobal) {
            this.costoPedido = costoPedido;
            this.h = h <= 0 ? 1.0 : h;
            this.diasEntregaGlobal = diasEntregaGlobal <= 0 ? 1 : diasEntregaGlobal;
        }

        public double getCostoPedido() { return costoPedido; }
        public double getH() { return h; }
        public int getDiasEntregaGlobal() { return diasEntregaGlobal; }
    }
}
