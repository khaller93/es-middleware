package at.ac.tuwien.ifs.es.middleware.service.analysis.analysis.classresource;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import at.ac.tuwien.ifs.es.middleware.kg.abstraction.rdf.Resource;
import at.ac.tuwien.ifs.es.middleware.service.analysis.dataset.resources.ClassResourceService;
import at.ac.tuwien.ifs.es.middleware.testutil.WineOntologyDatasetResource;
import at.ac.tuwien.ifs.es.middleware.testutil.util.TestUtil;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ClassResourceServiceTests {

  @Rule
  @Autowired
  public WineOntologyDatasetResource wineOntologyDatasetResource;
  @Autowired
  private ClassResourceService classResourceService;

  public void setUp() throws Exception {
    classResourceService.compute();
  }

  @Test
  public void getClassResourcesForWine_mustReturnAll4WineResources() {
    Optional<Set<Resource>> resourceSetOptional = classResourceService.getInstancesOfClass(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine"));
    assertNotNull(resourceSetOptional);
    assertTrue(resourceSetOptional.isPresent());
    assertThat(resourceSetOptional.get(),
        containsInAnyOrder(TestUtil.mapToResource(
            Arrays.asList(
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChateauDYchemSauterne",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossRothermelTrochenbierenausleseRiesling",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SchlossVolradTrochenbierenausleseRiesling",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhitehallLanePrimavera"))));
  }

  @Test
  public void getClassResourcesForWineGrape_mustReturnAll16WineGrapeResources() {
    Optional<Set<Resource>> resourceSetOptional = classResourceService.getInstancesOfClass(
        new Resource("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WineGrape"));
    assertNotNull(resourceSetOptional);
    assertTrue(resourceSetOptional.isPresent());
    assertThat(resourceSetOptional.get(),
        containsInAnyOrder(TestUtil.mapToResource(
            Arrays.asList(
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#CheninBlancGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#PinotBlancGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SauvignonBlancGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ChardonnayGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SemillonGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#ZinfandelGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#CabernetSauvignonGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#RieslingGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#PinotNoirGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#MerlotGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#PetiteSyrahGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#CabernetFrancGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#MalbecGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#PetiteVerdotGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#GamayGrape",
                "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#SangioveseGrape"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getClassResourcesForNull_mustThrowIllegalArgumentException() {
    classResourceService.getInstancesOfClass(null);
  }

  @Test
  public void getClassResourcesForUnknownClass_mustReturnEmptyOptional() {
    Optional<Set<Resource>> resourceSetOptional = classResourceService.getInstancesOfClass(
        new Resource("test://a"));
    assertNotNull(resourceSetOptional);
    assertFalse(resourceSetOptional.isPresent());
  }
}
