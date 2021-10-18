package com.hartwig.actin.clinical.feed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.util.TsvUtil;

import org.jetbrains.annotations.NotNull;

class FeedFileReader<T extends FeedEntry> {

    private static final String DELIMITER = "\t";

    private final FeedEntryCreator<T> feedEntryCreator;

    public FeedFileReader(final FeedEntryCreator<T> feedEntryCreator) {
        this.feedEntryCreator = feedEntryCreator;
    }

    @NotNull
    public List<T> read(@NotNull String feedTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(feedTsv).toPath());

        Map<String, Integer> fieldIndexMap = TsvUtil.createFieldIndexMap(splitFeedLine(lines.get(0)));
        List<T> entries = Lists.newArrayList();
        if (lines.size() > 1) {
            StringBuilder curLine = new StringBuilder(lines.get(1));
            for (String line : lines.subList(2, lines.size())) {
                // Entries appear on multiple lines in case they contain hard line breaks so need to split and append to the end.
                if (splitFeedLine(line).length != fieldIndexMap.size()) {
                    // Need to remove all DELIMITER since we split further down the track.
                    curLine.append("\n").append(line.replaceAll(DELIMITER, ""));
                } else {
                    addToEntries(entries, fieldIndexMap, curLine.toString());
                    curLine = new StringBuilder(line);
                }
            }
            // Need to add the final accumulated entry
            addToEntries(entries, fieldIndexMap, curLine.toString());
        }

        return entries;
    }

    @NotNull
    private static String[] splitFeedLine(@NotNull String line) {
        return cleanQuotes(line.split(DELIMITER));
    }

    @NotNull
    @VisibleForTesting
    static String[] cleanQuotes(@NotNull String[] inputs) {
        String[] cleaned = new String[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            cleaned[i] = cleanQuotes(inputs[i]);
        }

        return cleaned;
    }

    @NotNull
    private static String cleanQuotes(@NotNull String input) {
        int firstQuote = input.indexOf("\"");
        int lastQuote = input.lastIndexOf("\"");
        String cleaned = input;
        if (firstQuote >= 0 && lastQuote >= 0 && lastQuote > firstQuote) {
            cleaned = input.substring(firstQuote + 1, lastQuote);
        }

        // Replace all double quotes with single quotes.
        return cleaned.replaceAll("\"\"", "\"");
    }

    private void addToEntries(@NotNull List<T> entries, @NotNull Map<String, Integer> fieldIndexMap, @NotNull String line) {
        String[] parts = splitFeedLine(line);
        if (!allEmpty(parts)) {
            FeedLine feedLine = new FeedLine(fieldIndexMap, parts);
            if (feedEntryCreator.isValid(feedLine)) {
                entries.add(feedEntryCreator.fromLine(feedLine));
            }
        }
    }

    private static boolean allEmpty(@NotNull String[] array) {
        for (String entry : array) {
            if (!entry.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
