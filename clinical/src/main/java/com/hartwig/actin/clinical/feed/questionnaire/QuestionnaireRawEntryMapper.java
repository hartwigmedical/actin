package com.hartwig.actin.clinical.feed.questionnaire;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.util.Paths;

import org.jetbrains.annotations.NotNull;

public class QuestionnaireRawEntryMapper {

    private static final String QUESTIONNAIRE_MAPPING_TSV = "questionnaire_mapping.tsv";

    private final Map<String, String> correctionMap;

    public QuestionnaireRawEntryMapper(Map<String, String> correctionMap) {
        this.correctionMap = correctionMap;
    }

    public static QuestionnaireRawEntryMapper createFromCurationDirectory(String curationDirectory) throws IOException {
        Path filePath = Path.of(Paths.forceTrailingFileSeparator(curationDirectory) + QUESTIONNAIRE_MAPPING_TSV);

        try (Stream<String> fileStream = Files.lines(filePath)) {
            Map<String, String> correctionMap = fileStream.map(QuestionnaireRawEntryMapper::splitAndParseLineBreaks)
                    .collect(HashMap::new, (m, cols) -> m.put(cols.get(0), cols.get(1)), HashMap::putAll);

            return new QuestionnaireRawEntryMapper(correctionMap);
        }
    }

    @NotNull
    private static List<String> splitAndParseLineBreaks(String line) {
        return Arrays.stream(line.split("\t", 2)).map(entry -> entry.replace("\\n", "\n")).collect(Collectors.toList());
    }

    public String correctQuestionnaireEntry(String rawQuestionnaireText) {
        String correctedQuestionnaireText = rawQuestionnaireText;
        for (Map.Entry<String, String> correctionEntry : correctionMap.entrySet()) {
            correctedQuestionnaireText = correctedQuestionnaireText.replace(correctionEntry.getKey(), correctionEntry.getValue());
        }
        return correctedQuestionnaireText;
    }
}
