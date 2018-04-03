package at.ac.tuwien.ifs.exploratorysearch.dto.exploration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public class ExplorationResponse {

  private ExplorationResult result;
  private JsonNode values;

  /**
   *
   * @param result
   * @param values
   */
  @JsonCreator
  public ExplorationResponse(@JsonProperty("result") ExplorationResult result,
      @JsonProperty("values") JsonNode values) {
    this.result = result;
    this.values = values;
  }

  /**
   * Gets the result.
   *
   * @return the result.
   */
  public ExplorationResult getResult() {
    return result;
  }

  /**
   * Gets the values describing the given result.
   *
   * @return the values describing the given result.
   */
  public JsonNode getValues() {
    return values;
  }
}
