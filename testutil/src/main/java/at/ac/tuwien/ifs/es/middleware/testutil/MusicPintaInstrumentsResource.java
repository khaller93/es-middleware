package at.ac.tuwien.ifs.es.middleware.testutil;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import at.ac.tuwien.ifs.es.middleware.dao.rdf4j.RDF4JKnowledgeGraphDAO;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an {@link ExternalResource} that loads the music pinta instruments subset into the
 * specified {@link RDF4JKnowledgeGraphDAO}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Lazy
@Component
public class MusicPintaInstrumentsResource extends ExternalResource {

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

  private RDF4JKnowledgeGraphDAO knowledgeGraphDAO;

  public MusicPintaInstrumentsResource(
      @Autowired() @Qualifier("SpecifiedKnowledgeGraphDAO") KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphDAO = (RDF4JKnowledgeGraphDAO) knowledgeGraphDAO;
  }

  @Override
  protected void before() throws Throwable {
    try (RepositoryConnection con = knowledgeGraphDAO.getRepository().getConnection()) {
      con.add(testModel);
    }
  }

  @Override
  protected void after() {
    try (RepositoryConnection con = knowledgeGraphDAO.getRepository().getConnection()) {
      con.clear();
    }
  }
}
