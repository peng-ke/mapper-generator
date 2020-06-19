package com.pk.mappergenerator.core;

import com.pk.mappergenerator.parser.Parser;
import lombok.Data;

@Data
public class DataInfo {
	private String fieldName;
	private String columnName;
	private String javaType;
	private String jdbcType;
	private String comment;

	private DataInfo(String fieldName, String columnName, String javaType, String jdbcType, String comment) {
		this.fieldName = fieldName;
		this.columnName = columnName;
		this.javaType = javaType;
		this.jdbcType = jdbcType;
		this.comment = comment;
	}

	public static DataInfo getDataInfo(String columnName, String columnType, String comment, Parser parser) {
		String fildName = parser.getFildName(columnName);
		String javaType = parser.getJavaType(columnType);
		return new DataInfo(fildName, columnName, javaType, columnType, comment);
	}
}
