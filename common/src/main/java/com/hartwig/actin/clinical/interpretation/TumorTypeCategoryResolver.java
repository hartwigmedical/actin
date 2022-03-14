package com.hartwig.actin.clinical.interpretation;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TumorTypeCategory;

import org.jetbrains.annotations.NotNull;

public final class TumorTypeCategoryResolver {

    private static final String DELIMITER = ", ";

    private TumorTypeCategoryResolver() {
    }

    @NotNull
    public static Set<TumorTypeCategory> fromStringList(@NotNull String categoryStringList) {
        Set<TumorTypeCategory> categories = Sets.newTreeSet();
        for (String categoryString : categoryStringList.split(DELIMITER)) {
            categories.add(fromString(categoryString));
        }
        return categories;
    }

    @NotNull
    public static TumorTypeCategory fromString(@NotNull String categoryString) {
        return TumorTypeCategory.valueOf(categoryString.trim().replaceAll(" ", "_").toUpperCase());
    }

    @NotNull
    public static String toStringList(@NotNull Set<TumorTypeCategory> categories) {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (TumorTypeCategory category : categories) {
            joiner.add(toString(category));
        }
        return joiner.toString();
    }

    @NotNull
    public static String toString(@NotNull TumorTypeCategory category) {
        String string = category.toString().replaceAll("_", " ");
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}