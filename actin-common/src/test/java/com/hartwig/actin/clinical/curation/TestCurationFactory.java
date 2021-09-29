package com.hartwig.actin.clinical.curation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.config.CancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableCancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableNonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutablePrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableToxicityConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestCurationFactory {

    private TestCurationFactory() {
    }

    @NotNull
    public static CurationModel createProperTestCurationModel() {
        return new CurationModel(createTestCurationDatabase());
    }

    @NotNull
    public static CurationModel createMinimalTestCurationModel() {
        return new CurationModel(ImmutableCurationDatabase.builder().build());
    }

    @NotNull
    private static CurationDatabase createTestCurationDatabase() {
        return ImmutableCurationDatabase.builder()
                .primaryTumorConfigs(createTestPrimaryTumorConfigs())
                .oncologicalHistoryConfigs(createTestOncologicalHistoryConfigs())
                .nonOncologicalHistoryConfigs(createTestNonOncologicalHistoryConfigs())
                .ecgConfigs(createTestECGConfigs())
                .cancerRelatedComplicationConfigs(createTestCancerRelatedComplicationConfigs())
                .toxicityConfigs(createTestToxicityConfigs())
                .build();
    }

    @NotNull
    private static List<PrimaryTumorConfig> createTestPrimaryTumorConfigs() {
        List<PrimaryTumorConfig> configs = Lists.newArrayList();

        configs.add(ImmutablePrimaryTumorConfig.builder()
                .input("Unknown | Carcinoma")
                .primaryTumorLocation("Unknown")
                .primaryTumorSubLocation("CUP")
                .primaryTumorType("Carcinoma")
                .primaryTumorSubType(Strings.EMPTY)
                .primaryTumorExtraDetails(Strings.EMPTY)
                .addDoids("299")
                .build());

        return configs;
    }

    @NotNull
    private static List<OncologicalHistoryConfig> createTestOncologicalHistoryConfigs() {
        List<OncologicalHistoryConfig> configs = Lists.newArrayList();

        configs.add(ImmutableOncologicalHistoryConfig.builder()
                .input("Resection 2020")
                .ignore(false)
                .curatedObject(ImmutablePriorTumorTreatment.builder()
                        .name("Resection")
                        .year(2020)
                        .category("Surgery")
                        .isSystemic(false)
                        .surgeryType("Primary Resection")
                        .build())
                .build());

        configs.add(ImmutableOncologicalHistoryConfig.builder()
                .input("Breast cancer 2018")
                .ignore(false)
                .curatedObject(ImmutablePriorSecondPrimary.builder()
                        .tumorLocation("Breast")
                        .tumorSubLocation(Strings.EMPTY)
                        .tumorType("Carcinoma")
                        .tumorSubType(Strings.EMPTY)
                        .year(2018)
                        .isSecondPrimaryCured(false)
                        .build())
                .build());

        configs.add(ImmutableOncologicalHistoryConfig.builder().input("no systemic treatment").ignore(true).build());

        return configs;
    }

    @NotNull
    private static List<NonOncologicalHistoryConfig> createTestNonOncologicalHistoryConfigs() {
        List<NonOncologicalHistoryConfig> configs = Lists.newArrayList();

        configs.add(ImmutableNonOncologicalHistoryConfig.builder()
                .input("sickness")
                .ignore(false)
                .curated(ImmutablePriorOtherCondition.builder().name("sick").category("being sick").build())
                .build());

        configs.add(ImmutableNonOncologicalHistoryConfig.builder().input("not a condition").ignore(true).build());

        return configs;
    }

    @NotNull
    private static List<ECGConfig> createTestECGConfigs() {
        List<ECGConfig> configs = Lists.newArrayList();

        configs.add(ImmutableECGConfig.builder().input("Weird aberration").interpretation("Cleaned aberration").build());

        return configs;
    }

    @NotNull
    private static List<CancerRelatedComplicationConfig> createTestCancerRelatedComplicationConfigs() {
        List<CancerRelatedComplicationConfig> configs = Lists.newArrayList();

        configs.add(ImmutableCancerRelatedComplicationConfig.builder().input("term").name("curated").build());

        return configs;
    }

    @NotNull
    private static List<ToxicityConfig> createTestToxicityConfigs() {
        List<ToxicityConfig> configs = Lists.newArrayList();

        configs.add(ImmutableToxicityConfig.builder().ignore(false).input("neuropathy gr3").name("neuropathy").grade(3).build());

        return configs;
    }
}
