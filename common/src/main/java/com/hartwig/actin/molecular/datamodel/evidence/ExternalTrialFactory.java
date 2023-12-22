package com.hartwig.actin.molecular.datamodel.evidence;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ExternalTrialFactory {

    @NotNull
    public static ImmutableExternalTrial create(@NotNull String title, @NotNull Set<String> countries, @NotNull String website) {
        return ImmutableExternalTrial.builder().title(title).countries(countries).website(website).build();
    }
}

