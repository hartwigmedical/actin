package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class LesionDataTest {

    @Test
    public void shouldReturnNullForEmptyResponse() {
        assertResultForInput("", "", null, null);
    }

    @Test
    public void shouldReturnNullForUnknownResponse() {
        assertResultForInput("unknown", "n.v.t.", null, null);
    }

    @Test
    public void shouldReturnTrueForAffirmativeResponseAndAccuratelyReportIfActive() {
        assertResultForInput("YES", "yes", true, true);
        assertResultForInput("YES", "no", true, false);
        assertResultForInput("YES", "unknown ", true, null);
        assertResultForInput("YES", "", true, null);
    }

    @Test
    public void shouldReturnFalseForNegativeResponseAndReportInactiveIfProvided() {
        assertResultForInput("NO", "no", false, false);
        assertResultForInput("NO", "unknown", false, null);
        assertResultForInput("NO", "", false, null);
    }

    private void assertResultForInput(@NotNull String presentInput, @NotNull String activeInput, @Nullable Boolean present,
            @Nullable Boolean active) {
        LesionData lesionData = LesionData.fromString(presentInput, activeInput);
        assertEquals(present, lesionData.present());
        assertEquals(active, lesionData.active());
    }
}
