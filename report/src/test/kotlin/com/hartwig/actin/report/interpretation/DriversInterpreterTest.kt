package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVirusFactory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory.interpretedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val EVENT_VARIANT = "variant"
private const val EVENT_CN = "CN"
private const val EVENT_HD = "HD"
private const val EVENT_DISRUPTION = "disruption"
private const val EVENT_FUSION = "fusion"
private const val EVENT_VIRUS = "virus"

class DriversInterpreterTest {

    @Test
    fun `Should include non-actionable reportable drivers`() {
        val test = createMolecularTestWithDriverEvidence(TestClinicalEvidenceFactory.createEmpty(), true)
        assertCountForMolecularTest(1, test)
    }

    @Test
    fun `Should skip non-actionable not-reportable drivers`() {
        val test = createMolecularTestWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.createEmpty())
        assertCountForMolecularTest(0, test)
    }

    @Test
    fun `Should include non-reportable drivers with ACTIN trial matches`() {
        val test = createMolecularTestWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.createEmpty())
        assertCountForMolecularTestAndCohorts(
            1,
            test,
            createCohortsForEvents(listOf(EVENT_VARIANT, EVENT_CN, EVENT_HD, EVENT_DISRUPTION, EVENT_FUSION, EVENT_VIRUS))
        )
    }

    @Test
    fun `Should include non-reportable drivers with approved treatment matches`() {
        val test =
            createMolecularTestWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.withApprovedTreatment("treatment"))
        assertCountForMolecularTest(1, test)
    }

    @Test
    fun `Should include non-reportable drivers with external trial matches`() {
        val test = createMolecularTestWithNonReportableDriverWithEvidence(
            TestClinicalEvidenceFactory.withEligibleTrial(TestExternalTrialFactory.createTestTrial())
        )
        assertCountForMolecularTest(1, test)
    }

    private fun assertCountForMolecularTest(expectedCount: Int, molecularTest: MolecularTest) {
        assertCountForMolecularTestAndCohorts(expectedCount, molecularTest, emptyList())
    }

    private fun assertCountForMolecularTestAndCohorts(expectedCount: Int, molecularTest: MolecularTest, cohorts: List<InterpretedCohort>) {
        val interpreter = MolecularDriversInterpreter(molecularTest.drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        assertThat(interpreter.filteredVariants()).hasSize(expectedCount)
        assertThat(interpreter.filteredCopyNumbers()).hasSize(expectedCount)
        assertThat(interpreter.filteredHomozygousDisruptions()).hasSize(expectedCount)
        assertThat(interpreter.filteredDisruptions()).hasSize(expectedCount)
        assertThat(interpreter.filteredFusions()).hasSize(expectedCount)
        assertThat(interpreter.filteredViruses()).hasSize(expectedCount)
    }

    private fun createMolecularTestWithNonReportableDriverWithEvidence(evidence: ClinicalEvidence): MolecularTest {
        return createMolecularTestWithDriverEvidence(evidence, false)
    }

    private fun createMolecularTestWithDriverEvidence(evidence: ClinicalEvidence, isReportable: Boolean): MolecularTest {
        return TestMolecularFactory.createMinimalWholeGenomeTest().copy(drivers = createDriversWithEvidence(evidence, isReportable))
    }

    private fun createDriversWithEvidence(evidence: ClinicalEvidence, isReportable: Boolean): Drivers {
        return Drivers(
            variants = listOf(
                TestVariantFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_VARIANT)
            ),
            copyNumbers = listOf(
                TestCopyNumberFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_CN)
            ),
            homozygousDisruptions = listOf(
                TestHomozygousDisruptionFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_HD)
            ),
            disruptions = listOf(
                TestDisruptionFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_DISRUPTION)
            ),
            fusions = listOf(
                TestFusionFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_FUSION)
            ),
            viruses = listOf(
                TestVirusFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_VIRUS)
            )
        )
    }

    private fun createCohortsForEvents(events: List<String>): List<InterpretedCohort> {
        return listOf(
            interpretedCohort(acronym = "trial 1", molecularInclusionEvents = events, isPotentiallyEligible = true, isOpen = true),
            interpretedCohort(acronym = "trial 2", molecularInclusionEvents = events, isPotentiallyEligible = true, isOpen = false)
        )
    }
}