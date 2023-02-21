package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig;
import com.hartwig.actin.clinical.curation.config.TestCurationConfigFactory;
import com.hartwig.actin.clinical.datamodel.TestPriorOtherConditionFactory;
import com.hartwig.actin.clinical.datamodel.TestPriorSecondPrimaryFactory;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class CurationDatabaseValidatorTest {

    @Test
    public void doesNotCrashOnTestCurationDatabase() {
        CurationDatabaseValidator validator = TestCurationFactory.createMinimalTestCurationDatabaseValidator();

        validator.validate(TestCurationFactory.createTestCurationDatabase());
    }

    @Test
    public void canIdentifyInvalidPrimaryTumorConfigs() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDatabaseValidator.CANCER_PARENT_DOID, "child");

        PrimaryTumorConfig valid = TestCurationConfigFactory.primaryTumorConfigBuilder().addDoids("child").build();
        assertTrue(CurationDatabaseValidator.validatePrimaryTumorConfigs(Lists.newArrayList(valid), doidModel));

        PrimaryTumorConfig invalid = TestCurationConfigFactory.primaryTumorConfigBuilder().addDoids("invalid").build();
        assertFalse(CurationDatabaseValidator.validatePrimaryTumorConfigs(Lists.newArrayList(invalid), doidModel));
    }

    @Test
    public void canIdentifyInvalidSecondPrimaryConfigs() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDatabaseValidator.CANCER_PARENT_DOID, "child");

        SecondPrimaryConfig valid = TestCurationConfigFactory.secondPrimaryConfigBuilder()
                .curated(TestPriorSecondPrimaryFactory.builder().addDoids("child").build())
                .build();
        assertTrue(CurationDatabaseValidator.validateSecondPrimaryConfigs(Lists.newArrayList(valid), doidModel));

        SecondPrimaryConfig invalid = TestCurationConfigFactory.secondPrimaryConfigBuilder()
                .curated(TestPriorSecondPrimaryFactory.builder().addDoids("invalid").build())
                .build();
        assertFalse(CurationDatabaseValidator.validateSecondPrimaryConfigs(Lists.newArrayList(invalid), doidModel));
    }

    @Test
    public void canIdentifyInvalidNonOncologicalHistoryConfigs() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDatabaseValidator.GENERIC_PARENT_DOID, "child");

        NonOncologicalHistoryConfig valid = TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder()
                .curated(TestPriorOtherConditionFactory.builder().addDoids("child").build())
                .build();
        assertTrue(CurationDatabaseValidator.validateNonOncologicalHistoryConfigs(Lists.newArrayList(valid), doidModel));

        NonOncologicalHistoryConfig other = TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder().curated(1).build();
        assertTrue(CurationDatabaseValidator.validateNonOncologicalHistoryConfigs(Lists.newArrayList(other), doidModel));

        NonOncologicalHistoryConfig invalid = TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder()
                .curated(TestPriorOtherConditionFactory.builder().addDoids("invalid").build())
                .build();
        assertFalse(CurationDatabaseValidator.validateNonOncologicalHistoryConfigs(Lists.newArrayList(invalid), doidModel));
    }

    @Test
    public void canIdentifyInvalidIntoleranceConfigs() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDatabaseValidator.GENERIC_PARENT_DOID, "child");

        IntoleranceConfig valid = TestCurationConfigFactory.intoleranceConfigBuilder().addDoids("child").build();
        assertTrue(CurationDatabaseValidator.validateIntoleranceConfigs(Lists.newArrayList(valid), doidModel));

        IntoleranceConfig invalid = TestCurationConfigFactory.intoleranceConfigBuilder().addDoids("invalid").build();
        assertFalse(CurationDatabaseValidator.validateIntoleranceConfigs(Lists.newArrayList(invalid), doidModel));
    }

    @Test
    public void emptyDoidsIsInvalid() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        assertFalse(CurationDatabaseValidator.hasValidDoids(Sets.newHashSet(), doidModel, "parent"));
    }
}