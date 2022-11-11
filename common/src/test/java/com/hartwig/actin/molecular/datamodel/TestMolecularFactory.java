package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestAmplificationFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableHlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestMolecularFactory {

    private static final LocalDate TODAY = LocalDate.now();

    private static final int DAYS_SINCE_MOLECULAR_ANALYSIS = 5;

    private TestMolecularFactory() {
    }

    @NotNull
    public static MolecularRecord createMinimalTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .patientId(TestDataFactory.TEST_PATIENT)
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .type(ExperimentType.WGS)
                .evidenceSource(Strings.EMPTY)
                .externalTrialSource(Strings.EMPTY)
                .containsTumorCells(true)
                .hasSufficientQuality(true)
                .characteristics(ImmutableMolecularCharacteristics.builder().build())
                .drivers(ImmutableMolecularDrivers.builder().build())
                .immunology(ImmutableMolecularImmunology.builder().isReliable(false).build())
                .build();
    }

    @NotNull
    public static MolecularRecord createProperTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createMinimalTestMolecularRecord())
                .date(TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS))
                .characteristics(createProperTestCharacteristics())
                .drivers(createProperTestDrivers())
                .immunology(createProperTestImmunology())
                .pharmaco(createProperTestPharmaco())
                .build();
    }

    @NotNull
    public static MolecularRecord createExhaustiveTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createProperTestMolecularRecord())
                .drivers(createExhaustiveTestDrivers())
                .build();
    }

    @NotNull
    private static MolecularCharacteristics createProperTestCharacteristics() {
        return ImmutableMolecularCharacteristics.builder()
                .purity(0.98)
                .predictedTumorOrigin(ImmutablePredictedTumorOrigin.builder().tumorType("Melanoma").likelihood(0.996).build())
                .isMicrosatelliteUnstable(false)
                .isHomologousRepairDeficient(false)
                .tumorMutationalBurden(13.71)
                .tumorMutationalLoad(185)
                .build();
    }

    @NotNull
    private static MolecularDrivers createProperTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .addVariants(TestVariantFactory.builder()
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene("BRAF")
                        .variantCopyNumber(4.1)
                        .totalCopyNumber(6.0)
                        .clonalLikelihood(1.0)
                        .build())
                .addLosses(TestLossFactory.builder()
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene("PTEN")
                        .build())
                .build();
    }

    @NotNull
    private static MolecularImmunology createProperTestImmunology() {
        return ImmutableMolecularImmunology.builder()
                .isReliable(true)
                .addHlaAlleles(ImmutableHlaAllele.builder().name("A*02:01").tumorCopyNumber(1.2).hasSomaticMutations(false).build())
                .build();
    }

    @NotNull
    private static Set<PharmacoEntry> createProperTestPharmaco() {
        return Sets.newHashSet(ImmutablePharmacoEntry.builder()
                .gene("DPYD")
                .addHaplotypes(ImmutableHaplotype.builder().name("1* HOM").function("Normal function").build())
                .build());
    }

    @NotNull
    private static MolecularDrivers createExhaustiveTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .from(createProperTestDrivers())
                .addAmplifications(TestAmplificationFactory.builder()
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene("MYC")
                        .minCopies(38)
                        .maxCopies(38)
                        .build())
                .addHomozygousDisruptions(TestHomozygousDisruptionFactory.builder()
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene("PTEN")
                        .build())
                .addDisruptions(TestDisruptionFactory.builder()
                        .driverLikelihood(DriverLikelihood.LOW)
                        .gene("PTEN")
                        .type("DEL")
                        .junctionCopyNumber(1.1)
                        .undisruptedCopyNumber(1.8)
                        .range("Intron 1 downstream")
                        .build())
                .addFusions(TestFusionFactory.builder()
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .geneStart("EML4")
                        .fusedExonUp(2)
                        .geneEnd("ALK")
                        .fusedExonDown(4)
                        .driverType(FusionDriverType.KNOWN_PAIR)
                        .build())
                .addViruses(TestVirusFactory.builder()
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .name("Human papillomavirus type 16d")
                        .integrations(3)
                        .build())
                .build();
    }
}
