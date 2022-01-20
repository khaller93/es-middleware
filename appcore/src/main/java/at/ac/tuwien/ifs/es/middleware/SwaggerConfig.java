package at.ac.tuwien.ifs.es.middleware;

import java.util.ArrayList;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
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

  private static final Contact GENERIC_CONTACT = new Contact("SemSys",
      "https://semsys.ifs.tuwien.ac.at/", "semsys@list.tuwien.ac.at");

  private final ApiInfo API_INFO;

  @Autowired
  public SwaggerConfig(VersionManager versionManager) {
    API_INFO = new ApiInfo("Exploratory Search Service",
        "This application provides services for exploring a knowledge graph stored in a supported triplestore/graph database.",
        versionManager.getVersion(), "urn:tos", GENERIC_CONTACT, null, null,
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
