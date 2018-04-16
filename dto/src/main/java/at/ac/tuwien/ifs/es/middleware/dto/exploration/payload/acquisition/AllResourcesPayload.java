package at.ac.tuwien.ifs.es.middleware.dto.exploration.payload.acquisition;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
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

  @JsonSerialize(contentUsing = BlankOrIRIJsonUtil.Serializer.class)
  @JsonDeserialize(contentUsing = BlankOrIRIJsonUtil.Deserializer.class)
  private List<BlankNodeOrIRI> include;
  @JsonSerialize(contentUsing = BlankOrIRIJsonUtil.Serializer.class)
  @JsonDeserialize(contentUsing = BlankOrIRIJsonUtil.Deserializer.class)
  private List<BlankNodeOrIRI> exclude;
  @JsonSerialize(contentUsing = BlankOrIRIJsonUtil.Serializer.class)
  @JsonDeserialize(contentUsing = BlankOrIRIJsonUtil.Deserializer.class)
  private List<BlankNodeOrIRI> namespaces;

  public AllResourcesPayload(@JsonProperty("include") List<BlankNodeOrIRI> include,
      @JsonProperty("exclude") List<BlankNodeOrIRI> exclude,
      @JsonProperty("namespaces") List<BlankNodeOrIRI> namespaces) {
    this.include = include;
    this.exclude = exclude;
    this.namespaces = namespaces;
  }

  public List<BlankNodeOrIRI> getIncludedClasses() {
    return include;
  }

  public List<BlankNodeOrIRI> getExcludedClasses() {
    return exclude;
  }

  public List<BlankNodeOrIRI> getNamespaces() {
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
