package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableCopyNumber;
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

    private static final Set<PurpleDriverType> COPY_NUMBER_DRIVERS =
            Sets.newHashSet(PurpleDriverType.AMP, PurpleDriverType.PARTIAL_AMP, PurpleDriverType.DEL);

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
        for (PurpleCopyNumber copyNumber : purple.copyNumbers()) {
            PurpleDriver driver = findCopyNumberDriver(purple.drivers(), copyNumber.gene());

            if (geneFilter.include(copyNumber.gene())) {
                copyNumbers.add(ImmutableCopyNumber.builder()
                        .from(GeneAlterationFactory.convertAlteration(copyNumber.gene(),
                                evidenceDatabase.geneAlterationForAmplification(copyNumber)))
                        .isReportable(driver != null)
                        .event(DriverEventFactory.amplificationEvent(copyNumber))
                        .driverLikelihood(driver != null ? DriverLikelihood.HIGH : null)
                        .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForAmplification(copyNumber)))
                        .type(copyNumber.minCopyNumber() > purple.fit().ploidy() ? CopyNumberType.FULL_GAIN : CopyNumberType.LOSS)
                        .minCopies((int) Math.round(copyNumber.minCopyNumber()))
                        .maxCopies((int) Math.round(copyNumber.maxCopyNumber()))
                        .build());
            } else if (driver != null) {
                LOGGER.warn("Filtered a reported copy number event on gene {}", driver.gene());
            }
        }
        return copyNumbers;
    }

    @Nullable
    private static PurpleDriver findCopyNumberDriver(@NotNull Set<PurpleDriver> drivers, @NotNull String geneToFind) {
        for (PurpleDriver driver : drivers) {
            if (COPY_NUMBER_DRIVERS.contains(driver.type()) && driver.gene().equals(geneToFind)) {
                return driver;
            }
        }
        return null;
    }
}
