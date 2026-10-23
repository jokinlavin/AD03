package ad03;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class Ejercicio3MatricularAlumno {

    public static void main(String[] args) {

        // Leemos los datos del teclado
        try (Scanner sc = new Scanner(System.in); Connection con = ConexionMySQL.abrir()) {
            System.out.print("DNI (8 números y una letra): ");
            // Quitamos los espcaios y ponemos en mayúscula
            String dni = sc.nextLine().trim().toUpperCase();
            // Validamos el formato del dni introducido
            if (!dni.matches("\\d{8}[A-Z]")) {
                System.out.println("DNI no válido.");
                return;
            }

            String nombre = buscarNombre(con, dni);
            boolean nuevo = nombre == null;
            if (nuevo) {
                System.out.print("Nombre del nuevo alumno: ");
                nombre = sc.nextLine().trim();
                if (nombre.isEmpty()) {
                    System.out.println("El nombre no puede estar vacío.");
                    return;
                }
            } else {
                System.out.println("Alumno existente: " + nombre);
            }

            mostrarCursos(con);
            System.out.print("ID del curso: ");
            int id;
            try { id = Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) {
                System.out.println("El ID debe ser un entero.");
                return;
            }

            boolean autoOriginal = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                if (!existeCurso(con, id)) throw new OperacionInvalida("El curso no existe.");
                if (yaMatriculado(con, dni, id)) throw new OperacionInvalida("El alumno ya está matriculado.");
                int capacidad = capacidad(con, id);
                int ocupadas = contar(con, id);
                if (ocupadas >= capacidad) throw new OperacionInvalida("El curso está completo.");

                if (nuevo) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO alumnos(dni,nombre) VALUES (?,?)")) {
                        ps.setString(1, dni); ps.setString(2, nombre); ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO matriculas(dni,id_curso,fecha_matricula) VALUES (?,?,?)")) {
                    ps.setString(1, dni); ps.setInt(2, id);
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }
                con.commit();
                System.out.println("Matrícula registrada correctamente.");
                if (nuevo) System.out.println("Alumno dado de alta en la misma transacción.");
            } catch (SQLException | OperacionInvalida e) {
                try { con.rollback(); } catch (SQLException re) { e.addSuppressed(re); }
                System.out.println("Operación cancelada: " + e.getMessage());
                System.out.println("Se ha ejecutado rollback.");
            } finally {
                try { con.setAutoCommit(autoOriginal); }
                catch (SQLException e) { System.err.println("No se pudo restaurar autoCommit: " + e.getMessage()); }
            }
        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        }
    }

    // Método para buscar el nombre
    private static String buscarNombre(Connection c, String dni) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT nombre FROM alumnos WHERE dni=?")) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }

    // Método para visualizar los cursos
    private static void mostrarCursos(Connection c) throws SQLException {
        String sql = "SELECT c.id_curso,c.titulo,a.capacidad,COUNT(m.dni) AS ocupadas," +
            " a.capacidad-COUNT(m.dni) AS libres" +
            " FROM cursos c JOIN aulas a ON a.id_aula=c.id_aula" +
            " LEFT JOIN matriculas m ON m.id_curso=c.id_curso " +
            " GROUP BY c.id_curso,c.titulo,c.fecha,a.capacidad" +
            " ORDER BY c.fecha,c.id_curso";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println("Cursos y plazas libres:");
            while (rs.next()) System.out.printf("%d | %s | libres %d de %d%n",
                rs.getInt("id_curso"), rs.getString("titulo"), rs.getInt("libres"), rs.getInt("capacidad"));
        }
    }
    
    // Comprobación del curso
    private static boolean existeCurso(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM cursos WHERE id_curso=?")) {
            ps.setInt(1,id); try (ResultSet rs=ps.executeQuery()) { return rs.next(); }
        }
    }
    
    // Comprobación de matrícula
    private static boolean yaMatriculado(Connection c, String dni, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM matriculas WHERE dni=? AND id_curso=?")) {
            ps.setString(1,dni); ps.setInt(2,id); try (ResultSet rs=ps.executeQuery()) { return rs.next(); }
        }
    }
    
    // Comprobación de la capacidad del curso
    private static int capacidad(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT a.capacidad FROM cursos c JOIN aulas a ON a.id_aula=c.id_aula WHERE c.id_curso=?")) {
            ps.setInt(1,id); try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se encontró la capacidad del curso.");
                return rs.getInt(1);
            }
        }
    }
    
    // Contamos las matrícula del curso
    private static int contar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM matriculas WHERE id_curso=?")) {
            ps.setInt(1,id); try (ResultSet rs=ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }
    
    private static class OperacionInvalida extends Exception {
        OperacionInvalida(String mensaje) { super(mensaje); }
    }
}
