package com.hartwig.actin.clinical.feed.patient;

import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class PatientEntryCreator implements FeedEntryCreator<PatientEntry> {

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
                .periodStart(FeedUtil.parseDate(parts[fieldIndexMap.get("period_start")]))
                .periodEnd(FeedUtil.parseOptionalDate(parts[fieldIndexMap.get("period_end")]))
                .build();
    }
}
