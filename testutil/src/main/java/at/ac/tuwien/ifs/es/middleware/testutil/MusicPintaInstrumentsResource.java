package at.ac.tuwien.ifs.es.middleware.testutil;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This is an {@link ExternalResource} that loads the music pinta instruments subset into the
 * specified {@link KnowledgeGraphDAO}.
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

  private KnowledgeGraphDAO knowledgeGraphDAO;

  public MusicPintaInstrumentsResource(@Autowired KnowledgeGraphDAO knowledgeGraphDAO) {
    this.knowledgeGraphDAO = knowledgeGraphDAO;
  }

  @Override
  protected void before() throws Throwable {
    Iterator<Statement> statementIterator = testModel.iterator();
    while (statementIterator.hasNext()) {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        for (int i = 0; i < 5000 && statementIterator.hasNext(); i++) {
          Rio.write(statementIterator.next(), out, RDFFormat.NTRIPLES);
        }
        knowledgeGraphDAO.update(String.format("INSERT DATA {%s}", new String(out.toByteArray())));
      }
    }
  }

  @Override
  protected void after() {
    knowledgeGraphDAO.update("DELETE {?s ?p ?o} WHERE {?s ?p ?o}");
  }
}
