package proyectoinventarios;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ConfiguracionService {

    private final ControlandoInventario control;
    private final DecimalFormat formatoMoneda = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));

    public ConfiguracionService() {
        this(new ControlandoInventario());
    }

    public ConfiguracionService(ControlandoInventario control) {
        this.control = control;
    }

    public EstadoConfiguracion cargarEstado() throws IOException {
        ControlandoInventario.ParametrosAnalisis parametros = control.leerParametrosRegistrados();
        if (parametros == null) {
            return EstadoConfiguracion.sinDatos();
        }
        return EstadoConfiguracion.conDatos(
                formatoMoneda.format(parametros.getCostoPedido()),
                formatoMoneda.format(parametros.getH()),
                String.valueOf(parametros.getDiasEntregaGlobal()));
    }

    public ResultadoConfiguracion guardar(ConfiguracionFormularioData datos, boolean esRegistroNuevo) {
        String costoPedidoTexto = limpiar(datos.getCostoPedido());
        String costoMantenimientoTexto = limpiar(datos.getCostoMantenimiento());
        String tiempoEntregaTexto = limpiar(datos.getTiempoEntrega());

        if (costoPedidoTexto.isEmpty() || costoMantenimientoTexto.isEmpty() || tiempoEntregaTexto.isEmpty()) {
            return ResultadoConfiguracion.error("costoPedido", "Hay que llenar todos los campos.");
        }

        try {
            double costoPedido = Double.parseDouble(costoPedidoTexto);
            if (costoPedido <= 0) {
                return ResultadoConfiguracion.error("costoPedido", "El costo por pedido debe ser numerico y mayor que 0.");
            }

            double costoMantenimiento = Double.parseDouble(costoMantenimientoTexto);
            if (costoMantenimiento <= 0) {
                return ResultadoConfiguracion.error("costoMantenimiento", "El costo de mantenimiento debe ser numerico y mayor que 0.");
            }

            if (tiempoEntregaTexto.contains(".")) {
                return ResultadoConfiguracion.error("tiempoEntrega", "El tiempo de entrega no debe permitir valores decimales.");
            }

            int tiempoEntrega = Integer.parseInt(tiempoEntregaTexto);
            if (tiempoEntrega <= 0) {
                return ResultadoConfiguracion.error("tiempoEntrega", "El tiempo de entrega debe ser numerico y mayor que 0.");
            }

            control.guardarParametrosAnalisis(costoPedido, costoMantenimiento, tiempoEntrega);
            return ResultadoConfiguracion.ok(
                    formatoMoneda.format(costoPedido),
                    formatoMoneda.format(costoMantenimiento),
                    String.valueOf(tiempoEntrega),
                    esRegistroNuevo
                            ? "Los parametros se registraron correctamente."
                            : "Los parametros se editaron correctamente.");
        } catch (NumberFormatException ex) {
            return ResultadoConfiguracion.error("costoPedido", "Los valores de datos deben ser numericos y mayores que 0.");
        } catch (IOException ex) {
            return ResultadoConfiguracion.error("general", "No fue posible guardar los parametros.");
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public static class ConfiguracionFormularioData {

        private final String costoPedido;
        private final String costoMantenimiento;
        private final String tiempoEntrega;

        public ConfiguracionFormularioData(String costoPedido, String costoMantenimiento, String tiempoEntrega) {
            this.costoPedido = costoPedido;
            this.costoMantenimiento = costoMantenimiento;
            this.tiempoEntrega = tiempoEntrega;
        }

        public String getCostoPedido() {
            return costoPedido;
        }

        public String getCostoMantenimiento() {
            return costoMantenimiento;
        }

        public String getTiempoEntrega() {
            return tiempoEntrega;
        }
    }

    public static class EstadoConfiguracion {

        private final boolean hayDatos;
        private final String costoPedido;
        private final String costoMantenimiento;
        private final String tiempoEntrega;

        private EstadoConfiguracion(boolean hayDatos, String costoPedido, String costoMantenimiento, String tiempoEntrega) {
            this.hayDatos = hayDatos;
            this.costoPedido = costoPedido;
            this.costoMantenimiento = costoMantenimiento;
            this.tiempoEntrega = tiempoEntrega;
        }

        public static EstadoConfiguracion sinDatos() {
            return new EstadoConfiguracion(false, "", "", "");
        }

        public static EstadoConfiguracion conDatos(String costoPedido, String costoMantenimiento, String tiempoEntrega) {
            return new EstadoConfiguracion(true, costoPedido, costoMantenimiento, tiempoEntrega);
        }

        public boolean isHayDatos() {
            return hayDatos;
        }

        public String getCostoPedido() {
            return costoPedido;
        }

        public String getCostoMantenimiento() {
            return costoMantenimiento;
        }

        public String getTiempoEntrega() {
            return tiempoEntrega;
        }
    }

    public static class ResultadoConfiguracion {

        private final boolean valido;
        private final String campo;
        private final String mensaje;
        private final String costoPedidoFormateado;
        private final String costoMantenimientoFormateado;
        private final String tiempoEntregaFormateado;

        private ResultadoConfiguracion(boolean valido, String campo, String mensaje,
                String costoPedidoFormateado, String costoMantenimientoFormateado, String tiempoEntregaFormateado) {
            this.valido = valido;
            this.campo = campo;
            this.mensaje = mensaje;
            this.costoPedidoFormateado = costoPedidoFormateado;
            this.costoMantenimientoFormateado = costoMantenimientoFormateado;
            this.tiempoEntregaFormateado = tiempoEntregaFormateado;
        }

        public static ResultadoConfiguracion ok(String costoPedido, String costoMantenimiento, String tiempoEntrega, String mensaje) {
            return new ResultadoConfiguracion(true, null, mensaje, costoPedido, costoMantenimiento, tiempoEntrega);
        }

        public static ResultadoConfiguracion error(String campo, String mensaje) {
            return new ResultadoConfiguracion(false, campo, mensaje, null, null, null);
        }

        public boolean isValido() {
            return valido;
        }

        public String getCampo() {
            return campo;
        }

        public String getMensaje() {
            return mensaje;
        }

        public String getCostoPedidoFormateado() {
            return costoPedidoFormateado;
        }

        public String getCostoMantenimientoFormateado() {
            return costoMantenimientoFormateado;
        }

        public String getTiempoEntregaFormateado() {
            return tiempoEntregaFormateado;
        }
    }
}
