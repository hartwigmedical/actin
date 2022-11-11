package com.hartwig.actin.report.interpretation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;

import org.jetbrains.annotations.NotNull;

public final class ActinEvidenceFactory {

    private ActinEvidenceFactory() {
    }

    @NotNull
    public static Multimap<String, String> create(@NotNull TreatmentMatch treatmentMatch) {
        Multimap<String, String> trialsPerInclusionEvent = ArrayListMultimap.create();

        return trialsPerInclusionEvent;
    }
}
