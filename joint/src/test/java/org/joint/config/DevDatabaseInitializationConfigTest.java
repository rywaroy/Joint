package org.joint.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class DevDatabaseInitializationConfigTest {

    @Test
    void devProfileEnablesSqlSchemaInitialization() throws IOException {
        Properties properties = new Properties();
        try (var inputStream = new ClassPathResource("application-dev.properties").getInputStream()) {
            properties.load(inputStream);
        }

        assertThat(properties.getProperty("spring.sql.init.mode")).isEqualTo("always");
        assertThat(properties.getProperty("spring.sql.init.schema-locations"))
                .isEqualTo("classpath:sql/schema.sql");
    }

    @Test
    void mysqlSchemaScriptDoesNotHardcodeDatabaseSelection() throws IOException {
        String schemaSql = new ClassPathResource("sql/schema.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(schemaSql).doesNotContain("CREATE DATABASE IF NOT EXISTS");
        assertThat(schemaSql).doesNotContain("USE joint;");
    }

    @Test
    void mysqlSchemaScriptBackfillsDeptRemarkColumnForExistingTables() throws IOException {
        String schemaSql = new ClassPathResource("sql/schema.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(schemaSql).contains("information_schema.COLUMNS");
        assertThat(schemaSql).contains("COLUMN_NAME = 'remark'");
        assertThat(schemaSql).contains("ALTER TABLE depts ADD COLUMN remark");
        assertThat(schemaSql).doesNotContain("ADD COLUMN IF NOT EXISTS remark");
    }
}
