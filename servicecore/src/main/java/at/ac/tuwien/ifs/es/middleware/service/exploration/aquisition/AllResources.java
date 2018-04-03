package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationResponse;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that acquires all resources, potentially
 * only of specific classes.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("AllAcquisitionSource")
public class AllResources implements AcquisitionSource {

  private final SPARQLService sparqlService;

  @Autowired
  public AllResources(SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public ExplorationResponse apply(JsonNode parameterMap) {
    //TODO: Implement
    return null;
  }
}
