package org.joint.modules.system.dict;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DictSchemaAlignmentTest {

    private static final Path MYSQL_SCHEMA = Path.of("src/main/resources/sql/schema.sql");
    private static final Path H2_SCHEMA = Path.of("src/test/resources/schema-h2.sql");

    @Test
    void schemaSeedsSystemDictMenuAndPermissions() throws Exception {
        String mysqlSchema = Files.readString(MYSQL_SCHEMA);

        assertThat(mysqlSchema).contains("CREATE TABLE IF NOT EXISTS dict_types");
        assertThat(mysqlSchema).contains("CREATE TABLE IF NOT EXISTS dict_data");
        assertThat(mysqlSchema).contains("'SystemDict'");
        assertThat(mysqlSchema).contains("'system:dict:list'");
        assertThat(mysqlSchema).contains("'system:dict:query'");
        assertThat(mysqlSchema).contains("'system:dict:create'");
        assertThat(mysqlSchema).contains("'system:dict:update'");
        assertThat(mysqlSchema).contains("'system:dict:delete'");
    }

    @Test
    void h2SchemaContainsDictTablesForIntegrationTests() throws Exception {
        String h2Schema = Files.readString(H2_SCHEMA);

        assertThat(h2Schema).contains("CREATE TABLE IF NOT EXISTS dict_types");
        assertThat(h2Schema).contains("CREATE TABLE IF NOT EXISTS dict_data");
    }
}
