package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import static com.google.common.base.Preconditions.checkArgument;

import at.ac.tuwien.ifs.es.middleware.service.knowledgegraph.GremlinService;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.RegisterForAnalyticalProcessing;
import at.ac.tuwien.ifs.es.middleware.gremlin.util.schema.PGS;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.AllClassesService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.ClassHierarchyService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is an implementation of {@link ClassResourceService} which uses the {@link GremlinService}
 * and {@link ClassHierarchyService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@Service
@RegisterForAnalyticalProcessing(name = "esm.service.analytics.dataset.class.resource.all.gremlin",
    requiresGremlin = true, prerequisites = {AllResourcesService.class, AllClassesService.class,
    ClassHierarchyService.class})
public class ClassResourceWithGremlinService implements ClassResourceService {

  private static final Logger logger = LoggerFactory
      .getLogger(ClassResourceWithGremlinService.class);

  private final GremlinService gremlinService;
  private final PGS schema;

  private final AllResourcesService allResourcesService;
  private final AllClassesService allClassesService;
  private final ClassHierarchyService classHierarchyService;
  private final DB mapDB;

  private final HTreeMap<Integer, int[]> classResourceMap;

  @Autowired
  public ClassResourceWithGremlinService(
      GremlinService gremlinService,
      AllResourcesService allResourcesService,
      AllClassesService allClassesService,
      ClassHierarchyService classHierarchyService, DB mapDB) {
    this.gremlinService = gremlinService;
    this.schema = gremlinService.getPropertyGraphSchema();
    this.allResourcesService = allResourcesService;
    this.allClassesService = allClassesService;
    this.classHierarchyService = classHierarchyService;
    this.mapDB = mapDB;
    this.classResourceMap = mapDB
        .hashMap("esm.service.analytics.dataset.class.resource.all.gremlin", Serializer.INTEGER,
            Serializer.INT_ARRAY).createOrOpen();
  }

  @Override
  public Optional<Set<Resource>> getInstancesOfClass(Resource classResource) {
    checkArgument(classResource != null, "The class resource must not be null.");
    Optional<Integer> classResourceKeyOpt = allResourcesService.getResourceKey(classResource);
    if (classResourceKeyOpt.isPresent()) {
      int[] resourceKeys = classResourceMap.get(classResourceKeyOpt.get());
      if (resourceKeys != null) {
        return Optional.of(Stream.of(ArrayUtils.toObject(resourceKeys))
            .map(allResourcesService::getResourceIdFor)
            .filter(Optional::isPresent).map(Optional::get).map(Resource::new)
            .collect(Collectors.toSet()));
      }
    }
    return Optional.empty();
  }

  @Override
  public void compute() {
    for (Resource classResource : allClassesService.getAllClasses()) {
      Optional<Integer> classResourceKeyOpt = allResourcesService.getResourceKey(classResource);
      if (classResourceKeyOpt.isPresent()) {
        Set<Resource> classesToConsider = classHierarchyService.getSubClasses(classResource);
        classesToConsider.add(classResource);
        gremlinService.lock();
        try {
          Set<Resource> resourceSet = new HashSet<>();
          gremlinService.traversal().V().has(schema.iri().identifierAsString(),
              P.within(classesToConsider.stream().map(Resource::getId)
                  .toArray(String[]::new))).in("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
              .dedup().map(traverser -> schema.iri().<String>apply(traverser.get()))
              .forEachRemaining(resourceID -> {
                resourceSet.add(new Resource(resourceID));
              });
          classResourceMap.put(classResourceKeyOpt.get(), ArrayUtils.toPrimitive(
              resourceSet.stream().map(allResourcesService::getResourceKey)
                  .filter(Optional::isPresent).map(Optional::get).toArray(Integer[]::new)));
          gremlinService.commit();
        } finally {
          gremlinService.unlock();
        }
      }
    }
    mapDB.commit();
  }
}
