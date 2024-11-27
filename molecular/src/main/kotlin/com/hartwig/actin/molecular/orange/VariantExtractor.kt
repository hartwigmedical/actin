package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.TranscriptImpact
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.VariantEffect
import com.hartwig.actin.datamodel.molecular.VariantType
import com.hartwig.actin.datamodel.molecular.orange.driver.ExtendedVariantDetails
import com.hartwig.actin.datamodel.molecular.sort.driver.VariantComparator
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleDriver
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleTranscriptImpact
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType
import org.apache.logging.log4j.LogManager

private const val ENSEMBL_TRANSCRIPT_IDENTIFIER: String = "ENST"

internal class VariantExtractor(private val geneFilter: GeneFilter) {

    private val logger = LogManager.getLogger(VariantExtractor::class.java)

    private val relevantCodingEffects = setOf(
        PurpleCodingEffect.MISSENSE,
        PurpleCodingEffect.SPLICE,
        PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT,
        PurpleCodingEffect.SYNONYMOUS
    )

    private val mutationDriverTypes = setOf(PurpleDriverType.MUTATION, PurpleDriverType.GERMLINE_MUTATION)

    fun extract(purple: PurpleRecord): List<Variant> {
        val drivers = DriverExtractor.relevantPurpleDrivers(purple)

        return VariantDedup.apply(relevantPurpleVariants(purple)).filter { variant ->
            val reported = variant.reported()
            val coding = relevantCodingEffects.contains(variant.canonicalImpact().codingEffect())
            val geneIncluded = geneFilter.include(variant.gene())
            if (reported && !geneIncluded) {
                throw IllegalStateException(
                    "Filtered a reported variant through gene filtering: '${DriverEventFactory.variantEvent(variant)}'."
                            + " Please make sure '${variant.gene()}' is configured as a known gene."
                )
            }
            geneIncluded && (reported || coding)
        }.map { variant ->
            val event = DriverEventFactory.variantEvent(variant)
            val driver = findBestMutationDriver(drivers, variant.gene(), variant.canonicalImpact().transcript())
            val driverLikelihood = determineDriverLikelihood(driver)
            val evidence = ClinicalEvidenceFactory.createNoEvidence()
            Variant(
                chromosome = variant.chromosome(),
                position = variant.position(),
                ref = variant.ref(),
                alt = variant.alt(),
                type = determineVariantType(variant),
                variantAlleleFrequency = variant.adjustedVAF(),
                canonicalImpact = extractCanonicalImpact(variant),
                otherImpacts = extractOtherImpacts(variant),
                extendedVariantDetails = ExtendedVariantDetails(
                    variantCopyNumber = ExtractionUtil.keep3Digits(variant.variantCopyNumber()),
                    totalCopyNumber = ExtractionUtil.keep3Digits(variant.adjustedCopyNumber()),
                    isBiallelic = variant.biallelic(),
                    phaseGroups = variant.localPhaseSets()?.toSet(),
                    clonalLikelihood = ExtractionUtil.keep3Digits(1 - variant.subclonalLikelihood()),
                ),
                isHotspot = variant.hotspot() == HotspotType.HOTSPOT,
                isReportable = variant.reported(),
                event = event,
                driverLikelihood = driverLikelihood,
                evidence = evidence,
                gene = variant.gene(),
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null,
            )
        }.toList().sortedWith(VariantComparator())
    }

    private fun relevantPurpleVariants(purple: PurpleRecord): Set<PurpleVariant> {
        return listOfNotNull(purple.allSomaticVariants(), purple.reportableGermlineVariants()).flatten().toSet()
    }

    internal fun determineVariantType(variant: PurpleVariant): VariantType {
        return when (variant.type()) {
            PurpleVariantType.MNP -> {
                VariantType.MNV
            }

            PurpleVariantType.SNP -> {
                VariantType.SNV
            }

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

            else -> {
                throw IllegalStateException("Cannot convert variant type: " + variant.type())
            }
        }
    }

    private fun determineDriverLikelihood(driver: PurpleDriver?): DriverLikelihood? {
        return DriverLikelihood.from(driver?.driverLikelihood())
    }

    private fun findBestMutationDriver(drivers: Set<PurpleDriver>, geneToFind: String, transcriptToFind: String): PurpleDriver? {
        return drivers.filter { driver ->
            val hasMutationType = mutationDriverTypes.contains(driver.type())
            val hasMatchingGeneTranscript = driver.gene() == geneToFind && driver.transcript() == transcriptToFind
            hasMutationType && hasMatchingGeneTranscript
        }.maxByOrNull(PurpleDriver::driverLikelihood)
    }

    private fun extractCanonicalImpact(variant: PurpleVariant): TranscriptImpact {
        if (!isEnsemblTranscript(variant.canonicalImpact())) {
            logger.warn("Canonical impact defined on non-ensembl transcript for variant '{}'", variant)
        }
        return toTranscriptImpact(variant.canonicalImpact())
    }

