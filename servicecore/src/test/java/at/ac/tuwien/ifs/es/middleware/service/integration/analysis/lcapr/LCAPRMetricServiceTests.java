package at.ac.tuwien.ifs.es.middleware.service.integration.analysis.lcapr;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr.LCAPRMetricService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class defines test cases for {@link LCAPRMetricService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class LCAPRMetricServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private PageRankCentralityMetricService pageRankCentralityMetricService;
  @Autowired
  private LCAPRMetricService lcaprMetricService;

  public void setUp() throws Exception {
    pageRankCentralityMetricService.compute();
    lcaprMetricService.compute();
  }

  @Test(expected = IllegalArgumentException.class)
  public void computeLCAPR_forNullPair_mustThrowIllegalArgumentException() {
    lcaprMetricService.getValueFor(null);
  }

  @Test
  public void computeLCAPRforSpecificWineResourcePair_mustReturnPROfWine() {
    Double lcaprValue = lcaprMetricService.getValueFor(ResourcePair.of(new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne"), new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera")));
    assertNotNull(lcaprValue);
    assertThat(lcaprValue, is(pageRankCentralityMetricService
        .getValueFor(new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"))));
  }

  @Test
  public void computeLCAPRForUnknownPair_mustReturnZeroValue() {
    Double lcaprValue = lcaprMetricService
        .getValueFor(ResourcePair.of(new Resource("test://a"), new Resource("test://b")));
    assertThat(lcaprValue, is(0.0));
  }
}
