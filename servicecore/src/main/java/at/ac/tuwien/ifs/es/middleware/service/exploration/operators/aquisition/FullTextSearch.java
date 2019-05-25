package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.ResourceCollection;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.resources.ResourceList;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.FullTextSearchPayload;
import at.ac.tuwien.ifs.es.middleware.common.exploration.RegisterForExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts.FullTextSearchService;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDFTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation get {@link AcquisitionSource} that searches for resources with the
 * given keyword. The result list is ordered descending by the full-text-search score get the
 * result. This operator is registered as {@code esm.source.fts} at {@link
 * at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow(FullTextSearch.OID)
public class FullTextSearch implements
    AcquisitionSource<ResourceCollection, FullTextSearchPayload> {

  private static final Logger logger = LoggerFactory.getLogger(FullTextSearch.class);

  public static final String OID = "esm.source.fts";

  private final FullTextSearchService fullTextSearchService;

  @Autowired
  public FullTextSearch(FullTextSearchService fullTextSearchService) {
    this.fullTextSearchService = fullTextSearchService;
  }

  @Override
  public String getUID() {
    return OID;
  }

  @Override
  public Class<ResourceCollection> getExplorationContextOutputClass() {
    return ResourceCollection.class;
  }

  @Override
  public Class<FullTextSearchPayload> getPayloadClass() {
    return FullTextSearchPayload.class;
  }

  @Override
  public ResourceCollection apply(FullTextSearchPayload payload) {
    List<Map<String, RDFTerm>> fullTextResultTable = fullTextSearchService
        .searchFullText(payload.getKeyword(),
            payload.getClasses() != null ? payload.getClasses().stream().map(Resource::value)
                .collect(Collectors.toList()) : Collections.emptyList(),
            payload.getOffset(), payload.getLimit(), payload.getFacetFilters());
    LinkedList<BlankNodeOrIRI> resourceList = new LinkedList<>();
    LinkedHashMap<String, Double> scoreMap = new LinkedHashMap<>();
    for (Map<String, RDFTerm> row : fullTextResultTable) {
      BlankNodeOrIRI resource = (BlankNodeOrIRI) row.get("resource");
      if (row.containsKey("score")) {
        scoreMap.put(RDFTermJsonUtil.stringValue(resource),
            Double.parseDouble(((Literal) row.get("score")).getLexicalForm()));
      }
      resourceList.addLast(resource);
    }
    ResourceList rlContext = ResourceList.of(resourceList);
    if (!scoreMap.isEmpty()) {
      for (Entry<String, Double> e : scoreMap.entrySet()) {
        rlContext.values().put(e.getKey(), JsonPointer.compile("/fts/score"),
            JsonNodeFactory.instance.numberNode(e.getValue()));
      }
    } else {
      logger.debug("The used full-text-search dao '{}' does not hand over a score.",
          fullTextSearchService.toString());
    }
    return rlContext;
  }
}
