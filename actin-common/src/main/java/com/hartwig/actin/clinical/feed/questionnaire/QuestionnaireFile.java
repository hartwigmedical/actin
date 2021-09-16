package com.hartwig.actin.clinical.feed.questionnaire;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public final class QuestionnaireFile {

    private static final String DELIMITER = "\t";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private QuestionnaireFile() {
    }

    @NotNull
    public static List<QuestionnaireEntry> read(@NotNull String questionnaireTsv) throws IOException {
        List<String> lines = FeedUtil.readFeedFile(questionnaireTsv);

        Map<String, Integer> fieldIndexMap = FeedUtil.createFieldIndexMap(lines.get(0), DELIMITER);
        List<QuestionnaireEntry> entries = Lists.newArrayList();
        if (lines.size() > 1) {
            StringBuilder curLine = new StringBuilder(lines.get(1));
            for (String line : lines.subList(2, lines.size())) {
                // Questionnaires appear on multiple lines so need to split and append to the end.
                if (FeedUtil.splitFeedLine(line, DELIMITER).length != 7) {
                    // Need to remove all DELIMITER since we split further down the track.
                    curLine.append("\n").append(line.replaceAll(DELIMITER, ""));
                } else {
                    entries.add(fromLine(fieldIndexMap, curLine.toString()));
                    curLine = new StringBuilder(line);
                }
            }
            // Need to add the final accumulated entry
            entries.add(fromLine(fieldIndexMap, curLine.toString()));
        }

        return entries;
    }

    @NotNull
    private static QuestionnaireEntry fromLine(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String line) {
        String[] parts = FeedUtil.splitFeedLine(line, DELIMITER);

        return ImmutableQuestionnaireEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .parentIdentifierValue(parts[fieldIndexMap.get("parent_identifier_value")])
                .authoredDateTime(parseDate(parts[fieldIndexMap.get("authored_datetime")]))
                .questionnaireQuestionnaireValue(parts[fieldIndexMap.get("questionnaire_Questionnaire_value")])
                .description(parts[fieldIndexMap.get("description")])
                .itemText(parts[fieldIndexMap.get("item_text")])
                .itemAnswerValueValueString(parts[fieldIndexMap.get("item_answer_value_valueString")])
                .build();
    }

    @NotNull
    private static LocalDate parseDate(@NotNull String date) {
        // This date contains microseconds so need to remove before parsing.
        return LocalDate.parse(date.substring(0, date.length() - 4), FORMAT);
    }
}
