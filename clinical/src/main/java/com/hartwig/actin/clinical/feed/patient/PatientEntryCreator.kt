package com.hartwig.actin.clinical.feed.patient;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class PatientEntryCreator implements FeedEntryCreator<PatientEntry> {

    private static final Logger LOGGER = LogManager.getLogger(PatientEntryCreator.class);

    public PatientEntryCreator() {
    }

    @NotNull
    @Override
    public PatientEntry fromLine(@NotNull final FeedLine line) {
        String subjectTrimmed = line.trimmed("subject");
        String subjectNormal = line.string("subject");
        if (!subjectNormal.equals(subjectTrimmed)) {
            LOGGER.warn("Patient ID detected with trailing whitespace: '{}'", subjectNormal);
        }

        return ImmutablePatientEntry.builder()
                .subject(subjectTrimmed)
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
