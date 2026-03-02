package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.orange.AminoAcid
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.finding.SmallVariant
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType
import org.apache.logging.log4j.LogManager

private const val ENSEMBL_TRANSCRIPT_IDENTIFIER: String = "ENST"

private val NONSENSE_OR_FRAMESHIFT_EFFECTS =
    setOf(PurpleVariantEffect.FRAMESHIFT, PurpleVariantEffect.START_LOST, PurpleVariantEffect.STOP_GAINED, PurpleVariantEffect.STOP_LOST)
private val SPLICE_EFFECTS = setOf(PurpleVariantEffect.SPLICE_ACCEPTOR, PurpleVariantEffect.SPLICE_DONOR)

class VariantExtractor(private val geneFilter: GeneFilter) {

    private val logger = LogManager.getLogger(VariantExtractor::class.java)

    private val relevantCodingEffects = setOf(
        PurpleCodingEffect.MISSENSE,
        PurpleCodingEffect.SPLICE,
        PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT,
        PurpleCodingEffect.SYNONYMOUS
    )

    fun extract(findings: List<SmallVariant>): List<Variant> {
        //TODO: Look at moving VariantDedup to findings
        return VariantDedup.apply(findings).filter { variant ->
            MappingUtil.includedInGeneFilter(variant, geneFilter, {
                val coding = relevantCodingEffects.contains(it.transcriptImpact().codingEffect())
                val inSpliceRegion = it.transcriptImpact().inSpliceRegion()
                (coding || inSpliceRegion)
            })
        }
            .map { variant ->
                val event = variant.event()
                Variant(
                    chromosome = variant.chromosome(),
                    position = variant.position(),
                    ref = variant.ref(),
                    alt = variant.alt(),
                    type = determineVariantType(variant),
                    variantAlleleFrequency = variant.adjustedVAF(),
                    canonicalImpact = extractCanonicalImpact(variant),
                    otherImpacts = extractOtherImpacts(variant),
                    sourceEvent = event,
                    variantCopyNumber = ExtractionUtil.keep3Digits(variant.variantCopyNumber()),
                    totalCopyNumber = ExtractionUtil.keep3Digits(variant.adjustedCopyNumber()),
                    isBiallelic = variant.biallelic(),
                    clonalLikelihood = ExtractionUtil.keep3Digits(1 - variant.subclonalLikelihood()),
                    phaseGroups = variant.localPhaseSets()?.toSet(),
                    exonSkippingIsConfirmed = false,
                    isCancerAssociatedVariant = variant.hotspot() == HotspotType.HOTSPOT,
                    isReportable = variant.isReported,
                    event = event,
                    driverLikelihood = MappingUtil.determineDriverLikelihood(variant),
                    evidence = ExtractionUtil.noEvidence(),
                    gene = variant.gene(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                )
            }.sorted()
    }

    fun determineVariantType(variant: SmallVariant): VariantType {
        return when (variant.type()) {
            PurpleVariantType.MNP -> VariantType.MNV

            PurpleVariantType.SNP -> VariantType.SNV

            PurpleVariantType.INDEL -> {
                run {
                    if (variant.ref().length > variant.alt().length) {
                        return VariantType.DELETE
                    } else if (variant.alt().length > variant.ref().length) {
                        return VariantType.INSERT
                    }
                }
                run { throw IllegalStateException("Cannot convert variant type: " + variant.type()) }
            }

            else -> throw IllegalStateException("Cannot convert variant type: " + variant.type())
        }
    }

    private fun extractCanonicalImpact(variant: SmallVariant): TranscriptVariantImpact {
        if (!isEnsemblTranscript(variant.transcriptImpact())) {
            logger.warn("Canonical impact defined on non-ensembl transcript for variant '{}'", variant)
        }
        return toTranscriptImpact(variant.transcriptImpact(), variant.gene())
    }

    private fun extractOtherImpacts(variant: SmallVariant): Set<TranscriptVariantImpact> {
        return setOf(toTranscriptImpact(variant.otherImpact(), variant.gene()))
    }

    fun isEnsemblTranscript(transcriptImpact: SmallVariant.TranscriptImpact): Boolean {
        return transcriptImpact.transcript().startsWith(ENSEMBL_TRANSCRIPT_IDENTIFIER)
    }

    private fun toTranscriptImpact(transcriptImpact: SmallVariant.TranscriptImpact, gene: String): TranscriptVariantImpact {
        val shouldAnnotateAsSpliceOverNonsenseOrFrameshift =
            shouldAnnotateAsSpliceOverNonsenseOrFrameshift(transcriptImpact.effects(), gene)

        return TranscriptVariantImpact(
            transcriptId = transcriptImpact.transcript(),
            hgvsCodingImpact = transcriptImpact.hgvsCodingImpact(),
            hgvsProteinImpact = if (shouldAnnotateAsSpliceOverNonsenseOrFrameshift) "p.?" else AminoAcid.forceSingleLetterAminoAcids(
                transcriptImpact.hgvsProteinImpact()
            ),
            affectedCodon = transcriptImpact.affectedCodon(),
            affectedExon = transcriptImpact.affectedExon(),
            inSpliceRegion = transcriptImpact.inSpliceRegion(),
            effects = toEffects(transcriptImpact.effects()),
            codingEffect = if (shouldAnnotateAsSpliceOverNonsenseOrFrameshift) CodingEffect.SPLICE else determineCodingEffect(
                transcriptImpact.codingEffect()
            )
        )
    }

    private fun shouldAnnotateAsSpliceOverNonsenseOrFrameshift(effects: Set<PurpleVariantEffect>, gene: String): Boolean =
        gene == "MET" && effects.any { it in SPLICE_EFFECTS } && effects.any { it in NONSENSE_OR_FRAMESHIFT_EFFECTS }

    private fun toEffects(effects: Set<PurpleVariantEffect>): Set<VariantEffect> {
        return effects.map { effect: PurpleVariantEffect -> determineVariantEffect(effect) }.toSet()
    }

    fun determineVariantEffect(effect: PurpleVariantEffect): VariantEffect {
        return when (effect) {
            PurpleVariantEffect.STOP_GAINED -> VariantEffect.STOP_GAINED
            PurpleVariantEffect.STOP_LOST -> VariantEffect.STOP_LOST
            PurpleVariantEffect.START_LOST -> VariantEffect.START_LOST
            PurpleVariantEffect.FRAMESHIFT -> VariantEffect.FRAMESHIFT
            PurpleVariantEffect.SPLICE_ACCEPTOR -> VariantEffect.SPLICE_ACCEPTOR
            PurpleVariantEffect.SPLICE_DONOR -> VariantEffect.SPLICE_DONOR
            PurpleVariantEffect.INFRAME_INSERTION -> VariantEffect.INFRAME_INSERTION
            PurpleVariantEffect.INFRAME_DELETION -> VariantEffect.INFRAME_DELETION
            PurpleVariantEffect.MISSENSE -> VariantEffect.MISSENSE
            PurpleVariantEffect.PHASED_MISSENSE -> VariantEffect.PHASED_MISSENSE
            PurpleVariantEffect.PHASED_INFRAME_INSERTION -> VariantEffect.PHASED_INFRAME_INSERTION
            PurpleVariantEffect.PHASED_INFRAME_DELETION -> VariantEffect.PHASED_INFRAME_DELETION
            PurpleVariantEffect.SYNONYMOUS -> VariantEffect.SYNONYMOUS
            PurpleVariantEffect.PHASED_SYNONYMOUS -> VariantEffect.PHASED_SYNONYMOUS
            PurpleVariantEffect.INTRONIC -> VariantEffect.INTRONIC
            PurpleVariantEffect.FIVE_PRIME_UTR -> VariantEffect.FIVE_PRIME_UTR
            PurpleVariantEffect.THREE_PRIME_UTR -> VariantEffect.THREE_PRIME_UTR
            PurpleVariantEffect.UPSTREAM_GENE -> VariantEffect.UPSTREAM_GENE
            PurpleVariantEffect.NON_CODING_TRANSCRIPT -> VariantEffect.NON_CODING_TRANSCRIPT
            PurpleVariantEffect.OTHER -> VariantEffect.OTHER
        }
    }

    fun determineCodingEffect(codingEffect: PurpleCodingEffect): CodingEffect? {
        return when (codingEffect) {
            PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT -> CodingEffect.NONSENSE_OR_FRAMESHIFT
            PurpleCodingEffect.SPLICE -> CodingEffect.SPLICE
            PurpleCodingEffect.MISSENSE -> CodingEffect.MISSENSE
            PurpleCodingEffect.SYNONYMOUS -> CodingEffect.SYNONYMOUS
            PurpleCodingEffect.NONE -> CodingEffect.NONE
            PurpleCodingEffect.UNDEFINED -> null
        }
    }
}