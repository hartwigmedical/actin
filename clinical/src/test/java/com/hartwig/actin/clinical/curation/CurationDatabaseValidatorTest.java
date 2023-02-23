package com.hartwig.actin.clinical.curation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

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
    public void shouldIdentifyInvalidPrimaryTumorConfigs() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDatabaseValidator.CANCER_PARENT_DOID, "child");

        PrimaryTumorConfig valid = TestCurationConfigFactory.primaryTumorConfigBuilder().addDoids("child").build();
        assertTrue(CurationDatabaseValidator.validatePrimaryTumorConfigs(Lists.newArrayList(valid), doidModel));

        PrimaryTumorConfig invalid = TestCurationConfigFactory.primaryTumorConfigBuilder().addDoids("child", "invalid").build();
        assertFalse(CurationDatabaseValidator.validatePrimaryTumorConfigs(Lists.newArrayList(invalid), doidModel));
    }

    @Test
    public void shouldIdentifyInvalidSecondPrimaryConfigs() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDatabaseValidator.CANCER_PARENT_DOID, "child");

        SecondPrimaryConfig valid = TestCurationConfigFactory.secondPrimaryConfigBuilder()
                .curated(TestPriorSecondPrimaryFactory.builder().addDoids("child").build())
                .build();
        assertTrue(CurationDatabaseValidator.validateSecondPrimaryConfigs(Lists.newArrayList(valid), doidModel));

        SecondPrimaryConfig missing = TestCurationConfigFactory.secondPrimaryConfigBuilder().curated(null).build();
        assertTrue(CurationDatabaseValidator.validateSecondPrimaryConfigs(Lists.newArrayList(missing), doidModel));

        SecondPrimaryConfig invalid = TestCurationConfigFactory.secondPrimaryConfigBuilder()
                .curated(TestPriorSecondPrimaryFactory.builder().addDoids("child", "invalid").build())
                .build();
        assertFalse(CurationDatabaseValidator.validateSecondPrimaryConfigs(Lists.newArrayList(invalid), doidModel));
    }

    @Test
    public void shouldIdentifyInvalidNonOncologicalHistoryConfigs() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDatabaseValidator.GENERIC_PARENT_DOID, "child");

        NonOncologicalHistoryConfig valid = TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder()
                .priorOtherCondition(Optional.of(TestPriorOtherConditionFactory.builder().addDoids("child").build()))
                .build();
        assertTrue(CurationDatabaseValidator.validateNonOncologicalHistoryConfigs(Lists.newArrayList(valid), doidModel));

        NonOncologicalHistoryConfig missing =
                TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder().priorOtherCondition(Optional.empty()).build();
        assertTrue(CurationDatabaseValidator.validateNonOncologicalHistoryConfigs(Lists.newArrayList(missing), doidModel));

        NonOncologicalHistoryConfig invalid = TestCurationConfigFactory.nonOncologicalHistoryConfigBuilder()
                .priorOtherCondition(Optional.of(TestPriorOtherConditionFactory.builder().addDoids("child", "invalid").build()))
                .build();
        assertFalse(CurationDatabaseValidator.validateNonOncologicalHistoryConfigs(Lists.newArrayList(invalid), doidModel));
    }

    @Test
    public void shouldIdentifyInvalidIntoleranceConfigs() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDatabaseValidator.GENERIC_PARENT_DOID, "child");

        IntoleranceConfig valid = TestCurationConfigFactory.intoleranceConfigBuilder().addDoids("child").build();
        assertTrue(CurationDatabaseValidator.validateIntoleranceConfigs(Lists.newArrayList(valid), doidModel));

        IntoleranceConfig invalid = TestCurationConfigFactory.intoleranceConfigBuilder().addDoids("child", "invalid").build();
        assertFalse(CurationDatabaseValidator.validateIntoleranceConfigs(Lists.newArrayList(invalid), doidModel));
    }

    @Test
    public void emptyDoidsIsInvalid() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        assertFalse(CurationDatabaseValidator.hasValidDoids(Sets.newHashSet(), doidModel, "parent"));
    }
}