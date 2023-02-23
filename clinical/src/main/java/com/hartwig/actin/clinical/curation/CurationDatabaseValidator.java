package com.hartwig.actin.clinical.curation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.doid.DoidModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

class CurationDatabaseValidator {

    private static final Logger LOGGER = LogManager.getLogger(CurationDatabaseValidator.class);

    static final String GENERIC_PARENT_DOID = "4"; // "disease"
    static final String CANCER_PARENT_DOID = "14566"; // "disease of cellular proliferation"

    @NotNull
    private final DoidModel doidModel;

    public CurationDatabaseValidator(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    public void validate(@NotNull CurationDatabase database) {
        LOGGER.info("Running curation database validation");

        boolean allPrimaryTumorConfigsValid = validatePrimaryTumorConfigs(database.primaryTumorConfigs(), doidModel);
        boolean allSecondPrimaryConfigsValid = validateSecondPrimaryConfigs(database.secondPrimaryConfigs(), doidModel);
        boolean allNonOncologicalHistoryConfigsValid =
                validateNonOncologicalHistoryConfigs(database.nonOncologicalHistoryConfigs(), doidModel);
        boolean allIntoleranceConfigsValid = validateIntoleranceConfigs(database.intoleranceConfigs(), doidModel);

        if (allPrimaryTumorConfigsValid && allSecondPrimaryConfigsValid && allNonOncologicalHistoryConfigsValid
                && allIntoleranceConfigsValid) {
            LOGGER.info(" Curation database validation found no quality issues");
        }
    }

    @VisibleForTesting
    static boolean validatePrimaryTumorConfigs(@NotNull List<PrimaryTumorConfig> primaryTumorConfigs, @NotNull DoidModel doidModel) {
        boolean allValid = true;
        for (PrimaryTumorConfig primaryTumorConfig : primaryTumorConfigs) {
            if (!hasValidDoids(primaryTumorConfig.doids(), doidModel, CANCER_PARENT_DOID)) {
                allValid = false;
                LOGGER.warn(" Invalid primary tumor doids configured for '{}': {}", primaryTumorConfig.input(), primaryTumorConfig.doids());
            }
        }
        return allValid;
    }

    @VisibleForTesting
    static boolean validateSecondPrimaryConfigs(@NotNull List<SecondPrimaryConfig> secondPrimaryConfigs, @NotNull DoidModel doidModel) {
        boolean allValid = true;
        for (SecondPrimaryConfig secondPrimaryConfig : secondPrimaryConfigs) {
            PriorSecondPrimary priorSecondPrimary = secondPrimaryConfig.curated();
            if (priorSecondPrimary != null) {
                if (!hasValidDoids(priorSecondPrimary.doids(), doidModel, CANCER_PARENT_DOID)) {
                    allValid = false;
                    LOGGER.warn(" Invalid second primary doids configured for '{}': {}",
                            secondPrimaryConfig.input(),
                            secondPrimaryConfig.curated().doids());
                }
            }
        }
        return allValid;
    }

    @VisibleForTesting
    static boolean validateNonOncologicalHistoryConfigs(@NotNull List<NonOncologicalHistoryConfig> nonOncologicalHistoryConfigs,
            @NotNull DoidModel doidModel) {
        boolean allValid = true;
        for (NonOncologicalHistoryConfig nonOncologicalHistoryConfig : nonOncologicalHistoryConfigs) {
            if (nonOncologicalHistoryConfig.priorOtherCondition().isPresent()) {
                PriorOtherCondition priorOtherCondition = nonOncologicalHistoryConfig.priorOtherCondition().get();
                if (!hasValidDoids(priorOtherCondition.doids(), doidModel, GENERIC_PARENT_DOID)) {
                    allValid = false;
                    LOGGER.warn(" Invalid prior other condition doids configured for '{}': {}",
                            nonOncologicalHistoryConfig.input(),
                            priorOtherCondition.doids());
                }
            }
        }
        return allValid;
    }

    @VisibleForTesting
    static boolean validateIntoleranceConfigs(@NotNull List<IntoleranceConfig> intoleranceConfigs, @NotNull DoidModel doidModel) {
        boolean allValid = true;
        for (IntoleranceConfig intoleranceConfig : intoleranceConfigs) {
            if (!hasValidDoids(intoleranceConfig.doids(), doidModel, GENERIC_PARENT_DOID)) {
                allValid = false;
                LOGGER.warn(" Invalid intolerance doids configured for '{}': {}", intoleranceConfig.input(), intoleranceConfig.doids());
            }
        }
        return allValid;
    }

    @VisibleForTesting
    static boolean hasValidDoids(@NotNull Set<String> doids, @NotNull DoidModel doidModel, @NotNull String expectedParentDoid) {
        if (doids.isEmpty()) {
            return false;
        }

         return doids.stream().allMatch(doid -> doidModel.doidWithParents(doid).contains(expectedParentDoid));
    }
}
