package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry

internal object MolecularTestFactory {

    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val baseMolecular = TestMolecularFactory.createMinimalTestMolecularRecord()

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

    fun archerPriorMolecularVariantRecord(gene: String?, hgvs: String?): PriorMolecularTest {
        return PriorMolecularTest(
            test = "Archer FP Lung Target",
            item = gene,
            measure = hgvs,
            impliesPotentialIndeterminateStatus = false
        )
    }

    fun archerPriorMolecularNoFusionsFoundRecord(): PriorMolecularTest {
        return archerPriorMolecularVariantRecord(null, "GEEN fusie(s) aangetoond")
    }

    fun avlPanelPriorMolecularNoMutationsFoundRecord(): PriorMolecularTest {
        return PriorMolecularTest(
            test = "AvL Panel",
            item = null,
            measure = "GEEN mutaties aangetoond met behulp van het AVL Panel",
            impliesPotentialIndeterminateStatus = false
        )
    }

    fun withPriorTests(priorTests: List<PriorMolecularTest>): PatientRecord {
        return base.copy(molecularHistory = MolecularHistory.fromInputs(listOf(baseMolecular), priorTests))
    }

    fun withPriorTestsAndNoOrangeMolecular(priorTests: List<PriorMolecularTest>): PatientRecord {
        return base.copy(molecularHistory = MolecularHistory.fromInputs(emptyList(), priorTests))
    }

    fun withPriorTest(priorTest: PriorMolecularTest): PatientRecord {
        return withPriorTests(listOf(priorTest))
    }

    fun withVariant(variant: Variant): PatientRecord {
        return withDriver(variant)
    }

