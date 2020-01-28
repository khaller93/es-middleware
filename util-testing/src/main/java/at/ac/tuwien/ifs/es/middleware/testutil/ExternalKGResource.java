package at.ac.tuwien.ifs.es.middleware.testutil;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGFullTextSearchDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGGremlinDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.KGSparqlDAO;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.config.KnowledgeGraphDAOConfig;
import at.ac.tuwien.ifs.es.middleware.scheduler.SchedulerPipeline;
import at.ac.tuwien.ifs.es.middleware.scheduler.TaskStatus.VALUE;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.assertj.core.util.Lists;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jetbrains.annotations.NotNull;
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

  private final KGSparqlDAO sparqlDAO;
  private final SchedulerPipeline pipeline;
  private final String insertSPARQLQuery;

  public ExternalKGResource(KGSparqlDAO sparqlDAO, SchedulerPipeline pipeline, Model testData)
      throws IOException {
    this.sparqlDAO = sparqlDAO;
    this.pipeline = pipeline;
    this.insertSPARQLQuery = getInsertSparqlQuery(testData);
  }

  private String getInsertSparqlQuery(Model testData) throws IOException {
    Iterator<Statement> statementIterator = testData.iterator();
    if (statementIterator.hasNext()) {
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        while (statementIterator.hasNext()) {
          Rio.write(statementIterator.next(), out, RDFFormat.NTRIPLES);
        }
        return String.format("INSERT DATA {%s}", new String(out.toByteArray()));
      }
    }
    return null;
  }

  @Override
  protected void before() throws Throwable {
    before(Lists.newArrayList(KGSparqlDAO.class.getName(),
        KGGremlinDAO.class.getName(), KGFullTextSearchDAO.class.getName()));
  }

  public void before(List<String> taskIds) throws IOException {
    /* clean */
    UpdatedFuture updatedFuture = new UpdatedFuture(Lists.newArrayList(KGSparqlDAO.class.getName(),
        KGGremlinDAO.class.getName(), KGFullTextSearchDAO.class.getName()),
        Instant.now().toEpochMilli());
    sparqlDAO.update("DELETE {?s ?p ?o} WHERE {?s ?p ?o}");
    updatedFuture.get();
    /* insert data */
    if (insertSPARQLQuery != null) {
      updatedFuture = new UpdatedFuture(taskIds, Instant.now().toEpochMilli());
      sparqlDAO.update(insertSPARQLQuery);
      updatedFuture.get();
    }
  }

  /**
   * Future for waiting that some tasks are computed successfully.
   */
  public class UpdatedFuture implements Future<Boolean> {

    private final List<String> waitingForIds;
    private final Lock lock;
    private boolean canceled;

    public UpdatedFuture(Collection<String> waitingForIds, long timestamp) {
      this.waitingForIds = new LinkedList<>(waitingForIds);
      this.lock = new ReentrantLock(true);
      this.canceled = false;
      this.waitingForIds.forEach(id -> {
        pipeline.registerChangeListener(id, (cid, status) -> {
          if (status.getTimestamp() >= timestamp && VALUE.OK.equals(status.getStatus())) {
            lock.lock();
            try {
              UpdatedFuture.this.waitingForIds.remove(cid);
            } finally {
              lock.unlock();
            }
          }
        });
      });
    }

    @Override
    public boolean cancel(boolean b) {
      canceled = true;
      return true;
    }

    @Override
    public boolean isCancelled() {
      return canceled;
    }

    @Override
    public boolean isDone() {
      lock.lock();
      try {
        return waitingForIds.isEmpty() || canceled;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public Boolean get() {
      boolean empty = false;
      do {
        lock.lock();
        try {
          empty = waitingForIds.isEmpty();
          Thread.sleep(100);
        } catch (InterruptedException e) {

        } finally {
          lock.unlock();
        }
      } while (!empty && !canceled);
      return empty;
    }

    @Override
    public Boolean get(long l, @NotNull TimeUnit timeUnit) {
      lock.lock();
      try {
        timeUnit.sleep(l);
        return waitingForIds.isEmpty();
      } catch (InterruptedException e) {

      } finally {
        lock.unlock();
      }
      return false;
    }
  }

}
