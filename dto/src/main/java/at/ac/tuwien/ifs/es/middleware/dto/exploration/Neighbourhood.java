package at.ac.tuwien.ifs.es.middleware.dto.exploration;

public class Neighbourhood implements ExplorationResult {

  @Override
  public <T extends ExplorationResult> T deepCopy() {
    return null;
  }
}
