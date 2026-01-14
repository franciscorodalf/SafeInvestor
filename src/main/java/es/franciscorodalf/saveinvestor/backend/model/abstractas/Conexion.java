package es.franciscorodalf.saveinvestor.backend.model.abstractas;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Conexion {

    private static final String DEFAULT_DB_PATH = "src/main/resources/database/saveinvestor.db";
    private static final String SETUP_SCRIPT_PATH = "src/main/resources/database/setup.sql";
    private static boolean initialized;

    static {
        try {
            initializeDatabase();
        } catch (IOException | SQLException e) {
            throw new ExceptionInInitializerError("No se pudo inicializar la base de datos: " + e.getMessage());
        }
    }

    private String rutaArchivoBD;
    private static Connection connection;

    public Conexion() {
        this.rutaArchivoBD = DEFAULT_DB_PATH;
    }

    public Conexion(String unaRutaArchivoBD) throws SQLException {
        if (unaRutaArchivoBD == null || unaRutaArchivoBD.isEmpty()) {
            throw new SQLException("El fichero es nulo o vacío");
        }

        File file = new File(unaRutaArchivoBD);
        if (!file.exists()) {
            throw new SQLException("No existe la base de datos: " + unaRutaArchivoBD);
        }

        this.rutaArchivoBD = unaRutaArchivoBD;
    }

    public String getRutaArchivoBD() {
        return this.rutaArchivoBD;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + rutaArchivoBD);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener la conexión a la BD");
            e.printStackTrace();
        }

        return connection;
    }

    public Connection conectar() throws SQLException {
        return getConnection(); 
    }

    public static void cerrar() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }

    private static void initializeDatabase() throws IOException, SQLException {
        if (initialized) {
            return;
        }

        synchronized (Conexion.class) {
            if (initialized) {
                return;
            }

            Path dbPath = Paths.get(DEFAULT_DB_PATH).toAbsolutePath();
            Path scriptPath = Paths.get(SETUP_SCRIPT_PATH).toAbsolutePath();

            Files.createDirectories(dbPath.getParent());

            try (Connection initConnection = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                runSqlScript(initConnection, scriptPath);
            }

            initialized = true;
        }
    }

    private static void runSqlScript(Connection connection, Path scriptPath) throws IOException, SQLException {
        if (!Files.exists(scriptPath)) {
            throw new IOException("No se encontró el script de inicialización en " + scriptPath);
        }

        try (BufferedReader reader = Files.newBufferedReader(scriptPath, StandardCharsets.UTF_8);
             Statement statement = connection.createStatement()) {

            StringBuilder currentStatement = new StringBuilder();
            boolean insideBlockComment = false;
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (insideBlockComment) {
                    if (trimmed.contains("*/")) {
                        insideBlockComment = false;
                    }
                    continue;
                }

                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }

                if (trimmed.startsWith("/*")) {
                    insideBlockComment = !trimmed.contains("*/");
                    continue;
                }

                int inlineCommentIndex = line.indexOf("--");
                if (inlineCommentIndex >= 0) {
                    line = line.substring(0, inlineCommentIndex);
                }

                currentStatement.append(line).append('\n');

                if (trimmed.endsWith(";")) {
                    executeStatement(statement, currentStatement);
                    currentStatement.setLength(0);
                }
            }

            if (currentStatement.length() > 0) {
                executeStatement(statement, currentStatement);
            }
        }
    }

    private static void executeStatement(Statement statement, StringBuilder sqlBuilder) throws SQLException {
        String sql = sqlBuilder.toString().trim();
        if (sql.isEmpty()) {
            return;
        }
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        if (!sql.isEmpty()) {
            statement.execute(sql);
        }
    }
}
