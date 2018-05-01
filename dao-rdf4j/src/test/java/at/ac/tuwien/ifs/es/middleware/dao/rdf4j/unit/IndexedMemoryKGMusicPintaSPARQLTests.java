package at.ac.tuwien.ifs.es.middleware.dao.rdf4j.unit;

import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.IndexedMemoryKnowledgeGraph;
import at.ac.tuwien.ifs.es.middleware.testutil.AbstractMusicPintaSPARQLTests;
import org.springframework.test.context.ContextConfiguration;

/**
 * This test class implements the {@link AbstractMusicPintaSPARQLTests} for {@link
 * IndexedMemoryKnowledgeGraph}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@ContextConfiguration(classes = {
    IndexedMemoryKnowledgeGraph.class
}, inheritLocations = true)

public class IndexedMemoryKGMusicPintaSPARQLTests extends AbstractMusicPintaSPARQLTests {

}
