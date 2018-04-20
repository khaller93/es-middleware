package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition.FullTextSearchPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  private FullTextSearchDAO fullTextSearchDAO;
  private ObjectMapper parameterMapper;

  public FullTextSearch(
      @Autowired @Qualifier("SpecificFullTextSearchDAO") FullTextSearchDAO fullTextSearchDAO,
      @Autowired ObjectMapper parameterMapper) {
    this.fullTextSearchDAO = fullTextSearchDAO;
    this.parameterMapper = parameterMapper;
  }

  @Override
  public Class<FullTextSearchPayload> getParameterClass() {
    return FullTextSearchPayload.class;
  }

  @Override
  public ExplorationContext apply(FullTextSearchPayload payload) {
    //TODO: more sophisticated.
    return ResourceList.of(fullTextSearchDAO.searchFullText(payload.getKeyword()));
  }
}
