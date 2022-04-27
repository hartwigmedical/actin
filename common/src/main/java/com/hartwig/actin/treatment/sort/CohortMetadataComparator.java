package com.hartwig.actin.treatment.sort;

import java.util.Comparator;

import com.hartwig.actin.treatment.datamodel.CohortMetadata;

import org.jetbrains.annotations.NotNull;

public class CohortMetadataComparator implements Comparator<CohortMetadata> {

    @Override
    public int compare(@NotNull CohortMetadata metadata1, @NotNull CohortMetadata metadata2) {
        int idCompare = metadata1.cohortId().compareTo(metadata2.cohortId());
        if (idCompare != 0) {
            return idCompare;
        }

        int descriptionCompare = metadata1.description().compareTo(metadata2.description());
        if (descriptionCompare != 0) {
            return descriptionCompare;
        }

        int openCompare = Boolean.compare(metadata2.open(), metadata1.open());
        if (openCompare != 0) {
            return openCompare;
        }

        int slotsAvailableCompare = Boolean.compare(metadata2.slotsAvailable(), metadata1.slotsAvailable());
        if (slotsAvailableCompare != 0) {
            return slotsAvailableCompare;
        }

        return Boolean.compare(metadata1.blacklist(), metadata2.blacklist());
    }
}
