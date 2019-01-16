package at.ac.tuwien.ifs.es.middleware.dto.status;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * This class maintains the possible status of a knowledge graph DAO.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "status")
@JsonSubTypes({@JsonSubTypes.Type(value = KGDAOInitStatus.class, name = "INITIALIZING"),
    @JsonSubTypes.Type(value = KGDAOReadyStatus.class, name = "READY"),
    @JsonSubTypes.Type(value = KGDAOUpdatingStatus.class, name = "UPDATING"),
        @JsonSubTypes.Type(value = KGDAOFailedStatus.class, name = "FAILED")
})
public abstract class KGDAOStatus {

  public enum CODE {INITIATING, READY, UPDATING, FAILED}

  @JsonIgnore
  private CODE code;

  public KGDAOStatus(CODE code) {
    this.code = code;
  }

  /**
   * Gets the code for the status.
   *
   * @return the code for the status.
   */
  public CODE getCode() {
    return code;
  }

  @Override
  public String toString() {
    return "KGDAOStatus{" +
        "code=" + code +
        '}';
  }
}
