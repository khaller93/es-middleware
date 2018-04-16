package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.sparql.SelectQueryResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.VoidPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.sparql.SPARQLService;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that acquires all resources, potentially
 * only of specific classes or part of specific namespaces.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.source.all")
public class AllResources implements AcquisitionSource<VoidPayload> {

  private static final String ALL_QUERY = "SELECT DISTINCT ?s WHERE { ?s ?p ?o }";

  private final SPARQLService sparqlService;

  @Autowired
  public AllResources(SPARQLService sparqlService) {
    this.sparqlService = sparqlService;
  }

  @Override
  public Class<VoidPayload> getParameterClass() {
    return VoidPayload.class;
  }

  @Override
  public ExplorationContext apply(VoidPayload payload) {
    SelectQueryResult result = (SelectQueryResult) sparqlService.query(ALL_QUERY, true);
    List<BlankNodeOrIRI> resourcesList = new LinkedList<>();
    for (RDFTerm sTerm : result.value().column("s").values()) {
      if (sTerm instanceof BlankNodeOrIRI) {
        resourcesList.add((BlankNodeOrIRI) sTerm);
      }
    }
    return new ResourceList(resourcesList);
  }
}
