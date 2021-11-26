package com.hartwig.actin.clinical.util;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public final class TreatmentCategoryDisplay {

    private static final String DELIMITER = ", ";

    private TreatmentCategoryDisplay() {
    }

    @NotNull
    public static Set<TreatmentCategory> fromString(@NotNull String categoryString) {
        Set<TreatmentCategory> categories = Sets.newTreeSet();
        for (String value : categoryString.split(DELIMITER)) {
            categories.add(TreatmentCategory.valueOf(value.trim().replaceAll(" ", "_").toUpperCase()));
        }
        return categories;
    }

    @NotNull
    public static String toString(@NotNull Set<TreatmentCategory> categories) {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (TreatmentCategory category : categories) {
            String string = category.toString().replaceAll("_", " ");
            joiner.add(string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase());
        }
        return joiner.toString();
    }
}
