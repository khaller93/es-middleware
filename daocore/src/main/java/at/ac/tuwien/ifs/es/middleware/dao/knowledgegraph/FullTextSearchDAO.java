package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import java.util.Collections;
import java.util.List;
import org.apache.commons.rdf.api.BlankNodeOrIRI;

/**
 * Instances of this interface provide a full-text-search ability to the managed knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface FullTextSearchDAO {

  /**
   * Applies a full text search on the managed knowledge graph using the given {@code keyword} and
   * returns a list of resources ordered by relevance score.
   *
   * @param keyword which shall be used to explore resources.
   * @return a ranked list of distinct resource IRIs.
   */
  default List<BlankNodeOrIRI> searchFullText(String keyword) {
    return searchFullText(keyword, Collections.emptyList());
  }

  /**
   * Applies a full text search on the managed knowledge graph using the given {@code keyword} and
   * returns a list of resources ordered by relevance score. The resources are at least member of
   * one of the given classes. If there are no classes given (empty list), then there will be no
   * limitation in this sense.
   *
   * @param keyword which shall be used to explore resources.
   * @param clazzes IRI of classes of which the returned resources must be a member (at least of
   * one).
   * @return a ranked list of distinct resource IRIs.
   */
  List<BlankNodeOrIRI> searchFullText(String keyword, List<BlankNodeOrIRI> clazzes);

}
