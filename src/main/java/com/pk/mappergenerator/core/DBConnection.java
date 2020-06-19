package com.pk.mappergenerator.core;

import com.pk.mappergenerator.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
@AllArgsConstructor
public class DBConnection {

    private Connection connection;
    private DatabaseMetaData metaData;

    public static DBConnection connection(DatabaseConfig config) {

        String driver = config.getDriver();
        String url = config.getUrl();
        String username = config.getUsername();
        String password = config.getPassword();

        if (StringUtils.isBlank(driver) || StringUtils.isBlank(url)
                || StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
           throw new IllegalArgumentException("数据库配置有误！");
        }

        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            if (conn == null) {
                log.error("Database connection is null");
                return null;
            }
            DatabaseMetaData metaData = conn.getMetaData();
            if (metaData == null) {
                log.error("Database MetaData is null");
                return null;
            }

            return new DBConnection(conn, metaData);
        } catch (ClassNotFoundException e) {
            log.error("Jdbc driver not found - {}", driver, e);
        } catch (SQLException e) {
            log.error("Database connect failed", e);
        }
        return null;
    }

}
