package at.ac.tuwien.ifs.es.middleware.common.exploration.context.result;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.rdf.api.Literal;

/**
 * This is an implementation of {@link RDFValueTerm} that represents RDF literals.
 *
 * @author Kevin Haller
 */
public class RDFLiteral extends RDFValueTerm {

  @JsonIgnore
  private static final AtomicLong idCounter1 = new AtomicLong(1L);

  private String id;
  private Literal literal;

  public RDFLiteral(Literal literal) {
    this("L#" + idCounter1.getAndUpdate(l -> {
      if (l == Long.MAX_VALUE) {
        idCounter1.incrementAndGet();
        return 1L;
      } else {
        return l + 1;
      }
    }), literal);
  }

  public RDFLiteral(String id, Literal literal) {
    checkArgument(id != null, "The id must not be null.");
    checkArgument(literal != null, "The literal must not be null.");
    this.id = id;
    this.literal = literal;
  }

  @Override
  public String getId() {
    return id;
  }

  public Literal value() {
    return literal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RDFLiteral RDFLiteral = (RDFLiteral) o;
    return id.equals(RDFLiteral.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "RDFLiteral{" +
        "id='" + id + '\'' +
        ", literal=" + literal +
        '}';
  }
}
