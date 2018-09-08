package at.ac.tuwien.ifs.es.middleware.service.analysis;

import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.fts.FullTextSearchDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.gremlin.GremlinDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.dao.knowledgegraph.event.sparql.SPARQLDAOUpdatedEvent;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.fts.FullTextSearchService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.gremlin.GremlinService;
import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.sparql.SPARQLService;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AnalysisPipelineProcessor {

  /**
   * Registers the given {@code analysisService} with the given {@code requirements}.
   *
   * @param analysisService {@link AnalysisService} that shall be registered.
   * @param requiresSPARQL {@code true}, if {@link SPARQLService} is required, otherwise {@code
   * false}.
   * @param requiresFTS {@code true}, if {@link FullTextSearchService} is required, otherwise {@code
   * false}.
   * @param requiresGremlin {@code true}, if {@link GremlinService} is required, otherwise {@code
   * false}.
   * @param requirements {@link Class} representing analysis services that are required by the given
   * {@code analysisService}.
   */
  void registerAnalysisService(AnalysisService analysisService,
      boolean requiresSPARQL, boolean requiresFTS, boolean requiresGremlin,
      Set<Class<? extends AnalysisService>> requirements);

  class Entry {

    private AnalysisService analysisService;
    private Set<Class<?>> requirements;
    private Set<Class<? extends AnalysisService>> implementedServiceClasses;

    public Entry(AnalysisService analysisService, Set<Class<?>> requirements) {
      this.analysisService = analysisService;
      this.requirements = requirements;
      this.implementedServiceClasses = getImplementedAnalysisService();
    }

    private Entry(AnalysisService analysisService, Set<Class<?>> requirements,
        Set<Class<? extends AnalysisService>> implementedServiceClasses) {
      this.analysisService = analysisService;
      this.requirements = requirements;
      this.implementedServiceClasses = implementedServiceClasses;
    }

    public Entry deepCopy() {
      return new Entry(analysisService, new HashSet<>(requirements),
          new HashSet<>(implementedServiceClasses));
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends AnalysisService>> getImplementedAnalysisService() {
      Set<Class<? extends AnalysisService>> analysisInterfaces = new HashSet<>();
      analysisInterfaces.add(analysisService.getClass());
      Class<?>[] interfaces = analysisService.getClass().getInterfaces();
      for (Class<?> anInterface : interfaces) {
        if (AnalysisService.class.isAssignableFrom(anInterface)) {
          analysisInterfaces.add((Class<? extends AnalysisService>) anInterface);
        }
      }
      return analysisInterfaces;
    }

    public AnalysisService getAnalysisService() {
      return analysisService;
    }

    public Set<Class<?>> getOpenRequirements() {
      return requirements;
    }

    public Set<Class<? extends AnalysisService>> getImplementedAnalysisServiceClasses() {
      return implementedServiceClasses;
    }

    public void removeRequirement(Class<?> clazz) {
      requirements.remove(clazz);
    }

    public boolean hasOpenRequirements() {
      return !requirements.isEmpty();
    }

    @Override
    public String toString() {
      return "Entry{" +
          "analysisService=" + analysisService +
          ", requirements=" + requirements +
          ", implementedServiceClasses=" + implementedServiceClasses +
          '}';
    }
  }

}
