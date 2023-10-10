package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.FusionComparator;
import com.hartwig.hmftools.datamodel.linx.LinxFusion;
import com.hartwig.hmftools.datamodel.linx.LinxRecord;
import com.hartwig.serve.datamodel.fusion.KnownFusion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FusionExtractor {

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public FusionExtractor(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceDatabase evidenceDatabase) {
        this.geneFilter = geneFilter;
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public Set<Fusion> extract(@NotNull LinxRecord linx) {
        Set<Fusion> fusions = Sets.newTreeSet(new FusionComparator());
        for (LinxFusion fusion : linx.allSomaticFusions()) {
            String fusionEvent = DriverEventFactory.fusionEvent(fusion);
            if (geneFilter.include(fusion.geneStart()) || geneFilter.include(fusion.geneEnd())) {
                KnownFusion knownFusion = evidenceDatabase.lookupKnownFusion(fusion);
                fusions.add(ImmutableFusion.builder()
                        .isReportable(fusion.reported())
                        .event(fusionEvent)
                        .driverLikelihood(determineDriverLikelihood(fusion))
                        .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForFusion(fusion)))
                        .geneStart(fusion.geneStart())
                        .geneTranscriptStart(fusion.geneTranscriptStart())
                        .fusedExonUp(fusion.fusedExonUp())
                        .geneEnd(fusion.geneEnd())
                        .geneTranscriptEnd(fusion.geneTranscriptEnd())
                        .fusedExonDown(fusion.fusedExonDown())
                        .proteinEffect(knownFusion != null
                                ? GeneAlterationFactory.convertProteinEffect(knownFusion.proteinEffect())
                                : ProteinEffect.UNKNOWN)
                        .isAssociatedWithDrugResistance(knownFusion != null ? knownFusion.associatedWithDrugResistance() : null)
                        .driverType(determineDriverType(fusion))
                        .build());
            } else if (fusion.reported()) {
                throw new IllegalStateException(
                        "Filtered a reported fusion through gene filtering: '" + fusionEvent + "'. Please make sure either '"
                                + fusion.geneStart() + "' or '" + fusion.geneEnd() + "' is configured as a known gene.");
            }
        }
        return fusions;
    }

    @NotNull
    @VisibleForTesting
    static FusionDriverType determineDriverType(@NotNull LinxFusion fusion) {
        switch (fusion.reportedType()) {
            case PROMISCUOUS_3: {
                return FusionDriverType.PROMISCUOUS_3;
            }
            case PROMISCUOUS_5: {
                return FusionDriverType.PROMISCUOUS_5;
            }
            case PROMISCUOUS_BOTH: {
                return FusionDriverType.PROMISCUOUS_BOTH;
            }
            case IG_PROMISCUOUS: {
                return FusionDriverType.PROMISCUOUS_IG;
            }
            case KNOWN_PAIR: {
                return FusionDriverType.KNOWN_PAIR;
            }
            case IG_KNOWN_PAIR: {
                return FusionDriverType.KNOWN_PAIR_IG;
            }
            case EXON_DEL_DUP: {
                return FusionDriverType.KNOWN_PAIR_DEL_DUP;
            }
            case PROMISCUOUS_ENHANCER_TARGET: {
                return FusionDriverType.PROMISCUOUS_ENHANCER_TARGET;
            }
            case NONE: {
                return FusionDriverType.NONE;
            }
            default: {
                throw new IllegalStateException("Cannot determine driver type for fusion of type: " + fusion.reportedType());
            }
        }
    }

    @Nullable
    @VisibleForTesting
    static DriverLikelihood determineDriverLikelihood(@NotNull LinxFusion fusion) {
        switch (fusion.likelihood()) {
            case HIGH: {
                return DriverLikelihood.HIGH;
            }
            case LOW: {
                return DriverLikelihood.LOW;
            }
            case NA: {
                return null;
            }
            default: {
                throw new IllegalStateException("Cannot determine driver likelihood for fusion driver likelihood: " + fusion.likelihood());
            }
        }
    }
}
