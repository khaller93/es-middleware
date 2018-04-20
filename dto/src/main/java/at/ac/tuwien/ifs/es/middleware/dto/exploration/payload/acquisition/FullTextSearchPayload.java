package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

/**
 * This class is a POJO for the parameters expected by full-text-search operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class FullTextSearchPayload implements Serializable {

  private String keyword;
  private List<String> classes;
  private Integer offset;
  private Integer limit;

  public String getKeyword() {
    return keyword;
  }

  @JsonProperty(value = "keyword",required = true)
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
    return "FullTextSearchPayload{" +
        "keyword='" + keyword + '\'' +
        ", classes=" + classes +
        ", offset=" + offset +
        ", limit=" + limit +
        '}';
  }
}