package at.ac.tuwien.ifs.es.middleware.tests.integration;

import at.ac.tuwien.ifs.es.middleware.tests.integration.explorationflow.DynamicExploratoryMusicPintaFlowTest;
import at.ac.tuwien.ifs.es.middleware.tests.integration.explorationflow.ExploratoryControllerMusicPintaFTSTest;
import at.ac.tuwien.ifs.es.middleware.tests.integration.sparql.SPARQLControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({DynamicExploratoryMusicPintaFlowTest.class,
    ExploratoryControllerMusicPintaFTSTest.class,
    SPARQLControllerTest.class
})
public class IntegrationTestSuite {

}
