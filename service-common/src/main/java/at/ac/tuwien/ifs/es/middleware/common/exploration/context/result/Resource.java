package at.ac.tuwien.ifs.es.middleware.common.exploration.context.result;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.result.RDFTermJsonUtil;
import java.util.Objects;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This class represents a single resource as a result of exploration. This resource can be
 * identified by its IRI.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class Resource extends RDFValueTerm {

  private BlankNodeOrIRI resource;

  /**
   * Creates a new resource from given {@link BlankNodeOrIRI}.
   *
   * @param resource {@link BlankNodeOrIRI} for which a resource shall be created.
   */
  public Resource(BlankNodeOrIRI resource) {
    checkArgument(resource != null, "The blank node or IRI must not be null.");
    this.resource = resource;
  }

  /**
   *
   * @param resourceString
   */
  public Resource(String resourceString) {

    this.resource = RDFTermJsonUtil.valueOf(resourceString);
  }

  @Override
  public String getId() {
    return RDFTermJsonUtil.stringValue(resource);
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

    return Objects.equals(resource, resource1.resource);
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
