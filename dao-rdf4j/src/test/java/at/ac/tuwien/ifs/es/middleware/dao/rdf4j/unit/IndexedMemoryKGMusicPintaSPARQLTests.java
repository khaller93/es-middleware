package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaGremlinTests;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaSPARQLTests;
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
public class IndexedMemoryKGMusicPintaSPARQLTests extends AbstractMusicPintaSPARQLTests {

  @Autowired
  private IndexedMemoryKnowledgeGraph knowledgeGraph;

  @Override
  public KnowledgeGraphDAO getKnowledgeGraphDAO() {
    return knowledgeGraph;
  }
}
