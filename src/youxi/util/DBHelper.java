package youxi.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBHelper {

    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.dbUrl());
        config.setUsername(Config.dbUser());
        config.setPassword(Config.dbPassword());
        config.setMaximumPoolSize(Config.dbMaxPool());
        config.setMinimumIdle(Config.dbMinIdle());
        config.setConnectionTimeout(Config.dbConnTimeout());
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionInitSql("SET NAMES utf8mb4");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
