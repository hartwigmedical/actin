package com.hartwig.actin.clinical.feed.patient;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.feed.FeedFileReader;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class PatientFileReader extends FeedFileReader<PatientEntry> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    public PatientFileReader() {
    }

    @NotNull
    @Override
    public PatientEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutablePatientEntry.builder()
                .id(parts[fieldIndexMap.get("ID")])
                .subject(parts[fieldIndexMap.get("subject")])
                .birthYear(Integer.parseInt(parts[fieldIndexMap.get("birth_year")]))
                .sex(Sex.parseSex(parts[fieldIndexMap.get("gender")]))
                .periodStart(FeedUtil.parseDate(parts[fieldIndexMap.get("period_start")], FORMAT))
                .periodEnd(FeedUtil.parseOptionalDate(parts[fieldIndexMap.get("period_end")], FORMAT))
                .build();
    }
}
