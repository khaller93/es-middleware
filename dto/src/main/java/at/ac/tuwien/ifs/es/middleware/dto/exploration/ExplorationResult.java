package at.ac.tuwien.ifs.es.middleware.dto.exploration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * This is a marker interface for the result.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(use = Id.NAME)
@JsonSubTypes({
    @Type(value = ResourceList.class),
    @Type(value = ResourcePairs.class),
    @Type(value = Neighbourhood.class)
})
public interface ExplorationResult {

  /**
   * Returns a deep copy of this exploration result.
   *
   * @return a deep copy of this exploration result.
   */
  <T extends ExplorationResult> T deepCopy();

}
