package com.hartwig.actin.clinical.feed.questionnaire;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QuestionnaireRawEntryMapper {

    private final Map<String, String> correctionMap;

    public QuestionnaireRawEntryMapper(Map<String, String> correctionMap) {
        this.correctionMap = correctionMap;
    }

    public static QuestionnaireRawEntryMapper createFromFile(String filePath) throws IOException {
        Map<String, String> correctionMap = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            correctionMap.put(parts[0].replace("\\n", "\n"), parts[1].replace("\\n", "\n"));
        }
        return new QuestionnaireRawEntryMapper(correctionMap);
    }

    public String correctQuestionnaireEntry(String rawQuestionnaireText) {
        String correctedQuestionnaireText = rawQuestionnaireText;
        for (Map.Entry<String, String> correctionEntry : correctionMap.entrySet()) {
            correctedQuestionnaireText = correctedQuestionnaireText.replace(correctionEntry.getKey(), correctionEntry.getValue());
        }
        return correctedQuestionnaireText;
    }
}
