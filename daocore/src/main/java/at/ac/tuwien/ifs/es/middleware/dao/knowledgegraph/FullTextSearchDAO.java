package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * Instances provide a full-text-search interface to the managed knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface FullTextSearchDAO {

  /**
   * Applies a full text search on the managed knowledge graph using the given {@code keyword} and
   * returns a {@link List}, where the entries are ordered by the relevance core of the
   * full-text-search. A list entry (row) has at least one column, the resource column named {@code
   * resource}. Optionally, a column named {@code score} for the score.
   *
   * @param keyword which shall be used to explore resources.
   * @return a ranked list of distinct resource IRIs and optionally corresponding scores.
   */
  default List<Map<String, RDFTerm>> searchFullText(String keyword) {
    return searchFullText(keyword, Collections.emptyList());
  }

  /**
   * Applies a full text search on the managed knowledge graph using the given {@code keyword} and
   * returns a {@link List}, where the entries are ordered by the relevance core of the
   * full-text-search. A list entry (row) has at least one column, the resource column named {@code
   * resource}. Optionally, a column named {@code score} for the score. The resources are at least
   * member of one of the given classes. If there are no classes given (empty list), then there will
   * be no limitation in this sense.
   *
   * @param keyword which shall be used to explore resources.
   * @param classes IRI of classes of which the returned resources must be a member (at least of
   * one), must not be {@code null}, but can be empty.
   * @return a ranked list of distinct resource IRIs and optionally corresponding scores.
   */
  default List<Map<String, RDFTerm>> searchFullText(String keyword,
      List<BlankNodeOrIRI> classes) {
    return searchFullText(keyword, classes, null, null);
  }

  /**
   * Applies a full text search on the managed knowledge graph using the given {@code keyword} and
   * returns a {@link List}, where the entries are ordered by the relevance core of the
   * full-text-search. A list entry (row) has at least one column, the resource column named {@code
   * resource}. Optionally, a column named {@code score} for the score. The resources are at least
   * member of one of the given classes. If there are no classes given (empty list), then there will
   * be no limitation in this sense.
   *
   * @param keyword which shall be used to explore resources.
   * @param classes IRI of classes of which the returned resources must be a member (at least of
   * one), must not be {@code null}, but can be empty.
   * @return a ranked list of distinct resource IRIs and optionally corresponding scores.
   */
  List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit);

}
