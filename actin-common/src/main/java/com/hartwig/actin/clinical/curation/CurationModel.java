package com.hartwig.actin.clinical.curation;

import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.config.CurationConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CurationModel {

    private static final Logger LOGGER = LogManager.getLogger(CurationModel.class);

    @NotNull
    private final CurationDatabase database;

    @NotNull
    public static CurationModel fromCurationDirectory(@NotNull String clinicalCurationDirectory) throws IOException {
        return new CurationModel(CurationDatabaseReader.read(clinicalCurationDirectory));
    }

    @VisibleForTesting
    CurationModel(@NotNull final CurationDatabase database) {
        this.database = database;
    }

    @NotNull
    public TumorDetails toTumorDetails(@Nullable String inputTumorLocation, @Nullable String inputTumorType) {
        PrimaryTumorConfig primaryTumorConfig = null;
        if (inputTumorLocation != null && inputTumorType != null) {
            String inputPrimaryTumor = inputTumorLocation + " | " + inputTumorType;
            primaryTumorConfig = find(database.primaryTumorConfigs(), inputPrimaryTumor);
            if (primaryTumorConfig == null) {
                LOGGER.warn("  Could not find primary tumor config for input '{}'", inputPrimaryTumor);
            }
        }

        return ImmutableTumorDetails.builder()
                .primaryTumorLocation(primaryTumorConfig != null ? primaryTumorConfig.primaryTumorLocation() : null)
                .primaryTumorSubLocation(primaryTumorConfig != null ? primaryTumorConfig.primaryTumorSubLocation() : null)
                .primaryTumorType(primaryTumorConfig != null ? primaryTumorConfig.primaryTumorType() : null)
                .primaryTumorSubType(primaryTumorConfig != null ? primaryTumorConfig.primaryTumorSubType() : null)
                .primaryTumorExtraDetails(primaryTumorConfig != null ? primaryTumorConfig.primaryTumorExtraDetails() : null)
                .doids(primaryTumorConfig != null ? primaryTumorConfig.doids() : null)
                .build();
    }

    @NotNull
    public List<PriorTumorTreatment> toPriorTumorTreatments(@NotNull List<String> treatmentHistories) {
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        for (String treatmentHistory : treatmentHistories) {
            OncologicalHistoryConfig config = find(database.oncologicalHistoryConfigs(), treatmentHistory);
            if (config == null) {
                LOGGER.warn("  Could not oncological history config for input '{}'", treatmentHistory);
            } else if (!config.ignore()) {
                if (config.curatedObject() instanceof PriorTumorTreatment) {
                    priorTumorTreatments.add((PriorTumorTreatment) config.curatedObject());
                }
            }
        }
        return priorTumorTreatments;
    }

    @Nullable
    private static <T extends CurationConfig> T find(@NotNull List<T> configs, @NotNull String input) {
        for (T config : configs) {
            if (config.input().equals(input)) {
                return config;
            }
        }
        return null;
    }
}
