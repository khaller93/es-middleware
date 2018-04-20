package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This class defines a POJO for parameters expected by 'all sources' operator.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class AllResourcesPayload implements Serializable {

  private List<Resource> include;
  private List<Resource> exclude;
  private List<Resource> namespaces;

  @JsonCreator
  public AllResourcesPayload(@JsonProperty("include") List<Resource> include,
      @JsonProperty("exclude") List<Resource> exclude,
      @JsonProperty("namespaces") List<Resource> namespaces) {
    this.include = include;
    this.exclude = exclude;
    this.namespaces = namespaces;
  }

  public List<Resource> getIncludedClasses() {
    return include;
  }

  public List<Resource> getExcludedClasses() {
    return exclude;
  }

  public List<Resource> getNamespaces() {
    return namespaces;
  }

  @Override
  public String toString() {
    return "AllSourcesPayload{" +
        "include=" + include +
        ", exclude=" + exclude +
        ", namespaces=" + namespaces +
        '}';
  }
}
