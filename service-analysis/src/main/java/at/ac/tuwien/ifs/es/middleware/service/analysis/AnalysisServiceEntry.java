package at.ac.tuwien.ifs.es.middleware.service.analysis;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class wraps an {@link AnalysisService} and maintains useful for this service, making them
 * available without costly calls.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class AnalysisServiceEntry {

  private final String name;
  private final AnalysisService analysisService;
  private final Set<Class<?>> requirements;
  private final Set<Class<? extends AnalysisService>> implementedServiceClasses;
  private final boolean disabled;

  /**
   * Creates a new {@link AnalysisServiceEntry} wrapping the given {@link AnalysisService}.
   *
   * @param name of the {@link AnalysisService} as its specified in its annotation.
   * @param analysisService {@link AnalysisService} which shall be wrapped into this entry.
   * @param requirements DAO services ({@link at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.SPARQLService},
   * {@link at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService}, {@link
   * at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.FullTextSearchService}) or {@link
   * AnalysisService}s that are needed to be computed before this one can be executed.
   * @param disabled {@code true}, if this {@link AnalysisService} shall not be computed. otherwise
   * {@code false}.
   */
  AnalysisServiceEntry(String name, AnalysisService analysisService,
      Set<Class<?>> requirements, boolean disabled) {
    checkArgument(name != null && !name.isEmpty(), "The given name must not  be null.");
    checkArgument(analysisService != null, "The given analysis service must not be null.");
    this.name = name;
    this.analysisService = analysisService;
    this.requirements = requirements != null ? requirements : Collections.emptySet();
    this.implementedServiceClasses = getImplementedAnalysisService();
    this.disabled = disabled;
  }

  /**
   * Acquires the interfaces that are implemented by this {@link AnalysisService}.
   *
   * @return the interfaces that are implemented by this {@link AnalysisService}.
   */
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

  /**
   * Gets the name of analysis service, how it can be referenced in the exploration flow API.
   *
   * @return the name of analysis service, how it can be referenced in the exploration flow API.
   */
  public String getName() {
    return name;
  }

  /**
   * Checks whether this {@link AnalysisService} is disabled and shall not be scheduled.
   *
   * @return {@code true}, if it shall be scheduled, otherwise {@code false}.
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Gets the instance of the wrapped {@link AnalysisService}.
   *
   * @return the instance of the wrapped {@link AnalysisService}.
   */
  public AnalysisService getAnalysisService() {
    return analysisService;
  }

  /**
   * Gets a set of requirements (interfaces) needed by this {@link AnalysisService}.
   *
   * @return a set of requirements (interfaces) needed by this {@link AnalysisService}.
   */
  public Set<Class<?>> getRequirements() {
    return requirements;
  }

  /**
   * Gets a set of analysis services that are implemented by this service.
   *
   * @return a set of analysis services that are implemented by this service.
   */
  public Set<Class<? extends AnalysisService>> getImplementedAnalysisServiceClasses() {
    return implementedServiceClasses;
  }

  @Override
  public String toString() {
    return "AE {" +
        "name='" + name + '\'' +
        ", analysisService=" + analysisService +
        ", requirements=" + requirements +
        ", implementedServiceClasses=" + implementedServiceClasses +
        ", disabled=" + disabled +
        '}';
  }
}
