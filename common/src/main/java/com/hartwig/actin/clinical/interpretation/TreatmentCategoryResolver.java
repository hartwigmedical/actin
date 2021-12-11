package com.hartwig.actin.clinical.interpretation;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public final class TreatmentCategoryResolver {

    private static final String DELIMITER = ", ";

    private TreatmentCategoryResolver() {
    }

    @NotNull
    public static Set<TreatmentCategory> fromStringList(@NotNull String categoryStringList) {
        Set<TreatmentCategory> categories = Sets.newTreeSet();
        for (String categoryString : categoryStringList.split(DELIMITER)) {
            categories.add(fromString(categoryString));
        }
        return categories;
    }

    @NotNull
    public static TreatmentCategory fromString(@NotNull String categoryString) {
        return TreatmentCategory.valueOf(categoryString.trim().replaceAll(" ", "_").toUpperCase());
    }

    @NotNull
    public static String toStringList(@NotNull Set<TreatmentCategory> categories) {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (TreatmentCategory category : categories) {
            joiner.add(toString(category));
        }
        return joiner.toString();
    }

    @NotNull
    public static String toString(@NotNull TreatmentCategory category) {
        String string = category.toString().replaceAll("_", " ");
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
