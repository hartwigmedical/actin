package com.hartwig.actin.clinical.curation;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

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

    private CurationModel(@NotNull final CurationDatabase database) {
        this.database = database;
    }

    @NotNull
    public List<PriorTumorTreatment> toPriorTumorTreatments(@NotNull List<String> treatmentHistories) {
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        for (String treatmentHistory : treatmentHistories) {
            OncologicalHistoryConfig config = find(treatmentHistory);
            if (config == null) {
                LOGGER.warn("Could not find oncological history config for '{}'" + treatmentHistory);
            } else if (!config.ignore()) {
                if (config.curatedObject() instanceof PriorTumorTreatment) {
                    priorTumorTreatments.add((PriorTumorTreatment) config.curatedObject());
                }
            }
        }
        return priorTumorTreatments;
    }

    @Nullable
    private OncologicalHistoryConfig find(@NotNull String input) {
        for (OncologicalHistoryConfig config : database.oncologicalHistoryConfigs()) {
            if (config.input().equals(input)) {
                return config;
            }
        }
        return null;
    }
}
