package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology

internal object MolecularTestFactory {
    private val base = TestDataFactory.createMinimalTestPatientRecord()

    fun priorMolecularTest(
        test: String = "",
        item: String = "",
        measure: String? = null,
        scoreText: String? = null,
        impliesIndeterminate: Boolean = false,
        scoreValue: Double? = null,
        scoreValuePrefix: String? = null
    ): PriorMolecularTest {
        return PriorMolecularTest(
            test = test,
            item = item,
            measure = measure,
            scoreText = scoreText,
            scoreValuePrefix = scoreValuePrefix,
            scoreValue = scoreValue,
            impliesPotentialIndeterminateStatus = impliesIndeterminate
        )
    }

    fun withPriorTests(priorTests: List<PriorMolecularTest>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(priorMolecularTests = priorTests))
    }

    fun withPriorTest(priorTest: PriorMolecularTest): PatientRecord {
        return withPriorTests(listOf(priorTest))
    }

    fun withVariant(variant: Variant): PatientRecord {
        return withDriver(variant)
    }

    fun withHasTumorMutationalLoadAndVariants(hasHighTumorMutationalLoad: Boolean?, vararg variants: Variant): PatientRecord {
        return withMolecularRecord(
            base.molecular.copy(
                characteristics = base.molecular.characteristics.copy(hasHighTumorMutationalLoad = hasHighTumorMutationalLoad),
                drivers = base.molecular.drivers.copy(variants = setOf(*variants))
            )
        )
    }

    fun withHasTumorMutationalLoadAndVariantAndDisruption(
        hasHighTumorMutationalLoad: Boolean?,
        variant: Variant,
        disruption: Disruption
    ): PatientRecord {
        return withMolecularRecord(
            base.molecular.copy(
                characteristics = base.molecular.characteristics.copy(hasHighTumorMutationalLoad = hasHighTumorMutationalLoad),
                drivers = base.molecular.drivers.copy(
                    variants = setOf(variant), disruptions = setOf(disruption)
                )
            )
        )
    }

    fun withCopyNumber(copyNumber: CopyNumber): PatientRecord {
        return withDriver(copyNumber)
    }

    fun withPloidyAndCopyNumber(ploidy: Double?, copyNumber: CopyNumber): PatientRecord {
        return withCharacteristicsAndDriver(base.molecular.characteristics.copy(ploidy = ploidy), copyNumber)
    }

    fun withHomozygousDisruption(homozygousDisruption: HomozygousDisruption): PatientRecord {
        return withDriver(homozygousDisruption)
    }

    fun withDisruption(disruption: Disruption): PatientRecord {
        return withDriver(disruption)
    }

    fun withFusion(fusion: Fusion): PatientRecord {
        return withDriver(fusion)
    }

    fun withExperimentTypeAndContainingTumorCells(type: ExperimentType, containsTumorCells: Boolean): PatientRecord {
        return withMolecularRecord(base.molecular.copy(type = type, containsTumorCells = containsTumorCells))
    }

    fun withHlaAllele(hlaAllele: HlaAllele): PatientRecord {
        return withMolecularImmunology(MolecularImmunology(isReliable = true, hlaAlleles = setOf(hlaAllele)))
    }

    fun withUnreliableMolecularImmunology(): PatientRecord {
        return withMolecularImmunology(MolecularImmunology(isReliable = false, hlaAlleles = emptySet()))
    }

    private fun withMolecularImmunology(immunology: MolecularImmunology): PatientRecord {
        return withMolecularRecord(base.molecular.copy(immunology = immunology))
    }

    fun withExperimentTypeAndPriorTest(type: ExperimentType, priorTest: PriorMolecularTest): PatientRecord {
        return base.copy(
            molecular = base.molecular.copy(type = type),
            clinical = base.clinical.copy(priorMolecularTests = listOf(priorTest))
        )
    }

    fun withMicrosatelliteInstabilityAndVariant(isMicrosatelliteUnstable: Boolean?, variant: Variant): PatientRecord {
        return withCharacteristicsAndDriver(
            base.molecular.characteristics.copy(isMicrosatelliteUnstable = isMicrosatelliteUnstable), variant
        )
    }

    fun withMicrosatelliteInstabilityAndLoss(isMicrosatelliteUnstable: Boolean?, loss: CopyNumber): PatientRecord {
        return withCharacteristicsAndDriver(
            base.molecular.characteristics.copy(isMicrosatelliteUnstable = isMicrosatelliteUnstable), loss
        )
    }

    fun withMicrosatelliteInstabilityAndHomozygousDisruption(
        isMicrosatelliteUnstable: Boolean?, homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            base.molecular.characteristics.copy(isMicrosatelliteUnstable = isMicrosatelliteUnstable), homozygousDisruption
        )
    }

    fun withMicrosatelliteInstabilityAndDisruption(
        isMicrosatelliteUnstable: Boolean?,
        disruption: Disruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            base.molecular.characteristics.copy(isMicrosatelliteUnstable = isMicrosatelliteUnstable), disruption
        )
    }

    fun withHomologousRepairDeficiencyAndVariant(
        isHomologousRepairDeficient: Boolean?,
        variant: Variant
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            base.molecular.characteristics.copy(isHomologousRepairDeficient = isHomologousRepairDeficient), variant
        )
    }

    fun withHomologousRepairDeficiencyAndLoss(
        isHomologousRepairDeficient: Boolean?,
        loss: CopyNumber
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            base.molecular.characteristics.copy(isHomologousRepairDeficient = isHomologousRepairDeficient), loss
        )
    }

    fun withHomologousRepairDeficiencyAndHomozygousDisruption(
        isHomologousRepairDeficient: Boolean?,
        homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            base.molecular.characteristics.copy(isHomologousRepairDeficient = isHomologousRepairDeficient), homozygousDisruption
        )
    }

    fun withHomologousRepairDeficiencyAndDisruption(
        isHomologousRepairDeficient: Boolean?,
        disruption: Disruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            base.molecular.characteristics.copy(isHomologousRepairDeficient = isHomologousRepairDeficient), disruption
        )
    }

    fun withTumorMutationalBurden(tumorMutationalBurden: Double?): PatientRecord {
        return withMolecularRecord(
            base.molecular.copy(
                characteristics = base.molecular.characteristics.copy(tumorMutationalBurden = tumorMutationalBurden)
            )
        )
    }

    fun withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
        tumorMutationalBurden: Double?,
        hasSufficientQualityAndPurity: Boolean,
        hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularRecord(
            base.molecular.copy(
                characteristics = base.molecular.characteristics.copy(tumorMutationalBurden = tumorMutationalBurden),
                hasSufficientQualityAndPurity = hasSufficientQualityAndPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    fun withTumorMutationalLoad(tumorMutationalLoad: Int?): PatientRecord {
        return withMolecularRecord(
            base.molecular.copy(
                characteristics = base.molecular.characteristics.copy(tumorMutationalLoad = tumorMutationalLoad)
            )
        )
    }

    fun withTumorMutationalLoadAndHasSufficientQualityAndPurity(
        tumorMutationalLoad: Int?, hasSufficientQualityAndPurity: Boolean, hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularRecord(
            base.molecular.copy(
                characteristics = base.molecular.characteristics.copy(tumorMutationalLoad = tumorMutationalLoad),
                hasSufficientQualityAndPurity = hasSufficientQualityAndPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    private fun withDriver(driver: Driver): PatientRecord {
        return withCharacteristicsAndDriver(base.molecular.characteristics, driver)
    }

    private fun withCharacteristicsAndDriver(characteristics: MolecularCharacteristics, driver: Driver?): PatientRecord {
        val drivers = when (driver) {
            is Variant -> base.molecular.drivers.copy(variants = setOf(driver))
            is CopyNumber -> base.molecular.drivers.copy(copyNumbers = setOf(driver))
            is HomozygousDisruption -> base.molecular.drivers.copy(homozygousDisruptions = setOf(driver))
            is Disruption -> base.molecular.drivers.copy(disruptions = setOf(driver))
            is Fusion -> base.molecular.drivers.copy(fusions = setOf(driver))
            else -> base.molecular.drivers
        }
        return withMolecularRecord(base.molecular.copy(characteristics = characteristics, drivers = drivers))
    }

    private fun withMolecularRecord(molecular: MolecularRecord): PatientRecord {
        return base.copy(molecular = molecular)
    }
}