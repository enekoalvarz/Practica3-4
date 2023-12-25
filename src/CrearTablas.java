import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CrearTablas {

    public CrearTablas(Connection conn){
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            String[] queries = {
                    "CREATE TABLE IF NOT EXISTS Carta (id_carta INT AUTO_INCREMENT PRIMARY KEY, palo VARCHAR(20), valor VARCHAR(10));",
                    "CREATE TABLE IF NOT EXISTS Filtro (id_filtro INT AUTO_INCREMENT PRIMARY KEY, codigo VARCHAR(20), texto_explicativo VARCHAR(100));",
                    "CREATE TABLE IF NOT EXISTS ManoFiltro (id_mano INT AUTO_INCREMENT PRIMARY KEY, id_filtro INT, FOREIGN KEY (id_filtro) REFERENCES Filtro(id_filtro));",
                    "CREATE TABLE IF NOT EXISTS CartaMano (id_carta_mano INT AUTO_INCREMENT PRIMARY KEY, id_mano INT, id_carta INT, FOREIGN KEY (id_mano) REFERENCES ManoFiltro(id_mano), FOREIGN KEY (id_carta) REFERENCES Carta(id_carta));"
            };

            for (String query : queries) {
                stmt.executeUpdate(query);
            }

            System.out.println("TABLAS CREADAS EN CASO NECESARIO!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                //if (conn != null) conn.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
