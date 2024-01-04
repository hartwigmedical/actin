package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.actin.report.interpretation.EvaluatedCohortTestFactory.evaluatedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularDriversInterpreterTest {

    @Test
    fun shouldIncludeNonActionableReportableDrivers() {
        val record = createTestMolecularRecordWithDriverEvidence(TestActionableEvidenceFactory.createEmpty(), true)
        assertCountForRecord(1, record)
    }

    @Test
    fun shouldSkipNonActionableNotReportableDrivers() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty())
        assertCountForRecord(0, record)
    }

    @Test
    fun shouldIncludeNonReportableDriversWithActinTrialMatches() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty())
        assertCountForRecordAndCohorts(
            1,
            record,
            createCohortsForEvents(listOf(EVENT_VARIANT, EVENT_CN, EVENT_HD, EVENT_DISRUPTION, EVENT_FUSION, EVENT_VIRUS))
        )
    }

    @Test
    fun shouldIncludeNonReportableDriversWithApprovedTreatmentMatches() {
        val record =
            createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.withApprovedTreatment("treatment"))
        assertCountForRecord(1, record)
    }

    @Test
    fun shouldIncludeNonReportableDriversWithExternalTrialMatches() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(
            TestActionableEvidenceFactory.withExternalEligibleTrial(
                TestExternalTrialFactory.createMinimal()
            )
        )
        assertCountForRecord(1, record)
    }

    companion object {
        const val EVENT_VARIANT = "variant"
        const val EVENT_CN = "CN"
        const val EVENT_HD = "HD"
        const val EVENT_DISRUPTION = "disruption"
        const val EVENT_FUSION = "fusion"
        const val EVENT_VIRUS = "virus"

        private fun assertCountForRecord(expectedCount: Int, molecularRecord: MolecularRecord) {
            assertCountForRecordAndCohorts(expectedCount, molecularRecord, emptyList())
        }

        private fun assertCountForRecordAndCohorts(expectedCount: Int, molecularRecord: MolecularRecord, cohorts: List<EvaluatedCohort>) {
            val interpreter =
                MolecularDriversInterpreter(molecularRecord.drivers(), EvaluatedCohortsInterpreter.fromEvaluatedCohorts(cohorts))
            assertThat(interpreter.filteredVariants()).hasSize(expectedCount)
            assertThat(interpreter.filteredCopyNumbers()).hasSize(expectedCount)
            assertThat(interpreter.filteredHomozygousDisruptions()).hasSize(expectedCount)
            assertThat(interpreter.filteredDisruptions()).hasSize(expectedCount)
            assertThat(interpreter.filteredFusions()).hasSize(expectedCount)
            assertThat(interpreter.filteredViruses()).hasSize(expectedCount)
        }

        private fun createTestMolecularRecordWithNonReportableDriverWithEvidence(evidence: ActionableEvidence): MolecularRecord {
            return createTestMolecularRecordWithDriverEvidence(evidence, false)
        }

        private fun createTestMolecularRecordWithDriverEvidence(evidence: ActionableEvidence, isReportable: Boolean): MolecularRecord {
            return ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .drivers(createDriversWithEvidence(evidence, isReportable))
                .build()
        }

        private fun createDriversWithEvidence(evidence: ActionableEvidence, isReportable: Boolean): MolecularDrivers {
            return ImmutableMolecularDrivers.builder()
                .addVariants(TestVariantFactory.builder().isReportable(isReportable).evidence(evidence).event(EVENT_VARIANT).build())
                .addCopyNumbers(TestCopyNumberFactory.builder().isReportable(isReportable).evidence(evidence).event(EVENT_CN).build())
                .addHomozygousDisruptions(
                    TestHomozygousDisruptionFactory.builder()
                        .isReportable(isReportable)
                        .evidence(evidence)
                        .event(EVENT_HD)
                        .build()
                )
                .addDisruptions(
                    TestDisruptionFactory.builder()
                        .isReportable(isReportable)
                        .evidence(evidence)
                        .event(EVENT_DISRUPTION)
                        .build()
                )
                .addFusions(TestFusionFactory.builder().isReportable(isReportable).evidence(evidence).event(EVENT_FUSION).build())
                .addViruses(TestVirusFactory.builder().isReportable(isReportable).evidence(evidence).event(EVENT_VIRUS).build())
                .build()
        }

        private fun createCohortsForEvents(events: List<String>): List<EvaluatedCohort> {
            return listOf(
                evaluatedCohort(acronym = "trial 1", molecularEvents = events, isPotentiallyEligible = true, isOpen = true),
                evaluatedCohort(acronym = "trial 2", molecularEvents = events, isPotentiallyEligible = true, isOpen = false)
            )
        }
    }
}