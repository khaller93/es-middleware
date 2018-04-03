package at.ac.tuwien.ifs.exploratorysearch.service.exploration.aquisition;

import at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ExplorationResponse;
import at.ac.tuwien.ifs.exploratorysearch.dto.exploration.ResourceList;
import at.ac.tuwien.ifs.exploratorysearch.service.exception.ExplorationFlowSpecificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an implementation of {@link AcquisitionSource} that searches for resources with the given
 * keyword. The result list is ordered descending by the full-text-search score of the result.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("FtsAcquisitionSource")
public class FullTextSearch implements AcquisitionSource {

  private FullTextSearchDAO fullTextSearchDAO;

  public FullTextSearch(
      @Autowired @Qualifier("SpecificFullTextSearchDAO") FullTextSearchDAO fullTextSearchDAO) {
    this.fullTextSearchDAO = fullTextSearchDAO;
  }

  @Override
  public ExplorationResponse apply(JsonNode parameterMap) {
    parameterMap = parameterMap.get("fts");
    if (parameterMap.has("keyword")) {
      JsonNode keyword = parameterMap.get("keyword");
      if (keyword.isValueNode()) {
        return new ExplorationResponse(
            new ResourceList(fullTextSearchDAO.searchFullText(keyword.asText())),
            JsonNodeFactory.instance.objectNode());
      } else {
        throw new ExplorationFlowSpecificationException(
            "The specified 'keyword' must be a simple string value");
      }
    } else {
      throw new ExplorationFlowSpecificationException(
          "Full-Text/Keyword search requires a'keyword' to be specified in the parameter map.");
    }
  }
}
