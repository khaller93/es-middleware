package at.ac.tuwien.ifs.es.middleware.testutil;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is an external resource that loads the musicpinta instruments subset into the specified DAOs
 * in the {@link KnowledgeGraphDAOConfig}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Component
public class MusicPintaInstrumentsResource extends ExternalKGResource {

  private static Model testModel;

  static {
    try (InputStream testDatasetIn = MusicPintaInstrumentsResource.class.getResourceAsStream(
        "/datasets/musicpinta-instruments.ttl")) {
      testModel = Rio.parse(testDatasetIn, "http://leeds.ac.uk/resource/", RDFFormat.TURTLE);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format(
              "The MusicPintaInstrumentsResource could not be setup, because the test set cannot be loaded: %s",
              e.getMessage()));
    }
  }

  @Autowired
  public MusicPintaInstrumentsResource(@Qualifier("getSparqlDAO") KGSparqlDAO sparqlDAO,
      @Qualifier("getGremlinDAO") KGGremlinDAO gremlinDAO,
      @Qualifier("getFullTextSearchDAO") KGFullTextSearchDAO fullTextSearchDAO) {
    super(sparqlDAO, gremlinDAO, fullTextSearchDAO);
  }

  @Override
  public Model getKnowledgeGraphModel() {
    return testModel;
  }

}
