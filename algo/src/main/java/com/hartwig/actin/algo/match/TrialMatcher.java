package com.hartwig.actin.algo.match;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.ImmutableSampleTreatmentMatch;
import com.hartwig.actin.algo.datamodel.SampleTreatmentMatch;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;

public final class TrialMatcher {

    private TrialMatcher() {
    }

    @NotNull
    public static SampleTreatmentMatch determineEligibility(@NotNull PatientRecord patient, @NotNull List<Trial> trials) {
        return ImmutableSampleTreatmentMatch.builder().sampleId(patient.sampleId()).build();
    }
}
