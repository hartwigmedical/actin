package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import com.hartwig.actin.report.interpretation.EvaluatedCohortTestFactory.evaluatedCohort
import org.junit.Assert
import org.junit.Test

class MolecularDriverEntryFactoryTest {
    @Test
    fun canCreateMolecularDriverEntries() {
        val record = TestMolecularFactory.createExhaustiveTestMolecularRecord()
        val factory = createFactoryForMolecularRecord(record)
        val entries = factory.create()
        Assert.assertEquals(7, entries.count())
    }

    @Test
    fun shouldIncludeNonActionableReportableDrivers() {
        val record = createTestMolecularRecordWithDriverEvidence(TestActionableEvidenceFactory.createEmpty(), true)
        val factory = createFactoryForMolecularRecord(record)
        Assert.assertEquals(1, factory.create().count())
    }

    @Test
    fun shouldSkipNonActionableNotReportableDrivers() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty())
        val factory = createFactoryForMolecularRecord(record)
        Assert.assertEquals(0, factory.create().count())
    }

    @Test
    fun shouldIncludeNonReportableDriversWithActinTrialMatches() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.createEmpty())
        val driverToFind = record.drivers().viruses().iterator().next().event()
        Assert.assertEquals(1, createFactoryWithCohortsForEvent(record, driverToFind).create().count())
    }

    @Test
    fun shouldIncludeNonReportableDriversWithApprovedTreatmentMatches() {
        val record =
            createTestMolecularRecordWithNonReportableDriverWithEvidence(TestActionableEvidenceFactory.withApprovedTreatment("treatment"))
        val factory = createFactoryForMolecularRecord(record)
        Assert.assertEquals(1, factory.create().count())
    }

    @Test
    fun shouldIncludeNonReportableDriversWithExternalTrialMatches() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(
            TestActionableEvidenceFactory.withExternalEligibleTrial(
                "trial 1"
            )
        )
        val factory = createFactoryForMolecularRecord(record)
        Assert.assertEquals(1, factory.create().count())
    }

    @Test
    fun canMatchActinTrialToMolecularDrivers() {
        val record = TestMolecularFactory.createProperTestMolecularRecord()
        Assert.assertFalse(record.drivers().variants().isEmpty())
        val firstVariant = record.drivers().variants().iterator().next()
        val driverToFind = firstVariant.event()
        val entry = createFactoryWithCohortsForEvent(record, driverToFind).create()
            .find { it.driver.startsWith(driverToFind) }
            ?: throw IllegalStateException(
                "Could not find molecular driver entry starting with driver: $driverToFind"
            )
        Assert.assertEquals(1, entry.actinTrials.size.toLong())
        Assert.assertTrue(entry.actinTrials.contains("trial 1"))
    }

    companion object {
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
                .addViruses(TestVirusFactory.builder().isReportable(isReportable).evidence(evidence).build())
                .build()
        }

        private fun createFactoryForMolecularRecord(molecular: MolecularRecord): MolecularDriverEntryFactory {
            return createFactoryForMolecularRecordAndCohorts(molecular, emptyList())
        }

        private fun createFactoryForMolecularRecordAndCohorts(
            molecular: MolecularRecord,
            cohorts: List<EvaluatedCohort>
        ): MolecularDriverEntryFactory {
            return MolecularDriverEntryFactory(MolecularDriversInterpreter(molecular.drivers(), EvaluatedCohortsInterpreter(cohorts)))
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