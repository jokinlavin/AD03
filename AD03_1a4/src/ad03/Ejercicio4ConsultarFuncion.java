package ad03;

import java.sql.*;
import java.util.Scanner;

public class Ejercicio4ConsultarFuncion {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in); Connection con = ConexionMySQL.abrir()) {

            try (PreparedStatement ps = con.prepareStatement("SELECT id_curso,titulo,fecha FROM cursos ORDER BY fecha");
                 ResultSet rs = ps.executeQuery()) {
                System.out.println("Cursos:");
                while (rs.next()) System.out.printf("%d | %s | %s%n",
                    rs.getInt("id_curso"), rs.getString("titulo"), rs.getDate("fecha"));
            }
            System.out.print("ID del curso: ");
            int id;
            try { id = Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("ID no válido."); return; }

            // Lamamos a la función de la BBDD y recuperamos el resultado
            try (CallableStatement cs = con.prepareCall("{? = CALL obtener_numero_matriculados(?)}")) {
                cs.registerOutParameter(1, Types.INTEGER);
                cs.setInt(2, id);
                cs.execute();
                System.out.println("Número de matrículas: " + cs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Error al llamar a la función: " + e.getMessage());
        }
    }
}
