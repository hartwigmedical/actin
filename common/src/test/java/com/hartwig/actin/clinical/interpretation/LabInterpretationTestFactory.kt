package com.hartwig.actin.clinical.interpretation;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabUnit;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class LabInterpretationTestFactory {

    private LabInterpretationTestFactory() {
    }

    @NotNull
    public static ImmutableLabValue.Builder builder() {
        return ImmutableLabValue.builder()
                .date(LocalDate.of(2017, 10, 20))
                .code(Strings.EMPTY)
                .name(Strings.EMPTY)
                .comparator(Strings.EMPTY)
                .value(0D)
                .unit(LabUnit.NONE);
    }
}
