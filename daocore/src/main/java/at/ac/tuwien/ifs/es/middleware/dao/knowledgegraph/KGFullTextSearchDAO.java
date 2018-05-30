package at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts.*;
import at.ac.tuwien.ifs.es.middleware.dto.exception.KnowledgeGraphDAOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;

/**
 * Instances provide a full-text-search interface to the managed knowledge graph.
 * <p/>
 * Instances of this DAO go through a life cycle starting with an {@code initial} state. From this
 * {@code initial} state, the implementation of this DAO can go into the {@code ready} state, if
 * everything works fine and as intended. The transition from the {@code initial} state into the
 * {@code ready} state triggers a {@link FullTextSearchDAOReadyEvent}.
 * <p/>
 * During the lifetime of a {@code ready} DAO, changes can be made to the underlying database. Those
 * changes will transition this DAO into a {@code updating} state (leading to a {@link
 * FullTextSearchDAOUpdatingEvent}). If the updating is finalized, the DAO is {@code ready} again.
 * This will issue a {@link FullTextSearchDAOUpdatedEvent}.
 * <p/>
 * However, the DAO can always fail, and the resulting state will then be {@code failed}. A
 * transition into {@code failed}, will trigger a {@link FullTextSearchDAOFailedEvent}. A DAO can
 * always recover, and move to {@code ready} again, also triggering the corresponding event.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface KGFullTextSearchDAO {

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
   * @throws KnowledgeGraphDAOException if fts could not be applied successfully.
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
   * @throws KnowledgeGraphDAOException if fts could not be applied successfully.
   */
  List<Map<String, RDFTerm>> searchFullText(String keyword, List<BlankNodeOrIRI> classes,
      Integer offset, Integer limit) throws KnowledgeGraphDAOException;

}
