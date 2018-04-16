package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.IdentifiableResult;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * This class represents a pair of two resources that are both identified by their IRI. The pair
 * itself is identified by a number that is uniquely enumerated in the current JVM.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourcePair implements IdentifiableResult {

  private static final AtomicLong idCounter1 = new AtomicLong(1L);
  private static final AtomicLong idCounter2 = new AtomicLong(1L);

  private String id;
  private BlankNodeOrIRI first;
  private BlankNodeOrIRI second;

  public ResourcePair(BlankNodeOrIRI first, BlankNodeOrIRI second) {
    this.id = "#" + idCounter1.get() + "." + idCounter2.getAndUpdate(new LongUnaryOperator() {
      @Override
      public long applyAsLong(long l) {
        if (l == Long.MAX_VALUE) {
          idCounter1.incrementAndGet();
          return 1L;
        } else {
          return l + 1;
        }
      }
    });
    this.first = first;
    this.second = second;
  }

  public BlankNodeOrIRI getFirst() {
    return first;
  }

  public BlankNodeOrIRI getSecond() {
    return second;
  }

  @Override
  public String getId() {
    return id;
  }
}
