package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.aquisition.RemoveResourcesOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.RemoveResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.testutil.util.TestUtil;
import java.util.Arrays;
import java.util.Collections;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests {@link RemoveResourcesOperator}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RemoveResourcesOperator.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class RemoveResourcesOperatorTests {

  @Autowired
  private RemoveResourcesOperator removeResourcesOperator;
  private ResourceList resourceList;

  @Before
  public void setUp() throws Exception {
    resourceList = new ResourceList(Arrays.asList(TestUtil.mapToResource(Arrays.asList(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }

  @Test
  public void removeResourceSet_mustReturnCorrespondingList() {
    RemoveResourcesPayload payload = new RemoveResourcesPayload(Sets.newLinkedHashSet(new Resource(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling")));
    ExplorationContext context = removeResourcesOperator.apply(resourceList, payload);
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    assertThat(((ResourceList) context).asResourceList(),
        contains(TestUtil.mapToResource(Arrays.asList(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }

  @Test
  public void removeAllResources_mustReturnEmptyResourceList() {
    RemoveResourcesPayload payload = new RemoveResourcesPayload(Sets.newLinkedHashSet(new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling"),
        new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling"),
        new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera")));
    ExplorationContext context = removeResourcesOperator.apply(resourceList, payload);
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    assertThat(((ResourceList) context).asResourceList(), hasSize(0));
  }

  @Test
  public void removeNoResources_mustReturnSameResourceList() {
    RemoveResourcesPayload payload = new RemoveResourcesPayload(Collections.emptySet());
    ExplorationContext context = removeResourcesOperator.apply(resourceList, payload);
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    assertThat(((ResourceList) context).asResourceList(),
        contains(TestUtil.mapToResource(Arrays.asList(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }
}
