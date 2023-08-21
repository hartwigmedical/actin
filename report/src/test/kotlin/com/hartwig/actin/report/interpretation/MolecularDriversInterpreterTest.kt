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
import com.hartwig.actin.report.interpretation.EvaluatedCohortTestFactory.evaluatedCohort
import org.junit.Assert
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
                "trial 1"
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
            val interpreter = MolecularDriversInterpreter(molecularRecord.drivers(), EvaluatedCohortsInterpreter(cohorts))
            Assert.assertEquals(expectedCount.toLong(), interpreter.filteredVariants().count())
            Assert.assertEquals(expectedCount.toLong(), interpreter.filteredCopyNumbers().count())
            Assert.assertEquals(expectedCount.toLong(), interpreter.filteredHomozygousDisruptions().count())
            Assert.assertEquals(expectedCount.toLong(), interpreter.filteredDisruptions().count())
            Assert.assertEquals(expectedCount.toLong(), interpreter.filteredFusions().count())
            Assert.assertEquals(expectedCount.toLong(), interpreter.filteredViruses().count())
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