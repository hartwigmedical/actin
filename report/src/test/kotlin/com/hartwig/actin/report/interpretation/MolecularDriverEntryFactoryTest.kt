package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVirusFactory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory.interpretedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularDriverEntryFactoryTest {

    @Test
    fun `Should created molecular driver entries`() {
        val record = TestMolecularFactory.createExhaustiveTestMolecularRecord()
        val factory = createFactoryForMolecularRecord(record)
        val entries = factory.create()
        assertThat(entries).hasSize(8)
    }

    @Test
    fun `Should include non-actionable reportable drivers`() {
        val record = createTestMolecularRecordWithDriverEvidence(TestClinicalEvidenceFactory.createEmptyClinicalEvidence(), true)
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(1)
    }

    @Test
    fun `Should skip non actionable not reportable drivers`() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.createEmptyClinicalEvidence())
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(0)
    }

    @Test
    fun `Should include non-reportable drivers with actin trial matches`() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.createEmptyClinicalEvidence())
        val driverToFind = record.drivers.viruses.iterator().next().event
        assertThat(createFactoryWithCohortsForEvent(record, driverToFind).create()).hasSize(1)
    }

    @Test
    fun `Should include non reportable drivers with approved treatment matches`() {
        val record =
            createTestMolecularRecordWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.withApprovedTreatment("treatment"))
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(1)
    }

    @Test
    fun `Should include non-reportable drivers with external trial matches`() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(
            TestClinicalEvidenceFactory.withExternalEligibleTrial(
                TestClinicalEvidenceFactory.createTestExternalTrial()
            )
        )
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(1)
    }

    @Test
    fun `Should match actin trial to molecular drivers`() {
        val record = TestMolecularFactory.createProperTestMolecularRecord()
        assertThat(record.drivers.variants).isNotEmpty
        val firstVariant = record.drivers.variants.iterator().next()
        val driverToFind = firstVariant.event
        val entry = createFactoryWithCohortsForEvent(record, driverToFind).create()
            .find { it.name.startsWith(driverToFind) }
            ?: throw IllegalStateException(
                "Could not find molecular driver entry starting with driver: $driverToFind"
            )
        assertThat(entry.actinTrials).containsExactly("trial 1")
    }

    @Test
    fun `Should assign correct driver types to copy number drivers`() {
        assertCopyNumberType(CopyNumberType.LOSS, "Loss")
        assertCopyNumberType(CopyNumberType.FULL_GAIN, "Amplification")
        assertCopyNumberType(CopyNumberType.PARTIAL_GAIN, "Amplification")
        assertCopyNumberType(CopyNumberType.NONE, "Copy Number")
    }

    private fun assertCopyNumberType(copyNumberType: CopyNumberType, expectedDriverType: String) {
        val loss = TestMolecularFactory.createProperCopyNumber().copy(type = copyNumberType)
        val record = TestMolecularFactory.createProperTestMolecularRecord().copy(
            drivers = TestMolecularFactory.createProperTestDrivers()
                .copy(variants = emptySet(), copyNumbers = setOf(loss))
        )
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverType).isEqualTo(expectedDriverType)
    }

    private fun createTestMolecularRecordWithNonReportableDriverWithEvidence(evidence: ClinicalEvidence): MolecularRecord {
        return createTestMolecularRecordWithDriverEvidence(evidence, false)
    }

    private fun createTestMolecularRecordWithDriverEvidence(evidence: ClinicalEvidence, isReportable: Boolean): MolecularRecord {
        return TestMolecularFactory.createMinimalTestMolecularRecord().copy(drivers = createDriversWithEvidence(evidence, isReportable))
    }

    private fun createDriversWithEvidence(evidence: ClinicalEvidence, isReportable: Boolean): Drivers {
        return TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            viruses = setOf(TestVirusFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence))
        )
    }

    private fun createFactoryForMolecularRecord(molecular: MolecularRecord): MolecularDriverEntryFactory {
        return createFactoryForMolecularRecordAndCohorts(molecular, emptyList())
    }

    private fun createFactoryForMolecularRecordAndCohorts(
        molecular: MolecularRecord, cohorts: List<InterpretedCohort>
    ): MolecularDriverEntryFactory {
        return MolecularDriverEntryFactory(
            MolecularDriversInterpreter(molecular.drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        )
    }

    private fun createFactoryWithCohortsForEvent(molecularRecord: MolecularRecord, event: String): MolecularDriverEntryFactory {
        val cohorts = listOf(
            interpretedCohort(acronym = "trial 1", molecularEvents = setOf(event), isPotentiallyEligible = true, isOpen = true),
            interpretedCohort(acronym = "trial 2", molecularEvents = setOf(event), isPotentiallyEligible = true, isOpen = false)
        )
        return createFactoryForMolecularRecordAndCohorts(molecularRecord, cohorts)
    }
}