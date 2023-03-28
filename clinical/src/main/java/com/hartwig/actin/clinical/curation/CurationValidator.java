package com.hartwig.actin.clinical.curation;

import java.util.Set;

import com.hartwig.actin.doid.DoidModel;

public class CurationValidator {

    static final String DISEASE_DOID = "4";
    static final String DISEASE_OF_CELLULAR_PROLIFERATION_DOID = "14566";

    private final DoidModel doidModel;

    public CurationValidator(final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    public boolean isValidCancerDoidSet(Set<String> doids) {
        return hasValidDoids(doids, doidModel, DISEASE_OF_CELLULAR_PROLIFERATION_DOID);
    }

    public boolean isValidDiseaseDoidSet(Set<String> doids) {
        return hasValidDoids(doids, doidModel, DISEASE_DOID);
    }

    private static boolean hasValidDoids(Set<String> doids, DoidModel doidModel, String expectedParentDoid) {
        if (doids.isEmpty()) {
            return false;
        }

         return doids.stream().allMatch(doid -> doidModel.doidWithParents(doid).contains(expectedParentDoid));
    }
}
