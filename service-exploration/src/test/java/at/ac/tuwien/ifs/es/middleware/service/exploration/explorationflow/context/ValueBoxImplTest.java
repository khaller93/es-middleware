package at.ac.tuwien.ifs.es.middleware.service.exploration.explorationflow.context;

import at.ac.tuwien.ifs.es.middleware.service.exploration.context.util.box.ValueBox;
import at.ac.tuwien.ifs.es.middleware.service.exploration.context.util.box.ValueBoxImpl;

public class ValueBoxImplTest extends ValueBoxTest {

  @Override
  protected ValueBox getValueBox() {
    return new ValueBoxImpl();
  }
}
