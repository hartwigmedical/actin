package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusQCStatus;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.VirusComparator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class VirusExtractor {

    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public VirusExtractor(@NotNull final EvidenceDatabase evidenceDatabase) {
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public Set<Virus> extract(@NotNull VirusInterpreterRecord virusInterpreter) {
        Set<Virus> viruses = Sets.newTreeSet(new VirusComparator());
        for (VirusInterpreterEntry virus : virusInterpreter.entries()) {
            viruses.add(ImmutableVirus.builder()
                    .isReportable(virus.reported())
                    .event(DriverEventFactory.virusEvent(virus))
                    .driverLikelihood(determineDriverLikelihood(virus))
                    .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForVirus(virus)))
                    .name(virus.name())
                    .isReliable(virus.qcStatus() == VirusQCStatus.NO_ABNORMALITIES)
                    .type(determineType(virus.interpretation()))
                    .integrations(virus.integrations())
                    .build());
        }
        return viruses;
    }

    @NotNull
    @VisibleForTesting
    static DriverLikelihood determineDriverLikelihood(@NotNull VirusInterpreterEntry virus) {
        switch (virus.driverLikelihood()) {
            case HIGH: {
                return DriverLikelihood.HIGH;
            }
            case LOW:
            case UNKNOWN: {
                return DriverLikelihood.LOW;
            }
            default: {
                throw new IllegalStateException(
                        "Cannot determine driver likelihood type for virus driver likelihood: " + virus.driverLikelihood());
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
                return VirusType.MCV;
            }
            case EBV: {
                return VirusType.EBV;
            }
            case HPV: {
                return VirusType.HPV;
            }
            case HBV: {
                return VirusType.HBV;
            }
            case HHV8: {
                return VirusType.HHV8;
            }
            default: {
                throw new IllegalStateException("Cannot determine virus type for interpretation: " + interpretation);
            }
        }
    }
}
