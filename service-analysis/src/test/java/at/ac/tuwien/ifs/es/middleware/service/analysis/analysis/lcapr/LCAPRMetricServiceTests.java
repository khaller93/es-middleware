package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.lcapr;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.centrality.pagerank.PageRankCentralityMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.similarity.icpr.LCAPRMetricService;
import at.ac.tuwien.ifs.es.middleware.service.analysis.value.normalization.DecimalNormalizedAnalysisValue;
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
    DecimalNormalizedAnalysisValue lcaprValue = lcaprMetricService
        .getValueFor(ResourcePair.of(new Resource(
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne"),
            new Resource(
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera")));
    assertNotNull(lcaprValue);
    assertThat(lcaprValue.getValue().doubleValue(), is(closeTo(pageRankCentralityMetricService
        .getValueFor(new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"))
        .getValue().doubleValue(), 0.001)));
  }

  @Test
  public void computeLCAPRForUnknownPair_mustReturnZeroValue() {
    DecimalNormalizedAnalysisValue lcaprValue = lcaprMetricService
        .getValueFor(ResourcePair.of(new Resource("test://a"), new Resource("test://b")));
    assertThat(lcaprValue.getValue().doubleValue(), is(0.0));
  }
}
