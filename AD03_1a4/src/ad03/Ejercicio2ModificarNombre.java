package ad03;

import java.sql.*;
import java.util.Scanner;

public class Ejercicio2ModificarNombre {

    public static void main(String[] args) {

        // Leemos los datos del teclado
        try (Scanner sc = new Scanner(System.in); Connection con = ConexionMySQL.abrir()) {
            
            System.out.print("DNI del alumno: ");
            // Quitamos los espcaios y ponemos en mayúscula
            String dni = sc.nextLine().trim().toUpperCase();
            String actual;
            // Coonsultamos si existe el alumno con el dni introducido
            try (PreparedStatement ps = con.prepareStatement("SELECT nombre FROM alumnos WHERE dni = ?")) {
                ps.setString(1, dni);
                try (ResultSet rs = ps.executeQuery()) {
                    
                    if (!rs.next()) {
                        System.out.println("No existe ningún alumno con ese DNI.");
                        return;
                    }
                    actual = rs.getString("nombre");
                }
            }
            // Visualizamos el valor actual del nombre y pedimos el nuevo
            System.out.println("Nombre actual: " + actual);
            System.out.print("Nuevo nombre (vacío para cancelar): ");
            String nuevo = sc.nextLine().trim();
            if (nuevo.isEmpty()) {
                System.out.println("Sin cambios.");
                return;
            }
            // Actualizamos el nombre del dni indicado
            try (PreparedStatement ps = con.prepareStatement("UPDATE alumnos SET nombre = ? WHERE dni = ?")) {
                ps.setString(1, nuevo);
                ps.setString(2, dni);
                System.out.println(ps.executeUpdate() == 1 ? "Nombre actualizado." : "No se actualizó ningún registro.");
            }
        } catch (SQLException e) {
            System.err.println("Error al modificar alumno: " + e.getMessage());
        }
    }
}
