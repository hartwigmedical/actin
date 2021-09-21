package com.hartwig.actin.clinical.feed.encounter;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class EncounterEntryCreator implements FeedEntryCreator<EncounterEntry> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    public EncounterEntryCreator() {
    }

    @NotNull
    @Override
    public EncounterEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableEncounterEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .type1Display(parts[fieldIndexMap.get("type1_display")])
                .classDisplay(parts[fieldIndexMap.get("class_display")])
                .periodStart(FeedUtil.parseDate(parts[fieldIndexMap.get("period_start")], FORMAT))
                .periodEnd(FeedUtil.parseDate(parts[fieldIndexMap.get("period_end")], FORMAT))
                .identifierValue(parts[fieldIndexMap.get("identifier_value")])
                .identifierSystem(parts[fieldIndexMap.get("identifier_system")])
                .codeCodingCodeOriginal(parts[fieldIndexMap.get("code_coding_code_original")])
                .codeCodingDisplayOriginal(parts[fieldIndexMap.get("code_coding_display_original")])
                .presentedFormData(parts[fieldIndexMap.get("presentedForm_data")])
                .reason(parts[fieldIndexMap.get("reason")])
                .accessionValue(parts[fieldIndexMap.get("accession_value")])
                .build();
    }
}
