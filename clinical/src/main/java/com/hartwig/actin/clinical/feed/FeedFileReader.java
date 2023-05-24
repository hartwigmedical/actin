package com.hartwig.actin.clinical.feed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

class FeedFileReader<T extends FeedEntry> {

    private static final String DELIMITER = "\t";

    @NotNull
    private final FeedEntryCreator<T> feedEntryCreator;
    private final boolean expectLineBreaks;

    @NotNull
    public static <T extends FeedEntry> FeedFileReader<T> create(@NotNull FeedEntryCreator<T> feedEntryCreator) {
        return new FeedFileReader<>(feedEntryCreator, false);
    }

    public FeedFileReader(@NotNull final FeedEntryCreator<T> feedEntryCreator, final boolean expectLineBreaks) {
        this.feedEntryCreator = feedEntryCreator;
        this.expectLineBreaks = expectLineBreaks;
    }

    @NotNull
    public List<T> read(@NotNull String feedTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(feedTsv).toPath());

        Map<String, Integer> fields = TabularFile.createFields(splitFeedLine(lines.get(0)));
        List<T> entries = Lists.newArrayList();
        if (lines.size() > 1) {
            StringBuilder curLine = new StringBuilder(lines.get(1));
            for (String line : lines.subList(2, lines.size())) {
                // Entries appear on multiple lines in case they contain hard line breaks so append to the end.
                if (!hasExpectedFields(line, fields)) {
                    String lineToAppend = expectLineBreaks ? line.replaceAll(DELIMITER, "") : line;
                    if (expectLineBreaks) {
                        curLine.append("\n").append(lineToAppend);
                    } else if (hasExpectedFields(curLine.toString(), fields)) {
                        // Apparently the new unfinished line is the start of a new entry.
                        addToEntries(entries, fields, curLine.toString());
                        curLine = new StringBuilder(lineToAppend);
                    } else {
                        // The unfinished new line is part of something that is building up towards a valid entry.
                        curLine.append(lineToAppend);
                    }
                } else if (!hasExpectedFields(curLine.toString(), fields)) {
                    // This should only happen in case an unexpected line break happened in the first column.
                    curLine.append(line);
                } else {
                    addToEntries(entries, fields, curLine.toString());
                    curLine = new StringBuilder(line);
                }
            }
            // Need to add the final accumulated entry
            addToEntries(entries, fields, curLine.toString());
        }

        return entries;
    }

    private static boolean hasExpectedFields(@NotNull String line, @NotNull Map<String, Integer> fields) {
        return splitFeedLine(line).length == fields.size();
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

    private void addToEntries(@NotNull List<T> entries, @NotNull Map<String, Integer> fields, @NotNull String line) {
        String reformatted = fixLineBreaks(line);
        String[] parts = splitFeedLine(reformatted);
        if (!allEmpty(parts)) {
            FeedLine feedLine = new FeedLine(fields, parts);
            if (feedEntryCreator.isValid(feedLine)) {
                entries.add(feedEntryCreator.fromLine(feedLine));
            }
        }
    }

    @NotNull
    @VisibleForTesting
    static String fixLineBreaks(@NotNull String input) {
        return input.replace("\\n", "\n");
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
