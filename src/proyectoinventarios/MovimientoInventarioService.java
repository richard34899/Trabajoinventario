package proyectoinventarios;

import java.util.List;
import java.util.Map;

public class MovimientoInventarioService {

    public int encontrarIndiceProductoPorCodigo(String codigoCapturado, List<String> itemsProducto) {
        String codigo = normalizar(codigoCapturado);
        if (codigo.isEmpty() || !contieneDigitos(codigo)) {
            return 0;
        }

        for (int i = 0; i < itemsProducto.size(); i++) {
            String clave = extraerClaveItem(itemsProducto.get(i));
            if (normalizar(clave).equals(codigo)) {
                return i + 1;
            }
        }

        for (int i = 0; i < itemsProducto.size(); i++) {
            String clave = extraerClaveItem(itemsProducto.get(i));
            if (normalizar(clave).startsWith(codigo)) {
                return i + 1;
            }
        }

        return 0;
    }

    public void agregarOActualizarDetalle(List<ControlandoInventario.DetalleMovimientoData> detalles, String claveProducto,
            String nombreProducto, int cantidad, String tipoMovimiento, String motivo) {
        for (int i = 0; i < detalles.size(); i++) {
            ControlandoInventario.DetalleMovimientoData detalle = detalles.get(i);
            if (detalle.getClaveProducto().equalsIgnoreCase(claveProducto)
                    && detalle.getTipoMovimiento().equalsIgnoreCase(tipoMovimiento)) {
                int nuevaCantidad = "Ajuste".equalsIgnoreCase(tipoMovimiento)
                        ? cantidad
                        : detalle.getCantidad() + cantidad;
                detalles.set(i, new ControlandoInventario.DetalleMovimientoData(
                        claveProducto, nombreProducto, nuevaCantidad, tipoMovimiento, motivo));
                return;
            }
        }

        detalles.add(new ControlandoInventario.DetalleMovimientoData(
                claveProducto, nombreProducto, cantidad, tipoMovimiento, motivo));
    }

    public int obtenerCantidadPendiente(List<ControlandoInventario.DetalleMovimientoData> detalles, String claveProducto) {
        for (ControlandoInventario.DetalleMovimientoData detalle : detalles) {
            if (detalle.getClaveProducto().equalsIgnoreCase(claveProducto)) {
                return detalle.getCantidad();
            }
        }
        return 0;
    }

    public String calcularIndicadorStock(int stockActual, int stockMinimo) {
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

    public String resolverClaveExactaHistorial(String codigoCapturado, Map<String, String[]> productosPorClave) {
        String codigo = normalizar(codigoCapturado);
        if (codigo.isEmpty()) {
            return "";
        }

        if (productosPorClave.containsKey(codigo)) {
            return codigo;
        }

        if (!codigo.chars().allMatch(Character::isDigit)) {
            return "";
        }

        for (String clave : productosPorClave.keySet()) {
            if (extraerSoloDigitos(clave).equals(codigo)) {
                return clave;
            }
        }
        return "";
    }

    private boolean contieneDigitos(String texto) {
        return texto.chars().anyMatch(Character::isDigit);
    }

    private String extraerClaveItem(String itemProducto) {
        if (itemProducto == null || itemProducto.isBlank()) {
            return "";
        }
        return itemProducto.split(" - ", 2)[0];
    }

    private String extraerSoloDigitos(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder digitos = new StringBuilder();
        for (char caracter : texto.toCharArray()) {
            if (Character.isDigit(caracter)) {
                digitos.append(caracter);
            }
        }
        return digitos.toString();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }
}
