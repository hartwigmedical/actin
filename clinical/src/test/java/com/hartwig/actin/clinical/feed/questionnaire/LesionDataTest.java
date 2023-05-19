package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class LesionDataTest {

    @Test
    public void shouldReturnNullForEmptyResponse() {
        List.of("Active? : Symptomatic? : ", "-Active: -Symptomatic: ", "- Active: - Symptomatic: ")
                .forEach(input -> assertResultForInput(input, null, null));
    }

    @Test
    public void shouldReturnNullForUnknownResponse() {
        assertResultForInput("unknown - Active yes/no: n.v.t. - Symptomatic yes/no: n.v.t ", null, null);
    }

    @Test
    public void shouldReturnTrueForAffirmativeResponseAndAccuratelyReportIfActive() {
        assertResultForInput("YES -Active: yes ", true, true);
        assertResultForInput("YES -Active: no ", true, false);
        assertResultForInput("YES -Active: unknown ", true, null);
        assertResultForInput("YES -Active: ", true, null);
    }

    @Test
    public void shouldReturnFalseForNegativeResponseAndReportInactiveIfProvided() {
        assertResultForInput("NO -Active: no ", false, false);
        assertResultForInput("NO -Active: unknown ", false, null);
        assertResultForInput("NO -Active: ", false, null);
    }

    private void assertResultForInput(@NotNull String input, @Nullable Boolean present, @Nullable Boolean active) {
        LesionData lesionData = LesionData.fromString(input);
        assertEquals(present, lesionData.present());
        assertEquals(active, lesionData.active());
    }
}
