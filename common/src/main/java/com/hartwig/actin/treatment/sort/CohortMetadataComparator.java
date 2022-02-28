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

        if (metadata1.open() == metadata2.open()) {
            if (metadata1.blacklist() == metadata2.blacklist()) {
                return 0;
            } else {
                return metadata1.blacklist() ? 1 : -1;
            }
        } else {
            return metadata1.open() ? -1 : 1;
        }
    }
}
