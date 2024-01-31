package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.actin.report.interpretation.EvaluatedCohortTestFactory.evaluatedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularDriverEntryFactoryTest {

    @Test
    fun canCreateMolecularDriverEntries() {
        val record = TestMolecularFactory.createExhaustiveTestMolecularRecord()
        val factory = createFactoryForMolecularRecord(record)
        val entries = factory.create()
        assertThat(entries).hasSize(7)
    }

    @Test
    fun shouldIncludeNonActionableReportableDrivers() {
        val record = createTestMolecularRecordWithDriverEvidence(TestActionableEvidenceFactory.createEmpty(), true)
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(1)
    }

    @Test
    fun shouldSkipNonActionableNotReportableDrivers() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty())
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(0)
    }

    @Test
    fun shouldIncludeNonReportableDriversWithActinTrialMatches() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty())
        val driverToFind = record.drivers.viruses.iterator().next().event
        assertThat(createFactoryWithCohortsForEvent(record, driverToFind).create()).hasSize(1)
    }

    @Test
    fun shouldIncludeNonReportableDriversWithApprovedTreatmentMatches() {
        val record =
            createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.withApprovedTreatment("treatment"))
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(1)
    }

    @Test
    fun shouldIncludeNonReportableDriversWithExternalTrialMatches() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(
            TestActionableEvidenceFactory.withExternalEligibleTrial(
                TestExternalTrialFactory.createTestTrial()
            )
        )
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(1)
    }

    @Test
    fun shouldMatchActinTrialToMolecularDrivers() {
        val record = TestMolecularFactory.createProperTestMolecularRecord()
        assertThat(record.drivers.variants).isNotEmpty
        val firstVariant = record.drivers.variants.iterator().next()
        val driverToFind = firstVariant.event
        val entry = createFactoryWithCohortsForEvent(record, driverToFind).create()
            .find { it.driver.startsWith(driverToFind) }
            ?: throw IllegalStateException(
                "Could not find molecular driver entry starting with driver: $driverToFind"
            )
        assertThat(entry.actinTrials).containsExactly("trial 1")
    }

    companion object {
        private fun createTestMolecularRecordWithNonReportableDriverWithEvidence(evidence: ActionableEvidence): MolecularRecord {
            return createTestMolecularRecordWithDriverEvidence(evidence, false)
        }

        private fun createTestMolecularRecordWithDriverEvidence(evidence: ActionableEvidence, isReportable: Boolean): MolecularRecord {
            return TestMolecularFactory.createMinimalTestMolecularRecord().copy(drivers = createDriversWithEvidence(evidence, isReportable))
        }

        private fun createDriversWithEvidence(evidence: ActionableEvidence, isReportable: Boolean): MolecularDrivers {
            return TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
                viruses = setOf(TestVirusFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence))
            )
        }

        private fun createFactoryForMolecularRecord(molecular: MolecularRecord): MolecularDriverEntryFactory {
            return createFactoryForMolecularRecordAndCohorts(molecular, emptyList())
        }

        private fun createFactoryForMolecularRecordAndCohorts(
            molecular: MolecularRecord, cohorts: List<EvaluatedCohort>
        ): MolecularDriverEntryFactory {
            return MolecularDriverEntryFactory(
                MolecularDriversInterpreter(molecular.drivers, EvaluatedCohortsInterpreter.fromEvaluatedCohorts(cohorts))
            )
        }

        private fun createFactoryWithCohortsForEvent(molecularRecord: MolecularRecord, event: String): MolecularDriverEntryFactory {
            val cohorts = listOf(
                evaluatedCohort(acronym = "trial 1", molecularEvents = setOf(event), isPotentiallyEligible = true, isOpen = true),
                evaluatedCohort(acronym = "trial 2", molecularEvents = setOf(event), isPotentiallyEligible = true, isOpen = false)
            )
            return createFactoryForMolecularRecordAndCohorts(molecularRecord, cohorts)
        }
    }
}