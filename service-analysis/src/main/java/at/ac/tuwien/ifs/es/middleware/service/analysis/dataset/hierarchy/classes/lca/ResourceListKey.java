package at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.hierarchy.classes.lca;

import static com.google.common.base.Preconditions.checkNotNull;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class ResourceListKey implements Serializable {

  private final List<Resource> resourceList;

  ResourceListKey(Set<Resource> resourceList) {
    checkNotNull(resourceList);
    this.resourceList = resourceList.stream().sorted(
        Comparator.comparing(Resource::getId)).collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceListKey that = (ResourceListKey) o;
    for (int i = 0; i < resourceList.size(); i++) {
      if (!resourceList.get(i).equals(that.resourceList.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(resourceList.stream().map(Resource::getId).collect(Collectors.joining(",")));
  }

  @Override
  public String toString() {
    return "ResourceListKey[" + resourceList.stream().map(Resource::getId)
        .collect(Collectors.toList()) + ']';
  }
}
