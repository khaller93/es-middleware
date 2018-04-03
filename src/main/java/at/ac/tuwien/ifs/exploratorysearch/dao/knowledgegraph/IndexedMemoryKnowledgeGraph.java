package at.ac.tuwien.ifs.exploratorysearch.dao.knowledgegraph;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class is an implementation of {@link AbstractKnowledgeGraphDAO}. This class can be used for
 * testing the overlying services without starting a standalone triplestore instance.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component("IndexedMemoryDB")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class IndexedMemoryKnowledgeGraph extends AbstractKnowledgeGraphDAO implements
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

  public IndexedMemoryKnowledgeGraph() {
    LuceneSail luceneSail = new LuceneSail();
    luceneSail.setParameter(LuceneSail.LUCENE_RAMDIR_KEY, "true");
    luceneSail.setBaseSail(new MemoryStore());
    init(luceneSail);
  }

  @Override
  public List<Resource> searchFullText(String keyword, List<Resource> clazzes) {
    Repository repository = getRepository();
    try (RepositoryConnection connection = repository.getConnection()) {
      TupleQuery ftsQuery = connection.prepareTupleQuery(FTS_QUERY);
      ftsQuery.setBinding("term", repository.getValueFactory().createLiteral(keyword));
      List<BindingSet> resultSets = QueryResults.asList(ftsQuery.evaluate());
      return resultSets.stream()
          .map(bs -> (Resource) bs.getBinding("subj").getValue()).collect(
              Collectors.toList());
    }
  }
}
