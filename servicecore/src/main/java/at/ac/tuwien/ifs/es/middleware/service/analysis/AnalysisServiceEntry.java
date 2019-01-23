package at.ac.tuwien.ifs.es.middleware.service.analysis;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
class AnalysisServiceEntry {

  private String name;
  private AnalysisService analysisService;
  private Set<Class<?>> requirements;
  private Set<Class<? extends AnalysisService>> implementedServiceClasses;
  private boolean disabled;

  public AnalysisServiceEntry(String name, AnalysisService analysisService,
      Set<Class<?>> requirements, boolean disabled) {
    this.name = name;
    this.analysisService = analysisService;
    this.requirements = requirements;
    this.implementedServiceClasses = getImplementedAnalysisService();
    this.disabled = disabled;
  }

  private AnalysisServiceEntry(String name, AnalysisService analysisService,
      Set<Class<?>> requirements,
      Set<Class<? extends AnalysisService>> implementedServiceClasses) {
    this.name = name;
    this.analysisService = analysisService;
    this.requirements = requirements;
    this.implementedServiceClasses = implementedServiceClasses;
  }

  public String getName() {
    return name;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public AnalysisServiceEntry deepCopy() {
    return new AnalysisServiceEntry(this.name, analysisService, new HashSet<>(requirements),
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
    return "AE {" +
        "name='" + name + '\'' +
        ", analysisService=" + analysisService +
        ", requirements=" + requirements +
        ", implementedServiceClasses=" + implementedServiceClasses +
        ", disabled=" + disabled +
        '}';
  }
}
