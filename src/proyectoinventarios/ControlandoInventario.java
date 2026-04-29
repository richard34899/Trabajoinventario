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
}
