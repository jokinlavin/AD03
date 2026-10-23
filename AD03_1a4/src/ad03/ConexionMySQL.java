package ad03;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionMySQL {
    
    // Constantes con la información de conexión
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/dbformacion");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "secret");
    
    private ConexionMySQL() {}
    
    // Método que abre la conexión con la BBDD. Lo emplean los ejercicio del 1 al 4
    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
