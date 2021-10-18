package com.hartwig.actin.clinical.feed.patient;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class PatientEntryCreator implements FeedEntryCreator<PatientEntry> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    public PatientEntryCreator() {
    }

    @NotNull
    @Override
    public PatientEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutablePatientEntry.builder()
                .id(parts[fieldIndexMap.get("ID")])
                .subject(parts[fieldIndexMap.get("subject")])
                .birthYear(Integer.parseInt(parts[fieldIndexMap.get("birth_year")]))
                .gender(FeedUtil.parseGender(parts[fieldIndexMap.get("gender")]))
                .periodStart(FeedUtil.parseDate(parts[fieldIndexMap.get("period_start")], FORMAT))
                .periodEnd(FeedUtil.parseOptionalDate(parts[fieldIndexMap.get("period_end")], FORMAT))
                .build();
    }
}
