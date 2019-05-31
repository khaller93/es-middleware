package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.context;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.common.exploration.context.util.box.ValueBoxImpl;

public class ValueBoxImplTest extends ValueBoxTest {

  @Override
  protected ValueBox getValueBox() {
    return new ValueBoxImpl();
  }
}
