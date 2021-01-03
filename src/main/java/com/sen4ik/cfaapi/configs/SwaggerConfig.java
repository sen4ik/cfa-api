package com.sen4ik.cfaapi.configs;

import com.google.common.base.Predicates;
import com.sen4ik.cfaapi.base.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.builders.ApiInfoBuilder;

import static io.swagger.models.auth.In.HEADER;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger2
@Slf4j
public class SwaggerConfig extends WebMvcConfigurationSupport {

    // https://stackoverflow.com/questions/43545540/swagger-ui-no-mapping-found-for-http-request

    @Value("${prop.swagger.enabled}")
    private boolean enableSwagger;

    // @Value("${springfox.documentation.swagger.v2.host}")
    @Value("${server.host}")
    private String swaggerHost;

    @Bean
    public Docket api() {

        log.info("enableSwagger: " + enableSwagger);
        log.info("swaggerHost: " + swaggerHost);

        return new Docket(DocumentationType.SWAGGER_2)
                // https://github.com/springfox/springfox/issues/2194
                .securitySchemes(
                        singletonList(
                                new ApiKey("JWT", AUTHORIZATION, HEADER.name())
                        )
                )
                .securityContexts(
                        securityContext()
                )
                .enable(enableSwagger)
                .host(swaggerHost)
                .apiInfo(metaData())
                .select()
                // .apis(RequestHandlerSelectors.any())
                .apis(RequestHandlerSelectors.basePackage("com.sen4ik.cfaapi.controllers"))
                .paths(PathSelectors.any())
                .build();
    }

    private List<SecurityReference> securityReference = singletonList(SecurityReference.builder()
            .reference("JWT")
            .scopes(new AuthorizationScope[0])
            .build()
    );

    private List<HttpMethod> methods = Arrays.asList(
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.DELETE
    );

    private List<SecurityContext> securityContext() {
        List<SecurityContext> lsc = new ArrayList<>();

        lsc.add(SecurityContext.builder()
                .securityReferences(securityReference)
                .forPaths(PathSelectors.ant(Constants.API_PREFIX + "/category/**"))
                .forHttpMethods(Predicates.in(methods))
                .build()
        );

        lsc.add(SecurityContext.builder()
                .securityReferences(securityReference)
                .forPaths(PathSelectors.ant(Constants.API_PREFIX + "/tag/**"))
                .forHttpMethods(Predicates.in(methods))
                .build()
        );

        lsc.add(SecurityContext.builder()
                .securityReferences(securityReference)
                .forPaths(PathSelectors.ant(Constants.API_PREFIX + "/file/**"))
                .forHttpMethods(Predicates.in(methods))
                .build()
        );

        lsc.add(SecurityContext.builder()
                .securityReferences(securityReference)
                .forPaths(PathSelectors.ant(Constants.API_PREFIX + "/user/**"))
                .build()
        );

        lsc.add(SecurityContext.builder()
                .securityReferences(securityReference)
                .forPaths(PathSelectors.ant(Constants.API_PREFIX + "/everything/**"))
                .build()
        );

        lsc.add(SecurityContext.builder()
                .securityReferences(securityReference)
                .forPaths(PathSelectors.ant(Constants.API_PREFIX + "/playlist/**"))
                .build()
        );

        return lsc;
    }

    @Bean
    public UiConfiguration uiConfiguration() {
        return UiConfigurationBuilder.builder()
            // .deepLinking(true)
            // .displayOperationId(false)
            .defaultModelsExpandDepth(-1)
            .defaultModelExpandDepth(-1)
            // .defaultModelRendering(ModelRendering.EXAMPLE)
            // .displayRequestDuration(false)
            .docExpansion(DocExpansion.LIST)
            // .filter(false)
            // .maxDisplayedTags(null)
            // .operationsSorter(OperationsSorter.ALPHA)
            // .showExtensions(false)
            // .tagsSorter(TagsSorter.ALPHA)
            // .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
            // .validatorUrl(null)
            .build();
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder().title("CFA API Documentation")
                // .description("CFA API Documentation")
                .version("1.0")
                // .termsOfServiceUrl("http://something.com")
                .contact(new Contact("Artur Sentsov", null, ""))
                .license("Apache License Version 2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (enableSwagger) {
            registry.addResourceHandler("swagger-ui.html")
                    .addResourceLocations("classpath:/META-INF/resources/");
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
    }

}
