package at.ac.tuwien.ifs.es.middleware.service.integration.analysis.lca;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.Resource;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.resources.pairs.ResourcePair;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.classes.hierarchy.lca.LowestCommonAncestorService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class defines test cases for {@link LowestCommonAncestorService}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
public abstract class LowestCommonAncestorServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private LowestCommonAncestorService lowestCommonAncestorService;

  public void setUp() throws Exception {
    lowestCommonAncestorService.compute();
  }

  @Test(expected = IllegalArgumentException.class)
  public void getLCAForNullPair_mustThrowIllegalArgumentException() {
    lowestCommonAncestorService.getLowestCommonAncestor(null);
  }

  @Test
  public void getLCAForWinePair_mustReturnWineClass() {
    Set<Resource> lowestCommonAncestor = lowestCommonAncestorService
        .getLowestCommonAncestor(ResourcePair.of(new Resource(
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne"),
            new Resource(
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera")));
    assertNotNull(lowestCommonAncestor);
    assertThat(lowestCommonAncestor, containsInAnyOrder(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine")));
  }
}
