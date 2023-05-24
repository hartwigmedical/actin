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
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriverType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleHotspotType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.VariantComparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class VariantExtractor {

    private static final Set<PurpleCodingEffect> RELEVANT_CODING_EFFECTS = Sets.newHashSet(PurpleCodingEffect.MISSENSE,
            PurpleCodingEffect.SPLICE,
            PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT,
            PurpleCodingEffect.SYNONYMOUS);

    private static final Set<PurpleDriverType> MUTATION_DRIVER_TYPES =
            Sets.newHashSet(PurpleDriverType.MUTATION, PurpleDriverType.GERMLINE_MUTATION);

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
        for (PurpleVariant variant : VariantDedup.apply(purple.variants())) {
            boolean reportedOrCoding = variant.reported() || RELEVANT_CODING_EFFECTS.contains(variant.canonicalImpact().codingEffect());
            String event = DriverEventFactory.variantEvent(variant);
            if (geneFilter.include(variant.gene()) && reportedOrCoding) {
                PurpleDriver driver = findBestMutationDriver(purple.drivers(), variant.gene(), variant.canonicalImpact().transcript());
                DriverLikelihood driverLikelihood = determineDriverLikelihood(driver);

                ActionableEvidence evidence;
                if (driverLikelihood == DriverLikelihood.HIGH) {
                    evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVariant(variant));
                } else {
                    evidence = ActionableEvidenceFactory.createNoEvidence();
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
                        .isHotspot(variant.hotspot() == PurpleHotspotType.HOTSPOT)
                        .clonalLikelihood(ExtractionUtil.keep3Digits(1 - variant.subclonalLikelihood()))
                        .phaseGroups(variant.localPhaseSets())
                        .canonicalImpact(extractCanonicalImpact(variant))
                        .otherImpacts(extractOtherImpacts(variant))
                        .build());
            } else if (variant.reported()) {
                throw new IllegalStateException(
                        "Filtered a reported variant through gene filtering: '" + event + "'. Please make sure '" + variant.gene()
                                + "' is configured as a known gene.");
            }
        }
        return variants;
    }

    @NotNull
    @VisibleForTesting
    static VariantType determineVariantType(@NotNull PurpleVariant variant) {
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

    @Nullable
    @VisibleForTesting
    static DriverLikelihood determineDriverLikelihood(@Nullable PurpleDriver driver) {
        if (driver == null) {
            return null;
        }

        if (driver.driverLikelihood() >= 0.8) {
            return DriverLikelihood.HIGH;
        } else if (driver.driverLikelihood() >= 0.2) {
            return DriverLikelihood.MEDIUM;
        } else {
            return DriverLikelihood.LOW;
        }
    }

    @Nullable
    private static PurpleDriver findBestMutationDriver(@NotNull Set<PurpleDriver> drivers, @NotNull String geneToFind,
            @NotNull String transcriptToFind) {
        PurpleDriver best = null;
        for (PurpleDriver driver : drivers) {
            boolean hasMutationType = MUTATION_DRIVER_TYPES.contains(driver.type());
            boolean hasMatchingGeneTranscript = driver.gene().equals(geneToFind) && driver.transcript().equals(transcriptToFind);
            boolean isBetter = best == null || driver.driverLikelihood() > best.driverLikelihood();
            if (hasMutationType && hasMatchingGeneTranscript && isBetter) {
                best = driver;
            }
        }
        return best;
    }

    @NotNull
    private static TranscriptImpact extractCanonicalImpact(@NotNull PurpleVariant variant) {
        return toTranscriptImpact(variant.canonicalImpact());
    }

    @NotNull
    private static Set<TranscriptImpact> extractOtherImpacts(@NotNull PurpleVariant variant) {
        Set<TranscriptImpact> impacts = Sets.newHashSet();
        for (PurpleTranscriptImpact otherImpact : variant.otherImpacts()) {
            impacts.add(toTranscriptImpact(otherImpact));
        }
        return impacts;
    }

    @NotNull
    private static TranscriptImpact toTranscriptImpact(@NotNull PurpleTranscriptImpact purpleTranscriptImpact) {
        return ImmutableTranscriptImpact.builder()
                .transcriptId(purpleTranscriptImpact.transcript())
                .hgvsCodingImpact(purpleTranscriptImpact.hgvsCodingImpact())
                .hgvsProteinImpact(AminoAcid.forceSingleLetterAminoAcids(purpleTranscriptImpact.hgvsProteinImpact()))
                .affectedCodon(purpleTranscriptImpact.affectedCodon())
                .affectedExon(purpleTranscriptImpact.affectedExon())
                .isSpliceRegion(purpleTranscriptImpact.spliceRegion())
                .effects(toEffects(purpleTranscriptImpact.effects()))
                .codingEffect(determineCodingEffect(purpleTranscriptImpact.codingEffect()))
                .build();
    }

    @NotNull
    private static Set<VariantEffect> toEffects(@NotNull Set<PurpleVariantEffect> effects) {
        Set<VariantEffect> variantEffects = Sets.newHashSet();
        for (PurpleVariantEffect effect : effects) {
            variantEffects.add(determineVariantEffect(effect));
        }
        return variantEffects;
    }

    @NotNull
    @VisibleForTesting
    static VariantEffect determineVariantEffect(@NotNull PurpleVariantEffect effect) {
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
    @VisibleForTesting
    static CodingEffect determineCodingEffect(@NotNull PurpleCodingEffect codingEffect) {
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
