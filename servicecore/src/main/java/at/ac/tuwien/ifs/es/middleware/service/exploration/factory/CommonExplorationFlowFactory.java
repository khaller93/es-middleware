package at.ac.tuwien.ifs.es.middleware.service.exploration.factory;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.exploitation.DescriberPayload.TextLiteralPayload;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.RDFTermJsonUtil;
import at.ac.tuwien.ifs.es.middleware.service.exploration.ExplorationFlow;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.FullTextSearch;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.FullTextSearchPayload;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.exploitation.ResourceDescriber;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.exploitation.DescriberPayload;
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
   * @param classes a list get classes get which the resource must be a member.
   * @param limit the number get results to which the result list shall be restricted.
   * @param offset the number get results that shall be skipped.
   * @return {@link ExplorationFlow} for full-text-search with the given parameters.
   */
  public ExplorationFlow constructFullTextSearchFlow(String keyword, List<String> languages,
      List<String> classes, Integer limit, Integer offset) {
    ExplorationFlow flow = new ExplorationFlow();
    FullTextSearchPayload ftsParameterPayload = new FullTextSearchPayload(keyword,
        classes != null ? classes.stream().map(c -> new Resource(RDFTermJsonUtil.valueOf(c)))
            .collect(Collectors.toList()) : null, offset, limit, null);
    flow.appendFlowStep(fullTextSearch, ftsParameterPayload);
    DescriberPayload describerPayload = new DescriberPayload(false);
    if (languages != null && !languages.isEmpty()) {
      describerPayload.addTextContent("label",
          new TextLiteralPayload(Collections.singletonList(DescriberPayload.RDFS_LABEL_PROPERTY),
              languages));
      describerPayload.addTextContent("description",
          new TextLiteralPayload(
              Collections.singletonList(DescriberPayload.RDFS_COMMENT_PROPERTY), languages));
    }
    flow.appendFlowStep(resourceDescriber, describerPayload);
    return flow;
  }

}
