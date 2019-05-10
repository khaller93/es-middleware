package at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ExplorationContext;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.ResourceList;
import at.ac.tuwien.ifs.es.middleware.dto.exploration.context.result.Resource;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.exploitation.resourcelist.AddResourcesOperator;
import at.ac.tuwien.ifs.es.middleware.service.exploration.operators.payload.acquisition.AddResourcesPayload;
import at.ac.tuwien.ifs.es.middleware.testutil.util.TestUtil;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests {@link AddResourcesOperator}.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AddResourcesOperator.class})
@TestPropertySource(properties = {
    "esm.db.choice=RDF4J",
    "esm.db.sparql.choice=RDF4JMemoryStoreWithLucene",
    "esm.db.fts.choice=RDF4JLucene",
    "esm.db.gremlin.choice=ClonedInMemoryGremlin"
})
public class AddResourcesOperatorTests {

  @Autowired
  private AddResourcesOperator addResourcesOperator;
  private ResourceList resourceList;

  @Before
  public void setUp() throws Exception {
    resourceList = new ResourceList(Arrays.asList(TestUtil.mapToResource(Arrays.asList(
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
        "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }

  @Test
  public void addsResourcesAtEnd_mustReturnCorrespondingList() {
    AddResourcesPayload payload = new AddResourcesPayload(Collections.singletonList(
        new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne")));
    ExplorationContext context = addResourcesOperator.apply(resourceList, payload);
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    assertThat(((ResourceList) context).asResourceList(),
        contains(TestUtil.mapToResource(Arrays.asList(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne"))));
  }

  @Test
  public void addsResourcesAtBeginning_mustReturnCorrespondingList() {
    AddResourcesPayload payload = new AddResourcesPayload(Collections.singletonList(
        new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne")), null,
        0);
    ExplorationContext context = addResourcesOperator.apply(resourceList, payload);
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    assertThat(((ResourceList) context).asResourceList(),
        contains(TestUtil.mapToResource(Arrays.asList(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }

  @Test
  public void addsResourcesInTheMiddle_mustReturnCorrespondingList() {
    AddResourcesPayload payload = new AddResourcesPayload(Collections.singletonList(
        new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne")), null,
        2);
    ExplorationContext context = addResourcesOperator.apply(resourceList, payload);
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    assertThat(((ResourceList) context).asResourceList(),
        contains(TestUtil.mapToResource(Arrays.asList(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }

  @Test
  public void addsResourceDuplicates_mustReturnSameList() {
    AddResourcesPayload payload = new AddResourcesPayload(Arrays.asList(
        new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling"),
        new Resource(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling")),
        true);
    ExplorationContext context = addResourcesOperator.apply(resourceList, payload);
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    assertThat(((ResourceList) context).asResourceList(),
        contains(TestUtil.mapToResource(Arrays.asList(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }

  @Test
  public void addsEmptyResourceList_mustReturnSameList() {
    AddResourcesPayload payload = new AddResourcesPayload(Collections.emptyList());
    ExplorationContext context = addResourcesOperator.apply(resourceList, payload);
    assertNotNull(context);
    assertThat(context, instanceOf(ResourceList.class));
    assertThat(((ResourceList) context).asResourceList(),
        contains(TestUtil.mapToResource(Arrays.asList(
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
            "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }

}
