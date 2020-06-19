package com.pk.mappergenerator.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DatabaseConfig {
    private String url;
    private String driver;
    private String username;
    private String password;
}

