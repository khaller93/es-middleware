package at.ac.tuwien.ifs.es.middleware.service.integration;

import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.DynamicExplorationFlowFactoryMusicPintaTest;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition.AddResourcesOperatorTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition.AllResourcesTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition.FullTextSearchTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition.MultipleResourcesTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition.RemoveResourcesOperatorTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.acquisition.SingleResourceTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.DistinctTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.LimitTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.OffsetTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.OrderByTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.SampleTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.WeightedSumTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.normalization.MinMaxNormalizationTests;
import at.ac.tuwien.ifs.es.middleware.service.integration.explorationflow.aggregation.normalization.ZScoreTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This test suite is testing services on the exploration flow.
 *
 * @author Kevin Haller
 * @version 1.0
 * @since 1.0
 */
@RunWith(Suite.class)
@SuiteClasses({DynamicExplorationFlowFactoryMusicPintaTest.class, AllResourcesTests.class,
    FullTextSearchTests.class, MultipleResourcesTests.class, SingleResourceTests.class,
    MinMaxNormalizationTests.class, ZScoreTests.class, DistinctTests.class,
    LimitTests.class, OffsetTests.class, OrderByTests.class, SampleTests.class,
    WeightedSumTests.class, AddResourcesOperatorTests.class, RemoveResourcesOperatorTests.class})
public class ExplorationFlowTestSuite {


}
