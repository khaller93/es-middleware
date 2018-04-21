package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition.FullTextSearchPayload;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that searches for resources with the given
 * keyword. The result list is ordered descending by the full-text-search score of the result. This
 * operator is registered as {@code esm.source.fts} at {@link at.ac.tuwien.ifs.es.middleware.service.exploration.registry.ExplorationFlowRegistry}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
@RegisterForExplorationFlow("esm.source.fts")
public class FullTextSearch implements AcquisitionSource<FullTextSearchPayload> {

  private static final Logger logger = LoggerFactory.getLogger(FullTextSearch.class);

  private FullTextSearchDAO fullTextSearchDAO;

  public FullTextSearch(
      @Autowired @Qualifier("SpecificFullTextSearchDAO") FullTextSearchDAO fullTextSearchDAO) {
    this.fullTextSearchDAO = fullTextSearchDAO;
  }

  @Override
  public Class<FullTextSearchPayload> getParameterClass() {
    return FullTextSearchPayload.class;
  }

  @Override
  public ExplorationContext apply(FullTextSearchPayload payload) {
    logger.debug("FTS request {} handed over to exploration flow.");
    List<Map<String, RDFTerm>> fullTextResultTable = fullTextSearchDAO
        .searchFullText(payload.getKeyword(),
            payload.getClasses() != null ? payload.getClasses().stream().map(Resource::value)
                .collect(Collectors.toList()) : Collections.emptyList(),
            payload.getOffset(), payload.getLimit());
    LinkedList<BlankNodeOrIRI> resourceList = new LinkedList<>();
    LinkedHashMap<String, Double> scoreMap = new LinkedHashMap<>();
    for (Map<String, RDFTerm> row : fullTextResultTable) {
      BlankNodeOrIRI resource = (BlankNodeOrIRI) row.get("resource");
      if (row.containsKey("score")) {
        scoreMap.put(BlankOrIRIJsonUtil.stringValue(resource),
            Double.parseDouble(((Literal) row.get("score")).getLexicalForm()));
      }
      resourceList.addLast(resource);
    }
    ResourceList rlContext = ResourceList.of(resourceList);
    if (!scoreMap.isEmpty()) {
      for (Entry<String, Double> e : scoreMap.entrySet()) {
        ObjectNode ftsInfo = JsonNodeFactory.instance.objectNode();
        ftsInfo.put("score", e.getValue());
        rlContext.putValuesData(e.getKey(), Collections.singletonList("fts"), ftsInfo);
      }
    } else {
      logger.debug("The used full-text-search dao '{}' does not hand over a score.",
          fullTextSearchDAO.toString());
    }
    return rlContext;
  }
}
