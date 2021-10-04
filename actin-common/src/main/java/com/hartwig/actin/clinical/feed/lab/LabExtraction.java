package com.hartwig.actin.clinical.feed.lab;

import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public final class LabExtraction {

    private LabExtraction() {
    }

    @NotNull
    public static LabValue extract(@NotNull LabEntry entry) {
        return ImmutableLabValue.builder()
                .date(entry.issued())
                .code(entry.codeCodeOriginal())
                .name(entry.codeDisplayOriginal())
                .comparator(entry.valueQuantityComparator())
                .value(entry.valueQuantityValue())
                .unit(entry.valueQuantityUnit())
                .refLimitLow(0D)
                .refLimitUp(0D)
                .isOutsideRef(false)
                .build();
    }
}
