package at.ac.tuwien.ifs.es.middleware.service.exploration.factory;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.exploitation.DescriberPayload.TextLiteralPayload;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowServiceException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.FullTextSearch;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition.FullTextSearchPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.exploitation.ResourceDescriber;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.exploitation.DescriberPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.exploitation.ResourceDescriber.DescribeTerm;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
   * @param languages the languages for which a description and label shall be returned.
   * @param classes a list of classes of which the resource must be a member.
   * @param limit the number of results to which the result list shall be restricted.
   * @param offset the number of results that shall be skipped.
   * @return {@link ExplorationFlow} for full-text-search with the given parameters.
   */
  public ExplorationFlow constructFullTextSearchFlow(String keyword, List<String> languages,
      List<String> classes, Integer limit, Integer offset) {
    ExplorationFlow flow = new ExplorationFlow();
    try {
      FullTextSearchPayload ftsParameterPayload = fullTextSearch
          .getParameterClass().newInstance();
      ftsParameterPayload.setKeyword(keyword);
      ftsParameterPayload.setClasses(
          classes != null ? classes.stream().map(c -> new Resource(BlankOrIRIJsonUtil.valueOf(c)))
              .collect(Collectors.toList()) : null);
      ftsParameterPayload.setLimit(limit);
      ftsParameterPayload.setOffset(offset);
      flow.appendFlowStep(fullTextSearch, ftsParameterPayload);
      DescriberPayload describerPayload = new DescriberPayload(false);
      if (languages != null && !languages.isEmpty()) {
        describerPayload.addTextContent("label",
            new TextLiteralPayload(Collections.singletonList(DescriberPayload.RDFS_LABEL_PROPERTY), languages));
        describerPayload.addTextContent("description",
            new TextLiteralPayload(Collections.singletonList(DescriberPayload.RDFS_COMMENT_PROPERTY), languages));
      }
      flow.appendFlowStep(resourceDescriber, describerPayload);
      return flow;
    } catch (IllegalAccessException | InstantiationException e) {
      throw new ExplorationFlowServiceException(
          "The full-text-search flow could not be constructed, due to internal error.", e);
    }
  }

}
