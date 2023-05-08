package com.hartwig.actin.algo.serialization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.junit.Test;

public class TreatmentMatchJsonTest {

    private static final String ALGO_DIRECTORY = Resources.getResource("algo").getPath();
    private static final String TREATMENT_MATCH_JSON = ALGO_DIRECTORY + File.separator + "patient.treatment_match.json";

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
    public void shouldSortMessageSetsBeforeSerialization() {
        TreatmentMatch proper = TestTreatmentMatchFactory.createProperTreatmentMatch();
        TrialMatch trialMatch = proper.trialMatches().get(0);
        Map.Entry<Eligibility, Evaluation> evaluationEntry = trialMatch.evaluations().entrySet().iterator().next();

        TreatmentMatch match = ImmutableTreatmentMatch.copyOf(proper)
                .withTrialMatches(ImmutableTrialMatch.copyOf(trialMatch)
                        .withEvaluations(Map.of(evaluationEntry.getKey(),
                                ImmutableEvaluation.builder()
                                        .recoverable(false)
                                        .result(EvaluationResult.PASS)
                                        .passSpecificMessages(ImmutableSet.of("msg 2", "msg 1", "msg 3"))
                                        .build()))
                        .withCohorts());
        String expectedJson = "{\"patientId\":\"ACTN01029999\",\"sampleId\":\"ACTN01029999T\","
                + "\"referenceDate\":{\"year\":2021,\"month\":8,\"day\":2},\"referenceDateIsLive\":true,\"trialMatches\":["
                + "{\"identification\":{\"trialId\":\"Test Trial 1\",\"open\":true,\"acronym\":\"TEST-1\","
                + "\"title\":\"Example test trial 1\"},\"isPotentiallyEligible\":true,\"evaluations\":[["
                + "{\"references\":[{\"id\":\"I-01\",\"text\":\"Patient must be an adult\"}],"
                + "\"function\":{\"rule\":\"IS_AT_LEAST_X_YEARS_OLD\",\"parameters\":[]}},"
                + "{\"result\":\"PASS\",\"recoverable\":false,\"inclusionMolecularEvents\":[],\"exclusionMolecularEvents\":[],"
                + "\"passSpecificMessages\":[\"msg 1\",\"msg 2\",\"msg 3\"],\"passGeneralMessages\":[],"
                + "\"warnSpecificMessages\":[],\"warnGeneralMessages\":[],\"undeterminedSpecificMessages\":[],\"undeterminedGeneralMessages\":[],"
                + "\"failSpecificMessages\":[],\"failGeneralMessages\":[]}]],\"cohorts\":[]}]}";
        assertEquals(expectedJson, TreatmentMatchJson.toJson(match));
    }

    @Test
    public void canReadTreatmentMatchJson() throws IOException {
        TreatmentMatch match = TreatmentMatchJson.read(TREATMENT_MATCH_JSON);

        assertEquals("ACTN01029999", match.patientId());
        assertEquals(1, match.trialMatches().size());

        TrialMatch trialMatch = match.trialMatches().get(0);
        assertEquals(1, trialMatch.evaluations().size());
        assertEquals(3, trialMatch.cohorts().size());
    }
}