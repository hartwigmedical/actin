package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableTranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.VariantHotspot;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.orange.util.AminoAcid;
import com.hartwig.actin.molecular.sort.driver.VariantComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class VariantExtractor {

    private static final Logger LOGGER = LogManager.getLogger(VariantExtractor.class);

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public VariantExtractor(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceDatabase evidenceDatabase) {
        this.geneFilter = geneFilter;
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public Set<Variant> extract(@NotNull PurpleRecord purple) {
        Set<Variant> variants = Sets.newTreeSet(new VariantComparator());
        // TODO Populate "other impacts"
        // TODO Also read non-reportable variants.
        for (PurpleVariant variant : purple.variants()) {
            if (geneFilter.include(variant.gene())) {
                variants.add(ImmutableVariant.builder()
                        .from(ExtractionUtil.convertAlteration(variant.gene(), evidenceDatabase.lookupGeneAlteration(variant)))
                        .isReportable(variant.reported())
                        .event(DriverEventFactory.variantEvent(variant))
                        .driverLikelihood(determineDriverLikelihood(variant))
                        .evidence(ActionableEvidenceFactory.create(evidenceDatabase.matchToActionableEvidence(variant)))
                        .type(extractType(variant))
                        .variantCopyNumber(ExtractionUtil.keep3Digits(variant.alleleCopyNumber()))
                        .totalCopyNumber(ExtractionUtil.keep3Digits(variant.totalCopyNumber()))
                        .isBiallelic(variant.biallelic())
                        .isHotspot(variant.hotspot() == VariantHotspot.HOTSPOT)
                        .clonalLikelihood(ExtractionUtil.keep3Digits(variant.clonalLikelihood()))
                        .phaseGroup(variant.localPhaseSet())
                        .canonicalImpact(extractCanonicalImpact(variant))
                        .otherImpacts(Sets.newHashSet())
                        .build());
            } else if (variant.reported()) {
                LOGGER.warn("Filtered a reported variant on gene {}", variant.gene());
            }
        }
        return variants;
    }

    @NotNull
    @VisibleForTesting
    static VariantType extractType(@NotNull PurpleVariant variant) {
        switch (variant.type()) {
            case MNP: {
                return VariantType.MNV;
            }
            case SNP: {
                return VariantType.SNV;
            }
            case INDEL: {
                if (variant.ref().length() > variant.alt().length()) {
                    return VariantType.DELETE;
                } else if (variant.alt().length() > variant.ref().length()) {
                    return VariantType.INSERT;
                }
            }
            default: {
                throw new IllegalStateException("Cannot convert variant type: " + variant.type());
            }
        }
    }

    @NotNull
    @VisibleForTesting
    static DriverLikelihood determineDriverLikelihood(@NotNull PurpleVariant variant) {
        if (variant.driverLikelihood() >= 0.8) {
            return DriverLikelihood.HIGH;
        } else if (variant.driverLikelihood() >= 0.2) {
            return DriverLikelihood.MEDIUM;
        } else {
            return DriverLikelihood.LOW;
        }
    }

    @NotNull
    @VisibleForTesting
    static TranscriptImpact extractCanonicalImpact(@NotNull PurpleVariant variant) {
        // TODO Read splice region directly from purple rather than approximate it.
        // TODO Populate "affected codon" and "affected exon".
        return ImmutableTranscriptImpact.builder()
                .transcriptId(variant.canonicalTranscript())
                .hgvsCodingImpact(variant.canonicalHgvsCodingImpact())
                .hgvsProteinImpact(AminoAcid.forceSingleLetterAminoAcids(variant.canonicalHgvsProteinImpact()))
                .affectedCodon(null)
                .affectedExon(null)
                .isSpliceRegion(variant.canonicalCodingEffect() == PurpleCodingEffect.SPLICE)
                .effects(toEffects(variant.canonicalEffects()))
                .codingEffect(toCodingEffect(variant.canonicalCodingEffect()))
                .build();
    }

    @NotNull
    private static Set<VariantEffect> toEffects(@NotNull Set<PurpleVariantEffect> effects) {
        Set<VariantEffect> variantEffects = Sets.newHashSet();
        for (PurpleVariantEffect effect : effects) {
            variantEffects.add(toEffect(effect));
        }
        return variantEffects;
    }

    @NotNull
    @VisibleForTesting
    static VariantEffect toEffect(@NotNull PurpleVariantEffect effect) {
        switch (effect) {
            case STOP_GAINED: {
                return VariantEffect.STOP_GAINED;
            }
            case STOP_LOST: {
                return VariantEffect.STOP_LOST;
            }
            case START_LOST: {
                return VariantEffect.START_LOST;
            }
            case FRAMESHIFT: {
                return VariantEffect.FRAMESHIFT;
            }
            case SPLICE_ACCEPTOR: {
                return VariantEffect.SPLICE_ACCEPTOR;
            }
            case SPLICE_DONOR: {
                return VariantEffect.SPLICE_DONOR;
            }
            case INFRAME_INSERTION: {
                return VariantEffect.INFRAME_INSERTION;
            }
            case INFRAME_DELETION: {
                return VariantEffect.INFRAME_DELETION;
            }
            case MISSENSE: {
                return VariantEffect.MISSENSE;
            }
            case PHASED_INFRAME_INSERTION: {
                return VariantEffect.PHASED_INFRAME_INSERTION;
            }
            case PHASED_INFRAME_DELETION: {
                return VariantEffect.PHASED_INFRAME_DELETION;
            }
            case SYNONYMOUS: {
                return VariantEffect.SYNONYMOUS;
            }
            case INTRONIC: {
                return VariantEffect.INTRONIC;
            }
            case FIVE_PRIME_UTR: {
                return VariantEffect.FIVE_PRIME_UTR;
            }
            case THREE_PRIME_UTR: {
                return VariantEffect.THREE_PRIME_UTR;
            }
            case UPSTREAM_GENE: {
                return VariantEffect.UPSTREAM_GENE;
            }
            case NON_CODING_TRANSCRIPT: {
                return VariantEffect.NON_CODING_TRANSCRIPT;
            }
            case OTHER: {
                return VariantEffect.OTHER;
            }
            default: {
                throw new IllegalStateException("Could not convert purple variant effect: " + effect);
            }
        }
    }

    @Nullable
    private static CodingEffect toCodingEffect(@NotNull PurpleCodingEffect codingEffect) {
        switch (codingEffect) {
            case NONSENSE_OR_FRAMESHIFT: {
                return CodingEffect.NONSENSE_OR_FRAMESHIFT;
            }
            case SPLICE: {
                return CodingEffect.SPLICE;
            }
            case MISSENSE: {
                return CodingEffect.MISSENSE;
            }
            case SYNONYMOUS: {
                return CodingEffect.SYNONYMOUS;
            }
            case NONE: {
                return CodingEffect.NONE;
            }
            case UNDEFINED: {
                return null;
            }
            default: {
                throw new IllegalStateException("Could not convert purple coding effect: " + codingEffect);
            }
        }
    }
}
