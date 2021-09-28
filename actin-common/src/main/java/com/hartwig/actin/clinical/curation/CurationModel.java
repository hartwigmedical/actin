package com.hartwig.actin.clinical.curation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.curation.config.CancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.CurationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableCancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutablePrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableCancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
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
    private final Multimap<Class<? extends CurationConfig>, String> evaluatedInputs = HashMultimap.create();

    @NotNull
    public static CurationModel fromCurationDirectory(@NotNull String clinicalCurationDirectory) throws IOException {
        return new CurationModel(CurationDatabaseReader.read(clinicalCurationDirectory));
    }

    @VisibleForTesting
    CurationModel(@NotNull final CurationDatabase database) {
        this.database = database;
    }

    @NotNull
    public TumorDetails curateTumorDetails(@Nullable String inputTumorLocation, @Nullable String inputTumorType) {
        PrimaryTumorConfig primaryTumorConfig = null;
        if (inputTumorLocation != null && inputTumorType != null) {
            String inputPrimaryTumor = inputTumorLocation + " | " + inputTumorType;
            primaryTumorConfig = find(database.primaryTumorConfigs(), inputPrimaryTumor);
            if (primaryTumorConfig == null) {
                LOGGER.warn(" Could not find primary tumor config for input '{}'", inputPrimaryTumor);
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
    public List<PriorTumorTreatment> curatePriorTumorTreatments(@Nullable List<String> inputs) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        for (String input : inputs) {
            OncologicalHistoryConfig config = find(database.oncologicalHistoryConfigs(), input);
            if (config == null) {
                LOGGER.warn(" Could not find oncological history config for input '{}'", input);
            } else if (!config.ignore()) {
                if (config.curatedObject() instanceof PriorTumorTreatment) {
                    priorTumorTreatments.add((PriorTumorTreatment) config.curatedObject());
                }
            }
        }
        return priorTumorTreatments;
    }

    @NotNull
    public List<PriorSecondPrimary> curatePriorSecondPrimaries(@Nullable List<String> inputs) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();
        for (String input : inputs) {
            OncologicalHistoryConfig config = find(database.oncologicalHistoryConfigs(), input);
            if (config == null) {
                LOGGER.warn(" Could not find oncological history config for input '{}'", input);
            } else if (!config.ignore()) {
                if (config.curatedObject() instanceof PriorSecondPrimary) {
                    priorSecondPrimaries.add((PriorSecondPrimary) config.curatedObject());
                }
            }
        }
        return priorSecondPrimaries;
    }

    @Nullable
    public String curateAberrationECG(@Nullable String input) {
        if (input == null) {
            return null;
        }

        ECGConfig config = find(database.ecgConfigs(), input);

        // Assume ECGs can also be pass-through.
        return config != null ? config.interpretation() : input;
    }

    @NotNull
    public List<CancerRelatedComplication> curateCancerRelatedComplications(@Nullable List<String> inputs) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<CancerRelatedComplication> cancerRelatedComplications = Lists.newArrayList();
        for (String input : inputs) {
            CancerRelatedComplicationConfig config = find(database.cancerRelatedComplicationConfigs(), input);
            cancerRelatedComplications.add(ImmutableCancerRelatedComplication.builder()
                    .name(config != null ? config.name() : input)
                    .build());
        }
        return cancerRelatedComplications;
    }

    public void evaluate() {
        int warnCount = 0;
        for (Map.Entry<Class<? extends CurationConfig>, Collection<String>> entry : evaluatedInputs.asMap().entrySet()) {
            List<? extends CurationConfig> configs = configsForClass(entry.getKey());
            Collection<String> evaluated = entry.getValue();
            for (CurationConfig config : configs) {
                if (!evaluated.contains(config.input())) {
                    warnCount++;
                    LOGGER.warn(" Curation key '{}' not used for class {}", config.input(), entry.getKey().getSimpleName());
                }
            }
        }

        LOGGER.info(" {} warnings raised during curation model evaluation", warnCount);
    }

    @NotNull
    private List<? extends CurationConfig> configsForClass(@NotNull Class<? extends CurationConfig> classToLookUp) {
        if (classToLookUp == ImmutableECGConfig.class) {
            return database.ecgConfigs();
        } else if (classToLookUp == ImmutableOncologicalHistoryConfig.class) {
            return database.oncologicalHistoryConfigs();
        } else if (classToLookUp == ImmutablePrimaryTumorConfig.class) {
            return database.primaryTumorConfigs();
        } else if (classToLookUp == ImmutableCancerRelatedComplicationConfig.class) {
            return database.cancerRelatedComplicationConfigs();
        }

        throw new IllegalStateException("Class not found in curation database: " + classToLookUp);
    }

    @Nullable
    private <T extends CurationConfig> T find(@NotNull List<T> configs, @NotNull String input) {
        if (!configs.isEmpty()) {
            evaluatedInputs.put(configs.get(0).getClass(), input);
            for (T config : configs) {
                if (config.input().equals(input)) {
                    return config;
                }
            }
        }
        return null;
    }
}
