package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaGremlinTests;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test class implements the {@link AbstractMusicPintaGremlinTests} for {@link
 * IndexedMemoryKnowledgeGraph}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    IndexedMemoryKnowledgeGraph.class,
    InMemoryGremlinDAO.class
})
public class IndexedMemoryKGMusicPintaGremlinTests extends AbstractMusicPintaGremlinTests {

  @Autowired
  private KGSparqlDAO sparqlDAO;
  @Autowired
  private KGGremlinDAO gremlinDAO;


  @Override
  protected KGSparqlDAO getSparqlDAO() {
    return sparqlDAO;
  }

  @Override
  protected KGGremlinDAO getGremlinDAO() {
    return gremlinDAO;
  }
}
