package ad03;

import java.sql.*;

public class Ejercicio1ListadoCursos {

    // Constante con la sentencia SQL
    private static final String SQL = "SELECT c.id_curso, c.titulo, c.fecha, COUNT(m.dni) AS matriculados," +
        " a.capacidad, a.nombre AS aula, a.edificio" +
        " FROM cursos c" +
        " JOIN aulas a ON a.id_aula = c.id_aula" +
        " LEFT JOIN matriculas m ON m.id_curso = c.id_curso" +
        " GROUP BY c.id_curso, c.titulo, c.fecha, a.capacidad, a.nombre, a.edificio"+
        " ORDER BY c.fecha, c.id_curso";

    public static void main(String[] args) {

        try (Connection con = ConexionMySQL.abrir();
             
            PreparedStatement ps = con.prepareStatement(SQL);
            ResultSet rs = ps.executeQuery()) {
            
            // Listamos la cabecera
            System.out.printf("%-3s | %-30s | %-10s | %5s | %8s | %-28s | %s%n",
                    "ID", "TÍTULO", "FECHA", "ALUM.", "CAPAC.", "AULA", "EDIFICIO");
            // Listamos los registros
            while (rs.next()) {
                System.out.printf("%-3d | %-30s | %-10s | %5d | %8d | %-28s | %s%n",
                    rs.getInt("id_curso"), rs.getString("titulo"), rs.getDate("fecha"),
                    rs.getInt("matriculados"), rs.getInt("capacidad"),
                    rs.getString("aula"), rs.getString("edificio"));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar cursos: " + e.getMessage());
        }
    }
}
