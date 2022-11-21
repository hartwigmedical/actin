package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

class CopyNumberExtractor {

    private static final Logger LOGGER = LogManager.getLogger(CopyNumberExtractor.class);

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public CopyNumberExtractor(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceDatabase evidenceDatabase) {
        this.geneFilter = geneFilter;
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public Set<Amplification> extractAmplifications(@NotNull PurpleRecord purple) {
        Set<Amplification> amplifications = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleCopyNumber copyNumber : purple.copyNumbers()) {
            if (copyNumber.interpretation().isGain()) {
                if (geneFilter.include(copyNumber.gene())) {
                    boolean isPartial = copyNumber.interpretation() == CopyNumberInterpretation.PARTIAL_GAIN;
                    amplifications.add(ImmutableAmplification.builder()
                            .from(GeneAlterationFactory.convertAlteration(copyNumber.gene(),
                                    evidenceDatabase.geneAlterationForCopyNumber(copyNumber)))
                            .isReportable(copyNumber.reported())
                            .event(DriverEventFactory.copyNumberEvent(copyNumber))
                            .driverLikelihood(isPartial ? DriverLikelihood.MEDIUM : DriverLikelihood.HIGH)
                            .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber)))
                            .minCopies(copyNumber.minCopies())
                            .maxCopies(copyNumber.maxCopies())
                            .isPartial(isPartial)
                            .build());
                } else if (copyNumber.reported()) {
                    LOGGER.warn("Filtered a reported amplification on gene {}", copyNumber.gene());
                }
            }
        }
        return amplifications;
    }

    @NotNull
    public Set<Loss> extractLosses(@NotNull PurpleRecord purple) {
        Set<Loss> losses = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleCopyNumber copyNumber : purple.copyNumbers()) {
            if (copyNumber.interpretation().isLoss()) {
                if (geneFilter.include(copyNumber.gene())) {
                    losses.add(ImmutableLoss.builder()
                            .from(GeneAlterationFactory.convertAlteration(copyNumber.gene(),
                                    evidenceDatabase.geneAlterationForCopyNumber(copyNumber)))
                            .isReportable(copyNumber.reported())
                            .event(DriverEventFactory.copyNumberEvent(copyNumber))
                            .driverLikelihood(DriverLikelihood.HIGH)
                            .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(copyNumber)))
                            .minCopies(copyNumber.minCopies())
                            .maxCopies(copyNumber.maxCopies())
                            .isPartial(copyNumber.interpretation() == CopyNumberInterpretation.PARTIAL_LOSS)
                            .build());
                } else if (copyNumber.reported()) {
                    LOGGER.warn("Filtered a reported loss on gene {}", copyNumber.gene());
                }
            }
        }
        return losses;
    }
}
