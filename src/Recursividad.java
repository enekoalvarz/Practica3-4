import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Recursividad {

    Connection conn;

    // METODO 1
    public String invertirFrase(String frase) {
        if (frase.isEmpty() || frase.length() == 1) {
            return frase;
        } else {
            return invertirFrase(frase.substring(1)) + frase.charAt(0);
        }
    }

    //METODO 2
    public String invertirPalabras(String frase) {
        if (frase.isEmpty()) {
            return frase;
        } else {
            String[] palabras = frase.split("\\s", 2);
            return invertirFrase(palabras[0]) + " " + invertirPalabras(palabras.length > 1 ? palabras[1] : "");
        }
    }

    //METODO 3
    public void posiblesManos(int n, ArrayList<Carta> baraja) {
        ArrayList<Carta> manoActual = new ArrayList<>();
        generarCombinaciones(n, baraja, 0, manoActual);
    }
    private void generarCombinaciones(int n, ArrayList<Carta> baraja, int indice, ArrayList<Carta> manoActual) {
        if (manoActual.size() == n) {
            imprimirMano(manoActual);
            return;
        }

        for (int i = indice; i < baraja.size(); i++) {
            // Agregar la carta actual a la mano temporal
            manoActual.add(baraja.get(i));
            // obtener las combinaciones con la carta actual incluida
            generarCombinaciones(n, baraja, i + 1, manoActual);
            // Eliminar la carta actual para probar con la siguiente combinación
            manoActual.remove(manoActual.size() - 1);
        }
    }
    private void imprimirMano(ArrayList<Carta> mano) {
        for (Carta carta : mano) {
            System.out.print(carta.getValor() + " de " + carta.getPalo() + ", ");
        }
        System.out.println();
    }

    //METODO 4
    public void filtrarManos(int n, ArrayList<Carta> baraja, String condicion) {
        ArrayList<Carta> manoActual = new ArrayList<>();
        generarCombinacionesConCondicion(n, baraja, 0, manoActual, condicion);
    }
    private void generarCombinacionesConCondicion(int n, ArrayList<Carta> baraja, int indice, ArrayList<Carta> manoActual, String condicion) {
        if (manoActual.size() == n) {
            if (cumpleCondicion(manoActual, condicion)) {
                imprimirMano(manoActual);
            }
            return;
        }

        for (int i = indice; i < baraja.size(); i++) {
            manoActual.add(baraja.get(i));
            generarCombinacionesConCondicion(n, baraja, i + 1, manoActual, condicion);
            manoActual.remove(manoActual.size() - 1);
        }
    }
    private boolean cumpleCondicion(ArrayList<Carta> mano, String condicion) {
        for (Carta carta : mano) {
            if (carta.getValor().equals(condicion)) {
                return true; // La mano tiene al menos un As
            }
        }
        return false;
    }

    //METODO 5
    public void guardaManos(String filtro, ArrayList<Carta> mano) {
        conn = null;
        PreparedStatement stmt = null;
        String password = JOptionPane.showInputDialog("Siendo la url de la BD 'jdbc:mysql://localhost:3306/progpractica4' \ny el usuario 'root', introduce la contraseña:");

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/progpractica4", "root", password);
            if(!existeTablaCarta(conn)){
                new CrearTablas(conn);
                inicializarFiltros(conn);
                inicializarCartas(conn);
            }

            // Inserción en las tablas
            int idFiltro = obtenerIdFiltro(conn, filtro);
            int idMano = guardarManoFiltro(conn, idFiltro);
            guardarCartasMano(conn, idMano, mano);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private int obtenerIdFiltro(Connection conn, String filtro) throws SQLException {
        String sql = "SELECT id_filtro FROM Filtro WHERE codigo = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, filtro);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("id_filtro");
        } else {
            throw new SQLException("No se encontró el filtro con código: " + filtro);
        }
    }
    private int guardarManoFiltro(Connection conn, int idFiltro) throws SQLException {
        String sql = "INSERT INTO ManoFiltro (id_filtro) VALUES (?)";
        PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, idFiltro);
        stmt.executeUpdate();

        ResultSet generatedKeys = stmt.getGeneratedKeys();

        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        } else {
            throw new SQLException("No se pudo obtener el ID generado para la mano");
        }
    }
    private void guardarCartasMano(Connection conn, int idMano, ArrayList<Carta> mano) throws SQLException {
        String sql = "INSERT INTO CartaMano (id_mano, id_carta) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        for (Carta carta : mano) {
            stmt.setInt(1, idMano);
            stmt.setInt(2, obtenerIdCarta(conn, carta));
            stmt.executeUpdate();
        }
    }
    private int obtenerIdCarta(Connection conn, Carta carta) throws SQLException {
        String sql = "SELECT id_carta FROM Carta WHERE palo = ? AND valor = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, carta.getPalo());
        stmt.setString(2, carta.getValor());
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("id_carta");
        } else {
            throw new SQLException("No se encontró la carta en la base de datos");
        }    }

    private void inicializarFiltros(Connection conn) {
        try{
            String[] filtros = {
                    "Poker",
                    "Full",
                    "Escalera"
            };

            String insertQuery = "INSERT INTO Filtro (codigo) VALUES (?) ON DUPLICATE KEY UPDATE codigo = codigo";
            PreparedStatement stmt = conn.prepareStatement(insertQuery);

            for (String filtro : filtros) {
                stmt.setString(1, filtro);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void inicializarCartas(Connection conn){
        try{
            String[] palos = {"Corazones", "Diamantes", "Picas", "Treboles"};
            String[] valores = {"As", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jota", "Reina", "Rey"};

            String sql = "INSERT INTO Carta (palo, valor) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (String palo : palos) {
                for (String valor : valores) {
                    stmt.setString(1, palo);
                    stmt.setString(2, valor);
                    stmt.addBatch();
                }
            }

            // Ejecutar inserccion
            int[] result = stmt.executeBatch();
            System.out.println("Se han insertado " + result.length + " cartas en la base de datos.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean existeTablaCarta(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(null, null, "Carta", null);
        return rs.next();
    }

    public static void main(String[] args) {
        // EJEMPLOS
        String frase1 = "Hola mundo!";
        String frase2 = "Frase 2 para invertir";
        String frase3 = "Deusto";

        // EJEMPLOS 2
        ArrayList<Carta> baraja = new ArrayList<>();
        baraja.add(new Carta("Corazones", "As"));
        baraja.add(new Carta("Diamantes", "Rey"));
        baraja.add(new Carta("Picas", "Jota"));
        baraja.add(new Carta("Treboles", "Dos"));

        Recursividad recursividad = new Recursividad();

        //Metodo 1:
        System.out.println("Frase 1: "+frase1 +"\nInvertida: "+recursividad.invertirFrase(frase1));
        System.out.println("Frase 2: "+frase2 +"\nInvertida: "+recursividad.invertirFrase(frase2));
        System.out.println("Frase 3: "+frase3 +"\nInvertida: "+recursividad.invertirFrase(frase3)+"\n");

        //Metedo 2:
        System.out.println("Frase 1: "+frase1+"\nInvertida por palabras: "+recursividad.invertirPalabras(frase1));
        System.out.println("Frase 2: "+frase2+"\nInvertida por palabras: "+recursividad.invertirPalabras(frase2)+"\n");

        //Metodo 3:
        System.out.println("Posibles manos de 2 cartas: ");
        recursividad.posiblesManos(2, baraja); //Include un print en uno de sus metodos
        System.out.println("Posibles manos de 3 cartas:");
        recursividad.posiblesManos(3, baraja);

        //Metodo 4:
        System.out.println("\nPosibles manos de 2 cartas con condición de As:");
        recursividad.filtrarManos(2, baraja, "As");
        System.out.println("Posibles manos de 3 cartas con condición de Rey:");
        recursividad.filtrarManos(3, baraja, "Rey");

        //Metodo 5:
        recursividad.guardaManos("Poker", baraja);

    }
}
