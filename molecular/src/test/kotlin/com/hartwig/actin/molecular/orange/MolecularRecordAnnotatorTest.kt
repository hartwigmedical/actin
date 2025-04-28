//package com.hartwig.actin.molecular.orange
//
//import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
//import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
//import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
//import com.hartwig.actin.datamodel.molecular.driver.GeneRole
//import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
//import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
//import com.hartwig.actin.datamodel.molecular.driver.Variant
//import com.hartwig.actin.datamodel.molecular.driver.VariantType
//import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
//import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
//import com.hartwig.actin.molecular.evidence.EvidenceDatabase
//import com.hartwig.actin.molecular.evidence.TestEvidenceDatabaseFactory
//import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
//import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
//import com.hartwig.actin.molecular.panel.GENE
//import com.hartwig.actin.molecular.panel.HGVS_CODING
//import io.mockk.every
//import io.mockk.mockk
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.Test
//
//private const val ALT = "T"
//private const val REF = "G"
//private const val TRANSCRIPT = "transcript"
//private const val CHROMOSOME = "1"
//private const val POSITION = 1
//private const val HGVS_PROTEIN_1LETTER = "p.M1L"
//
//private val VARIANT = Variant(
//    chromosome = CHROMOSOME,
//    position = POSITION,
//    ref = REF,
//    alt = ALT,
//    type = VariantType.SNV,
//    variantAlleleFrequency = null,
//    canonicalImpact = TranscriptVariantImpact(
//        transcriptId = TRANSCRIPT,
//        codingEffect = CodingEffect.MISSENSE,
//        hgvsCodingImpact = HGVS_CODING,
//        hgvsProteinImpact = HGVS_PROTEIN_1LETTER,
//        isSpliceRegion = false,
//        affectedExon = 1,
//        affectedCodon = 1,
//        effects = emptySet()
//    ),
//    otherImpacts = emptySet(),
//    isHotspot = false,
//    isReportable = true,
//    event = "$GENE M1L",
//    driverLikelihood = null,
//    evidence = ClinicalEvidence(emptySet(), emptySet()),
//    gene = GENE,
//    geneRole = GeneRole.UNKNOWN,
//    proteinEffect = ProteinEffect.UNKNOWN,
//    isAssociatedWithDrugResistance = null
//)
//
//private val VARIANT_MATCH_CRITERIA =
//    VariantMatchCriteria(
//        gene = GENE,
//        codingEffect = CodingEffect.MISSENSE,
//        type = VariantType.SNV,
//        chromosome = CHROMOSOME,
//        position = POSITION,
//        ref = REF,
//        alt = ALT,
//        driverLikelihood = DriverLikelihood.HIGH,
//        isReportable = true,
//    )
//
//private val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()
//
//private val HOTSPOT = TestServeKnownFactory.hotspotBuilder().build()
//    .withGeneRole(com.hartwig.serve.datamodel.molecular.common.GeneRole.ONCO)
//    .withProteinEffect(com.hartwig.serve.datamodel.molecular.common.ProteinEffect.GAIN_OF_FUNCTION)
//    .withAssociatedWithDrugResistance(true)
//
//class MolecularRecordAnnotatorTest {
//
//    private val annotator = MolecularRecordAnnotator(TestEvidenceDatabaseFactory.createProperDatabase())
//
//    @Test
//    fun `Should retain characteristics during annotation that are originally present`() {
//        val annotated = annotator.annotate(TestMolecularFactory.createProperTestMolecularRecord())
//        with(annotated.characteristics) {
//            assertThat(microsatelliteStability?.evidence).isNotNull()
//            assertThat(homologousRecombination?.evidence).isNotNull()
//            assertThat(tumorMutationalBurden?.evidence).isNotNull()
//            assertThat(tumorMutationalLoad?.evidence).isNotNull()
//        }
//    }
//
//    @Test
//    fun `Should not create characteristics during annotation that are originally missing`() {
//        val annotated = annotator.annotate(TestMolecularFactory.createMinimalTestMolecularRecord())
//        with(annotated.characteristics) {
//            assertThat(microsatelliteStability).isNull()
//            assertThat(homologousRecombination).isNull()
//            assertThat(tumorMutationalBurden).isNull()
//            assertThat(tumorMutationalLoad).isNull()
//        }
//    }
//
//    @Test
//    fun `Should overwrite hotspots`() {
//        val evidenceDatabase = mockk<EvidenceDatabase> {
//            every { geneAlterationsForVariant(VARIANT_MATCH_CRITERIA.copy(driverLikelihood = null)) } returns HOTSPOT
//            every { evidenceForVariant(any()) } returns EMPTY_MATCH
//        }
//
//        val annotated = MolecularRecordAnnotator(evidenceDatabase).annotateVariant(VARIANT)
//        assertThat(annotated.isHotspot).isTrue()
//        assertThat(annotated.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
//        assertThat(annotated.proteinEffect).isEqualTo(ProteinEffect.GAIN_OF_FUNCTION)
//        assertThat(annotated.geneRole).isEqualTo(GeneRole.ONCO)
//    }
//}