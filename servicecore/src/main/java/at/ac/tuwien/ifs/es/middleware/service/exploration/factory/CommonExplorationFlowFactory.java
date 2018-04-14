package at.ac.tuwien.ifs.es.middleware.service.exploration.factory;

import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowServiceException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.FullTextSearch;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.FullTextSearch.FullTextSearchParameterPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.exploitation.ResourceDescriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This factory provides flows for common exploratory search use cases like full-text-search or
 * related entities.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
public class CommonExplorationFlowFactory {

  private FullTextSearch fullTextSearch;
  private ResourceDescriber resourceDescriber;
  private ObjectMapper parameterMapper;

  public CommonExplorationFlowFactory(@Autowired ObjectMapper parameterMapper) {
    this.parameterMapper = parameterMapper;
  }

  /**
   * Sets the {@link FullTextSearch} that shall be used for the common flows.
   *
   * @param fullTextSearch that shall be used for the common flows.
   */
  @Autowired
  private void setFullTextSearch(FullTextSearch fullTextSearch) {
    this.fullTextSearch = fullTextSearch;
  }

  /**
   * Sets the {@link ResourceDescriber} that shall be used for common flows.
   *
   * @param resourceDescriber that shall be used for common flows.
   */
  @Autowired
  public void setResourceDescriber(ResourceDescriber resourceDescriber) {
    this.resourceDescriber = resourceDescriber;
  }

  /**
   * Constructs a full-text-search flow with the given parameters.
   *
   * @param keyword the keyword phrase for the full-text-search.
   * @param clazzes a list of classes of which the resource must be a member.
   * @param limit the number of results to which the result list shall be restricted.
   * @param offset the number of results that shall be skipped.
   * @return {@link ExplorationFlow} for full-text-search with the given parameters.
   */
  public ExplorationFlow constructFullTextSearchFlow(String keyword, List<String> clazzes,
      Integer limit, Integer offset) {
    ExplorationFlow flow = new ExplorationFlow();
    try {
      FullTextSearchParameterPayload ftsParameterPayload = fullTextSearch
          .getParameterClass().newInstance();
      ftsParameterPayload.setKeyword(keyword);
      ftsParameterPayload.setClasses(clazzes);
      ftsParameterPayload.setLimit(limit);
      ftsParameterPayload.setOffset(offset);
      flow.appendFlowStep(fullTextSearch, parameterMapper.valueToTree(ftsParameterPayload));
      flow.appendFlowStep(resourceDescriber, JsonNodeFactory.instance.objectNode());
      return flow;
    } catch (IllegalAccessException | InstantiationException e) {
      throw new ExplorationFlowServiceException(
          "The full-text-search flow could not be constructed, due to internal error.", e);
    }
  }

}
