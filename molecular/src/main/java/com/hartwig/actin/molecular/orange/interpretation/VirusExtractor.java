package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.VirusComparator;
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus;
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus;
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation;
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterData;
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class VirusExtractor {

    static final VirusBreakendQCStatus QC_PASS_STATUS = VirusBreakendQCStatus.NO_ABNORMALITIES;

    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public VirusExtractor(@NotNull final EvidenceDatabase evidenceDatabase) {
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public Set<Virus> extract(@NotNull VirusInterpreterData virusInterpreter) {
        Set<Virus> viruses = Sets.newTreeSet(new VirusComparator());
        for (AnnotatedVirus virus : virusInterpreter.allViruses()) {
            viruses.add(ImmutableVirus.builder()
                    .isReportable(virus.reported())
                    .event(DriverEventFactory.virusEvent(virus))
                    .driverLikelihood(determineDriverLikelihood(virus.virusDriverLikelihoodType()))
                    .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVirus(virus)))
                    .name(virus.name())
                    .isReliable(virus.qcStatus() == QC_PASS_STATUS)
                    .type(determineType(virus.interpretation()))
                    .integrations(virus.integrations())
                    .build());
        }
        return viruses;
    }

    @Nullable
    @VisibleForTesting
    static DriverLikelihood determineDriverLikelihood(@NotNull VirusLikelihoodType driverLikelihood) {
        switch (driverLikelihood) {
            case HIGH: {
                return DriverLikelihood.HIGH;
            }
            case LOW: {
                return DriverLikelihood.LOW;
            }
            case UNKNOWN: {
                return null;
            }
            default: {
                throw new IllegalStateException(
                        "Cannot determine driver likelihood type for virus driver likelihood: " + driverLikelihood);
            }
        }
    }

    @NotNull
    @VisibleForTesting
    static VirusType determineType(@Nullable VirusInterpretation interpretation) {
        if (interpretation == null) {
            return VirusType.OTHER;
        }

        switch (interpretation) {
            case MCV: {
                return VirusType.MERKEL_CELL_VIRUS;
            }
            case EBV: {
                return VirusType.EPSTEIN_BARR_VIRUS;
            }
            case HPV: {
                return VirusType.HUMAN_PAPILLOMA_VIRUS;
            }
            case HBV: {
                return VirusType.HEPATITIS_B_VIRUS;
            }
            case HHV8: {
                return VirusType.HUMAN_HERPES_VIRUS_8;
            }
            default: {
                throw new IllegalStateException("Cannot determine virus type for interpretation: " + interpretation);
            }
        }
    }
}
