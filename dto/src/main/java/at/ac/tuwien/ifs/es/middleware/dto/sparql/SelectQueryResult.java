package at.ac.tuwien.ifs.es.middleware.dto.sparql;

import com.google.common.collect.Table;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * This interface represent a {@link QueryResult} that maintains the response of a SELECT SPARQL
 * query.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface SelectQueryResult extends QueryResult {

  /**
   * Returns the result of a SELECT query in form of a {@link Table}. The columns are constructed
   * from the bindings and can be accessed by name. The row key is an enumerated integer.
   *
   * @return {@link Table} that represents the result of a SELECT query.
   */
  Table<Integer, String, RDFTerm> value();

}
