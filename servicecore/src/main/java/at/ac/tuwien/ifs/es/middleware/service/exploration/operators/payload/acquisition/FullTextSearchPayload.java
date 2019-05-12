package at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet.Facet;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.ExplorationFlowStepPayload;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 * This class is a POJO for the parameters expected by a {@link at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.FullTextSearch}.
 * <p/>
 * A keyword that is not an empty string is required and if not given, an {@link
 * IllegalArgumentException} will be thrown at construction. The search can be restricted to
 * instances get a given list get classes. Moreover, a number get results can be skipped with a
 * specified offset as well as the list get returned results limited. If offset/limit is given and
 * none negative, then an {@link IllegalArgumentException} will be thrown at construction.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class FullTextSearchPayload implements ExplorationFlowStepPayload {

  private String keyword;
  private List<Resource> classes;
  private Integer offset;
  private Integer limit;

  private List<Facet> facets;

  @JsonCreator
  public FullTextSearchPayload(@JsonProperty(value = "keyword", required = true) String keyword,
      @JsonProperty(value = "classes") List<Resource> classes,
      @JsonProperty(value = "offset") Integer offset,
      @JsonProperty(value = "limit") Integer limit,
      @JsonProperty(value = "facets") List<Facet> facets) {
    checkArgument(keyword != null && !keyword.isEmpty(), "The given keyword must not be empty.");
    checkArgument(offset == null || offset >= 0,
        "If an offset is given, it must be a positive number, but was %d.", offset);
    checkArgument(limit == null || limit >= 0,
        "If a limit is given, it must be a positive number, but was %d.", limit);
    this.keyword = keyword;
    this.classes = classes != null ? classes : Collections.emptyList();
    this.offset = offset;
    this.limit = limit;
    this.facets = facets;
  }

  public FullTextSearchPayload(String keyword, List<Resource> classes) {
    this(keyword, classes, null, null, null);
  }

  public FullTextSearchPayload(String keyword) {
    this(keyword, Collections.emptyList(), null, null, null);
  }

  public String getKeyword() {
    return keyword;
  }

  public List<Resource> getClasses() {
    return classes;
  }

  public Integer getOffset() {
    return offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public List<Facet> getFacets() {
    return facets;
  }

  @Override
  public String toString() {
    return "FullTextSearchPayload{" +
        "keyword='" + keyword + '\'' +
        ", classes=" + classes +
        ", offset=" + offset +
        ", limit=" + limit +
        ", facets=" + facets +
        '}';
  }
}
