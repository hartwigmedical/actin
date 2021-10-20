package com.hartwig.actin.clinical.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class LabInterpretationTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2020, 1, 1);

    @Test
    public void canDealWithMissingLabValues() {
        LabInterpretation interpretation = new LabInterpretation(ArrayListMultimap.create(), ArrayListMultimap.create());

        assertNull(interpretation.mostRecentRelevantDate());
        assertNull(interpretation.mostRecentByName("name"));
        assertNull(interpretation.mostRecentByName("code"));
    }

    @Test
    public void canInterpretLabValues() {
        String name = "name";
        String code = "code";

        LabInterpretation interpretation = createTestLabInterpretation(name, code);

        assertEquals(TEST_DATE, interpretation.mostRecentRelevantDate());
        assertNotNull(interpretation.mostRecentByName(name));
        assertNotNull(interpretation.mostRecentByCode(code));

        assertNull(interpretation.mostRecentByName("not a name"));
        assertNull(interpretation.mostRecentByName("not a code"));
    }

    @NotNull
    private static LabInterpretation createTestLabInterpretation(@NotNull String name, @NotNull String code) {
        Multimap<String, LabValue> labValuesByName = ArrayListMultimap.create();
        labValuesByName.put(name, builder().name(name).build());
        labValuesByName.put(name, builder().name(name).build());

        Multimap<String, LabValue> labValuesByCode = ArrayListMultimap.create();
        labValuesByCode.put(code, builder().code(code).build());
        labValuesByCode.put(code, builder().code(code).build());

        return new LabInterpretation(labValuesByName, labValuesByCode);
    }

    @NotNull
    private static ImmutableLabValue.Builder builder() {
        return ImmutableLabValue.builder()
                .date(TEST_DATE)
                .code(Strings.EMPTY)
                .name(Strings.EMPTY)
                .comparator(Strings.EMPTY)
                .value(0D)
                .unit(Strings.EMPTY);
    }
}