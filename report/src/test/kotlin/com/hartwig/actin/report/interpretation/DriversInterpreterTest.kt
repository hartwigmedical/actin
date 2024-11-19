package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVirusFactory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
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
        val record = createTestMolecularRecordWithDriverEvidence(TestClinicalEvidenceFactory.createEmptyClinicalEvidence(), true)
        assertCountForRecord(1, record)
    }

    @Test
    fun `Should skip non-actionable not-reportable drivers`() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.createEmptyClinicalEvidence())
        assertCountForRecord(0, record)
    }

    @Test
    fun `Should include non-reportable drivers with ACTIN trial matches`() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.createEmptyClinicalEvidence())
        assertCountForRecordAndCohorts(
            1,
            record,
            createCohortsForEvents(listOf(EVENT_VARIANT, EVENT_CN, EVENT_HD, EVENT_DISRUPTION, EVENT_FUSION, EVENT_VIRUS))
        )
    }

    @Test
    fun `Should include non-reportable drivers with approved treatment matches`() {
        val record =
            createTestMolecularRecordWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.withApprovedTreatment("treatment"))
        assertCountForRecord(1, record)
    }

    @Test
    fun `Should include non-reportable drivers with external trial matches`() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(
            TestClinicalEvidenceFactory.withExternalEligibleTrial(
                TestClinicalEvidenceFactory.createTestExternalTrial()
            )
        )
        assertCountForRecord(1, record)
    }

    private fun assertCountForRecord(expectedCount: Int, molecularRecord: MolecularRecord) {
        assertCountForRecordAndCohorts(expectedCount, molecularRecord, emptyList())
    }

    private fun assertCountForRecordAndCohorts(expectedCount: Int, molecularRecord: MolecularRecord, cohorts: List<InterpretedCohort>) {
        val interpreter =
            MolecularDriversInterpreter(molecularRecord.drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        assertThat(interpreter.filteredVariants()).hasSize(expectedCount)
        assertThat(interpreter.filteredCopyNumbers()).hasSize(expectedCount)
        assertThat(interpreter.filteredHomozygousDisruptions()).hasSize(expectedCount)
        assertThat(interpreter.filteredDisruptions()).hasSize(expectedCount)
        assertThat(interpreter.filteredFusions()).hasSize(expectedCount)
        assertThat(interpreter.filteredViruses()).hasSize(expectedCount)
    }

    private fun createTestMolecularRecordWithNonReportableDriverWithEvidence(evidence: ClinicalEvidence): MolecularRecord {
        return createTestMolecularRecordWithDriverEvidence(evidence, false)
    }

    private fun createTestMolecularRecordWithDriverEvidence(evidence: ClinicalEvidence, isReportable: Boolean): MolecularRecord {
        return TestMolecularFactory.createMinimalTestMolecularRecord().copy(drivers = createDriversWithEvidence(evidence, isReportable))
    }

    private fun createDriversWithEvidence(evidence: ClinicalEvidence, isReportable: Boolean): Drivers {
        return Drivers(
            variants = setOf(
                TestVariantFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_VARIANT)
            ),
            copyNumbers = setOf(
                TestCopyNumberFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_CN)
            ),
            homozygousDisruptions = setOf(
                TestHomozygousDisruptionFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_HD)
            ),
            disruptions = setOf(
                TestDisruptionFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_DISRUPTION)
            ),
            fusions = setOf(
                TestFusionFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_FUSION)
            ),
            viruses = setOf(
                TestVirusFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence, event = EVENT_VIRUS)
            )
        )
    }

    private fun createCohortsForEvents(events: List<String>): List<InterpretedCohort> {
        return listOf(
            interpretedCohort(acronym = "trial 1", molecularEvents = events, isPotentiallyEligible = true, isOpen = true),
            interpretedCohort(acronym = "trial 2", molecularEvents = events, isPotentiallyEligible = true, isOpen = false)
        )
    }
}