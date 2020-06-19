package com.pk.mappergenerator.parser;


import com.pk.mappergenerator.config.OtherConfig;
import com.pk.mappergenerator.util.Const;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class Parser {

	private final Map<String, String> JAVA_2_JDBC = new HashMap<>();
	private final Map<String, String> JDBC_2_JAVA = new HashMap<>();
	private OtherConfig otherConfig;

	public Parser(OtherConfig otherConfig){
		JAVA_2_JDBC.put("String", "VARCHAR");
		JAVA_2_JDBC.put("java.util.Date", "DATE");
		JAVA_2_JDBC.put("java.time.LocalDate", "DATE");
		JAVA_2_JDBC.put("java.time.LocalDateTime", "DATE");
		JAVA_2_JDBC.put("java.math.BigDecimal", "DECIMAL");
		JAVA_2_JDBC.put("Float", "NUMERIC");
		JAVA_2_JDBC.put("Double", "NUMERIC");
		JAVA_2_JDBC.put("Long", "NUMERIC");
		JAVA_2_JDBC.put("Integer", "NUMERIC");
		JAVA_2_JDBC.put("int", "NUMERIC");

		JDBC_2_JAVA.put("CHAR", "String");
		JDBC_2_JAVA.put("VARCHAR", "String");
		JDBC_2_JAVA.put("CLOB", "String");
		JDBC_2_JAVA.put("TEXT", "String");
		JDBC_2_JAVA.put("DECIMAL", "java.math.BigDecimal");
		JDBC_2_JAVA.put("FLOAT", "Float");
		JDBC_2_JAVA.put("DOUBLE", "Double");
		JDBC_2_JAVA.put("BIGINT", "Long");
		JDBC_2_JAVA.put("INT", "Integer");
		JDBC_2_JAVA.put("TINYINT", "Integer");

		if (otherConfig.isUseJava8Time()) {
			JDBC_2_JAVA.put("DATE", "java.time.LocalDate");
			JDBC_2_JAVA.put("DATETIME", "java.time.LocalDateTime");
		} else {
			JDBC_2_JAVA.put("DATE", "java.util.Date");
			JDBC_2_JAVA.put("DATETIME", "java.util.Date");
		}
	}

	public static String getFildName(String columnName) {
		String[] items = columnName.split(Const.UNDER_LINE);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			items[i] = items[i].toLowerCase();
			if (i > 0) {
				builder.append(StringUtils.capitalize(items[i]));
			} else {
				builder.append(items[i]);
			}
		}
		return builder.toString();
	}

	public String getJavaType(String jdbcType) {
		return JDBC_2_JAVA.get(jdbcType.toUpperCase());
	}
}
