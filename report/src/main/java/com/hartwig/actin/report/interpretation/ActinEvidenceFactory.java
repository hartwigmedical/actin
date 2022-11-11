package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;

import org.jetbrains.annotations.NotNull;

public final class ActinEvidenceFactory {

    private ActinEvidenceFactory() {
    }

    @NotNull
    public static Set<String> inclusionEvents(@NotNull TreatmentMatch treatmentMatch) {
        return trialsPerInclusionEvent(treatmentMatch).keySet();
    }

    @NotNull
    public static Multimap<String, String> trialsPerInclusionEvent(@NotNull TreatmentMatch treatmentMatch) {
        Multimap<String, String> trialsPerInclusionEvent = ArrayListMultimap.create();

        return trialsPerInclusionEvent;
    }
}
