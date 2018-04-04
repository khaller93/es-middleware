package at.ac.tuwien.ifs.es.middleware.dao.rdf4j;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.FullTextSearchDAO;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of {@link RDF4JKnowledgeGraphDAO}. This class can be used for
 * testing the overlying services without starting a standalone triplestore instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("IndexedMemoryDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class IndexedMemoryKnowledgeGraph extends RDF4JKnowledgeGraphDAO implements
    FullTextSearchDAO {

  private final static Logger logger = LoggerFactory.getLogger(IndexedMemoryKnowledgeGraph.class);

  private final static String FTS_QUERY =
      "PREFIX search: <http://www.openrdf.org/contrib/lucenesail#> \n"
          + "SELECT ?subj "
          + "WHERE "
          + "{"
          + "  ?subj search:matches ["
          + "    search:query \"guitar\";"
          + "    search:score ?score"
          + "  ]"
          + "} ORDER BY DESC(?score)";

  private RDF4J valueFactory = new RDF4J();

  /**
   * Creates a new {@link at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO} in
   * memory that is indexed.
   */
  public IndexedMemoryKnowledgeGraph() {
    LuceneSail luceneSail = new LuceneSail();
    luceneSail.setParameter(LuceneSail.LUCENE_RAMDIR_KEY, "true");
    luceneSail.setBaseSail(new MemoryStore());
    init(luceneSail);
  }

  @Override
  public List<BlankNodeOrIRI> searchFullText(String keyword, List<BlankNodeOrIRI> clazzes) {
    Repository repository = getRepository();
    try (RepositoryConnection connection = repository.getConnection()) {
      TupleQuery ftsQuery = connection.prepareTupleQuery(FTS_QUERY);
      ftsQuery.setBinding("term", repository.getValueFactory().createLiteral(keyword));
      List<BindingSet> resultSets = QueryResults.asList(ftsQuery.evaluate());
      return resultSets.stream()
          .map(bs -> valueFactory.asRDFTerm((Resource) bs.getBinding("subj").getValue())).collect(
              Collectors.toList());
    }
  }
}
