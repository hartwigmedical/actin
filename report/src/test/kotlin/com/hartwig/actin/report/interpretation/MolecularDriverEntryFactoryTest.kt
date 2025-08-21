package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
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
        val record = TestMolecularFactory.createExhaustiveWholeGenomeTest()
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
        val record = TestMolecularFactory.createProperWholeGenomeTest()
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
    fun `Should assign correct driver description to copy number drivers`() {
        assertCopyNumberDescription(CopyNumberType.DEL, "PTEN del", 0, 0, "PTEN del, 0 copies")
        assertCopyNumberDescription(CopyNumberType.FULL_GAIN, "PTEN amp", 3, 3, "PTEN amp, 3 copies")
        assertCopyNumberDescription(CopyNumberType.PARTIAL_GAIN, "PTEN partial amp", 1, 3, "PTEN partial amp, 3 copies (1 full copies)")
        assertCopyNumberDescription(CopyNumberType.NONE, "PTEN copy number", 1, 3, "PTEN copy number, 1 copies")
    }

    @Test
    fun `Should assign correct driver type to variant drivers with gene role ONCO`() {
        val oncoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.ONCO, isCancerAssociatedVariant = true)
        val oncoNoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.ONCO, isCancerAssociatedVariant = false)

        assertVariantType(
            oncoCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (cancer-associated variant with unknown protein effect)"
        )
        assertVariantType(
            oncoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT),
            "Mutation (cancer-associated variant with no protein effect)"
        )
        assertVariantType(
            oncoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (cancer-associated variant with no protein effect)"
        )
        assertVariantType(oncoCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (cancer-associated variant)")
        assertVariantType(oncoCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (gain of function)")
        assertVariantType(
            oncoNoCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (no known cancer-associated variant, not biallelic, high driver)"
        )
        assertVariantType(oncoNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (no protein effect, high driver)")
        assertVariantType(oncoNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (no protein effect, high driver)")
        assertVariantType(oncoNoCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (gain of function, high driver)")
    }

    @Test
    fun `Should assign correct driver type to variant drivers with gene role TSG`() {
        val tsgCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.TSG, isCancerAssociatedVariant = true)
        val tsgCavBi = tsgCav.copy(isBiallelic = true)
        val tsgNoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.TSG, isCancerAssociatedVariant = false)
        val tsgNoCavBi = tsgNoCav.copy(isBiallelic = true)

        assertVariantType(
            tsgCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (cancer-associated variant with unknown protein effect)"
        )
        assertVariantType(
            tsgCav.copy(proteinEffect = ProteinEffect.NO_EFFECT),
            "Mutation (cancer-associated variant with no protein effect)"
        )
        assertVariantType(
            tsgCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (cancer-associated variant with no protein effect)"
        )
        assertVariantType(tsgCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (loss of function)")
        assertVariantType(tsgCavBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (loss of function, biallelic)")
        assertVariantType(tsgCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (cancer-associated variant)")
        assertVariantType(tsgCavBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (cancer-associated variant, biallelic)")
        assertVariantType(
            tsgNoCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (no known cancer-associated variant, not biallelic, high driver)"
        )
        assertVariantType(
            tsgNoCavBi.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (no known cancer-associated variant, biallelic, high driver)"
        )
        assertVariantType(tsgNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (no protein effect, high driver)")
        assertVariantType(tsgNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (no protein effect, high driver)")
        assertVariantType(tsgNoCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (loss of function, high driver)")
        assertVariantType(
            tsgNoCavBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION),
            "Mutation (loss of function, biallelic, high driver)"
        )
    }

    @Test
    fun `Should assign correct driver type to variant drivers with gene role BOTH`() {
        val bothCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.BOTH, isCancerAssociatedVariant = true)
        val bothCavBi = bothCav.copy(isBiallelic = true)

        val bothNoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.BOTH, isCancerAssociatedVariant = false)
        val bothNoCavBi = bothNoCav.copy(isBiallelic = true)

        assertVariantType(
            bothCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (cancer-associated variant with unknown protein effect)"
        )
        assertVariantType(
            bothCav.copy(proteinEffect = ProteinEffect.NO_EFFECT),
            "Mutation (cancer-associated variant with no protein effect)"
        )
        assertVariantType(
            bothCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (cancer-associated variant with no protein effect)"
        )
        assertVariantType(bothCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (cancer-associated variant)")
        assertVariantType(bothCavBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (cancer-associated variant, biallelic)")
        assertVariantType(bothCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (cancer-associated variant)")
        assertVariantType(bothCavBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (cancer-associated variant, biallelic)")
        assertVariantType(
            bothNoCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (no known cancer-associated variant, not biallelic, high driver)"
        )
        assertVariantType(
            bothNoCavBi.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (no known cancer-associated variant, biallelic, high driver)"
        )
        assertVariantType(bothNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (no protein effect, high driver)")
        assertVariantType(bothNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (no protein effect, high driver)")
    }

    @Test
    fun `Should assign correct driver type to variant drivers with unknown gene role`() {
        val unknownCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.UNKNOWN, isCancerAssociatedVariant = true)
        val unknownBi = unknownCav.copy(isBiallelic = true)

        val unknownNoCav = TestMolecularFactory.createProperVariant().copy(geneRole = GeneRole.UNKNOWN, isCancerAssociatedVariant = false)
        val unknownNoCavBi = unknownNoCav.copy(isBiallelic = true)

        assertVariantType(
            unknownCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (cancer-associated variant with unknown protein effect)"
        )
        assertVariantType(
            unknownCav.copy(proteinEffect = ProteinEffect.NO_EFFECT),
            "Mutation (cancer-associated variant with no protein effect)"
        )
        assertVariantType(
            unknownCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED),
            "Mutation (cancer-associated variant with no protein effect)"
        )
        assertVariantType(unknownCav.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (cancer-associated variant)")
        assertVariantType(unknownBi.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION), "Mutation (cancer-associated variant, biallelic)")
        assertVariantType(unknownCav.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (cancer-associated variant)")
        assertVariantType(unknownBi.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION), "Mutation (cancer-associated variant, biallelic)")
        assertVariantType(
            unknownNoCav.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (no known cancer-associated variant, not biallelic, high driver)"
        )
        assertVariantType(
            unknownNoCavBi.copy(proteinEffect = ProteinEffect.UNKNOWN),
            "Mutation (no known cancer-associated variant, biallelic, high driver)"
        )
        assertVariantType(unknownNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT), "Mutation (no protein effect, high driver)")
        assertVariantType(unknownNoCav.copy(proteinEffect = ProteinEffect.NO_EFFECT_PREDICTED), "Mutation (no protein effect, high driver)")
    }

    @Test
    fun `Should set driver likelihood for variants on same gene`() {
        val variants = listOf(
            TestMolecularFactory.createProperVariant()
                .copy(gene = "GENE1", driverLikelihood = DriverLikelihood.HIGH, isCancerAssociatedVariant = true),
            TestMolecularFactory.createProperVariant()
                .copy(gene = "GENE1", driverLikelihood = DriverLikelihood.MEDIUM, isCancerAssociatedVariant = false)
        )
        val record = TestMolecularFactory.createProperWholeGenomeTest()
            .copy(drivers = TestMolecularFactory.createProperTestDrivers().copy(variants = variants, copyNumbers = emptyList()))
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverType).isEqualTo("Mutation (gain of function)")
        assertThat(result[1].driverType).isEqualTo("Mutation (gain of function)")
    }

    @Test
    fun `Should set driver likelihood for variants on different genes`() {
        val variants = listOf(
            TestMolecularFactory.createProperVariant()
                .copy(gene = "GENE1", driverLikelihood = DriverLikelihood.HIGH, isCancerAssociatedVariant = true),
            TestMolecularFactory.createProperVariant()
                .copy(gene = "GENE2", driverLikelihood = DriverLikelihood.MEDIUM, isCancerAssociatedVariant = false),
            TestMolecularFactory.createProperVariant().copy(gene = "GENE3", driverLikelihood = null, isCancerAssociatedVariant = false)
        )
        val record = TestMolecularFactory.createProperWholeGenomeTest()
            .copy(drivers = TestMolecularFactory.createProperTestDrivers().copy(variants = variants, copyNumbers = emptyList()))
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverType).isEqualTo("Mutation (gain of function)")
        assertThat(result[1].driverType).isEqualTo("Mutation (gain of function, medium driver)")
        assertThat(result[2].driverType).isEqualTo("Mutation (gain of function)")
    }

    @Test
    fun `Should not show driver likelihood for copy numbers and disruptions`() {
        val copyNumbers = listOf(TestMolecularFactory.createProperCopyNumber())
        val disruptions = listOf(TestDisruptionFactory.createMinimal().copy(isReportable = true))
        val homozygousDisruptions = listOf(TestMolecularFactory.createMinimalHomozygousDisruption().copy(isReportable = true))
        val record = TestMolecularFactory.createProperWholeGenomeTest().copy(
            drivers = TestMolecularFactory.createProperTestDrivers()
                .copy(
                    variants = emptyList(),
                    copyNumbers = copyNumbers,
                    disruptions = disruptions,
                    homozygousDisruptions = homozygousDisruptions
                )
        )
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverType).isEqualTo("Deletion")
        assertThat(result[1].driverType).isEqualTo("Disruption (homozygous)")
        assertThat(result[2].driverType).isEqualTo("Disruption (not biallelic)")
    }

    @Test
    fun `Should show driver likelihood for fusions and viruses except when known fusion`() {
        val fusions = listOf(
            TestMolecularFactory.createProperFusion(),
            TestMolecularFactory.createProperFusion()
                .copy(driverLikelihood = DriverLikelihood.LOW, driverType = FusionDriverType.PROMISCUOUS_3)
        )
        val viruses = listOf(TestVirusFactory.createMinimal().copy(isReportable = true, driverLikelihood = DriverLikelihood.MEDIUM))
        val record = TestMolecularFactory.createProperWholeGenomeTest().copy(
            drivers = TestMolecularFactory.createProperTestDrivers()
                .copy(variants = emptyList(), copyNumbers = emptyList(), viruses = viruses, fusions = fusions)
        )
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverType).isEqualTo("Known fusion")
        assertThat(result[1].driverType).isEqualTo("Virus (medium driver)")
        assertThat(result[2].driverType).isEqualTo("3' promiscuous fusion (low driver)")
    }

    private fun assertVariantType(variant: Variant, expectedDriverType: String) {
        val record = TestMolecularFactory.createProperWholeGenomeTest()
            .copy(drivers = TestMolecularFactory.createProperTestDrivers().copy(variants = listOf(variant), copyNumbers = emptyList()))
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverType).isEqualTo(expectedDriverType)
    }

    private fun assertCopyNumberType(copyNumberType: CopyNumberType, expectedDriverType: String) {
        val copyNumber = TestMolecularFactory.createProperCopyNumber()
            .copy(canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(copyNumberType))
        val record = TestMolecularFactory.createProperWholeGenomeTest().copy(
            drivers = TestMolecularFactory.createProperTestDrivers()
                .copy(variants = emptyList(), copyNumbers = listOf(copyNumber))
        )
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].driverType).isEqualTo(expectedDriverType)
    }

    private fun assertCopyNumberDescription(
        copyNumberType: CopyNumberType,
        event: String,
        minCopies: Int,
        maxCopies: Int,
        expectedDescription: String
    ) {
        val copyNumber = TestMolecularFactory.createProperCopyNumber().copy(
            event = event,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                type = copyNumberType,
                minCopies = minCopies,
                maxCopies = maxCopies
            )
        )
        val record = TestMolecularFactory.createProperWholeGenomeTest()
            .copy(drivers = TestMolecularFactory.createProperTestDrivers().copy(variants = emptyList(), copyNumbers = listOf(copyNumber)))
        val result = createFactoryForMolecularRecord(record).create()
        assertThat(result[0].description).isEqualTo(expectedDescription)
    }

    private fun createTestMolecularRecordWithNonReportableDriverWithEvidence(evidence: ClinicalEvidence): MolecularRecord {
        return createTestMolecularRecordWithDriverEvidence(evidence, false)
    }

    private fun createTestMolecularRecordWithDriverEvidence(evidence: ClinicalEvidence, isReportable: Boolean): MolecularRecord {
        return TestMolecularFactory.createMinimalWholeGenomeTest().copy(drivers = createDriversWithEvidence(evidence, isReportable))
    }

    private fun createDriversWithEvidence(evidence: ClinicalEvidence, isReportable: Boolean): Drivers {
        return TestMolecularFactory.createMinimalWholeGenomeTest().drivers.copy(
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
            interpretedCohort(acronym = "trial 1", molecularInclusionEvents = setOf(event), isPotentiallyEligible = true, isOpen = true),
            interpretedCohort(acronym = "trial 2", molecularInclusionEvents = setOf(event), isPotentiallyEligible = true, isOpen = false)
        )
        return createFactoryForMolecularRecordAndCohorts(molecularRecord, cohorts)
    }
}