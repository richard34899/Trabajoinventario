/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyectoinventarios;

/**
 *
 * @author Romero
 */
public class ProyectoInventarios {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        InicioDashboard.aplicarLookAndFeel();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InicioDashboard().setVisible(true);
            }
        });
    }
    
}
