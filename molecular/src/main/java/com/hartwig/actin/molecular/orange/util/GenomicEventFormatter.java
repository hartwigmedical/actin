package com.hartwig.actin.molecular.orange.util;

import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class GenomicEventFormatter {

    private static final Map<String, String> EVENT_REPLACEMENTS = Maps.newHashMap();

    static {
        EVENT_REPLACEMENTS.put("full gain", "amp");
        EVENT_REPLACEMENTS.put("partial gain", "amp");
        EVENT_REPLACEMENTS.put("full loss", "del");
        EVENT_REPLACEMENTS.put("partial loss", "del");
        EVENT_REPLACEMENTS.put("homozygous disruption", "disruption");
        EVENT_REPLACEMENTS.put("Microsatellite unstable", "MSI");
        EVENT_REPLACEMENTS.put("High tumor mutation load", "High TML");
        EVENT_REPLACEMENTS.put(" - ", "-");
        EVENT_REPLACEMENTS.put("p\\.", Strings.EMPTY);
    }

    private GenomicEventFormatter() {
    }

    @NotNull
    public static String format(@NotNull String event) {
        String formattedEvent = event;
        if (event.contains("p.")) {
            formattedEvent = AminoAcid.forceSingleLetterAminoAcids(event);
        }

        for (Map.Entry<String, String> replacement : EVENT_REPLACEMENTS.entrySet()) {
            formattedEvent = formattedEvent.replaceAll(replacement.getKey(), replacement.getValue());
        }
        return formattedEvent;
    }
}