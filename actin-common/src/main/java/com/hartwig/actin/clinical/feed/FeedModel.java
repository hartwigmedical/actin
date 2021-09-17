package com.hartwig.actin.clinical.feed;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;

import org.jetbrains.annotations.NotNull;

public class FeedModel {

    @NotNull
    private final ClinicalFeed feed;

    @NotNull
    public static FeedModel fromFeedDirectory(@NotNull String clinicalFeedDirectory) throws IOException {
        return new FeedModel(ClinicalFeedReader.read(clinicalFeedDirectory));
    }

    private FeedModel(@NotNull final ClinicalFeed feed) {
        this.feed = feed;
    }

    @NotNull
    public Set<String> subjects() {
        Set<String> subjects = Sets.newTreeSet(Comparator.naturalOrder());
        for (PatientEntry entry : feed.patientEntries()) {
            subjects.add(entry.subject());
        }
        return subjects;
    }
}
