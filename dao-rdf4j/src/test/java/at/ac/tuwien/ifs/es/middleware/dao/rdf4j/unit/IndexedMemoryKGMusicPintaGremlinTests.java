package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.InMemoryGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaGremlinTests;
import org.springframework.test.context.ContextConfiguration;

/**
 * This test class implements the {@link AbstractMusicPintaGremlinTests} for {@link
 * IndexedMemoryKnowledgeGraph}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@ContextConfiguration(classes = {
    IndexedMemoryKnowledgeGraph.class,
    InMemoryGremlinDAO.class
})
public class IndexedMemoryKGMusicPintaGremlinTests extends AbstractMusicPintaGremlinTests {

}
