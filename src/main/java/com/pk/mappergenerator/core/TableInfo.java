package com.pk.mappergenerator.core;

import com.pk.mappergenerator.config.DatabaseConfig;
import com.pk.mappergenerator.config.OtherConfig;
import com.pk.mappergenerator.parser.Parser;
import com.pk.mappergenerator.util.Const;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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

		if (StringUtils.isBlank(driver) || StringUtils.isBlank(url)
				|| StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
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

	public String getFieldsDeclareInfo() {
		StringBuffer sb = new StringBuffer(Const.TAB);
			sb.append("private Integer id;");
			sb.append(Const.ENDL);
		for (DataInfo dataInfo : dataInfos) {
			if (dataInfo.getFieldName().equalsIgnoreCase(Const.ID))
				continue;// id property is in the BaseModel
			if (!StringUtils.isBlank(dataInfo.getComment())) {
				sb.append(Const.TAB);
				sb.append("/*  ");
				sb.append(dataInfo.getComment());
				sb.append("  */");
			}
			sb.append(Const.ENDL);
			sb.append(Const.TAB);
			sb.append("private ");
			sb.append(dataInfo.getJavaType());
			sb.append(" ");
			sb.append(dataInfo.getFieldName());
			sb.append(";");
			sb.append(Const.ENDL);
		}
		return sb.toString();
	}

	public String getInsertStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append(Const.TAB2);
		sb.append("insert into ");
		sb.append(name);
		sb.append("(");
		sb.append(this.getColumnNames());
		sb.append(" ) ");
		sb.append(Const.ENDL);
		sb.append(Const.TAB2);
		sb.append("values (");
		sb.append("#{id},");
		for (DataInfo dataInfo : dataInfos) {
			sb.append("#{");
			sb.append(dataInfo.getFieldName());
			sb.append("},");
		}
		deleteLastStr(sb, 1);
		sb.append(")");
		return sb.toString();
	}

	public String getColumnNames() {
		StringBuilder sb = new StringBuilder();
		sb.append(primaryKey);
			sb.append(", ");
		for (DataInfo dataInfo : dataInfos) {
			sb.append(dataInfo.getColumnName());
			sb.append(", ");
		}
		deleteLastStr(sb, 2);
		return sb.toString();
	}

	public String getUpdateStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append(Const.TAB2);
		sb.append("update ");
		sb.append(name);
		sb.append(" set ");
		for (DataInfo dataInfo : dataInfos) {
			sb.append(dataInfo.getColumnName());
			sb.append("=#{");
			sb.append(dataInfo.getFieldName());
			sb.append("}, ");
		}
		deleteLastStr(sb, 2);
		sb.append(" where ");
		sb.append(primaryKey);
		sb.append("=#{id}");
		return sb.toString();
	}

	public String getResultMap() {
		StringBuilder sb = new StringBuilder();
		sb.append(Const.TAB2);
		sb.append("<id property=\"id\"");
		sb.append(" column=\"");
		sb.append(primaryKey);
		sb.append("\"");
		sb.append(" jdbcType=\"BIGINT\" />");
		sb.append(Const.ENDL);
		for (DataInfo dataInfo : dataInfos) {
			sb.append(Const.TAB2);
			sb.append("<result property=\"");
			sb.append(dataInfo.getFieldName());
			sb.append("\" column=\"");
			sb.append(dataInfo.getColumnName());
			sb.append("\"");
			sb.append(" jdbcType=\"");
			sb.append(dataInfo.getJdbcType());
			sb.append("\"");
			sb.append("/>");
			sb.append(Const.ENDL);
		}
		deleteLastStr(sb, Const.ENDL.length());
		return sb.toString();
	}

	public String getOtherCondition() {
		StringBuilder sb = new StringBuilder();
		for (DataInfo dataInfo : dataInfos) {
			sb.append(Const.TAB2);
			sb.append("<if test= \"");
			sb.append(dataInfo.getFieldName());
			sb.append(" != null\">");
			sb.append(" and ");
			sb.append(dataInfo.getColumnName());
			sb.append(" = #{");
			sb.append(dataInfo.getFieldName());
			sb.append("}");
			sb.append("</if>");
			sb.append(Const.ENDL);
		}
		deleteLastStr(sb, Const.ENDL.length());
		return sb.toString();
	}

	public String getUpdateMapModel() {// 动态字段更新
		StringBuffer sb = new StringBuffer();
		sb.append(Const.TAB2);
		sb.append("update ");
		sb.append(name);
		sb.append(Const.ENDL);
		sb.append(Const.TAB2);
		sb.append("<set>");
		sb.append(Const.ENDL);
		for (DataInfo dataInfo : dataInfos) {
			sb.append(Const.TAB3);
			sb.append("<if test=\"");
			sb.append(dataInfo.getFieldName());
			sb.append(" != null \"> ");
			sb.append(Const.ENDL);
			sb.append(Const.TAB4);
			sb.append(dataInfo.getColumnName());
			sb.append(" = #{");
			sb.append(dataInfo.getFieldName());
			sb.append("},");
			sb.append(Const.ENDL);
			sb.append(Const.TAB3);
			sb.append("</if>");
			sb.append(Const.ENDL);
		}
		sb.append(Const.TAB2);
		sb.append("</set>");
		sb.append(Const.ENDL);
		sb.append(Const.TAB2);
		sb.append(" where ");
		sb.append(primaryKey);
		sb.append("=#{id}");
		return sb.toString();
	}

	public void deleteLastStr(StringBuilder sb, int count) {
		int length = sb.length();
		if (length >= count) {
			sb.delete(length - count, length);
		} else {
			sb.delete(0, length);
		}
	}
}
