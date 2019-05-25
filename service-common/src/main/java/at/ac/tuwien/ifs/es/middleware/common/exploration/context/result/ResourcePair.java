package at.ac.tuwien.ifs.es.middleware.common.exploration.context.result;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.IdentifiableResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This class represents a pair of two {@link Resource}s. The pair itself is identified by a number
 * that is uniquely enumerated in the current JVM.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourcePair implements IdentifiableResult {

  @JsonIgnore
  private static final AtomicLong idCounter1 = new AtomicLong(1L);
  @JsonIgnore
  private static final AtomicLong idCounter2 = new AtomicLong(1L);

  private String id;
  private Resource first;
  private Resource second;

  @JsonCreator
  private ResourcePair(@JsonProperty(value = "id", required = true) String id,
      @JsonProperty(value = "first", required = true) Resource first,
      @JsonProperty(value = "second", required = true) Resource second) {
    checkArgument(id != null, "The given id must not be null.");
    checkArgument(first != null, "The given first resource must not be null.");
    checkArgument(second != null, "The given second resource must not be null.");
    this.id = id;
    this.first = first;
    this.second = second;
  }

  /**
   * Creates a new resource pair with the given {@link BlankNodeOrIRI}s and assigns a new id.
   *
   * @param first {@link BlankNodeOrIRI} that shall be the first entry of the pair.
   * @param second {@link BlankNodeOrIRI} that shall be the second entry of the pair.
   * @return a new resource pair with the given {@link BlankNodeOrIRI}s.
   */
  public static ResourcePair of(Resource first, Resource second) {
    String id = "#" + idCounter1.get() + "." + idCounter2.getAndUpdate(l -> {
      if (l == Long.MAX_VALUE) {
        idCounter1.incrementAndGet();
        return 1L;
      } else {
        return l + 1;
      }
    });
    return new ResourcePair(id, first, second);
  }

  @Override
  public String getId() {
    return id;
  }

  public Resource getFirst() {
    return first;
  }

  public Resource getSecond() {
    return second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourcePair that = (ResourcePair) o;
    return Objects.equals(first, that.first) &&
        Objects.equals(second, that.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "ResourcePair{" +
        "id='" + id + '\'' +
        ", first=" + first +
        ", second=" + second +
        '}';
  }
}
