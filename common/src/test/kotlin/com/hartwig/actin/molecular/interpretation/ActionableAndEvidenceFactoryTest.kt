package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val REPORTABLE_VARIANT = TestVariantFactory.createMinimal().copy(
    event = "variant1", evidence = TestClinicalEvidenceFactory.createExhaustive(), isReportable = true
)
val UNREPORTED_VARIANT = TestVariantFactory.createMinimal().copy(
    event = "variant2", evidence = TestClinicalEvidenceFactory.createExhaustive(), isReportable = false
)

class ActionableAndEvidenceFactoryTest {

    @Test
    fun `Should return result if sufficient quality`() {
        val test = createMinimalMolecularTest(true)
        assertThat(
            ActionableAndEvidenceFactory.createTreatmentEvidences(test)
        ).hasSize(1)
    }

    @Test
    fun `Should return empty result if not sufficient quality`() {
        val test = createMinimalMolecularTest(false)
        assertThat(
            ActionableAndEvidenceFactory.createTreatmentEvidences(test)
        ).isEmpty()
    }

    @Test
    fun `Should include all actionables`() {
        var test = TestMolecularFactory.createExhaustiveWholeGenomeTest()
        test = test.copy(
            characteristics = test.characteristics.copy(
                homologousRecombination = test.characteristics.homologousRecombination?.copy(isDeficient = true),
            )
        )

        val evidences = ActionableAndEvidenceFactory.createTreatmentEvidences(test)

        val countsByType: Map<Class<*>, Long> = evidences.map { it.first }
            .groupingBy { it::class.java }
            .eachCount()
            .mapValues { it.value.toLong() }

        assertThat(countsByType)
            .containsEntry(Variant::class.java, 2L)
            .containsEntry(CopyNumber::class.java, 3L)
            .containsEntry(HomozygousDisruption::class.java, 1L)
            .containsEntry(Disruption::class.java, 1L)
            .containsEntry(Fusion::class.java, 1L)
            .containsEntry(Virus::class.java, 2L)
            .containsEntry(MicrosatelliteStability::class.java, 1L)
            .containsEntry(HomologousRecombination::class.java, 1L)
            .containsEntry(TumorMutationalBurden::class.java, 1L)
            .containsEntry(TumorMutationalLoad::class.java, 1L).hasSize(10)
    }

    @Test
    fun `Should apply filter`() {
        val test = createMolecularTest()
        assertThat(
            ActionableAndEvidenceFactory.createTreatmentEvidences(test, { a ->
                if (a is Driver) {
                    a.isReportable
                } else {
                    true
                }
            })
        ).containsOnly(Pair(REPORTABLE_VARIANT, REPORTABLE_VARIANT.evidence.treatmentEvidence))
    }

    @Test
    fun `Should filter null event name`() {
        var test = createMolecularTest()
        test = test.copy(
            characteristics = test.characteristics.copy(
                homologousRecombination = HomologousRecombination(
                    isDeficient = false,
                    score = 1.0,
                    type = HomologousRecombinationType.BRCA1_TYPE,
                    brca1Value = 1.0,
                    brca2Value = 0.0,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        )
        assertThat(
            ActionableAndEvidenceFactory.createTreatmentEvidences(test)
        ).containsOnly(
            Pair(REPORTABLE_VARIANT, REPORTABLE_VARIANT.evidence.treatmentEvidence),
            Pair(UNREPORTED_VARIANT, UNREPORTED_VARIANT.evidence.treatmentEvidence)
        )
    }

    private fun createMolecularTest(): MolecularTest {
        val test = TestMolecularFactory.createMinimalWholeGenomeTest()
        return test.copy(
            drivers = test.drivers.copy(
                variants = listOf(
                    REPORTABLE_VARIANT,
                    UNREPORTED_VARIANT
                ),
            )
        )
    }

    private fun createMinimalMolecularTest(hasSufficientQuality: Boolean): MolecularTest {
        val test = TestMolecularFactory.createMinimalWholeGenomeTest().copy(hasSufficientQuality = hasSufficientQuality)
        return test.copy(
            characteristics = test.characteristics.copy(
                microsatelliteStability = MicrosatelliteStability(
                    microsatelliteIndelsPerMb = 10.2,
                    isUnstable = true,
                    evidence = TestClinicalEvidenceFactory.createExhaustive()
                ),
            )
        )
    }
}