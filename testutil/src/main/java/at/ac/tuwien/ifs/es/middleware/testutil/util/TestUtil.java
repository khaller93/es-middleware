package at.ac.tuwien.ifs.es.middleware.testutil.util;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public final class TestUtil {


  public static Resource[] mapToResource(List<String> resourceList) {
    if (resourceList == null) {
      return null;
    }
    return resourceList.stream().map(Resource::new).toArray(Resource[]::new);
  }

}
