package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;

import org.jetbrains.annotations.NotNull;

public final class TestMolecularDataFactory {

    private static final LocalDate TODAY = LocalDate.now();

    private static final int DAYS_SINCE_MOLECULAR_ANALYSIS = 5;

    private TestMolecularDataFactory() {
    }

    @NotNull
    public static MolecularRecord createMinimalTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .type(ExperimentType.WGS)
                .hasReliableQuality(true)
                .tumorMutationalBurden(0D)
                .tumorMutationalLoad(0)
                .evidence(ImmutableMolecularEvidence.builder().build())
                .build();
    }

    @NotNull
    public static MolecularRecord createProperTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createMinimalTestMolecularRecord())
                .date(TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS))
                .doids(createTestDoids())
                .isMicrosatelliteUnstable(false)
                .isHomologousRepairDeficient(false)
                .tumorMutationalBurden(13.71)
                .tumorMutationalLoad(185)
                .build();
    }

    @NotNull
    private static Set<String> createTestDoids() {
        return Sets.newHashSet("8923");
    }
}
