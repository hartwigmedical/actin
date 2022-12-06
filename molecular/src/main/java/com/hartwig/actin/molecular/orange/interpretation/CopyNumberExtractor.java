package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableCopyNumber;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumberInterpretation;
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
        for (PurpleCopyNumber copyNumber : purple.copyNumbers()) {
            PurpleDriver driver = findCopyNumberDriver(purple.drivers(), copyNumber.gene());

            if (geneFilter.include(copyNumber.gene())) {
                String event = copyNumber.gene() + " copy-neutral";
                ActionableEvidence evidence = ImmutableActionableEvidence.builder().build();
                GeneAlteration alteration = GeneAlterationFactory.convertAlteration(copyNumber.gene(), null);
                if (driver != null) {
                    if (DEL_DRIVERS.contains(driver.type())) {
                        event = DriverEventFactory.lossEvent(copyNumber);
                        alteration = GeneAlterationFactory.convertAlteration(copyNumber.gene(),
                                evidenceDatabase.geneAlterationForLoss(copyNumber));
                        evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForLoss(copyNumber));
                    } else {
                        event = DriverEventFactory.amplificationEvent(copyNumber);
                        alteration = GeneAlterationFactory.convertAlteration(copyNumber.gene(),
                                evidenceDatabase.geneAlterationForAmplification(copyNumber));
                        evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForAmplification(copyNumber));
                    }
                }

                copyNumbers.add(ImmutableCopyNumber.builder()
                        .from(alteration)
                        .isReportable(driver != null)
                        .event(event)
                        .driverLikelihood(driver != null ? DriverLikelihood.HIGH : null)
                        .evidence(evidence)
                        .type(determineType(copyNumber.interpretation()))
                        .minCopies(copyNumber.minCopies())
                        .maxCopies(copyNumber.maxCopies())
                        .build());
            } else if (driver != null) {
                LOGGER.warn("Filtered a reported copy number event on gene {}", driver.gene());
            }
        }
        return copyNumbers;
    }

    @NotNull
    private static CopyNumberType determineType(@NotNull PurpleCopyNumberInterpretation interpretation) {
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
