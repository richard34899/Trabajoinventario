/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectoinventarios;

/**
 *
 * @author Romero
 */
public class Producto {
    private String clave, nombre, categoria, entrega, demanda;
    private int stockA, stockM;
    private double precio, costo;
    private boolean activo;

    // El Constructor (Es el que "arma" el objeto)
    public Producto(String clave, String nombre, String categoria, int stockA, int stockM, double precio, double costo, String entrega, String demanda, boolean activo) {
        this.clave = clave;
        this.nombre = nombre;
        this.categoria = categoria;
        this.stockA = stockA;
        this.stockM = stockM;
        this.precio = precio;
        this.costo = costo;
        this.entrega = entrega;
        this.demanda = demanda;
        this.activo = activo;
    }

    // Getters (Para que el controlador pueda leer la clave)
    public String getClave() { return clave; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    // El método toString (Define cómo se escribe el producto en el CSV)
    @Override
    public String toString() {
        return clave + "," + nombre + "," + categoria + "," + stockA + "," + 
               stockM + "," + precio + "," + costo + "," + entrega + "," + 
               demanda + "," + activo;
    }
}
