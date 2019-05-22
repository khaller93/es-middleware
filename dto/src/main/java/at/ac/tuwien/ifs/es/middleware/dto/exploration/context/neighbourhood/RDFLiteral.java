package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.neighbourhood;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kevin Haller
 */
public class Literal implements RDFTerm {

  @JsonIgnore
  private static final AtomicLong idCounter1 = new AtomicLong(1L);

  private String id;
  private org.apache.commons.rdf.api.Literal literal;

  public Literal(org.apache.commons.rdf.api.Literal literal) {
    checkArgument(literal != null, "The literal must not be null.");
    this.literal = literal;
    this.id = "L#" + idCounter1.getAndUpdate(l -> {
      if (l == Long.MAX_VALUE) {
        idCounter1.incrementAndGet();
        return 1L;
      } else {
        return l + 1;
      }
    });
  }

  @Override
  public String getId() {
    return id;
  }

  public org.apache.commons.rdf.api.Literal value(){
    return literal;
  }



  @Override
  public String toString() {
    return "Literal{" +
        "id='" + id + '\'' +
        ", literal=" + literal +
        '}';
  }
}
