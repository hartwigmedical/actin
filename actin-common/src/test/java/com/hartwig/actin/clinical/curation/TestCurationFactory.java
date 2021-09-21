package com.hartwig.actin.clinical.curation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.config.ImmutableOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public final class TestCurationFactory {

    private TestCurationFactory() {
    }

    @NotNull
    public static CurationModel createTestCurationModel() {
        return new CurationModel(createTestCurationDatabase());
    }

    @NotNull
    private static CurationDatabase createTestCurationDatabase() {
        return ImmutableCurationDatabase.builder().oncologicalHistoryConfigs(createTestOncologicalHistoryConfigs()).build();
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

        configs.add(ImmutableOncologicalHistoryConfig.builder().input("no systemic treatment").ignore(true).build());

        return configs;
    }
}
