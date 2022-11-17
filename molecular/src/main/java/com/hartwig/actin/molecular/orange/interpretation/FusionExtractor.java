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
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.FusionComparator;

import org.jetbrains.annotations.NotNull;

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
        for (LinxFusion fusion : linx.fusions()) {
            fusions.add(ImmutableFusion.builder()
                    .isReportable(true)
                    .event(DriverEventFactory.fusionEvent(fusion))
                    .driverLikelihood(determineDriverLikelihood(fusion))
                    .evidence(ExtractionUtil.createEmptyEvidence())
                    .geneStart(fusion.geneStart())
                    .geneTranscriptStart(fusion.geneTranscriptStart())
                    .fusedExonUp(fusion.fusedExonUp())
                    .geneEnd(fusion.geneEnd())
                    .geneTranscriptEnd(fusion.geneTranscriptEnd())
                    .fusedExonDown(fusion.fusedExonDown())
                    .proteinEffect(ProteinEffect.UNKNOWN)
                    .isAssociatedWithDrugResistance(null)
                    .driverType(determineDriverType(fusion))
                    .build());
        }
        return fusions;
    }

    @NotNull
    @VisibleForTesting
    static FusionDriverType determineDriverType(@NotNull LinxFusion fusion) {
        switch (fusion.type()) {
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
            default: {
                throw new IllegalStateException("Cannot determine driver type for fusion of type: " + fusion.type());
            }
        }
    }

    @NotNull
    @VisibleForTesting
    static DriverLikelihood determineDriverLikelihood(@NotNull LinxFusion fusion) {
        switch (fusion.driverLikelihood()) {
            case HIGH: {
                return DriverLikelihood.HIGH;
            }
            case LOW: {
                return DriverLikelihood.LOW;
            }
            default: {
                throw new IllegalStateException(
                        "Cannot determine driver likelihood for fusion driver likelihood: " + fusion.driverLikelihood());
            }
        }
    }

}
