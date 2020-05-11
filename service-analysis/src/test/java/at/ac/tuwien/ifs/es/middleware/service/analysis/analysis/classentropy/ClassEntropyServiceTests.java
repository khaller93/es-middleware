package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.classentropy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class tests {@link ClassEntropyService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class ClassEntropyServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private ClassEntropyService classEntropyService;

  public void setUp() throws Exception {
    classEntropyService.compute();
  }

  @Test
  public void getEntropyForWine_mustReturnNonZeroValue() {
    DecimalNormalizedAnalysisValue wineEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"));
    assertNotNull(wineEntropy);
    assertThat(wineEntropy.getValue().doubleValue(), greaterThan(0.0));
    DecimalNormalizedAnalysisValue italianWineEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ItalianWine"));
    assertNotNull(italianWineEntropy);
    assertThat(italianWineEntropy.getValue().doubleValue(), greaterThan(0.0));
    assertThat(wineEntropy.getValue().doubleValue(),
        lessThan(italianWineEntropy.getValue().doubleValue()));
  }

  @Test
  public void getEntropyForNonClass_mustBeNull() {
    DecimalNormalizedAnalysisValue categoryEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bancroft"));
    assertNull(categoryEntropy);
  }

  @Test
  public void getEntropyForUnknownResource_mustBeNull() {
    DecimalNormalizedAnalysisValue unknownResourceEntropy = classEntropyService
        .getEntropyForClass(new Resource("test:a"));
    assertNull(unknownResourceEntropy);
  }
}
