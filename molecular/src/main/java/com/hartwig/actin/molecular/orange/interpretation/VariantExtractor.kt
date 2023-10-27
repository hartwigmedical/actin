package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.ImmutableTranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.sort.driver.VariantComparator
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

internal class VariantExtractor(private val geneFilter: GeneFilter, private val evidenceDatabase: EvidenceDatabase) {
    fun extract(purple: PurpleRecord): MutableSet<Variant> {
        val variants: MutableSet<Variant> = Sets.newTreeSet(VariantComparator())
        val purpleVariants = relevantPurpleVariants(purple)
        val drivers = relevantPurpleDrivers(purple)
        for (variant in VariantDedup.apply(purpleVariants)) {
            val reportedOrCoding = variant.reported() || RELEVANT_CODING_EFFECTS.contains(variant.canonicalImpact().codingEffect())
            val event = DriverEventFactory.variantEvent(variant)
            if (geneFilter.include(variant.gene()) && reportedOrCoding) {
                val driver = findBestMutationDriver(drivers, variant.gene(), variant.canonicalImpact().transcript())
                val driverLikelihood = determineDriverLikelihood(driver)
                val evidence: ActionableEvidence? = if (driverLikelihood == DriverLikelihood.HIGH) {
                    ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(variant))
                } else {
                    ActionableEvidenceFactory.createNoEvidence()
                }
                variants.add(ImmutableVariant.builder()
                    .from(GeneAlterationFactory.convertAlteration(variant.gene(), evidenceDatabase.geneAlterationForVariant(variant)))
                    .isReportable(variant.reported())
                    .event(event)
                    .driverLikelihood(driverLikelihood)
                    .evidence(evidence)
                    .type(determineVariantType(variant))
                    .variantCopyNumber(ExtractionUtil.keep3Digits(variant.variantCopyNumber()))
                    .totalCopyNumber(ExtractionUtil.keep3Digits(variant.adjustedCopyNumber()))
                    .isBiallelic(variant.biallelic())
                    .isHotspot(variant.hotspot() == HotspotType.HOTSPOT)
                    .clonalLikelihood(ExtractionUtil.keep3Digits(1 - variant.subclonalLikelihood()))
                    .phaseGroups(variant.localPhaseSets())
                    .canonicalImpact(extractCanonicalImpact(variant))
                    .otherImpacts(extractOtherImpacts(variant))
                    .build())
            } else check(!variant.reported()) {
                ("Filtered a reported variant through gene filtering: '" + event + "'. Please make sure '" + variant.gene()
                        + "' is configured as a known gene.")
            }
        }
        return variants
    }

    companion object {
        private val LOGGER = LogManager.getLogger(VariantExtractor::class.java)
        private const val ENSEMBL_TRANSCRIPT_IDENTIFIER: String = "ENST"
        private val RELEVANT_CODING_EFFECTS = setOf(PurpleCodingEffect.MISSENSE,
            PurpleCodingEffect.SPLICE,
            PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT,
            PurpleCodingEffect.SYNONYMOUS)
        private val MUTATION_DRIVER_TYPES = setOf(PurpleDriverType.MUTATION, PurpleDriverType.GERMLINE_MUTATION)

        @VisibleForTesting
        fun determineVariantType(variant: PurpleVariant): VariantType {
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

        @VisibleForTesting
        fun determineDriverLikelihood(driver: PurpleDriver?): DriverLikelihood? {
            if (driver == null) {
                return null
            }
            return if (driver.driverLikelihood() >= 0.8) {
                DriverLikelihood.HIGH
            } else if (driver.driverLikelihood() >= 0.2) {
                DriverLikelihood.MEDIUM
            } else {
                DriverLikelihood.LOW
            }
        }

        private fun findBestMutationDriver(drivers: Set<PurpleDriver>, geneToFind: String,
                                           transcriptToFind: String): PurpleDriver? {
            var best: PurpleDriver? = null
            for (driver in drivers) {
                val hasMutationType = MUTATION_DRIVER_TYPES.contains(driver.type())
                val hasMatchingGeneTranscript = driver.gene() == geneToFind && driver.transcript() == transcriptToFind
                val isBetter = best == null || driver.driverLikelihood() > best.driverLikelihood()
                if (hasMutationType && hasMatchingGeneTranscript && isBetter) {
                    best = driver
                }
            }
            return best
        }

        private fun extractCanonicalImpact(variant: PurpleVariant): TranscriptImpact {
            if (!isEnsemblTranscript(variant.canonicalImpact())) {
                LOGGER.warn("Canonical impact defined on non-ensembl transcript for variant '{}'", variant)
            }
            return toTranscriptImpact(variant.canonicalImpact())
        }

        private fun extractOtherImpacts(variant: PurpleVariant): Set<TranscriptImpact> {
            return variant.otherImpacts()
                .filter { purpleTranscriptImpact: PurpleTranscriptImpact -> isEnsemblTranscript(purpleTranscriptImpact) }
                .map { purpleTranscriptImpact: PurpleTranscriptImpact -> toTranscriptImpact(purpleTranscriptImpact) }
                .toSet()

        }

        @VisibleForTesting
        fun isEnsemblTranscript(purpleTranscriptImpact: PurpleTranscriptImpact): Boolean {
            return purpleTranscriptImpact.transcript().startsWith(ENSEMBL_TRANSCRIPT_IDENTIFIER)
        }

        private fun toTranscriptImpact(purpleTranscriptImpact: PurpleTranscriptImpact): TranscriptImpact {
            return ImmutableTranscriptImpact.builder()
                .transcriptId(purpleTranscriptImpact.transcript())
                .hgvsCodingImpact(purpleTranscriptImpact.hgvsCodingImpact())
                .hgvsProteinImpact(AminoAcid.forceSingleLetterAminoAcids(purpleTranscriptImpact.hgvsProteinImpact()))
                .affectedCodon(purpleTranscriptImpact.affectedCodon())
                .affectedExon(purpleTranscriptImpact.affectedExon())
                .isSpliceRegion(purpleTranscriptImpact.inSpliceRegion())
                .effects(toEffects(purpleTranscriptImpact.effects()))
                .codingEffect(determineCodingEffect(purpleTranscriptImpact.codingEffect()))
                .build()
        }

        private fun toEffects(effects: Set<PurpleVariantEffect>): Set<VariantEffect> {
            return effects.map { effect: PurpleVariantEffect -> determineVariantEffect(effect) }.toSet()
        }

        @VisibleForTesting
        fun determineVariantEffect(effect: PurpleVariantEffect): VariantEffect {
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

        @VisibleForTesting
        fun determineCodingEffect(codingEffect: PurpleCodingEffect): CodingEffect? {
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

        fun relevantPurpleVariants(purple: PurpleRecord): MutableSet<PurpleVariant> {
            val purpleVariants: MutableSet<PurpleVariant> = Sets.newHashSet()
            purpleVariants.addAll(purple.allSomaticVariants())
            val reportableGermlineVariants = purple.reportableGermlineVariants()
            if (reportableGermlineVariants != null) {
                purpleVariants.addAll(reportableGermlineVariants)
            }
            return purpleVariants
        }

        fun relevantPurpleDrivers(purple: PurpleRecord): MutableSet<PurpleDriver> {
            val purpleDrivers: MutableSet<PurpleDriver> = Sets.newHashSet()
            purpleDrivers.addAll(purple.somaticDrivers())
            val germlineDrivers = purple.germlineDrivers()
            if (germlineDrivers != null) {
                purpleDrivers.addAll(germlineDrivers)
            }
            return purpleDrivers
        }
    }
}
