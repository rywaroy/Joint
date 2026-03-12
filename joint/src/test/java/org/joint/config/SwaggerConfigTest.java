package org.joint.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void openApiContainsJointMetadataAndBearerScheme() {
        SwaggerConfig swaggerConfig = new SwaggerConfig();

        OpenAPI openAPI = swaggerConfig.openAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Joint API");
        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey("Bearer");
        assertThat(openAPI.getSecurity()).hasSize(1);
    }
}
