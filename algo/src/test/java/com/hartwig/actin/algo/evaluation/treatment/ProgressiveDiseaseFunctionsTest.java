package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL;
import static com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class ProgressiveDiseaseFunctionsTest {

    @Test
    public void shouldContainTrueWhenPDAndStopReasonIsNull() {
        assertThat(treatmentResultedInPDOption(treatment(null, PD_LABEL))).contains(true);
    }

    @Test
    public void shouldContainTrueWhenPDAndBestResponseIsNull() {
        assertThat(treatmentResultedInPDOption(treatment(PD_LABEL, null))).contains(true);
    }

    @Test
    public void shouldBeEmptyWhenNotPDAndStopReasonIsNull() {
        assertThat(treatmentResultedInPDOption(treatment(null, "other"))).isEmpty();
    }

    @Test
    public void shouldBeEmptyWhenNotPDAndBestResponseIsNull() {
        assertThat(treatmentResultedInPDOption(treatment("other", null))).isEmpty();
    }

    @Test
    public void shouldContainTrueWhenStopReasonIsPD() {
        assertThat(treatmentResultedInPDOption(treatment(PD_LABEL, "other"))).contains(true);
    }

    @Test
    public void shouldContainTrueWhenBestResponseIsPD() {
        assertThat(treatmentResultedInPDOption(treatment("other", PD_LABEL))).contains(true);
    }

    @Test
    public void shouldContainFalseWhenStopReasonAndBestResponseAreKnownAndNotPD() {
        assertThat(treatmentResultedInPDOption(treatment("other", "something else"))).contains(false);
    }

    private static PriorTumorTreatment treatment(@Nullable String stopReason, @Nullable String bestResponse) {
        return ImmutablePriorTumorTreatment.builder()
                .name("test treatment")
                .isSystemic(true)
                .stopReason(stopReason)
                .bestResponse(bestResponse)
                .build();
    }
}