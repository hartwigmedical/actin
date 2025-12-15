package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry

internal object MolecularTestFactory {

    private val basePatient = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val baseMolecular = TestMolecularFactory.createMinimalWholeGenomeTest()

    fun ihcTest(
        item: String = "",
        measure: String? = null,
        scoreText: String? = null,
        impliesIndeterminate: Boolean = false,
        scoreValue: Double? = null,
        scoreValuePrefix: String? = null
    ): IhcTest {
        return IhcTest(
            item = item,
            measure = measure,
            scoreText = scoreText,
            scoreValuePrefix = scoreValuePrefix,
            scoreValue = scoreValue,
            impliesPotentialIndeterminateStatus = impliesIndeterminate
        )
    }

    fun withIhcTests(ihcTests: List<IhcTest>): PatientRecord {
        return basePatient.copy(ihcTests = ihcTests.toList())
    }

    fun withOnlyIhcTests(ihcTests: List<IhcTest>): PatientRecord {
        return TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(ihcTests = ihcTests.toList())
    }

    fun withIhcTests(vararg ihcTests: IhcTest): PatientRecord {
        return withIhcTests(ihcTests.toList())
    }

    fun withIhcTestsMicrosatelliteStabilityAndVariant(
        ihcTests: List<IhcTest>,
        isUnstable: Boolean?,
        variant: Variant
    ): PatientRecord {
        val patient = withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(
                microsatelliteStability = createTestMicrosatelliteStability(isUnstable)
            ), variant
        )
        return patient.copy(ihcTests = ihcTests)
    }

    fun withMolecularTests(molecularTests: List<MolecularTest>): PatientRecord {
        return basePatient.copy(molecularTests = listOf(baseMolecular) + molecularTests)
    }

    fun withCopyNumberAndIhcTests(copyNumber: CopyNumber, ihcTests: List<IhcTest>): PatientRecord {
        val molecular = baseMolecular.copy(
            characteristics = baseMolecular.characteristics.copy(purity = 0.80, ploidy = 3.0),
            drivers = baseMolecular.drivers.copy(copyNumbers = listOf(copyNumber))
        )
        return basePatient.copy(ihcTests = ihcTests, molecularTests = listOf(molecular))
    }

    fun withMolecularTestsAndNoOrangeMolecular(molecularTests: List<MolecularTest>): PatientRecord {
        return basePatient.copy(molecularTests = molecularTests)
    }

    fun withVariant(variant: Variant): PatientRecord {
        return withDriver(variant)
    }

    fun withHasTumorMutationalLoadAndVariants(hasHighTumorMutationalLoad: Boolean?, vararg variants: Variant): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(
                    tumorMutationalLoad = createTestTumorMutationalLoad(isHigh = hasHighTumorMutationalLoad)
                ),
                drivers = baseMolecular.drivers.copy(variants = listOf(*variants))
            )
        )
    }

    fun withHasTumorMutationalLoadAndVariantAndDisruption(
        hasHighTumorMutationalLoad: Boolean?,
        variant: Variant,
        disruption: Disruption
    ): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(
                    tumorMutationalLoad = createTestTumorMutationalLoad(isHigh = hasHighTumorMutationalLoad)
                ),
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

    fun withExperimentTypeAndHasSufficientQuality(type: ExperimentType, hasSufficientQuality: Boolean): PatientRecord {
        return withMolecularTest(baseMolecular.copy(experimentType = type, hasSufficientQuality = hasSufficientQuality))
    }

    fun withExperimentTypeAndVirus(type: ExperimentType, virus: Virus): PatientRecord {
        return withMolecularTest(withDriver(virus).molecularTests.first().copy(experimentType = type))
    }

    fun withExperimentTypeAndCopyNumber(type: ExperimentType, copyNumber: CopyNumber): PatientRecord {
        return withMolecularTest(withDriver(copyNumber).molecularTests.first().copy(experimentType = type))
    }

    fun withHlaAllele(hlaAllele: HlaAllele): PatientRecord {
        return withMolecularImmunology(MolecularImmunology(isReliable = true, hlaAlleles = setOf(hlaAllele)))
    }

    fun withHlaAlleleAndInsufficientQuality(hlaAllele: HlaAllele): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                hasSufficientQuality = false,
                immunology = MolecularImmunology(isReliable = true, hlaAlleles = setOf(hlaAllele))
            )
        )
    }

    fun withHaplotype(pharmacoEntry: PharmacoEntry): PatientRecord {
        return withMolecularTest(baseMolecular.copy(pharmaco = setOf(pharmacoEntry)))
    }

    fun withUnreliableMolecularImmunology(): PatientRecord {
        return withMolecularImmunology(MolecularImmunology(isReliable = false, hlaAlleles = emptySet()))
    }

    private fun withMolecularImmunology(immunology: MolecularImmunology): PatientRecord {
        return withMolecularTest(baseMolecular.copy(immunology = immunology))
    }

    fun withExperimentTypeAndHasSufficientQualityAndPriorTest(
        type: ExperimentType,
        hasSufficientQuality: Boolean,
        priorTest: IhcTest
    ): PatientRecord {
        return basePatient.copy(
            molecularTests = listOf(baseMolecular.copy(experimentType = type, hasSufficientQuality = hasSufficientQuality)),
            ihcTests = listOf(priorTest)
        )
    }

    fun withMicrosatelliteStabilityAndVariant(isUnstable: Boolean?, variant: Variant): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(microsatelliteStability = createTestMicrosatelliteStability(isUnstable)),
            variant
        )
    }

    fun withMicrosatelliteStabilityAndDeletion(isUnstable: Boolean?, del: CopyNumber): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(microsatelliteStability = createTestMicrosatelliteStability(isUnstable)), del
        )
    }

    fun withMicrosatelliteStabilityAndHomozygousDisruption(
        isUnstable: Boolean?,
        homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(microsatelliteStability = createTestMicrosatelliteStability(isUnstable)),
            homozygousDisruption
        )
    }

    fun withMicrosatelliteStabilityAndDisruption(isUnstable: Boolean?, disruption: Disruption): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(microsatelliteStability = createTestMicrosatelliteStability(isUnstable)),
            disruption
        )
    }

    fun withHomologousRecombinationAndVariant(isHrDeficient: Boolean, variant: Variant): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(homologousRecombination = createTestHomologousRecombination(isHrDeficient)), variant
        )
    }

    fun withHomologousRecombinationAndDeletion(isHrDeficient: Boolean, del: CopyNumber): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(homologousRecombination = createTestHomologousRecombination(isHrDeficient)), del
        )
    }

    fun withHomologousRecombinationAndHomozygousDisruption(
        isHrDeficient: Boolean,
        homozygousDisruption: HomozygousDisruption
    ): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(homologousRecombination = createTestHomologousRecombination(isHrDeficient)),
            homozygousDisruption
        )
    }

    fun withHomologousRecombinationAndDisruption(isHrDeficient: Boolean, disruption: Disruption): PatientRecord {
        return withCharacteristicsAndDriver(
            baseMolecular.characteristics.copy(homologousRecombination = createTestHomologousRecombination(isHrDeficient)), disruption
        )
    }

    fun withHomologousRecombinationAndVariantAndDisruption(
        isHrDeficient: Boolean,
        disruption: Disruption,
        variant: Variant
    ): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(
                    homologousRecombination = createTestHomologousRecombination(isHrDeficient)
                ),
                drivers = baseMolecular.drivers.copy(variants = listOf(variant), disruptions = listOf(disruption))
            )
        )
    }

    fun withTumorMutationalBurden(tumorMutationalBurden: Double?): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(
                    tumorMutationalBurden = tumorMutationalBurden?.let { createTestTumorMutationalBurden(score = it) }
                )
            )
        )
    }

    fun withMicrosatelliteStability(isUnstable: Boolean?): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(
                    microsatelliteStability = createTestMicrosatelliteStability(isUnstable)
                )
            )
        )
    }

    fun withTumorMutationalBurdenAndHasSufficientQualityAndPurity(
        tumorMutationalBurden: Double?,
        hasSufficientPurity: Boolean,
        hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(
                    tumorMutationalBurden = tumorMutationalBurden?.let { createTestTumorMutationalBurden(score = it) }
                ),
                hasSufficientPurity = hasSufficientPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    fun withTumorMutationalLoad(tumorMutationalLoad: Int?): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(
                    tumorMutationalLoad = tumorMutationalLoad?.let { createTestTumorMutationalLoad(score = it) }
                )
            )
        )
    }

    fun withHasSufficientQualityAndPurity(
        hasSufficientPurity: Boolean, hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                hasSufficientPurity = hasSufficientPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    fun withTumorMutationalLoadAndHasSufficientQualityAndPurity(
        tumorMutationalLoad: Int?, hasSufficientPurity: Boolean, hasSufficientQuality: Boolean
    ): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                characteristics = baseMolecular.characteristics.copy(
                    tumorMutationalLoad = tumorMutationalLoad?.let { createTestTumorMutationalLoad(score = it) }
                ),
                hasSufficientPurity = hasSufficientPurity,
                hasSufficientQuality = hasSufficientQuality
            )
        )
    }

    fun withDrivers(vararg drivers: Driver): PatientRecord {
        return withMolecularTest(
            baseMolecular.copy(
                drivers = Drivers(
                    variants = drivers.filterIsInstance<Variant>(),
                    copyNumbers = drivers.filterIsInstance<CopyNumber>(),
                    homozygousDisruptions = drivers.filterIsInstance<HomozygousDisruption>(),
                    disruptions = drivers.filterIsInstance<Disruption>(),
                    fusions = drivers.filterIsInstance<Fusion>(),
                    viruses = drivers.filterIsInstance<Virus>(),
                )
            )
        )
    }

    private fun createTestMicrosatelliteStability(isUnstable: Boolean?): MicrosatelliteStability? {
        return isUnstable?.let {
            MicrosatelliteStability(
                microsatelliteIndelsPerMb = null,
                isUnstable = it,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        }
    }

    private fun createTestHomologousRecombination(isDeficient: Boolean): HomologousRecombination {
        return HomologousRecombination(
            isDeficient = isDeficient,
            score = if (isDeficient) 1.0 else 0.0,
            type = if (isDeficient) HomologousRecombinationType.BRCA1_TYPE else HomologousRecombinationType.NONE,
            brca1Value = if (isDeficient) 1.0 else 0.0,
            brca2Value = 0.0,
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }

    private fun createTestTumorMutationalBurden(score: Double = 0.0, isHigh: Boolean? = false): TumorMutationalBurden? {
        return isHigh?.let {
            TumorMutationalBurden(
                score = score,
                isHigh = it,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        }
    }

    private fun createTestTumorMutationalLoad(score: Int = 0, isHigh: Boolean? = false): TumorMutationalLoad? {
        return isHigh?.let {
            TumorMutationalLoad(
                score = score,
                isHigh = it,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        }
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
            is Virus -> baseMolecular.drivers.copy(viruses = listOf(driver))
            else -> baseMolecular.drivers
        }
        return withMolecularTest(baseMolecular.copy(characteristics = characteristics, drivers = drivers))
    }

    private fun withMolecularTest(test: MolecularTest?): PatientRecord {
        return basePatient.copy(molecularTests = test?.let { listOf(it) } ?: emptyList())
    }
}