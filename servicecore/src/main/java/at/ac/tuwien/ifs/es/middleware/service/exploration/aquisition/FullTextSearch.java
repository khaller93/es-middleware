package at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.ResourceList;
import at.ac.tuwien.ifs.es.middleware.service.exception.ExplorationFlowSpecificationException;
import at.ac.tuwien.ifs.es.middleware.service.exploration.registry.RegisterForExplorationFlow;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
public class FullTextSearch implements AcquisitionSource {

  private FullTextSearchDAO fullTextSearchDAO;
  private ObjectMapper parameterMapper;

  public FullTextSearch(
      @Autowired @Qualifier("SpecificFullTextSearchDAO") FullTextSearchDAO fullTextSearchDAO,
      @Autowired ObjectMapper parameterMapper) {
    this.fullTextSearchDAO = fullTextSearchDAO;
    this.parameterMapper = parameterMapper;
  }

  @Override
  public Class<FullTextSearchParameterPayload> getParameterClass() {
    return FullTextSearchParameterPayload.class;
  }

  @Override
  public ExplorationContext apply(JsonNode parameterMap) {
    try {
      FullTextSearchParameterPayload ftsParameterPayload = parameterMapper
          .treeToValue(parameterMap, getParameterClass());
      return new ResourceList(fullTextSearchDAO.searchFullText(ftsParameterPayload.getKeyword()));
    } catch (JsonProcessingException e) {
      throw new ExplorationFlowSpecificationException(
          String.format("Parameter payload given for the full-text search is invalid. %s",
              e.getMessage()), e);
    }
  }

  /**
   * This class is a POJO for the parameters expected by this {@link FullTextSearch}. This
   */
  public static class FullTextSearchParameterPayload {

    @JsonProperty(required = true)
    private String keyword;
    private List<String> classes;
    private Integer offset;
    private Integer limit;

    public String getKeyword() {
      return keyword;
    }

    public void setKeyword(String keyword) {
      this.keyword = keyword;
    }

    public List<String> getClasses() {
      return classes;
    }

    public void setClasses(List<String> classes) {
      this.classes = classes;
    }

    public Integer getOffset() {
      return offset;
    }

    public void setOffset(Integer offset) {
      this.offset = offset;
    }

    public Integer getLimit() {
      return limit;
    }

    public void setLimit(Integer limit) {
      this.limit = limit;
    }

    @Override
    public String toString() {
      return "FullTextSearchParameterPayload{" +
          "keyword='" + keyword + '\'' +
          ", classes=" + classes +
          ", offset=" + offset +
          ", limit=" + limit +
          '}';
    }
  }
}
