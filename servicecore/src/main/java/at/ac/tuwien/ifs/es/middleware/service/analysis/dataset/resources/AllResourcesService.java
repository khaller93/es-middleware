package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.AnalysisService;
import java.util.List;
import java.util.Optional;

/**
 * An implementation of {@link AnalysisService} that provides a method to fetch all resources in a
 * knowledge graph.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public interface AllResourcesService extends AnalysisService {

  /**
   * Gets a list of all {@link Resource}s in the knowledge graph.
   *
   * @return a list of all {@link Resource}s in the knowledge graph.
   */
  List<Resource> getResourceList();

  /**
   * Gets the {@link Integer} key of the given resource, if it has been mapped correctly cleanSetup.
   * The long key is not going to change over time (is durable).
   *
   * @param resource for which the {@link Integer} key shall be returned.
   * @return {@link Integer} key of the given resource, if mapped, otherwise {@link
   * Optional#empty()}.
   */
  Optional<Integer> getResourceKey(Resource resource);

  /**
   * Gets the resource id for the given {@code key}.
   *
   * @param key {@link Integer} for which the corresponding resource id shall be returned. It must
   * not be null.
   * @return resource id for the given {@code key}, or otherwise {@link * Optional#empty()}, if not
   * mapped.
   */
  Optional<String> getResourceIdFor(Integer key);

}
