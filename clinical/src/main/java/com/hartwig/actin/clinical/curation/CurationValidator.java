package com.hartwig.actin.clinical.curation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.doid.DoidModel;

public class CurationValidator {

    static final String GENERIC_PARENT_DOID = "4"; // "disease"
    static final String CANCER_PARENT_DOID = "14566"; // "disease of cellular proliferation"

    private final DoidModel doidModel;

    public CurationValidator(final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    public boolean isValidCancerDoidSet(Set<String> doids) {
        return hasValidDoids(doids, doidModel, CANCER_PARENT_DOID);
    }

    public boolean isValidGenericDoidSet(Set<String> doids) {
        return hasValidDoids(doids, doidModel, GENERIC_PARENT_DOID);
    }

    @VisibleForTesting
    static boolean hasValidDoids(Set<String> doids, DoidModel doidModel, String expectedParentDoid) {
        if (doids.isEmpty()) {
            return false;
        }

         return doids.stream().allMatch(doid -> doidModel.doidWithParents(doid).contains(expectedParentDoid));
    }
}
