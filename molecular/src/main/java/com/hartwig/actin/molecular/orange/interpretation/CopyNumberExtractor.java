package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableCopyNumber;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriverType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CopyNumberExtractor {

    private static final Set<PurpleDriverType> AMP_DRIVERS = Sets.newHashSet(PurpleDriverType.AMP, PurpleDriverType.PARTIAL_AMP);
    private static final Set<PurpleDriverType> DEL_DRIVERS = Sets.newHashSet(PurpleDriverType.DEL);

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public CopyNumberExtractor(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceDatabase evidenceDatabase) {
        this.geneFilter = geneFilter;
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public Set<CopyNumber> extract(@NotNull PurpleRecord purple) {
        Set<CopyNumber> copyNumbers = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleGainLoss gainLoss : purple.gainsLosses()) {
            PurpleDriver driver = findCopyNumberDriver(purple.drivers(), gainLoss.gene());
            String event = DriverEventFactory.gainLossEvent(gainLoss);

            if (geneFilter.include(gainLoss.gene())) {
                copyNumbers.add(ImmutableCopyNumber.builder()
                        .from(GeneAlterationFactory.convertAlteration(gainLoss.gene(),
                                evidenceDatabase.geneAlterationForCopyNumber(gainLoss)))
                        .isReportable(driver != null)
                        .event(event)
                        .driverLikelihood(driver != null ? DriverLikelihood.HIGH : null)
                        .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForCopyNumber(gainLoss)))
                        .type(determineType(gainLoss.interpretation()))
                        .minCopies(gainLoss.minCopies())
                        .maxCopies(gainLoss.maxCopies())
                        .build());
            } else if (driver != null) {
                throw new IllegalStateException(
                        "Filtered a reported copy number through gene filtering: '" + event + "'. Please make sure '" + gainLoss.gene()
                                + "' is configured as a known gene.");
            }
        }
        return copyNumbers;
    }

    @NotNull
    @VisibleForTesting
    static CopyNumberType determineType(@NotNull PurpleGainLossInterpretation interpretation) {
        switch (interpretation) {
            case FULL_GAIN: {
                return CopyNumberType.FULL_GAIN;
            }
            case PARTIAL_GAIN: {
                return CopyNumberType.PARTIAL_GAIN;
            }
            case FULL_LOSS:
            case PARTIAL_LOSS: {
                return CopyNumberType.LOSS;
            }
            default: {
                throw new IllegalStateException("Could not determine copy number type for purple interpretation: " + interpretation);
            }
        }
    }

    @Nullable
    private static PurpleDriver findCopyNumberDriver(@NotNull Set<PurpleDriver> drivers, @NotNull String geneToFind) {
        for (PurpleDriver driver : drivers) {
            if ((DEL_DRIVERS.contains(driver.type()) || AMP_DRIVERS.contains(driver.type())) && driver.gene().equals(geneToFind)) {
                return driver;
            }
        }
        return null;
    }
}
