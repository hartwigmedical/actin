package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import org.apache.logging.log4j.util.Strings

internal object MolecularTestFactory {
    fun priorBuilder(): ImmutablePriorMolecularTest.Builder {
        return ImmutablePriorMolecularTest.builder().test(Strings.EMPTY).item(Strings.EMPTY).impliesPotentialIndeterminateStatus(false)
    }

    fun withPriorTests(priorTests: List<PriorMolecularTest?>): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder()
            .from(base)
            .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).priorMolecularTests(priorTests).build())
            .build()
    }

    fun withPriorTest(priorTest: PriorMolecularTest): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder()
            .from(base)
            .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).addPriorMolecularTests(priorTest).build())
            .build()
    }

    fun withVariant(variant: Variant): PatientRecord {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addVariants(variant).build())
    }

    fun withHasTumorMutationalLoadAndVariants(
        hasHighTumorMutationalLoad: Boolean?,
        vararg variants: Variant
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .hasHighTumorMutationalLoad(hasHighTumorMutationalLoad)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addVariants(*variants).build())
                .build()
        )
    }

    fun withHasTumorMutationalLoadAndVariantAndDisruption(
        hasHighTumorMutationalLoad: Boolean?,
        variant: Variant,
        disruption: Disruption
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .hasHighTumorMutationalLoad(hasHighTumorMutationalLoad)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addVariants(variant).addDisruptions(disruption).build())
                .build()
        )
    }

    fun withCopyNumber(copyNumber: CopyNumber): PatientRecord {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addCopyNumbers(copyNumber).build())
    }

    fun withPloidyAndCopyNumber(ploidy: Double?, copyNumber: CopyNumber): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder().from(base.characteristics()).ploidy(ploidy).build())
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addCopyNumbers(copyNumber).build())
                .build()
        )
    }

    fun withHomozygousDisruption(homozygousDisruption: HomozygousDisruption): PatientRecord {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addHomozygousDisruptions(homozygousDisruption).build())
    }

    fun withDisruption(disruption: Disruption): PatientRecord {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addDisruptions(disruption).build())
    }

    fun withFusion(fusion: Fusion): PatientRecord {
        return withMolecularDrivers(ImmutableMolecularDrivers.builder().addFusions(fusion).build())
    }

    fun withExperimentTypeAndContainingTumorCells(type: ExperimentType, containsTumorCells: Boolean): PatientRecord {
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .type(type)
                .containsTumorCells(containsTumorCells)
                .build()
        )
    }

    fun withHlaAllele(hlaAllele: HlaAllele): PatientRecord {
        return withMolecularImmunology(ImmutableMolecularImmunology.builder().isReliable(true).addHlaAlleles(hlaAllele).build())
    }

    fun withUGT1A1(pharmacoEntry: PharmacoEntry): PatientRecord {
        return withMolecularRecord(
            ImmutableMolecularRecord.builder().from(TestMolecularFactory.createMinimalTestMolecularRecord()).addPharmaco(pharmacoEntry)
                .build()
        )
    }

    fun withUnreliableMolecularImmunology(): PatientRecord {
        return withMolecularImmunology(ImmutableMolecularImmunology.builder().isReliable(false).build())
    }

    private fun withMolecularImmunology(immunology: MolecularImmunology): PatientRecord {
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .immunology(immunology)
                .build()
        )
    }

    fun withExperimentTypeAndPriorTest(type: ExperimentType, priorTest: PriorMolecularTest): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder()
            .from(base)
            .molecular(ImmutableMolecularRecord.builder().from(base.molecular()).type(type).build())
            .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).addPriorMolecularTests(priorTest).build())
            .build()
    }

    fun withMicrosatelliteInstabilityAndVariant(
        isMicrosatelliteUnstable: Boolean?,
        variant: Variant
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addVariants(variant).build())
                .build()
        )
    }

    fun withMicrosatelliteInstabilityAndLoss(isMicrosatelliteUnstable: Boolean?, loss: CopyNumber): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addCopyNumbers(loss).build())
                .build()
        )
    }

    fun withMicrosatelliteInstabilityAndHomozygousDisruption(
        isMicrosatelliteUnstable: Boolean?,
        homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addHomozygousDisruptions(homozygousDisruption).build())
                .build()
        )
    }

    fun withMicrosatelliteInstabilityAndDisruption(
        isMicrosatelliteUnstable: Boolean?,
        disruption: Disruption
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addDisruptions(disruption).build())
                .build()
        )
    }

    fun withHomologousRepairDeficiencyAndVariant(
        isHomologousRepairDeficient: Boolean?,
        variant: Variant
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addVariants(variant).build())
                .build()
        )
    }

    fun withHomologousRepairDeficiencyAndLoss(
        isHomologousRepairDeficient: Boolean?,
        loss: CopyNumber
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addCopyNumbers(loss).build())
                .build()
        )
    }

    fun withHomologousRepairDeficiencyAndHomozygousDisruption(
        isHomologousRepairDeficient: Boolean?,
        homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addHomozygousDisruptions(homozygousDisruption).build())
                .build()
        )
    }

    fun withHomologousRepairDeficiencyAndDisruption(
        isHomologousRepairDeficient: Boolean?,
        disruption: Disruption
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build()
                )
                .drivers(ImmutableMolecularDrivers.builder().from(base.drivers()).addDisruptions(disruption).build())
                .build()
        )
    }

    fun withTumorMutationalBurden(tumorMutationalBurden: Double?): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalBurden(tumorMutationalBurden)
                        .build()
                )
                .build()
        )
    }

    fun withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
        tumorMutationalBurden: Double?,
        hasSufficientQualityAndPurity: Boolean,
        hasSufficientQuality: Boolean
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalBurden(tumorMutationalBurden)
                        .build()
                )
                .hasSufficientQualityAndPurity(hasSufficientQualityAndPurity)
                .hasSufficientQuality(hasSufficientQuality)
                .build()
        )
    }

    fun withTumorMutationalLoad(tumorMutationalLoad: Int?): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalLoad(tumorMutationalLoad)
                        .build()
                )
                .build()
        )
    }

    fun withTumorMutationalLoadAndHasSufficientQualityAndPurity(
        tumorMutationalLoad: Int?,
        hasSufficientQualityAndPurity: Boolean,
        hasSufficientQuality: Boolean
    ): PatientRecord {
        val base = TestMolecularFactory.createMinimalTestMolecularRecord()
        return withMolecularRecord(
            ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(
                    ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalLoad(tumorMutationalLoad)
                        .build()
                )
                .hasSufficientQualityAndPurity(hasSufficientQualityAndPurity)
                .hasSufficientQuality(hasSufficientQuality)
                .build()
        )
    }

    private fun withMolecularDrivers(drivers: MolecularDrivers): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .molecular(
                ImmutableMolecularRecord.builder()
                    .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                    .drivers(drivers)
                    .build()
            )
            .build()
    }

    private fun withMolecularRecord(molecular: MolecularRecord): PatientRecord {
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).molecular(molecular).build()
    }
}