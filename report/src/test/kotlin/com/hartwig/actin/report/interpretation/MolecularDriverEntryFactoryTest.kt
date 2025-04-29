package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVirusFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory.interpretedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularDriverEntryFactoryTest {

    @Test
    fun `Should create molecular driver entries`() {
        val record = TestMolecularFactory.createExhaustiveTestMolecularRecord()
        val factory = createFactoryForMolecularRecord(record)
        val entries = factory.create()
        assertThat(entries).hasSize(8)
    }

    @Test
    fun `Should include non-actionable reportable drivers`() {
        val record = createTestMolecularRecordWithDriverEvidence(TestClinicalEvidenceFactory.createEmpty(), true)
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(1)
    }

    @Test
    fun `Should skip non actionable not reportable drivers`() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.createEmpty())
        val factory = createFactoryForMolecularRecord(record)
        assertThat(factory.create()).hasSize(0)
    }

    @Test
    fun `Should include non-reportable drivers with actin trial matches`() {
        val record = createTestMolecularRecordWithNonReportableDriverWithEvidence(TestClinicalEvidenceFactory.createEmpty())
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
            TestClinicalEvidenceFactory.withEligibleTrial(TestExternalTrialFactory.createTestTrial())
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
            .find { it.description.startsWith(driverToFind) }
            ?: throw IllegalStateException(
                "Could not find molecular driver entry starting with driver: $driverToFind"
            )
        assertThat(entry.actinTrials).containsExactly(TrialAcronymAndLocations("trial 1", emptySet()))
    }

    @Test
    fun `Should assign correct driver types to copy number drivers`() {
        assertCopyNumberType(CopyNumberType.DEL, "Deletion")
        assertCopyNumberType(CopyNumberType.FULL_GAIN, "Amplification")
        assertCopyNumberType(CopyNumberType.PARTIAL_GAIN, "Amplification")
        assertCopyNumberType(CopyNumberType.NONE, "Copy Number")
    }

    @Test
    fun `Should assign correct driver type to variant drivers with gene role ONCO`() {
        val oncoHotspot = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.ONCO, isHotspot = true)
        val oncoNoHotspot = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.ONCO, isHotspot = false)

        assertVariantType(oncoHotspot.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (Hotspot with unknown protein effect)")
        assertVariantType(oncoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (Hotspot with no protein effect)")
        assertVariantType(oncoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (Hotspot with no protein effect)")
        assertVariantType(oncoHotspot.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Hotspot)")
        assertVariantType(oncoHotspot.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Gain of function)")
        assertVariantType(oncoNoHotspot.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known hotspot)")
        assertVariantType(oncoNoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (No protein effect)")
        assertVariantType(oncoNoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (No protein effect)")
        assertVariantType(oncoNoHotspot.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (No known hotspot)")
        assertVariantType(oncoNoHotspot.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Gain of function)")
    }

    @Test
    fun `Should assign correct driver type to variant drivers with gene role TSG`() {
        val tsgHotspot = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.TSG, isHotspot = true)
        val tsgHotspotBi = tsgHotspot.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))
        val tsgNoHotspot = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.TSG, isHotspot = false)
        val tsgNoHotspotBi = tsgNoHotspot.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        assertVariantType(tsgHotspot.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (Hotspot with unknown protein effect)")
        assertVariantType(tsgHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (Hotspot with no protein effect)")
        assertVariantType(tsgHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (Hotspot with no protein effect)")
        assertVariantType(tsgHotspot.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Loss-of-function)")
        assertVariantType(tsgHotspotBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Loss-of-function, biallelic)")
        assertVariantType(tsgHotspot.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Hotspot)")
        assertVariantType(tsgHotspotBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Hotspot, biallelic)")
        assertVariantType(tsgNoHotspot.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known hotspot)")
        assertVariantType(tsgNoHotspotBi.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known hotspot, biallelic)")
        assertVariantType(tsgNoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (No protein effect)")
        assertVariantType(tsgNoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (No protein effect)")
        assertVariantType(tsgNoHotspot.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Loss-of-function)")
        assertVariantType(tsgNoHotspotBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Loss-of-function, biallelic)")
        assertVariantType(
            tsgNoHotspot.copy(
                proteinEffect = ProteinEffect.UNKNOWN,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal()
                    .copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT)
            ), "Mutation (Loss-of-function)"
        )
        assertVariantType(
            tsgNoHotspotBi.copy(
                proteinEffect = ProteinEffect.UNKNOWN,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal()
                    .copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT)
            ), "Mutation (Loss-of-function, biallelic)"
        )
        assertVariantType(tsgNoHotspot.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (No known hotspot)")
        assertVariantType(tsgNoHotspotBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (No known hotspot, biallelic)")
    }

    @Test
    fun `Should assign correct driver type to variant drivers with gene role BOTH`() {
        val bothHotspot = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.BOTH, isHotspot = true)
        val bothHotspotBi = bothHotspot.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        val bothNoHotspot = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.BOTH, isHotspot = false)
        val bothNoHotspotBi =
            bothNoHotspot.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        assertVariantType(bothHotspot.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (Hotspot with unknown protein effect)")
        assertVariantType(bothHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (Hotspot with no protein effect)")
        assertVariantType(bothHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (Hotspot with no protein effect)")
        assertVariantType(bothHotspot.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Hotspot)")
        assertVariantType(bothHotspotBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Hotspot, biallelic)")
        assertVariantType(bothHotspot.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Hotspot)")
        assertVariantType(bothHotspotBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Hotspot, biallelic)")
        assertVariantType(bothNoHotspot.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known hotspot)")
        assertVariantType(bothNoHotspotBi.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known hotspot, biallelic)")
        assertVariantType(bothNoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (No protein effect)")
        assertVariantType(bothNoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (No protein effect)")
        assertVariantType(bothNoHotspot.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (No known hotspot)")
        assertVariantType(bothNoHotspotBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (No known hotspot, biallelic)")
        assertVariantType(bothNoHotspot.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (No known hotspot)")
        assertVariantType(bothNoHotspotBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (No known hotspot, biallelic)")
    }

    @Test
    fun `Should assign correct driver type to variant drivers with unknown gene role`() {
        val unknownHotspot = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.UNKNOWN, isHotspot = true)
        val unknownBi = unknownHotspot.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        val unknownNoHotspot = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.UNKNOWN, isHotspot = false)
        val unknownNoHotspotBi =
            unknownNoHotspot.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        assertVariantType(unknownHotspot.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (Hotspot with unknown protein effect)")
        assertVariantType(unknownHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (Hotspot with no protein effect)")
        assertVariantType(
            unknownHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (Hotspot with no protein effect)"
        )
        assertVariantType(unknownHotspot.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Hotspot)")
        assertVariantType(unknownBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Hotspot, biallelic)")
        assertVariantType(unknownHotspot.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Hotspot)")
        assertVariantType(unknownBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Hotspot, biallelic)")
        assertVariantType(unknownNoHotspot.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known hotspot)")
        assertVariantType(unknownNoHotspotBi.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known hotspot, biallelic)")
        assertVariantType(unknownNoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (No protein effect)")
        assertVariantType(unknownNoHotspot.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (No protein effect)")
        assertVariantType(unknownNoHotspot.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (No known hotspot)")
        assertVariantType(unknownNoHotspotBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (No known hotspot, biallelic)")
        assertVariantType(unknownNoHotspot.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (No known hotspot)")
        assertVariantType(unknownNoHotspotBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (No known hotspot, biallelic)")
    }

    private fun assertVariantType(variant: Variant, expectedDriverType: String) {
        val record = TestMolecularFactory.createProperTestMolecularRecord()
            .copy(drivers = TestMolecularFactory.createProperTestDrivers().copy(variants = listOf(variant), copyNumbers = emptyList()))
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverType).isEqualTo(expectedDriverType)
    }

    private fun assertCopyNumberType(copyNumberType: CopyNumberType, expectedDriverType: String) {
        val copyNumber = TestMolecularFactory.createProperCopyNumber()
            .copy(canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(copyNumberType))
        val record = TestMolecularFactory.createProperTestMolecularRecord().copy(
            drivers = TestMolecularFactory.createProperTestDrivers()
                .copy(variants = emptyList(), copyNumbers = listOf(copyNumber))
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
            viruses = listOf(TestVirusFactory.createMinimal().copy(isReportable = isReportable, evidence = evidence))
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