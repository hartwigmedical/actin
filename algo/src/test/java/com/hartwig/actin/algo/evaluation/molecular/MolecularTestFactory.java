package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MolecularTestFactory {

    private MolecularTestFactory() {
    }

    @NotNull
    public static ImmutablePriorMolecularTest.Builder priorBuilder() {
        return ImmutablePriorMolecularTest.builder().test(Strings.EMPTY).item(Strings.EMPTY).impliesPotentialIndeterminateStatus(false);
    }

    @NotNull
    public static PatientRecord withPriorTests(@NotNull List<PriorMolecularTest> priorTests) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();
        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).priorMolecularTests(priorTests).build())
                .build();
    }

    @NotNull
    public static PatientRecord withPriorTest(@NotNull PriorMolecularTest priorTest) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();
        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).addPriorMolecularTests(priorTest).build())
                .build();
    }

    @NotNull
    public static PatientRecord withHasTumorMutationalLoadAndVariant(@Nullable Boolean hasHighTumorMutationalLoad, @NotNull Variant variant) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .hasHighTumorMutationalLoad(hasHighTumorMutationalLoad)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addVariants(variant).build())
                .build());
    }

    @NotNull
    public static PatientRecord withVariant(@NotNull Variant variant) {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addVariants(variant).build());
    }

    @NotNull
    public static PatientRecord withVariants(@NotNull Variant... variants) {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addAllVariants(Lists.newArrayList(variants)).build());
    }

    @NotNull
    public static PatientRecord withVariantAndDisruption(@NotNull Variant variant, @NotNull Disruption disruption) {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addVariants(variant).addDisruptions(disruption).build());
    }

    @NotNull
    public static PatientRecord withAmplification(@NotNull Amplification amplification) {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addAmplifications(amplification).build());
    }

    @NotNull
    public static PatientRecord withPloidyAndAmplification(@Nullable Double ploidy, @NotNull Amplification amplification) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder().from(base.characteristics()).ploidy(ploidy).build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addAmplifications(amplification).build())
                .build());
    }

    @NotNull
    public static PatientRecord withLoss(@NotNull Loss loss) {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addLosses(loss).build());
    }

    @NotNull
    public static PatientRecord withHomozygousDisruption(@NotNull HomozygousDisruption homozygousDisruption) {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addHomozygousDisruptions(homozygousDisruption).build());
    }

    @NotNull
    public static PatientRecord withDisruption(@NotNull Disruption disruption) {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addDisruptions(disruption).build());
    }

    @NotNull
    public static PatientRecord withFusion(@NotNull Fusion fusion) {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addFusions(fusion).build());
    }

    @NotNull
    public static PatientRecord withExperimentTypeAndContainingTumorCells(@NotNull ExperimentType type, boolean containsTumorCells) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .type(type)
                .containsTumorCells(containsTumorCells)
                .build());
    }

    @NotNull
    public static PatientRecord withHlaAllele(@NotNull HlaAllele hlaAllele) {
        return withMolecularImmunology(ImmutableMolecularImmunology.builder().isReliable(true).addHlaAlleles(hlaAllele).build());
    }

    @NotNull
    public static PatientRecord withUnreliableMolecularImmunology() {
        return withMolecularImmunology(ImmutableMolecularImmunology.builder().isReliable(false).build());
    }

    @NotNull
    private static PatientRecord withMolecularImmunology(@NotNull MolecularImmunology immunology) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .immunology(immunology)
                .build());
    }

    @NotNull
    public static PatientRecord withExperimentTypeAndPriorTest(@NotNull ExperimentType type, @NotNull PriorMolecularTest priorTest) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();
        return ImmutablePatientRecord.builder()
                .from(base)
                .molecular(ImmutableMolecularRecord.builder().from(base.molecular()).type(type).build())
                .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).addPriorMolecularTests(priorTest).build())
                .build();
    }

    @NotNull
    public static PatientRecord withMicrosatelliteInstabilityAndVariant(@Nullable Boolean isMicrosatelliteUnstable,
            @NotNull Variant variant) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addVariants(variant).build())
                .build());
    }

    @NotNull
    public static PatientRecord withMicrosatelliteInstabilityAndLoss(@Nullable Boolean isMicrosatelliteUnstable, @NotNull Loss loss) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addLosses(loss).build())
                .build());
    }

    @NotNull
    public static PatientRecord withMicrosatelliteInstabilityAndHomozygousDisruption(@Nullable Boolean isMicrosatelliteUnstable,
            @NotNull HomozygousDisruption homozygousDisruption) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addHomozygousDisruptions(homozygousDisruption).build())
                .build());
    }

    @NotNull
    public static PatientRecord withMicrosatelliteInstabilityAndDisruption(@Nullable Boolean isMicrosatelliteUnstable,
            @NotNull Disruption disruption) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addDisruptions(disruption).build())
                .build());
    }

    @NotNull
    public static PatientRecord withHomologousRepairDeficiencyAndVariant(@Nullable Boolean isHomologousRepairDeficient,
            @NotNull Variant variant) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addVariants(variant).build())
                .build());
    }

    @NotNull
    public static PatientRecord withHomologousRepairDeficiencyAndLoss(@Nullable Boolean isHomologousRepairDeficient, @NotNull Loss loss) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addLosses(loss).build())
                .build());
    }

    @NotNull
    public static PatientRecord withHomologousRepairDeficiencyAndHomozygousDisruption(@Nullable Boolean isHomologousRepairDeficient,
            @NotNull HomozygousDisruption homozygousDisruption) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addHomozygousDisruptions(homozygousDisruption).build())
                .build());
    }

    @NotNull
    public static PatientRecord withHomologousRepairDeficiencyAndDisruption(@Nullable Boolean isHomologousRepairDeficient,
            @NotNull Disruption disruption) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addDisruptions(disruption).build())
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalBurden(@Nullable Double tumorMutationalBurden) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalBurden(tumorMutationalBurden)
                        .build())
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalBurdenAndHasSufficientQuality(@Nullable Double tumorMutationalBurden,
            @NotNull Boolean hasSufficientQuality) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalBurden(tumorMutationalBurden)
                        .build())
                .hasSufficientQuality(hasSufficientQuality)
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalLoad(@Nullable Integer tumorMutationalLoad) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalLoad(tumorMutationalLoad)
                        .build())
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalLoadAndHasSufficientQuality(@Nullable Integer tumorMutationalLoad,
            @NotNull Boolean hasSufficientQuality) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalLoad(tumorMutationalLoad)
                        .build())
                .hasSufficientQuality(hasSufficientQuality)
                .build());
    }

    @NotNull
    private static PatientRecord withMolecularDrivers(@NotNull MolecularDrivers drivers) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .molecular(ImmutableMolecularRecord.builder()
                        .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                        .drivers(drivers)
                        .build())
                .build();
    }

    @NotNull
    private static PatientRecord withMolecularRecord(@NotNull MolecularRecord molecular) {
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).molecular(molecular).build();
    }
}
