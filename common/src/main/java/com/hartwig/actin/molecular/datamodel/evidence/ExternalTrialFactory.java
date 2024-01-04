package com.hartwig.actin.molecular.datamodel.evidence;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public final class ExternalTrialFactory {

    @NotNull
    public static ImmutableExternalTrial create(@NotNull String title, @NotNull Set<String> countries, @NotNull String url,
            @NotNull String nctId) {
        return ImmutableExternalTrial.builder().title(title).countries(countries).url(url).nctId(nctId).build();
    }
}

