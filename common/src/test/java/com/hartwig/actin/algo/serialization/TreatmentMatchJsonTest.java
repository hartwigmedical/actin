package com.hartwig.actin.algo.serialization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Resources;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentMatchJsonTest {

    private static final String ALGO_DIRECTORY = Resources.getResource("algo").getPath();
    private static final String TREATMENT_MATCH_JSON = ALGO_DIRECTORY + File.separator + "sample.treatment_match.json";

    @Test
    public void canConvertBackAndForthJson() {
        TreatmentMatch minimal = TestTreatmentMatchFactory.createMinimalTreatmentMatch();
        TreatmentMatch convertedMinimal = TreatmentMatchJson.fromJson(TreatmentMatchJson.toJson(minimal));

        assertEquals(minimal, convertedMinimal);

        TreatmentMatch proper = TestTreatmentMatchFactory.createProperTreatmentMatch();
        TreatmentMatch convertedProper = TreatmentMatchJson.fromJson(TreatmentMatchJson.toJson(proper));

        assertEquals(proper, convertedProper);
    }

    @Test
    public void canReadTreatmentMatchJson() throws IOException {
        assertTreatmentMatch(TreatmentMatchJson.read(TREATMENT_MATCH_JSON));
    }

    private static void assertTreatmentMatch(@NotNull TreatmentMatch match) {
        assertEquals("ACTN01029999T", match.sampleId());
        assertEquals(1, match.trialMatches().size());

        TrialEligibility trialEligibility = match.trialMatches().get(0);
        assertEquals(1, trialEligibility.evaluations().size());
        assertEquals(3, trialEligibility.cohorts().size());
    }
}