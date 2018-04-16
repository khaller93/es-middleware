package at.ac.tuwien.ifs.es.middleware.dto.exploration.context;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ResourcePairList implements ExplorationContext<ResourcePair> {


  @Override
  public Collection<ResourcePair> getResultsCollection() {
    return null;
  }

  @Override
  public void setResults(Collection<ResourcePair> results) {

  }

  @Override
  public void removeResult(ResourcePair result) {

  }

  @Override
  public void setMetadata(String name, Serializable data) {

  }

  @Override
  public void removeMetadata(String name) {

  }

  @Override
  public void putValuesData(String id, List<String> path, JsonNode data) {

  }

  @Override
  public void removeValuesData(String id) {

  }

  @Override
  public Optional<JsonNode> get(String id, List<String> path) {
    return null;
  }

  @Override
  public Iterator<ResourcePair> iterator() {
    return null;
  }
}
