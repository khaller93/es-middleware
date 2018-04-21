package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This class is a POJO for the parameters expected by full-text-search operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class FullTextSearchPayload implements Serializable {

  @JsonProperty(value = "keyword",required = true)
  private String keyword;
  private List<Resource> classes;
  private Integer offset;
  private Integer limit;

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public List<Resource> getClasses() {
    return classes;
  }

  public void setClasses(List<Resource> classes) {
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
    return "FullTextSearchPayload{" +
        "keyword='" + keyword + '\'' +
        ", classes=" + classes +
        ", offset=" + offset +
        ", limit=" + limit +
        '}';
  }
}
