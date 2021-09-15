package com.hartwig.actin.clinical.feed;

import java.io.File;
import java.io.IOException;

import org.jetbrains.annotations.NotNull;

public final class FeedFactory {

    private static final String PATIENT_TSV = "patient.tsv";
    private static final String LAB_TSV = "lab.tsv";

    private FeedFactory() {
    }

    @NotNull
    public static Feed loadFromClinicalDataDirectory(@NotNull String clinicalDataDirectory) throws IOException {
        return ImmutableFeed.builder()
                .patientEntries(PatientFile.read(clinicalDataDirectory + File.separator + PATIENT_TSV))
                .labEntries(LabFile.read(clinicalDataDirectory + File.separator + LAB_TSV))
                .build();
    }
}
