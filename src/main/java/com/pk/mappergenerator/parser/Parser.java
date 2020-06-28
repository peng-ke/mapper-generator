package com.pk.mappergenerator.parser;


import com.pk.mappergenerator.config.OtherConfig;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class Parser {

	private final Map<String, String> JDBC_2_MYBATIS = new HashMap<>();
	private final Map<String, String> JDBC_2_JAVA = new HashMap<>();
	private OtherConfig otherConfig;

	public Parser(OtherConfig otherConfig){
		JDBC_2_MYBATIS.put("TINYINT", "TINYINT");
		JDBC_2_MYBATIS.put("INT", "INTEGER");
		JDBC_2_MYBATIS.put("BIGINT", "BIGINT");
		JDBC_2_MYBATIS.put("CHAR", "CHAR");
		JDBC_2_MYBATIS.put("VARCHAR", "VARCHAR");
		JDBC_2_MYBATIS.put("TEXT", "LONGVARCHAR");
		JDBC_2_MYBATIS.put("DATE", "DATE");
		JDBC_2_MYBATIS.put("DATETIME", "TIMESTAMP");
		JDBC_2_MYBATIS.put("TIMESTAMP", "TIMESTAMP");
		JDBC_2_MYBATIS.put("FLOAT", "REAL");
		JDBC_2_MYBATIS.put("DOUBLE", "DOUBLE");
		JDBC_2_MYBATIS.put("DECIMAL", "DECIMAL");
		JDBC_2_MYBATIS.put("BLOB", "LONGVARBINARY");

		JDBC_2_JAVA.put("TINYINT", "Byte");
		JDBC_2_JAVA.put("SMALLINT", "Integer");
		JDBC_2_JAVA.put("INT", "Integer");
		JDBC_2_JAVA.put("BIGINT", "Long");
		JDBC_2_JAVA.put("CHAR", "String");
		JDBC_2_JAVA.put("VARCHAR", "String");
		JDBC_2_JAVA.put("TEXT", "String");
		JDBC_2_JAVA.put("FLOAT", "Float");
		JDBC_2_JAVA.put("DOUBLE", "Double");
		JDBC_2_JAVA.put("DECIMAL", "java.math.BigDecimal");
		JDBC_2_JAVA.put("BLOB", "byte[]");

		if (otherConfig.isUseJava8Time()) {
			JDBC_2_JAVA.put("DATE", "java.time.LocalDate");
			JDBC_2_JAVA.put("DATETIME", "java.time.LocalDateTime");
			JDBC_2_JAVA.put("TIMESTAMP", "java.time.LocalDateTime");
		} else {
			JDBC_2_JAVA.put("DATE", "java.util.Date");
			JDBC_2_JAVA.put("DATETIME", "java.util.Date");
			JDBC_2_JAVA.put("TIMESTAMP", "java.util.Date");
		}
	}

	public String getJavaType(String jdbcType) {
		return JDBC_2_JAVA.get(getKey(jdbcType));
	}

	public String getMybatisType(String columnType) {
		return JDBC_2_MYBATIS.get(getKey(columnType));
	}

	private String getKey(String type) {
		return type.toUpperCase().replace(" UNSIGNED", "");
	}
}
