package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.finding.datamodel.SmallVariant
import org.apache.logging.log4j.LogManager

private const val ENSEMBL_TRANSCRIPT_IDENTIFIER: String = "ENST"

private val NONSENSE_OR_FRAMESHIFT_EFFECTS =
    setOf(
        SmallVariant.VariantEffect.FRAMESHIFT,
        SmallVariant.VariantEffect.START_LOST,
        SmallVariant.VariantEffect.STOP_GAINED,
        SmallVariant.VariantEffect.STOP_LOST
    )
private val SPLICE_EFFECTS = setOf(SmallVariant.VariantEffect.SPLICE_ACCEPTOR, SmallVariant.VariantEffect.SPLICE_DONOR)

class VariantExtractor(private val geneFilter: GeneFilter) {

    private val logger = LogManager.getLogger(VariantExtractor::class.java)

    private val relevantCodingEffects = setOf(
        SmallVariant.CodingEffect.MISSENSE,
        SmallVariant.CodingEffect.SPLICE,
        SmallVariant.CodingEffect.NONSENSE_OR_FRAMESHIFT,
        SmallVariant.CodingEffect.SYNONYMOUS
    )

    fun extract(findings: List<SmallVariant>): List<Variant> {
        //TODO: Look at moving VariantDedup to findings
        return VariantDedup.apply(findings).filter { variant ->
            MappingUtil.includedInGeneFilter(variant, geneFilter, {
                val coding = relevantCodingEffects.contains(it.transcriptImpact().codingEffect())
                val inSpliceRegion = it.transcriptImpact().inSpliceRegion()
                (coding || inSpliceRegion)
            })
        }.map { variant ->
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
                isCancerAssociatedVariant = variant.hotspot() == SmallVariant.HotspotType.HOTSPOT,
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
            SmallVariant.VariantType.MNP -> VariantType.MNV

            SmallVariant.VariantType.SNP -> VariantType.SNV

            SmallVariant.VariantType.INDEL -> {
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
        val otherImpact = variant.otherImpact
        return if (otherImpact == null) {
            emptySet()
        } else {
            setOf(toTranscriptImpact(otherImpact, variant.gene()))
        }
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

    private fun shouldAnnotateAsSpliceOverNonsenseOrFrameshift(effects: Set<SmallVariant.VariantEffect>, gene: String): Boolean =
        gene == "MET" && effects.any { it in SPLICE_EFFECTS } && effects.any { it in NONSENSE_OR_FRAMESHIFT_EFFECTS }

    private fun toEffects(effects: Set<SmallVariant.VariantEffect>): Set<VariantEffect> {
        return effects.map { effect: SmallVariant.VariantEffect -> determineVariantEffect(effect) }.toSet()
    }

    fun determineVariantEffect(effect: SmallVariant.VariantEffect): VariantEffect {
        return when (effect) {
            SmallVariant.VariantEffect.STOP_GAINED -> VariantEffect.STOP_GAINED
            SmallVariant.VariantEffect.STOP_LOST -> VariantEffect.STOP_LOST
            SmallVariant.VariantEffect.START_LOST -> VariantEffect.START_LOST
            SmallVariant.VariantEffect.FRAMESHIFT -> VariantEffect.FRAMESHIFT
            SmallVariant.VariantEffect.SPLICE_ACCEPTOR -> VariantEffect.SPLICE_ACCEPTOR
            SmallVariant.VariantEffect.SPLICE_DONOR -> VariantEffect.SPLICE_DONOR
            SmallVariant.VariantEffect.INFRAME_INSERTION -> VariantEffect.INFRAME_INSERTION
            SmallVariant.VariantEffect.INFRAME_DELETION -> VariantEffect.INFRAME_DELETION
            SmallVariant.VariantEffect.MISSENSE -> VariantEffect.MISSENSE
            SmallVariant.VariantEffect.PHASED_MISSENSE -> VariantEffect.PHASED_MISSENSE
            SmallVariant.VariantEffect.PHASED_INFRAME_INSERTION -> VariantEffect.PHASED_INFRAME_INSERTION
            SmallVariant.VariantEffect.PHASED_INFRAME_DELETION -> VariantEffect.PHASED_INFRAME_DELETION
            SmallVariant.VariantEffect.SYNONYMOUS -> VariantEffect.SYNONYMOUS
            SmallVariant.VariantEffect.PHASED_SYNONYMOUS -> VariantEffect.PHASED_SYNONYMOUS
            SmallVariant.VariantEffect.INTRONIC -> VariantEffect.INTRONIC
            SmallVariant.VariantEffect.FIVE_PRIME_UTR -> VariantEffect.FIVE_PRIME_UTR
            SmallVariant.VariantEffect.THREE_PRIME_UTR -> VariantEffect.THREE_PRIME_UTR
            SmallVariant.VariantEffect.UPSTREAM_GENE -> VariantEffect.UPSTREAM_GENE
            SmallVariant.VariantEffect.NON_CODING_TRANSCRIPT -> VariantEffect.NON_CODING_TRANSCRIPT
            SmallVariant.VariantEffect.OTHER -> VariantEffect.OTHER
        }
    }

    fun determineCodingEffect(codingEffect: SmallVariant.CodingEffect): CodingEffect? {
        return when (codingEffect) {
            SmallVariant.CodingEffect.NONSENSE_OR_FRAMESHIFT -> CodingEffect.NONSENSE_OR_FRAMESHIFT
            SmallVariant.CodingEffect.SPLICE -> CodingEffect.SPLICE
            SmallVariant.CodingEffect.MISSENSE -> CodingEffect.MISSENSE
            SmallVariant.CodingEffect.SYNONYMOUS -> CodingEffect.SYNONYMOUS
            SmallVariant.CodingEffect.NONE -> CodingEffect.NONE
            SmallVariant.CodingEffect.UNDEFINED -> null
        }
    }
}