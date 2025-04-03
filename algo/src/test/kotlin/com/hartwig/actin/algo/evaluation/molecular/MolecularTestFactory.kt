package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry

internal object MolecularTestFactory {

    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val baseMolecular = TestMolecularFactory.createMinimalTestMolecularRecord()

    fun priorIHCTest(
        test: String = "",
        item: String = "",
        measure: String? = null,
        scoreText: String? = null,
        impliesIndeterminate: Boolean = false,
        scoreValue: Double? = null,
        scoreValuePrefix: String? = null
    ): PriorIHCTest {
        return PriorIHCTest(
            test = test,
            item = item,
            measure = measure,
            scoreText = scoreText,
            scoreValuePrefix = scoreValuePrefix,
            scoreValue = scoreValue,
            impliesPotentialIndeterminateStatus = impliesIndeterminate
        )
    }

    fun withIHCTests(ihcTests: List<PriorIHCTest>): PatientRecord {
        return base.copy(priorIHCTests = ihcTests.toList())
    }

    fun withIHCTests(vararg ihcTests: PriorIHCTest): PatientRecord {
        return withIHCTests(ihcTests.toList())
    }

    fun withMolecularTests(molecularTests: List<MolecularTest>): PatientRecord {
        return base.copy(molecularHistory = MolecularHistory(listOf(baseMolecular) + molecularTests))
    }

    fun withCopyNumberAndPriorIHCTests(copyNumber: CopyNumber, priorIHCTests: List<PriorIHCTest>): PatientRecord {
        val molecular = baseMolecular.copy(
            characteristics = MolecularCharacteristics(0.80, 3.0),
            drivers = baseMolecular.drivers.copy(copyNumbers = listOf(copyNumber))
        )
        return base.copy(priorIHCTests = priorIHCTests, molecularHistory = MolecularHistory(listOf(molecular)))
    }

    fun withMolecularTestsAndNoOrangeMolecular(molecularTests: List<MolecularTest>): PatientRecord {
        return base.copy(molecularHistory = MolecularHistory(molecularTests))
    }

    fun withVariant(variant: Variant): PatientRecord {
        return withDriver(variant)
    }

