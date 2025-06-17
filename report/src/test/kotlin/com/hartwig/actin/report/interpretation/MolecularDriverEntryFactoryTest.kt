package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
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
        assertThat(entries).hasSize(13)
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
        val oncoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.ONCO, isCancerAssociatedVariant = true)
        val oncoNoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.ONCO, isCancerAssociatedVariant = false)

        assertVariantType(
            oncoCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (Cancer-associated variant with unknown protein effect)"
        )
        assertVariantType(
            oncoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT),
            "Mutation (Cancer-associated variant with no protein effect)"
        )
        assertVariantType(
            oncoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (Cancer-associated variant with no protein effect)"
        )
        assertVariantType(oncoCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Cancer-associated variant)")
        assertVariantType(oncoCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Gain of function)")
        assertVariantType(oncoNoCav.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known cancer-associated variant)")
        assertVariantType(oncoNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (No protein effect)")
        assertVariantType(oncoNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (No protein effect)")
        assertVariantType(oncoNoCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (No known cancer-associated variant)")
        assertVariantType(oncoNoCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Gain of function)")
    }

    @Test
    fun `Should assign correct driver type to variant drivers with gene role TSG`() {
        val tsgCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.TSG, isCancerAssociatedVariant = true)
        val tsgCavBi = tsgCav.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))
        val tsgNoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.TSG, isCancerAssociatedVariant = false)
        val tsgNoCavBi = tsgNoCav.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        assertVariantType(
            tsgCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (Cancer-associated variant with unknown protein effect)"
        )
        assertVariantType(
            tsgCav.copy(proteinEffect = ProteinEffect.NO_EFFECT),
            "Mutation (Cancer-associated variant with no protein effect)"
        )
        assertVariantType(
            tsgCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (Cancer-associated variant with no protein effect)"
        )
        assertVariantType(tsgCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Loss of function)")
        assertVariantType(tsgCavBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Loss of function, biallelic)")
        assertVariantType(tsgCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Cancer-associated variant)")
        assertVariantType(tsgCavBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Cancer-associated variant, biallelic)")
        assertVariantType(tsgNoCav.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known cancer-associated variant)")
        assertVariantType(
            tsgNoCavBi.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (No known cancer-associated variant, biallelic)"
        )
        assertVariantType(tsgNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (No protein effect)")
        assertVariantType(tsgNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (No protein effect)")
        assertVariantType(tsgNoCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Loss of function)")
        assertVariantType(tsgNoCavBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Loss of function, biallelic)")
        assertVariantType(tsgNoCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (No known cancer-associated variant)")
        assertVariantType(
            tsgNoCavBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION),
            "Mutation (No known cancer-associated variant, biallelic)"
        )
    }

    @Test
    fun `Should assign correct driver type to variant drivers with gene role BOTH`() {
        val bothCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.BOTH, isCancerAssociatedVariant = true)
        val bothCavBi = bothCav.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        val bothNoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.BOTH, isCancerAssociatedVariant = false)
        val bothNoCavBi = bothNoCav.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        assertVariantType(
            bothCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (Cancer-associated variant with unknown protein effect)"
        )
        assertVariantType(
            bothCav.copy(proteinEffect = ProteinEffect.NO_EFFECT),
            "Mutation (Cancer-associated variant with no protein effect)"
        )
        assertVariantType(
            bothCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (Cancer-associated variant with no protein effect)"
        )
        assertVariantType(bothCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Cancer-associated variant)")
        assertVariantType(bothCavBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Cancer-associated variant, biallelic)")
        assertVariantType(bothCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Cancer-associated variant)")
        assertVariantType(bothCavBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Cancer-associated variant, biallelic)")
        assertVariantType(bothNoCav.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known cancer-associated variant)")
        assertVariantType(
            bothNoCavBi.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (No known cancer-associated variant, biallelic)"
        )
        assertVariantType(bothNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (No protein effect)")
        assertVariantType(bothNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (No protein effect)")
        assertVariantType(bothNoCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (No known cancer-associated variant)")
        assertVariantType(
            bothNoCavBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION),
            "Mutation (No known cancer-associated variant, biallelic)"
        )
        assertVariantType(bothNoCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (No known cancer-associated variant)")
        assertVariantType(
            bothNoCavBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION),
            "Mutation (No known cancer-associated variant, biallelic)"
        )
    }

    @Test
    fun `Should assign correct driver type to variant drivers with unknown gene role`() {
        val unknownCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.UNKNOWN, isCancerAssociatedVariant = true)
        val unknownBi = unknownCav.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        val unknownNoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.UNKNOWN, isCancerAssociatedVariant = false)
        val unknownNoCavBi = unknownNoCav.copy(extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true))

        assertVariantType(
            unknownCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (Cancer-associated variant with unknown protein effect)"
        )
        assertVariantType(
            unknownCav.copy(proteinEffect = ProteinEffect.NO_EFFECT),
            "Mutation (Cancer-associated variant with no protein effect)"
        )
        assertVariantType(
            unknownCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (Cancer-associated variant with no protein effect)"
        )
        assertVariantType(unknownCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Cancer-associated variant)")
        assertVariantType(unknownBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (Cancer-associated variant, biallelic)")
        assertVariantType(unknownCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Cancer-associated variant)")
        assertVariantType(unknownBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (Cancer-associated variant, biallelic)")
        assertVariantType(unknownNoCav.copy(proteinEffect = ProteinEffect.UNKNOWN), "Mutation (No known cancer-associated variant)")
        assertVariantType(
            unknownNoCavBi.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (No known cancer-associated variant, biallelic)"
        )
        assertVariantType(unknownNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (No protein effect)")
        assertVariantType(unknownNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (No protein effect)")
        assertVariantType(
            unknownNoCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION),
            "Mutation (No known cancer-associated variant)"
        )
        assertVariantType(
            unknownNoCavBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION),
            "Mutation (No known cancer-associated variant, biallelic)"
        )
        assertVariantType(
            unknownNoCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION),
            "Mutation (No known cancer-associated variant)"
        )
        assertVariantType(
            unknownNoCavBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION),
            "Mutation (No known cancer-associated variant, biallelic)"
        )
    }

    @Test
    fun `Should set driver likelihood display for variants on same gene`() {
        val variants = listOf(
            TestMolecularFactory.createProperVariant()
                .copy(gene = "GENE1", driverLikelihood = DriverLikelihood.HIGH, isCancerAssociatedVariant = true),
            TestMolecularFactory.createProperVariant()
                .copy(gene = "GENE1", driverLikelihood = DriverLikelihood.MEDIUM, isCancerAssociatedVariant = false)
        )
        val record = TestMolecularFactory.createProperTestMolecularRecord()
            .copy(drivers = TestMolecularFactory.createProperTestDrivers().copy(variants = variants, copyNumbers = emptyList()))
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverLikelihoodDisplay).isEqualTo("")
        assertThat(result[1].driverLikelihoodDisplay).isEqualTo("")
    }

    @Test
    fun `Should set driver likelihood display for variants on different genes`() {
        val variants = listOf(
            TestMolecularFactory.createProperVariant()
                .copy(gene = "GENE1", driverLikelihood = DriverLikelihood.HIGH, isCancerAssociatedVariant = true),
            TestMolecularFactory.createProperVariant()
                .copy(gene = "GENE2", driverLikelihood = DriverLikelihood.MEDIUM, isCancerAssociatedVariant = false),
            TestMolecularFactory.createProperVariant().copy(gene = "GENE3", driverLikelihood = null, isCancerAssociatedVariant = false)
        )
        val record = TestMolecularFactory.createProperTestMolecularRecord()
            .copy(drivers = TestMolecularFactory.createProperTestDrivers().copy(variants = variants, copyNumbers = emptyList()))
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverLikelihoodDisplay).isEqualTo("")
        assertThat(result[1].driverLikelihoodDisplay).isEqualTo("Medium")
        assertThat(result[2].driverLikelihoodDisplay).isEqualTo("N/A")
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