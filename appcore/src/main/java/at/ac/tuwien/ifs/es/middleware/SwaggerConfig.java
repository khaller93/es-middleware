package at.ac.tuwien.ifs.es.middleware;

import java.util.ArrayList;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  private static final Contact GENERIC_CONTACT = new Contact("IFS-TU",
      "https://www.ifs.tuwien.ac.at/", "sek@ifs.tuwien.ac.at");

  private final ApiInfo API_INFO;
  @Value("${application.version}")
  private String version;

  public SwaggerConfig() {
    API_INFO = new ApiInfo("Exploratory Search Middleware",
        "This application provides services for exploring a knowledge graph stored in a supported triplestore/graph database.",
        version, "urn:tos", GENERIC_CONTACT, "MIT", "https://opensource.org/licenses/MIT",
        new ArrayList<VendorExtension>());
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(API_INFO)
        .produces(Collections.singleton("application/json"))
        .consumes(Collections.singleton("application/json"));
  }

}
