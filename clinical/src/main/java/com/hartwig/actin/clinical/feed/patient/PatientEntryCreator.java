package com.hartwig.actin.clinical.feed.patient;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class PatientEntryCreator implements FeedEntryCreator<PatientEntry> {

    public PatientEntryCreator() {
    }

    @NotNull
    @Override
    public PatientEntry fromLine(@NotNull final FeedLine line) {
        return ImmutablePatientEntry.builder()
                .subject(line.string("subject"))
                .birthYear(line.integer("birth_year"))
                .gender(line.gender("gender"))
                .periodStart(line.date("period_start"))
                .periodEnd(line.optionalDate("period_end"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return true;
    }
}
