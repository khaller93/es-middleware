package at.ac.tuwien.ifs.es.middleware.testutil;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOFailedStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.status.KGDAOStatus.CODE;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.rules.ExternalResource;

/**
 * This is an external resource that loads the musicpinta instruments subset into the specified DAOs
 * in the {@link KnowledgeGraphDAOConfig}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class ExternalKGResource extends ExternalResource {

  private KGSparqlDAO sparqlDAO;
  private KGGremlinDAO gremlinDAO;
  private KGFullTextSearchDAO fullTextSearchDAO;

  private LinkedBlockingQueue<KGDAOStatus> sparqlReadyQueue = new LinkedBlockingQueue<>();
  private LinkedBlockingQueue<KGDAOStatus> gremlinReadyQueue = new LinkedBlockingQueue<>();
  private LinkedBlockingQueue<KGDAOStatus> fulltextsearchQueue = new LinkedBlockingQueue<>();

  public ExternalKGResource(KGSparqlDAO sparqlDAO, KGGremlinDAO gremlinDAO,
      KGFullTextSearchDAO fullTextSearchDAO) {
    this.sparqlDAO = sparqlDAO;
    this.sparqlDAO.addStatusChangeListener(newStatus -> {
      if (newStatus.getCode().equals(CODE.READY)) {
        sparqlReadyQueue.add(newStatus);
      }
    });
    this.gremlinDAO = gremlinDAO;
    this.gremlinDAO.addStatusChangeListener(newStatus -> {
      if (newStatus.getCode().equals(CODE.READY)) {
        gremlinReadyQueue.add(newStatus);
      }
    });
    this.fullTextSearchDAO = fullTextSearchDAO;
    this.fullTextSearchDAO.addStatusChangeListener(newStatus -> {
      if (newStatus.getCode().equals(CODE.READY)) {
        fulltextsearchQueue.add(newStatus);
      }
    });
  }

  public abstract Model getKnowledgeGraphModel();

  public void cleanSetup() throws Throwable {
    sparqlReadyQueue.clear();
    gremlinReadyQueue.clear();
    fulltextsearchQueue.clear();
    /*  */
    gremlinDAO.lock();
    try {
      gremlinDAO.traversal().V().drop().iterate();
      gremlinDAO.traversal().E().drop().iterate();
      gremlinDAO.commit();
    } catch (Exception e) {
      gremlinDAO.rollback();
      throw e;
    } finally {
      gremlinDAO.unlock();
    }
    sparqlDAO.update("DELETE {?s ?p ?o} WHERE {?s ?p ?o}");
    /*  */
    Iterator<Statement> statementIterator = getKnowledgeGraphModel().iterator();
    while (statementIterator.hasNext()) {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        while (statementIterator.hasNext()) {
          Rio.write(statementIterator.next(), out, RDFFormat.NTRIPLES);
        }
        sparqlDAO.update(String.format("INSERT DATA {%s}", new String(out.toByteArray())));
      }
    }
  }

  @Override
  protected void before() throws Throwable {
    this.cleanSetup();
    this.waitForAllDAOsBeingReady();
  }

  public void waitForSPARQLDAOBeingReady() throws InterruptedException {
    if (!this.sparqlDAO.getStatus().getCode().equals(CODE.READY)) {
      KGDAOStatus status;
      do {
        status = this.sparqlReadyQueue.poll(1, TimeUnit.MINUTES);
      } while (status != null && !status.getCode().equals(CODE.READY) && !status.getCode()
          .equals(CODE.FAILED));
      if (status.getCode().equals(CODE.FAILED)) {
        throw new IllegalStateException(String
            .format("Ingesting data failed. %s", ((KGDAOFailedStatus) status).getErrorMessage()));
      }
    }
  }

  public void waitForGremlinDAOBeingReady() throws InterruptedException {
    if (!this.gremlinDAO.getStatus().getCode().equals(CODE.READY)) {
      KGDAOStatus status;
      do {
        status = this.gremlinReadyQueue.poll(1, TimeUnit.MINUTES);
      } while (status != null && !status.getCode().equals(CODE.READY) && !status.getCode()
          .equals(CODE.FAILED));
      if (status.getCode().equals(CODE.FAILED)) {
        throw new IllegalStateException(String
            .format("Ingesting data failed. %s", ((KGDAOFailedStatus) status).getErrorMessage()));
      }
    }
  }

  public void waitForFTSDAOBeingReady() throws InterruptedException {
    if (!this.fullTextSearchDAO.getStatus().getCode().equals(CODE.READY)) {
      KGDAOStatus status;
      do {
        status = this.fulltextsearchQueue.poll(1, TimeUnit.MINUTES);
      } while (status != null && !status.getCode().equals(CODE.READY) && !status.getCode()
          .equals(CODE.FAILED));
      if (status.getCode().equals(CODE.FAILED)) {
        throw new IllegalStateException(String
            .format("Ingesting data failed. %s", ((KGDAOFailedStatus) status).getErrorMessage()));
      }
    }
  }

  public void waitForAllDAOsBeingReady() throws InterruptedException {
    Thread.sleep(1000);
    waitForSPARQLDAOBeingReady();
    waitForGremlinDAOBeingReady();
    waitForFTSDAOBeingReady();
  }

}