    fun withHasTumorMutationalLoadAndVariants(hasHighTumorMutationalLoad: Boolean?, vararg variants: Variant): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(hasHighTumorMutationalLoad = hasHighTumorMutationalLoad),
                drivers = baseMolecular.drivers.copy(variants = listOf(*variants))
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
                    variants = listOf(variant), disruptions = listOf(disruption)
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
        return withMolecularRecord(baseMolecular.copy(experimentType = type, containsTumorCells = containsTumorCells))
    }

    fun withExperimentTypeAndCopyNumber(type: ExperimentType, copyNumber: CopyNumber): PatientRecord {
        return withMolecularRecord(withDriver(copyNumber).molecularHistory.latestOrangeMolecularRecord()?.copy(experimentType = type))
    }

    fun withHlaAllele(hlaAllele: HlaAllele): PatientRecord {
        return withMolecularImmunology(MolecularImmunology(isReliable = true, hlaAlleles = setOf(hlaAllele)))
    }

    fun withHlaAlleleAndInsufficientQuality(hlaAllele: HlaAllele): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                immunology = MolecularImmunology(isReliable = true, hlaAlleles = setOf(hlaAllele)),
                hasSufficientQuality = false
            )
        )
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
        priorTest: PriorIHCTest
    ): PatientRecord {
        return base.copy(
            molecularHistory = MolecularHistory(
                listOf(baseMolecular.copy(experimentType = type, containsTumorCells = containsTumorCells))
            ),
            priorIHCTests = listOf(priorTest)
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

    fun withHomologousRecombinationDeficiencyAndVariant(
        isHomologousRecombinationDeficient: Boolean?,
        variant: Variant
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isHomologousRecombinationDeficient = isHomologousRecombinationDeficient), variant
        )
    }

    fun withHomologousRecombinationDeficiencyAndLoss(
        isHomologousRecombinationDeficient: Boolean?,
        loss: CopyNumber
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isHomologousRecombinationDeficient = isHomologousRecombinationDeficient), loss
        )
    }

    fun withHomologousRecombinationDeficiencyAndHomozygousDisruption(
        isHomologousRecombinationDeficient: Boolean?,
        homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isHomologousRecombinationDeficient = isHomologousRecombinationDeficient), homozygousDisruption
        )
    }

    fun withHomologousRecombinationDeficiencyAndDisruption(
        isHomologousRecombinationDeficient: Boolean?,
        disruption: Disruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(isHomologousRecombinationDeficient = isHomologousRecombinationDeficient), disruption
        )
    }

    fun withHomologousRecombinationDeficiencyAndVariantAndDisruption(
        isHomologousRecombinationDeficient: Boolean?,
        disruption: Disruption,
        variant: Variant
    ): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(isHomologousRecombinationDeficient = isHomologousRecombinationDeficient),
                drivers = baseMolecular.drivers.copy(variants = listOf(variant), disruptions = listOf(disruption))
            )
        )
    }

    fun withTumorMutationalBurden(tumorMutationalBurden: Double?): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(tumorMutationalBurden = tumorMutationalBurden)
            )
        )
    }

    fun withIsMicrosatelliteUnstable(microsatelliteStatus: Boolean?): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(isMicrosatelliteUnstable = microsatelliteStatus)
            )
        )
    }

    fun withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
        tumorMutationalBurden: Double?,
        hasSufficientPurity: Boolean,
        hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(tumorMutationalBurden = tumorMutationalBurden),
                hasSufficientPurity = hasSufficientPurity,
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

    fun withHasSufficientQualityAndPurity(
        hasSufficientPurity: Boolean, hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                hasSufficientPurity = hasSufficientPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    fun withTumorMutationalLoadAndHasSufficientQualityAndPurity(
        tumorMutationalLoad: Int?, hasSufficientPurity: Boolean, hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(tumorMutationalLoad = tumorMutationalLoad),
                hasSufficientPurity = hasSufficientPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    fun withDrivers(vararg drivers: Driver): PatientRecord {
        return withMolecularRecord(
            baseMolecular.copy(
                drivers = Drivers(
                    variants = drivers.filterIsInstance<Variant>(),
                    copyNumbers = drivers.filterIsInstance<CopyNumber>(),
                    homozygousDisruptions = drivers.filterIsInstance<HomozygousDisruption>(),
                    disruptions = drivers.filterIsInstance<Disruption>(),
                    fusions = drivers.filterIsInstance<Fusion>()
                )
            )
        )
    }

    private fun withDriver(driver: Driver): PatientRecord {
        return withCharacteristicsAndDriver(baseMolecular.characteristics, driver)
    }

    private fun withCharacteristicsAndDriver(characteristics: MolecularCharacteristics, driver: Driver?): PatientRecord {
        val drivers = when (driver) {
            is Variant -> baseMolecular.drivers.copy(variants = listOf(driver))
            is CopyNumber -> baseMolecular.drivers.copy(copyNumbers = listOf(driver))
            is HomozygousDisruption -> baseMolecular.drivers.copy(homozygousDisruptions = listOf(driver))
            is Disruption -> baseMolecular.drivers.copy(disruptions = listOf(driver))
            is Fusion -> baseMolecular.drivers.copy(fusions = listOf(driver))
            else -> baseMolecular.drivers
        }
        return withMolecularRecord(baseMolecular.copy(characteristics = characteristics, drivers = drivers))
    }

    private fun withMolecularRecord(molecular: MolecularRecord?): PatientRecord {
        return base.copy(molecularHistory = MolecularHistory(molecular?.let { listOf(it) } ?: emptyList()))
    }
}