    private fun extractOtherImpacts(variant: PurpleVariant): Set<TranscriptImpact> {
        return variant.otherImpacts()
            .filter { purpleTranscriptImpact: PurpleTranscriptImpact -> isEnsemblTranscript(purpleTranscriptImpact) }
            .map { purpleTranscriptImpact: PurpleTranscriptImpact -> toTranscriptImpact(purpleTranscriptImpact) }
            .toSet()
    }

    internal fun isEnsemblTranscript(purpleTranscriptImpact: PurpleTranscriptImpact): Boolean {
        return purpleTranscriptImpact.transcript().startsWith(ENSEMBL_TRANSCRIPT_IDENTIFIER)
    }

    private fun toTranscriptImpact(purpleTranscriptImpact: PurpleTranscriptImpact): TranscriptImpact {
        return TranscriptImpact(
            transcriptId = purpleTranscriptImpact.transcript(),
            hgvsCodingImpact = purpleTranscriptImpact.hgvsCodingImpact(),
            hgvsProteinImpact = AminoAcid.forceSingleLetterAminoAcids(purpleTranscriptImpact.hgvsProteinImpact()),
            affectedCodon = purpleTranscriptImpact.affectedCodon(),
            affectedExon = purpleTranscriptImpact.affectedExon(),
            isSpliceRegion = purpleTranscriptImpact.inSpliceRegion(),
            effects = toEffects(purpleTranscriptImpact.effects()),
            codingEffect = determineCodingEffect(purpleTranscriptImpact.codingEffect())
        )
    }

    private fun toEffects(effects: Set<PurpleVariantEffect>): Set<VariantEffect> {
        return effects.map { effect: PurpleVariantEffect -> determineVariantEffect(effect) }.toSet()
    }

    internal fun determineVariantEffect(effect: PurpleVariantEffect): VariantEffect {
        return when (effect) {
            PurpleVariantEffect.STOP_GAINED -> {
                VariantEffect.STOP_GAINED
            }

            PurpleVariantEffect.STOP_LOST -> {
                VariantEffect.STOP_LOST
            }

            PurpleVariantEffect.START_LOST -> {
                VariantEffect.START_LOST
            }

            PurpleVariantEffect.FRAMESHIFT -> {
                VariantEffect.FRAMESHIFT
            }

            PurpleVariantEffect.SPLICE_ACCEPTOR -> {
                VariantEffect.SPLICE_ACCEPTOR
            }

            PurpleVariantEffect.SPLICE_DONOR -> {
                VariantEffect.SPLICE_DONOR
            }

            PurpleVariantEffect.INFRAME_INSERTION -> {
                VariantEffect.INFRAME_INSERTION
            }

            PurpleVariantEffect.INFRAME_DELETION -> {
                VariantEffect.INFRAME_DELETION
            }

            PurpleVariantEffect.MISSENSE -> {
                VariantEffect.MISSENSE
            }

            PurpleVariantEffect.PHASED_MISSENSE -> {
                VariantEffect.PHASED_MISSENSE
            }

            PurpleVariantEffect.PHASED_INFRAME_INSERTION -> {
                VariantEffect.PHASED_INFRAME_INSERTION
            }

            PurpleVariantEffect.PHASED_INFRAME_DELETION -> {
                VariantEffect.PHASED_INFRAME_DELETION
            }

            PurpleVariantEffect.SYNONYMOUS -> {
                VariantEffect.SYNONYMOUS
            }

            PurpleVariantEffect.PHASED_SYNONYMOUS -> {
                VariantEffect.PHASED_SYNONYMOUS
            }

            PurpleVariantEffect.INTRONIC -> {
                VariantEffect.INTRONIC
            }

            PurpleVariantEffect.FIVE_PRIME_UTR -> {
                VariantEffect.FIVE_PRIME_UTR
            }

            PurpleVariantEffect.THREE_PRIME_UTR -> {
                VariantEffect.THREE_PRIME_UTR
            }

            PurpleVariantEffect.UPSTREAM_GENE -> {
                VariantEffect.UPSTREAM_GENE
            }

            PurpleVariantEffect.NON_CODING_TRANSCRIPT -> {
                VariantEffect.NON_CODING_TRANSCRIPT
            }

            PurpleVariantEffect.OTHER -> {
                VariantEffect.OTHER
            }

            else -> {
                throw IllegalStateException("Could not convert purple variant effect: $effect")
            }
        }
    }

    internal fun determineCodingEffect(codingEffect: PurpleCodingEffect): CodingEffect? {
        return when (codingEffect) {
            PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT -> {
                CodingEffect.NONSENSE_OR_FRAMESHIFT
            }

            PurpleCodingEffect.SPLICE -> {
                CodingEffect.SPLICE
            }

            PurpleCodingEffect.MISSENSE -> {
                CodingEffect.MISSENSE
            }

            PurpleCodingEffect.SYNONYMOUS -> {
                CodingEffect.SYNONYMOUS
            }

            PurpleCodingEffect.NONE -> {
                CodingEffect.NONE
            }

            PurpleCodingEffect.UNDEFINED -> {
                null
            }

            else -> {
                throw IllegalStateException("Could not convert purple coding effect: $codingEffect")
            }
        }
    }
}