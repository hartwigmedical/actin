package com.hartwig.actin.molecular.orange.util;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class EventFormatter {

    private static final Map<String, String> STRING_REPLACEMENTS = Maps.newHashMap();
    private static final Set<String> STRING_REMOVALS = Sets.newHashSet();

    static {
        STRING_REPLACEMENTS.put("full gain", "amp");
        STRING_REPLACEMENTS.put("partial gain", "amp");
        STRING_REPLACEMENTS.put("full loss", "del");
        STRING_REPLACEMENTS.put("partial loss", "del");
        STRING_REPLACEMENTS.put("homozygous disruption", "disruption");
        STRING_REPLACEMENTS.put("Microsatellite unstable", "MSI");
        STRING_REPLACEMENTS.put("High tumor mutation load", "High TML");
        STRING_REPLACEMENTS.put(" - ", "-");
        STRING_REPLACEMENTS.put("p\\.\\?", "splice");

        STRING_REMOVALS.add("p\\.");
    }

    private EventFormatter() {
    }

    @NotNull
    public static String format(@NotNull String event) {
        String formattedEvent = AminoAcid.forceSingleLetterAminoAcids(event);

        for (Map.Entry<String, String> replacement : STRING_REPLACEMENTS.entrySet()) {
            formattedEvent = formattedEvent.replaceAll(replacement.getKey(), replacement.getValue());
        }

        for (String removal : STRING_REMOVALS) {
            formattedEvent = formattedEvent.replaceAll(removal, Strings.EMPTY);
        }

        return formattedEvent;
    }
}
