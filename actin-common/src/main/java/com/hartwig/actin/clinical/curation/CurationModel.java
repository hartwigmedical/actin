package com.hartwig.actin.clinical.curation;

import java.io.IOException;
import java.time.LocalDate;
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
import com.hartwig.actin.clinical.curation.config.ImmutableLesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableNonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutablePrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableToxicityConfig;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.curation.translation.AllergyTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.Translation;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableAllergy;
import com.hartwig.actin.clinical.datamodel.ImmutableCancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CurationModel {

    private static final Logger LOGGER = LogManager.getLogger(CurationModel.class);

    @NotNull
    private final CurationDatabase database;
    @NotNull
    private final Multimap<Class<? extends CurationConfig>, String> evaluatedCurationInputs = HashMultimap.create();
    @NotNull
    private final Multimap<Class<? extends Translation>, Translation> evaluatedTranslations = HashMultimap.create();

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

    @NotNull
    public List<PriorOtherCondition> curatePriorOtherConditions(@Nullable List<String> inputs) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<PriorOtherCondition> priorOtherConditions = Lists.newArrayList();
        for (String input : inputs) {
            NonOncologicalHistoryConfig config = find(database.nonOncologicalHistoryConfigs(), input);
            if (config == null) {
                LOGGER.warn(" Could not find non-oncological history config for input '{}'", input);
            } else if (!config.ignore()) {
                priorOtherConditions.add(config.curated());
            }
        }
        return priorOtherConditions;
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

    @NotNull
    public List<Toxicity> curateQuestionnaireToxicities(@Nullable LocalDate date, @Nullable List<String> inputs) {
        if (inputs == null || date == null) {
            return Lists.newArrayList();
        }

        List<Toxicity> toxicities = Lists.newArrayList();
        for (String input : inputs) {
            ToxicityConfig config = find(database.toxicityConfigs(), input);
            if (config == null) {
                LOGGER.warn(" Could not find toxicity config for input '{}'", input);
            } else if (!config.ignore()) {
                toxicities.add(ImmutableToxicity.builder()
                        .name(config.name())
                        .evaluatedDate(date)
                        .source(ToxicitySource.QUESTIONNAIRE)
                        .grade(config.grade())
                        .build());
            }
        }
        return toxicities;
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

    @Nullable
    public String curateLesionLocation(@Nullable String input) {
        if (input == null) {
            return null;
        }

        LesionLocationConfig config = find(database.lesionLocationConfigs(), input);

        // Assume lesion locations can also be pass-through.
        return config != null ? config.location() : CurationUtil.capitalizeFirstLetter(input);
    }

    @Nullable
    public Medication curateMedicationDosage(@NotNull String input) {
        MedicationDosageConfig config = find(database.medicationDosageConfigs(), input);

        if (config == null) {
            LOGGER.warn(" Could not find medication dosage config for '{}'", input);
            return null;
        } else {
            // Dosage should potentially become string
            return ImmutableMedication.builder()
                    .name(Strings.EMPTY)
                    .type(Strings.EMPTY)
                    .dosage(0D)
                    .unit(config.unit())
                    .frequencyUnit(config.frequencyUnit())
                    .ifNeeded(config.ifNeeded())
                    .build();
        }
    }

    @NotNull
    public LabValue translateLabValue(@NotNull LabValue input) {
        LaboratoryTranslation translation = findLaboratoryTranslation(input);

        if (translation != null) {
            evaluatedTranslations.put(LaboratoryTranslation.class, translation);
            return ImmutableLabValue.builder().from(input).code(translation.translatedCode()).name(translation.translatedName()).build();
        } else {
            return input;
        }
    }

    @Nullable
    private LaboratoryTranslation findLaboratoryTranslation(@NotNull LabValue input) {
        for (LaboratoryTranslation entry : database.laboratoryTranslations()) {
            if (entry.code().equals(input.code()) && entry.name().equals(input.name())) {
                return entry;
            }
        }

        LOGGER.warn("Could not find laboratory translation for lab value with code '{}' and name '{}'", input.code(), input.name());
        return null;
    }

    @NotNull
    public Allergy translateAllergy(@NotNull Allergy input) {
        AllergyTranslation translation = findAllergyTranslation(input);

        if (translation != null) {
            evaluatedTranslations.put(AllergyTranslation.class, translation);
            return ImmutableAllergy.builder().from(input).name(translation.translatedName()).build();
        } else {
            return input;
        }
    }

    @Nullable
    private AllergyTranslation findAllergyTranslation(@NotNull Allergy input) {
        for (AllergyTranslation entry : database.allergyTranslations()) {
            if (entry.name().equals(input.name())) {
                return entry;
            }
        }

        LOGGER.warn("Could not find allergy translation for allergy with name '{}'", input.name());
        return null;
    }

    public void evaluate() {
        int warnCount = 0;
        for (Map.Entry<Class<? extends CurationConfig>, Collection<String>> entry : evaluatedCurationInputs.asMap().entrySet()) {
            List<? extends CurationConfig> configs = configsForClass(entry.getKey());
            Collection<String> evaluated = entry.getValue();
            for (CurationConfig config : configs) {
                if (!evaluated.contains(config.input())) {
                    warnCount++;
                    LOGGER.warn(" Curation key '{}' not used for class {}", config.input(), entry.getKey().getSimpleName());
                }
            }
        }

        for (Map.Entry<Class<? extends Translation>, Collection<Translation>> entry : evaluatedTranslations.asMap().entrySet()) {
            List<? extends Translation> translations = translationsForClass(entry.getKey());
            Collection<Translation> evaluated = entry.getValue();
            for (Translation translation : translations) {
                if (!evaluated.contains(translation)) {
                    warnCount++;
                    LOGGER.warn(" Translation '{}' not used", translation);
                }
            }
        }

        LOGGER.info(" {} warnings raised during curation model evaluation", warnCount);
    }

    @NotNull
    private List<? extends CurationConfig> configsForClass(@NotNull Class<? extends CurationConfig> classToLookUp) {
        if (classToLookUp == ImmutablePrimaryTumorConfig.class) {
            return database.primaryTumorConfigs();
        } else if (classToLookUp == ImmutableLesionLocationConfig.class) {
            return database.lesionLocationConfigs();
        } else if (classToLookUp == ImmutableOncologicalHistoryConfig.class) {
            return database.oncologicalHistoryConfigs();
        } else if (classToLookUp == ImmutableNonOncologicalHistoryConfig.class) {
            return database.nonOncologicalHistoryConfigs();
        } else if (classToLookUp == ImmutableCancerRelatedComplicationConfig.class) {
            return database.cancerRelatedComplicationConfigs();
        } else if (classToLookUp == ImmutableECGConfig.class) {
            return database.ecgConfigs();
        } else if (classToLookUp == ImmutableToxicityConfig.class) {
            return database.toxicityConfigs();
        } else if (classToLookUp == ImmutableMedicationDosageConfig.class) {
            return database.medicationDosageConfigs();
        }

        throw new IllegalStateException("Class not found in curation database: " + classToLookUp);
    }

    @NotNull
    private List<? extends Translation> translationsForClass(@NotNull Class<? extends Translation> classToLookup) {
        if (classToLookup == LaboratoryTranslation.class) {
            return database.laboratoryTranslations();
        } else if (classToLookup == AllergyTranslation.class) {
            return database.allergyTranslations();
        }

        throw new IllegalStateException("Class not found in curation database: " + classToLookup);
    }

    @Nullable
    private <T extends CurationConfig> T find(@NotNull List<T> configs, @NotNull String input) {
        if (!configs.isEmpty()) {
            evaluatedCurationInputs.put(configs.get(0).getClass(), input);
            for (T config : configs) {
                if (config.input().equals(input)) {
                    return config;
                }
            }
        }
        return null;
    }
}
