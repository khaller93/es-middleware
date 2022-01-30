package at.ac.tuwien.ifs.es.middleware.service.analysis.normalizer;

import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils.Normalizer;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.utils.PartialNormalizer;

public class PartialNormalizerTests extends SimpleNormalizerTests {

  @Override
  protected Normalizer<String> getNormalizer() {
    return new PartialNormalizer<>();
  }
}
