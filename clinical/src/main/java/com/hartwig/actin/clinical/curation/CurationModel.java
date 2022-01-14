package com.hartwig.actin.clinical.curation;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.curation.config.CancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.CurationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableCancerRelatedComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableInfectionConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableLesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableNonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutablePrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableToxicityConfig;
import com.hartwig.actin.clinical.curation.config.InfectionConfig;
import com.hartwig.actin.clinical.curation.config.LesionLocationCategory;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.curation.translation.AllergyTranslation;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.Translation;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableAllergy;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableCancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
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

        if (primaryTumorConfig == null) {
            return ImmutableTumorDetails.builder().build();
        } else {
            return ImmutableTumorDetails.builder()
                    .primaryTumorLocation(primaryTumorConfig.primaryTumorLocation())
                    .primaryTumorSubLocation(primaryTumorConfig.primaryTumorSubLocation())
                    .primaryTumorType(primaryTumorConfig.primaryTumorType())
                    .primaryTumorSubType(primaryTumorConfig.primaryTumorSubType())
                    .primaryTumorExtraDetails(primaryTumorConfig.primaryTumorExtraDetails())
                    .doids(primaryTumorConfig.doids())
                    .build();
        }
    }

    @NotNull
    public TumorDetails overrideKnownLesionLocations(@NotNull TumorDetails tumorDetails, @Nullable List<String> otherLesions) {
        if (otherLesions == null) {
            return tumorDetails;
        }

        Set<LesionLocationCategory> matches = Sets.newHashSet();
        for (String lesion : otherLesions) {
            String reformatted = CurationUtil.capitalizeFirstLetterOnly(lesion);
            LesionLocationConfig config = find(database.lesionLocationConfigs(), reformatted);
            if (config != null && config.category() != null) {
                matches.add(config.category());
            }
        }

        if (matches.isEmpty()) {
            return tumorDetails;
        }

        ImmutableTumorDetails.Builder builder = ImmutableTumorDetails.builder().from(tumorDetails);
        if (matches.contains(LesionLocationCategory.BONE)) {
            builder.hasBoneLesions(true);
        }

        if (matches.contains(LesionLocationCategory.LIVER)) {
            builder.hasLiverLesions(true);
        }

        if (matches.contains(LesionLocationCategory.BRAIN)) {
            builder.hasBrainLesions(true);
        }

        if (matches.contains(LesionLocationCategory.CNS)) {
            builder.hasCnsLesions(true);
        }
        return builder.build();
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
            } else if (!config.ignore() && config.curated() instanceof PriorOtherCondition) {
                priorOtherConditions.add((PriorOtherCondition) config.curated());
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
                    .name(config != null ? config.name() : CurationUtil.capitalizeFirstLetterOnly(input))
                    .build());
        }

        // Add an entry if there is nothing known about cancer related complications.
        if (cancerRelatedComplications.isEmpty()) {
            cancerRelatedComplications.add(ImmutableCancerRelatedComplication.builder().name("Unknown").build());
        }
        return cancerRelatedComplications;
    }

    @NotNull
    public List<Toxicity> curateQuestionnaireToxicities(@Nullable List<String> inputs, @NotNull LocalDate date) {
        if (inputs == null) {
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
    public ECG curateECG(@Nullable ECG input) {
        if (input == null) {
            return null;
        }

        ECGConfig config = find(database.ecgConfigs(), input.aberrationDescription());

        // Assume ECGs can also be pass-through.
        if (config != null) {
            if (config.ignore()) {
                return null;
            } else {
                return ImmutableECG.builder()
                        .from(input)
                        .aberrationDescription(config.interpretation())
                        .qtcfValue(config.qtcfValue())
                        .qtcfUnit(config.qtcfUnit())
                        .build();
            }
        } else {
            return input;
        }
    }

    @Nullable
    public InfectionStatus curateInfectionStatus(@Nullable InfectionStatus input) {
        if (input == null) {
            return null;
        }

        InfectionConfig config = find(database.infectionConfigs(), input.description());

        // Assume infections can also be pass-through.
        if (config != null) {
            String interpretation = config.interpretation();
            if (interpretation.equals("NULL")) {
                return null;
            } else {
                return ImmutableInfectionStatus.builder().from(input).description(interpretation).build();
            }
        } else {
            return input;
        }
    }

    @Nullable
    public Double determineLVEF(@Nullable List<String> inputs) {
        if (inputs == null) {
            return null;
        }

        for (String input : inputs) {
            NonOncologicalHistoryConfig config = find(database.nonOncologicalHistoryConfigs(), input);
            if (config != null && !config.ignore() && config.curated() instanceof Double) {
                return (Double) config.curated();
            }
        }

        return null;
    }

    @Nullable
    public List<String> curateOtherLesions(@Nullable List<String> otherLesions) {
        if (otherLesions == null) {
            return null;
        }

        List<String> curatedOtherLesions = Lists.newArrayList();
        for (String lesion : otherLesions) {
            String reformatted = CurationUtil.capitalizeFirstLetterOnly(lesion);
            LesionLocationConfig config = find(database.lesionLocationConfigs(), reformatted);
            if (config == null) {
                curatedOtherLesions.add(reformatted);
            } else if (config.category() == null && !config.location().isEmpty()) {
                // We only want to include lesions from the other lesions in actual other lesions if there is no category assigned.
                curatedOtherLesions.add(config.location());
            }
        }

        return curatedOtherLesions;
    }

    @Nullable
    public String curateBiopsyLocation(@Nullable String input) {
        if (input == null) {
            return null;
        }

        String reformatted = CurationUtil.capitalizeFirstLetterOnly(input);

        LesionLocationConfig config = find(database.lesionLocationConfigs(), reformatted);
        return config != null ? config.location() : reformatted;
    }

    @Nullable
    public Medication curateMedicationDosage(@NotNull String input) {
        MedicationDosageConfig config = find(database.medicationDosageConfigs(), input);

        if (config == null) {
            // TODO: Change to warn once the medications are more final.
            LOGGER.debug(" Could not find medication dosage config for '{}'", input);
            return null;
        } else {
            return ImmutableMedication.builder()
                    .name(Strings.EMPTY)
                    .dosageMin(config.dosageMin())
                    .dosageMax(config.dosageMax())
                    .dosageUnit(config.dosageUnit())
                    .frequency(config.frequency())
                    .frequencyUnit(config.frequencyUnit())
                    .ifNeeded(config.ifNeeded())
                    .build();
        }
    }

    @NotNull
    public Medication annotateWithMedicationCategory(@NotNull Medication medication) {
        MedicationCategoryConfig config = find(database.medicationCategoryConfigs(), medication.name());

        if (config == null) {
            LOGGER.warn(" Could not find medication category config for '{}'", medication.name());
            return medication;
        } else {
            return ImmutableMedication.builder().from(medication).categories(config.categories()).build();
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

        LOGGER.warn(" Could not find laboratory translation for lab value with code '{}' and name '{}'", input.code(), input.name());
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

        LOGGER.warn(" Could not find allergy translation for allergy with name '{}'", input.name());
        return null;
    }

    @NotNull
    public BloodTransfusion translateBloodTransfusion(@NotNull BloodTransfusion input) {
        BloodTransfusionTranslation translation = findBloodTransfusionTranslation(input);

        if (translation != null) {
            evaluatedTranslations.put(BloodTransfusionTranslation.class, translation);
            return ImmutableBloodTransfusion.builder().from(input).product(translation.translatedProduct()).build();
        } else {
            return input;
        }
    }

    @Nullable
    private BloodTransfusionTranslation findBloodTransfusionTranslation(@NotNull BloodTransfusion input) {
        for (BloodTransfusionTranslation entry : database.bloodTransfusionTranslations()) {
            if (entry.product().equals(input.product())) {
                return entry;
            }
        }

        LOGGER.warn(" Could not find blood transfusion translation for blood transfusion with product '{}'", input.product());
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
        } else if (classToLookUp == ImmutableInfectionConfig.class) {
            return database.infectionConfigs();
        } else if (classToLookUp == ImmutableToxicityConfig.class) {
            return database.toxicityConfigs();
        } else if (classToLookUp == ImmutableMedicationDosageConfig.class) {
            return database.medicationDosageConfigs();
        } else if (classToLookUp == ImmutableMedicationCategoryConfig.class) {
            return database.medicationCategoryConfigs();
        }

        throw new IllegalStateException("Class not found in curation database: " + classToLookUp);
    }

    @NotNull
    private List<? extends Translation> translationsForClass(@NotNull Class<? extends Translation> classToLookup) {
        if (classToLookup == LaboratoryTranslation.class) {
            return database.laboratoryTranslations();
        } else if (classToLookup == AllergyTranslation.class) {
            return database.allergyTranslations();
        } else if (classToLookup == BloodTransfusionTranslation.class) {
            return database.bloodTransfusionTranslations();
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
