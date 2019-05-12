package at.ac.tuwien.ifs.es.middleware.dto.exploration.context.util.box;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/**
 * This class returns instances of {@link ValueBox}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public final class ValueBoxFactory {

  /**
   * Returns a brand new {@link ValueBox}.
   *
   * @return a brand new {@link ValueBox}.
   */
  public static ValueBox newBox() {
    return new ValueBoxImpl();
  }

  /**
   * Returns a brand new {@link ValueBox} with the data in the given map.
   *
   * @param dataMap a map of data, which should be initially stored into the value box.
   * @return a brand new {@link ValueBox} with the data in the given map.
   */
  public static ValueBox newBox(Map<String, JsonNode> dataMap) {
    return new ValueBoxImpl(dataMap);
  }

}