    fun withHasTumorMutationalLoadAndVariants(hasHighTumorMutationalLoad: Boolean?, vararg variants: Variant): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(hasHighTumorMutationalLoad = hasHighTumorMutationalLoad),
                drivers = baseMolecular.drivers.copy(variants = setOf(*variants))
            )
        )
    }

    fun withHasTumorMutationalLoadAndVariantAndDisruption(
        hasHighTumorMutationalLoad: Boolean?,
        variant: Variant,
        disruption: Disruption
    ): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(hasHighTumorMutationalLoad = hasHighTumorMutationalLoad),
                drivers = baseMolecular.drivers.copy(
                    variants = setOf(variant), disruptions = setOf(disruption)
                )
            )
        )
    }

    fun withCopyNumber(copyNumber: CopyNumber): PatientRecord {
        return withDriver(copyNumber)
    }

    fun withPloidyAndCopyNumber(ploidy: Double?, copyNumber: CopyNumber): PatientRecord {
        return withCharacteristicsAndDriver(baseMolecular.characteristics.copy(ploidy = ploidy), copyNumber)
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
        return withMolecularRecord(baseMolecular.copy(type = type, containsTumorCells = containsTumorCells))
    }

    fun withExperimentTypeAndCopyNumber(type: ExperimentType, copyNumber: CopyNumber): PatientRecord {
        return withMolecularRecord(withDriver(copyNumber).molecularHistory.latestMolecularRecord()?.copy(type = type))
    }

    fun withHlaAllele(hlaAllele: HlaAllele): PatientRecord {
        return withMolecularImmunology(MolecularImmunology(isReliable = true, hlaAlleles = setOf(hlaAllele)))
    }

    fun withHaplotype(pharmacoEntry: PharmacoEntry): PatientRecord {
        return withMolecularRecord(baseMolecular.copy(pharmaco = setOf(pharmacoEntry)))
    }

    fun withUnreliableMolecularImmunology(): PatientRecord {
        return withMolecularImmunology(MolecularImmunology(isReliable = false, hlaAlleles = emptySet()))
    }

    private fun withMolecularImmunology(immunology: MolecularImmunology): PatientRecord {
        return withMolecularRecord(baseMolecular.copy(immunology = immunology))
    }

    fun withExperimentTypeAndContainingTumorCellsAndPriorTest(
        type: ExperimentType,
        containsTumorCells: Boolean,
        priorTest: PriorMolecularTest
    ): PatientRecord {
        return base.copy(
            molecularHistory = MolecularHistory.fromInputs(
                listOf(baseMolecular.copy(type = type, containsTumorCells = containsTumorCells)),
                listOf(priorTest)
            )
        )
    }

    fun withMicrosatelliteInstabilityAndVariant(isMicrosatelliteUnstable: Boolean?, variant: Variant): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isMicrosatelliteUnstable = isMicrosatelliteUnstable), variant
        )
    }

    fun withMicrosatelliteInstabilityAndLoss(isMicrosatelliteUnstable: Boolean?, loss: CopyNumber): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isMicrosatelliteUnstable = isMicrosatelliteUnstable), loss
        )
    }

    fun withMicrosatelliteInstabilityAndHomozygousDisruption(
        isMicrosatelliteUnstable: Boolean?, homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isMicrosatelliteUnstable = isMicrosatelliteUnstable), homozygousDisruption
        )
    }

    fun withMicrosatelliteInstabilityAndDisruption(
        isMicrosatelliteUnstable: Boolean?,
        disruption: Disruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isMicrosatelliteUnstable = isMicrosatelliteUnstable), disruption
        )
    }

    fun withHomologousRepairDeficiencyAndVariant(
        isHomologousRepairDeficient: Boolean?,
        variant: Variant
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isHomologousRepairDeficient = isHomologousRepairDeficient), variant
        )
    }

    fun withHomologousRepairDeficiencyAndLoss(
        isHomologousRepairDeficient: Boolean?,
        loss: CopyNumber
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isHomologousRepairDeficient = isHomologousRepairDeficient), loss
        )
    }

    fun withHomologousRepairDeficiencyAndHomozygousDisruption(
        isHomologousRepairDeficient: Boolean?,
        homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isHomologousRepairDeficient = isHomologousRepairDeficient), homozygousDisruption
        )
    }

    fun withHomologousRepairDeficiencyAndDisruption(
        isHomologousRepairDeficient: Boolean?,
        disruption: Disruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isHomologousRepairDeficient = isHomologousRepairDeficient), disruption
        )
    }

    fun withTumorMutationalBurden(tumorMutationalBurden: Double?): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(tumorMutationalBurden = tumorMutationalBurden)
            )
        )
    }

    fun withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
        tumorMutationalBurden: Double?,
        hasSufficientQualityAndPurity: Boolean,
        hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(tumorMutationalBurden = tumorMutationalBurden),
                hasSufficientQualityAndPurity = hasSufficientQualityAndPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    fun withTumorMutationalLoad(tumorMutationalLoad: Int?): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(tumorMutationalLoad = tumorMutationalLoad)
            )
        )
    }

    fun withTumorMutationalLoadAndHasSufficientQualityAndPurity(
        tumorMutationalLoad: Int?, hasSufficientQualityAndPurity: Boolean, hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(tumorMutationalLoad = tumorMutationalLoad),
                hasSufficientQualityAndPurity = hasSufficientQualityAndPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    private fun withDriver(driver: Driver): PatientRecord {
        return withCharacteristicsAndDriver(baseMolecular.characteristics, driver)
    }

    private fun withCharacteristicsAndDriver(characteristics: MolecularCharacteristics, driver: Driver?): PatientRecord {
        val drivers = when (driver) {
            is Variant -> baseMolecular.drivers.copy(variants = setOf(driver))
            is CopyNumber -> baseMolecular.drivers.copy(copyNumbers = setOf(driver))
            is HomozygousDisruption -> baseMolecular.drivers.copy(homozygousDisruptions = setOf(driver))
            is Disruption -> baseMolecular.drivers.copy(disruptions = setOf(driver))
            is Fusion -> baseMolecular.drivers.copy(fusions = setOf(driver))
            else -> baseMolecular.drivers
        }
        return withMolecularRecord(baseMolecular.copy(characteristics = characteristics, drivers = drivers))
    }

    private fun withMolecularRecord(molecular: MolecularRecord?): PatientRecord {
        return base.copy(molecularHistory = MolecularHistory.fromInputs(listOfNotNull(molecular), emptyList()))
    }
}