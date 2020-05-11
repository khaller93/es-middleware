package at.ac.tuwien.ifs.es.middleware.kg.abstraction.facet;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

@JsonTypeName("excludeInstancesOf")
public class ExcludeInstancesOfFacetFilter implements FacetFilter {

  private List<Resource> classes;

  @JsonCreator
  public ExcludeInstancesOfFacetFilter(@JsonProperty("classes") List<Resource> classes) {
    this.classes = classes;
  }

  public List<Resource> getClasses() {
    return classes;
  }

  @Override
  public String toString() {
    return "ExcludeInstancesOfFacetFilter{" +
        "classes=" + classes +
        '}';
  }
}
