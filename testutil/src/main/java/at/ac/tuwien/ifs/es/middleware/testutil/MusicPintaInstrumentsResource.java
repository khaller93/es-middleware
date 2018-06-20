package at.ac.tuwien.ifs.es.middleware.testutil;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.gremlin.AbstractClonedGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dto.status.KGDAOStatus.CODE;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * This is an {@link ExternalResource} that loads the music pinta instruments subset into the
 * specified {@link KnowledgeGraphDAOConfig}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
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

  private KGSparqlDAO sparqlDAO;
  private KGGremlinDAO gremlinDAO;

  LinkedBlockingQueue<ApplicationEvent> applicationEvents = new LinkedBlockingQueue<>();

  public MusicPintaInstrumentsResource(KGSparqlDAO sparqlDAO, KGGremlinDAO gremlinDAO) {
    this.sparqlDAO = sparqlDAO;
    this.gremlinDAO = gremlinDAO;
  }

  @Override
  protected void before() throws Throwable {
    Iterator<Statement> statementIterator = testModel.iterator();
    while (statementIterator.hasNext()) {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        for (int i = 0; statementIterator.hasNext(); i++) {
          Rio.write(statementIterator.next(), out, RDFFormat.NTRIPLES);
        }
        sparqlDAO.update(String.format("INSERT DATA {%s}", new String(out.toByteArray())));
      }
    }
  }

  public void waitForAllDAOsBeingReady() throws InterruptedException {
    ((AbstractClonedGremlinDAO)gremlinDAO).setUpdatedListener(
        applicationEvent -> applicationEvents.add(applicationEvent));
    ApplicationEvent event = this.applicationEvents.poll(120, TimeUnit.SECONDS);
    if(event == null){
      throw new IllegalStateException("The Gremlin DAO is not ready for the tests.");
    }
  }

  @Override
  protected void after() {
    sparqlDAO.update("DELETE {?s ?p ?o} WHERE {?s ?p ?o}");
    gremlinDAO.traversal().V().drop().iterate();
    gremlinDAO.traversal().E().drop().iterate();
  }
}
