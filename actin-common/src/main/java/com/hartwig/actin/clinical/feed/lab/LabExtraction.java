package com.hartwig.actin.clinical.feed.lab;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LabExtraction {

    private static final Logger LOGGER = LogManager.getLogger(LabExtraction.class);

    private LabExtraction() {
    }

    @NotNull
    public static LabValue extract(@NotNull LabEntry entry) {
        Limits limits = extractLimits(entry.referenceRangeText());
        double value = entry.valueQuantityValue();
        Boolean isOutsideRef = null;
        if (limits.lower() != null || limits.upper() != null) {
            isOutsideRef = (limits.lower() != null && value < limits.lower()) || (limits.upper() != null && value > limits.upper());
        }

        return ImmutableLabValue.builder()
                .date(entry.issued())
                .code(entry.codeCodeOriginal())
                .name(entry.codeDisplayOriginal())
                .comparator(entry.valueQuantityComparator())
                .value(value)
                .unit(entry.valueQuantityUnit())
                .refLimitLow(limits.lower())
                .refLimitUp(limits.upper())
                .isOutsideRef(isOutsideRef)
                .build();
    }

    @NotNull
    @VisibleForTesting
    static Limits extractLimits(@NotNull String referenceRangeText) {
        Double lower = null;
        Double upper = null;
        if (referenceRangeText.contains("-")) {
            String[] limits = referenceRangeText.split("-");
            lower = FeedUtil.parseDouble(limits[0].trim());
            upper = FeedUtil.parseDouble(limits[1].trim());
        } else if (referenceRangeText.contains(">")) {
            int index = referenceRangeText.indexOf(">");
            lower = FeedUtil.parseDouble(referenceRangeText.substring(index + 1).trim());
        } else if (referenceRangeText.contains("<")) {
            int index = referenceRangeText.indexOf("<");
            upper = FeedUtil.parseDouble(referenceRangeText.substring(index + 1).trim());
        } else if (!referenceRangeText.isEmpty()) {
            LOGGER.warn("Could not parse lab value referenceRangeText '{}'", referenceRangeText);
        }

        return new Limits(lower, upper);
    }

    static class Limits {

        @Nullable
        private final Double lower;
        @Nullable
        private final Double upper;

        Limits(@Nullable final Double lower, @Nullable final Double upper) {
            this.lower = lower;
            this.upper = upper;
        }

        @Nullable
        @VisibleForTesting
        Double lower() {
            return lower;
        }

        @Nullable
        @VisibleForTesting
        public Double upper() {
            return upper;
        }
    }
}
