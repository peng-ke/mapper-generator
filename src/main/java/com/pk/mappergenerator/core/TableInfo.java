package com.pk.mappergenerator.core;

import com.pk.mappergenerator.config.DatabaseConfig;
import com.pk.mappergenerator.config.OtherConfig;
import com.pk.mappergenerator.parser.Parser;
import com.pk.mappergenerator.util.Const;
import com.pk.mappergenerator.util.StringUtil;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class TableInfo {

	private String name;
	private String primaryKey;
	private Parser parser;
	private LinkedHashSet<DataInfo> dataInfos = new LinkedHashSet<>();

	public static TableInfo parseTable(DatabaseConfig config, String tableName, OtherConfig otherConfig) {

		String driver = config.getDriver();
		String url = config.getUrl();
		String username = config.getUsername();
		String password = config.getPassword();

		if (StringUtil.isBlank(driver) || StringUtil.isBlank(url)
				|| StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
			throw new IllegalArgumentException("数据库配置有误！");
		}
		try (Connection conn = DriverManager.getConnection(url, username, password)) {
			Class.forName(driver);

			if (conn == null) {
				log.error("Database connection is null");
				return null;
			}
			DatabaseMetaData metaData = conn.getMetaData();
			if (metaData == null) {
				log.error("Database MetaData is null");
				return null;
			}
			return parseDbTable(conn, metaData, tableName, otherConfig);
		} catch (ClassNotFoundException e) {
			log.error("Jdbc driver not found - {}", driver, e);
		} catch (SQLException e) {
			log.error("Database connect failed", e);
		}
		return null;
	}

	private static TableInfo parseDbTable(Connection conn, DatabaseMetaData metaData, String tableName, OtherConfig otherConfig) {
		TableInfo tableInfo = new TableInfo();
		tableInfo.setName(tableName);
		tableInfo.setParser(new Parser(otherConfig));

		try (ResultSet primaryRS = metaData.getPrimaryKeys(null, null, tableName);
			 ResultSet generalRS = metaData.getColumns(conn.getCatalog(), null, tableName, null);) {
			if (primaryRS.next()) {
				tableInfo.setPrimaryKey(primaryRS.getString(Const.COLUMN_NAME));
			}
			if (primaryRS.next()) {
				log.error("该表为复合主键，不适用于代码脚手架生成工具");
				return null;
			}
			if (!generalRS.next()) {
				log.error("Table : {} not found.", tableName);
				return null;
			}

			while (generalRS.next()) {
				String columnName = generalRS.getString(Const.COLUMN_NAME);
				String columnType = generalRS.getString(Const.TYPE_NAME);
				String comment = null;
				if (otherConfig.isPojoComment()) {
					comment = generalRS.getString(Const.REMARKS);
				}
				DataInfo dataInfo = DataInfo.getDataInfo(columnName, columnType, comment, tableInfo.getParser());
				tableInfo.addDataInfo(dataInfo);
			}

		} catch (SQLException e) {
			log.error("Table : {}  parse error.", tableName, e);
			return null;
		}

		return tableInfo;
	}

	public void addDataInfo(DataInfo dataInfo) {
		dataInfos.add(dataInfo);
	}

}
