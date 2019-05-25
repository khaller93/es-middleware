package at.ac.tuwien.ifs.es.middleware.service.integration.analysis.classentropy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import at.ac.tuwien.ifs.es.middleware.common.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.ClassEntropyService;
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
    Double wineEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"));
    assertNotNull(wineEntropy);
    assertThat(wineEntropy, greaterThan(0.0));
    Double italianWineEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ItalianWine"));
    assertNotNull(italianWineEntropy);
    assertThat(italianWineEntropy, greaterThan(0.0));
    assertThat(wineEntropy, lessThan(italianWineEntropy));
  }

  @Test
  public void getEntropyForNonClass_mustBeNull() {
    Double categoryEntropy = classEntropyService
        .getEntropyForClass(
            new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Bancroft"));
    assertNull(categoryEntropy);
  }

  @Test
  public void getEntropyForUnknownResource_mustBeNull() {
    Double unknownResourceEntropy = classEntropyService
        .getEntropyForClass(new Resource("test:a"));
    assertNull(unknownResourceEntropy);
  }
}
