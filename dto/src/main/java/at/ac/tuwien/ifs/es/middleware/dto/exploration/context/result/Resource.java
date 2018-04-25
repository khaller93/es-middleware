package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.BlankOrIRIJsonUtil;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.util.ResourceJsonUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This class represents a single resource as a result of exploration. This resource can be
 * identified by its IRI.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonSerialize(using = ResourceJsonUtil.Serializer.class)
@JsonDeserialize(using = ResourceJsonUtil.Deserializer.class)
public final class Resource implements IdentifiableResult {

  private BlankNodeOrIRI resource;

  public Resource(BlankNodeOrIRI resource) {
    this.resource = resource;
  }

  public Resource(String resourceString) {
    this.resource = BlankOrIRIJsonUtil.valueOf(resourceString);
  }

  @Override
  public String getId() {
    return BlankOrIRIJsonUtil.stringValue(resource);
  }

  public BlankNodeOrIRI value() {
    return resource;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Resource resource1 = (Resource) o;

    return resource != null ? resource.equals(resource1.resource) : resource1.resource == null;
  }

  @Override
  public int hashCode() {
    return resource != null ? resource.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Resource{" +
        "resource=" + resource +
        '}';
  }
}
