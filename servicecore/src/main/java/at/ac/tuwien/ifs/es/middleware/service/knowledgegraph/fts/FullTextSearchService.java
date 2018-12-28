package at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts;

import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphDAOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * This service provides methods for apply a full-text search on the knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface FullTextSearchService {

  /**
   * Applies a full text search on the managed knowledge graph using the given {@code keyword} and
   * returns a {@link List}, where the entries are ordered by the relevance core of the
   * full-text-search. A list entry (row) has at least one column, the resource column named {@code
   * resource}. Optionally, a column named {@code score} for the score.
   *
   * @param keyword which shall be used to explore resources.
   * @return a ranked list of distinct resource IRIs and optionally corresponding scores.
   * @throws KnowledgeGraphDAOException if fts could not be applied successfully.
   */
  List<Map<String, RDFTerm>> searchFullText(String keyword);

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
   * @throws KnowledgeGraphDAOException if fts could not be applied successfully.
   */
  List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes);

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
   * @throws KnowledgeGraphDAOException if fts could not be applied successfully.
   */
  List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) throws KnowledgeGraphDAOException;

}
