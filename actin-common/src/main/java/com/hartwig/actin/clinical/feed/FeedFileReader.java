package com.hartwig.actin.clinical.feed;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.util.FileUtil;

import org.jetbrains.annotations.NotNull;

class FeedFileReader<T extends FeedEntry> {

    private static final String DELIMITER = "\t";

    private final FeedEntryCreator<T> feedEntryCreator;

    public FeedFileReader(final FeedEntryCreator<T> feedEntryCreator) {
        this.feedEntryCreator = feedEntryCreator;
    }

    @NotNull
    public List<T> read(@NotNull String feedTsv) throws IOException {
        List<String> lines = readFeedFile(feedTsv);

        Map<String, Integer> fieldIndexMap = FileUtil.createFieldIndexMap(splitFeedLine(lines.get(0)));
        List<T> entries = Lists.newArrayList();
        if (lines.size() > 1) {
            StringBuilder curLine = new StringBuilder(lines.get(1));
            for (String line : lines.subList(2, lines.size())) {
                // Entries appear on multiple lines in case they contain hard line breaks so need to split and append to the end.
                if (splitFeedLine(line).length != fieldIndexMap.size()) {
                    // Need to remove all DELIMITER since we split further down the track.
                    curLine.append("\n").append(line.replaceAll(DELIMITER, ""));
                } else {
                    entries.add(feedEntryCreator.fromParts(fieldIndexMap, splitFeedLine(curLine.toString())));
                    curLine = new StringBuilder(line);
                }
            }
            // Need to add the final accumulated entry
            entries.add(feedEntryCreator.fromParts(fieldIndexMap, splitFeedLine(curLine.toString())));
        }

        return entries;
    }

    @NotNull
    private static List<String> readFeedFile(@NotNull String feedTsv) throws IOException {
        // Feed files are delivered in UTF_16LE so need to convert.
        return Files.readAllLines(new File(feedTsv).toPath(), StandardCharsets.UTF_16LE);
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
}
