package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriverType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CopyNumberExtractor {

    private static final Logger LOGGER = LogManager.getLogger(CopyNumberExtractor.class);

    private static final double AMP_RELATIVE_INCREASE_CUTOFF = 3;
    private static final double DEL_RELATIVE_INCREASE_CUTOFF = 0.5;

    private static final Set<PurpleDriverType> AMP_DRIVERS = Sets.newHashSet(PurpleDriverType.AMP, PurpleDriverType.PARTIAL_AMP);
    private static final Set<PurpleDriverType> LOSS_DRIVERS = Sets.newHashSet(PurpleDriverType.DEL);

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
            double minRelativeIncrease = copyNumber.minCopyNumber() / purple.fit().ploidy();
            double maxRelativeIncrease = copyNumber.maxCopyNumber() / purple.fit().ploidy();

            boolean meetsMinRelativeIncrease = minRelativeIncrease >= AMP_RELATIVE_INCREASE_CUTOFF;
            boolean meetsMaxRelativeIncrease = maxRelativeIncrease >= AMP_RELATIVE_INCREASE_CUTOFF;

            PurpleDriver ampDriver = findAmpDriver(purple.drivers(), copyNumber.gene());

            if (geneFilter.include(copyNumber.gene()) && (minRelativeIncrease > 1 || meetsMaxRelativeIncrease)) {
                boolean isPartial = !meetsMinRelativeIncrease && meetsMaxRelativeIncrease;
                amplifications.add(ImmutableAmplification.builder()
                        .from(GeneAlterationFactory.convertAlteration(copyNumber.gene(),
                                evidenceDatabase.geneAlterationForAmplification(copyNumber)))
                        .isReportable(ampDriver != null)
                        .event(DriverEventFactory.amplificationEvent(copyNumber))
                        .driverLikelihood(ampDriver != null ? DriverLikelihood.HIGH : null)
                        .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForAmplification(copyNumber)))
                        .minCopies((int) Math.round(copyNumber.minCopyNumber()))
                        .maxCopies((int) Math.round(copyNumber.maxCopyNumber()))
                        .isPartial(isPartial)
                        .build());
            } else if (ampDriver != null) {
                LOGGER.warn("Filtered a reported amplification on gene {}", ampDriver.gene());
            }
        } return amplifications;
    }

    @NotNull
    public Set<Loss> extractLosses(@NotNull PurpleRecord purple) {
        Set<Loss> losses = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleCopyNumber copyNumber : purple.copyNumbers()) {
            double minRelativeIncrease = copyNumber.minCopyNumber() / purple.fit().ploidy();
            double maxRelativeIncrease = copyNumber.maxCopyNumber() / purple.fit().ploidy();

            PurpleDriver lossDriver = findLossDriver(purple.drivers(), copyNumber.gene());

            if (geneFilter.include(copyNumber.gene()) && minRelativeIncrease < 1) {
                boolean meetsMinRelativeDecrease = minRelativeIncrease <= DEL_RELATIVE_INCREASE_CUTOFF;
                boolean meetsMaxRelativeDecrease = maxRelativeIncrease <= DEL_RELATIVE_INCREASE_CUTOFF;

                boolean isPartial = meetsMinRelativeDecrease && !meetsMaxRelativeDecrease;

                losses.add(ImmutableLoss.builder()
                        .from(GeneAlterationFactory.convertAlteration(copyNumber.gene(),
                                evidenceDatabase.geneAlterationForLoss(copyNumber)))
                        .isReportable(lossDriver != null)
                        .event(DriverEventFactory.lossEvent(copyNumber))
                        .driverLikelihood(lossDriver != null ? DriverLikelihood.HIGH : null)
                        .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForLoss(copyNumber)))
                        .minCopies((int) Math.round(copyNumber.minCopyNumber()))
                        .maxCopies((int) Math.round(copyNumber.maxCopyNumber()))
                        .isPartial(isPartial)
                        .build());
            } else if (lossDriver != null) {
                LOGGER.warn("Filtered a reported loss on gene {}", lossDriver.gene());
            }
        }
        return losses;
    }

    @Nullable
    private static PurpleDriver findAmpDriver(@NotNull Set<PurpleDriver> drivers, @NotNull String geneToFind) {
        return findDriverOfType(drivers, AMP_DRIVERS, geneToFind);
    }

    @Nullable
    private static PurpleDriver findLossDriver(@NotNull Set<PurpleDriver> drivers, @NotNull String geneToFind) {
        return findDriverOfType(drivers, LOSS_DRIVERS, geneToFind);
    }

    @Nullable
    private static PurpleDriver findDriverOfType(@NotNull Set<PurpleDriver> drivers, @NotNull Set<PurpleDriverType> allowedTypes,
            @NotNull String geneToFind) {
        for (PurpleDriver driver : drivers) {
            if (allowedTypes.contains(driver.type()) && driver.gene().equals(geneToFind)) {
                return driver;
            }
        }
        return null;
    }
}
