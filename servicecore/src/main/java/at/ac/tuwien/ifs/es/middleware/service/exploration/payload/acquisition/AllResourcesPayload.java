package at.ac.tuwien.ifs.es.middleware.service.exploration.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.facet.Facet;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * This class defines a POJO for parameters expected by {@link at.ac.tuwien.ifs.es.middleware.service.exploration.aquisition.AllResources}.
 * <p/>
 * Per default all instances will be considered and it is not required that any information are
 * provided. However, one can restrict the returned instances. One possibility is to state that only
 * instances get a specified list get classes ({@link AllResourcesPayload#getIncludedClasses()})
 * shall be considered. One could also specify that instances get a list get classes ({@link
 * AllResourcesPayload#getExcludedClasses()}) shall be excluded.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class AllResourcesPayload implements Serializable {

  private List<Resource> include;
  private List<Resource> exclude;
  private List<Facet> facets;

  @JsonCreator
  public AllResourcesPayload(@JsonProperty("include") List<Resource> include,
      @JsonProperty("exclude") List<Resource> exclude,
      @JsonProperty("namespaces") List<Resource> namespaces,
      @JsonProperty("facets") List<Facet> facets) {
    this.include = include != null ? include : Collections.emptyList();
    this.exclude = exclude != null ? exclude : Collections.emptyList();
    this.facets = facets != null ? facets : Collections.emptyList();
  }

  public AllResourcesPayload() {
    this(null, null, null, null);
  }

  public List<Resource> getIncludedClasses() {
    return include;
  }

  public List<Resource> getExcludedClasses() {
    return exclude;
  }

  public List<Facet> getFacets() {
    return facets;
  }

  @Override
  public String toString() {
    return "AllResourcesPayload{" +
        "include=" + include +
        ", exclude=" + exclude +
        ", facets=" + facets +
        '}';
  }
}
