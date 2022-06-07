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
import com.hartwig.actin.clinical.curation.config.ComplicationConfig;
import com.hartwig.actin.clinical.curation.config.CurationConfig;
import com.hartwig.actin.clinical.curation.config.ECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableComplicationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableECGConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableInfectionConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableIntoleranceConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableLesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMedicationNameConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableMolecularTestConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableNonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutablePrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableSecondPrimaryConfig;
import com.hartwig.actin.clinical.curation.config.ImmutableToxicityConfig;
import com.hartwig.actin.clinical.curation.config.InfectionConfig;
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig;
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig;
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfig;
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig;
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig;
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig;
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig;
import com.hartwig.actin.clinical.curation.config.ToxicityConfig;
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableBloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.Translation;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
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

            Set<PrimaryTumorConfig> configs = find(database.primaryTumorConfigs(), inputPrimaryTumor);
            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find primary tumor config for input '{}'", inputPrimaryTumor);
            } else if (configs.size() > 1) {
                LOGGER.warn(" Primary tumor '{}' matched to multiple configs!", inputPrimaryTumor);
            } else {
                primaryTumorConfig = configs.iterator().next();
            }
        }

        if (primaryTumorConfig == null) {
            return ImmutableTumorDetails.builder().build();
        }

        return ImmutableTumorDetails.builder()
                .primaryTumorLocation(primaryTumorConfig.primaryTumorLocation())
                .primaryTumorSubLocation(primaryTumorConfig.primaryTumorSubLocation())
                .primaryTumorType(primaryTumorConfig.primaryTumorType())
                .primaryTumorSubType(primaryTumorConfig.primaryTumorSubType())
                .primaryTumorExtraDetails(primaryTumorConfig.primaryTumorExtraDetails())
                .doids(primaryTumorConfig.doids())
                .build();
    }

    @NotNull
    public TumorDetails overrideKnownLesionLocations(@NotNull TumorDetails tumorDetails, @Nullable String biopsyLocation,
            @Nullable List<String> otherLesions) {
        Set<LesionLocationCategory> matches = Sets.newHashSet();
        List<String> lesionsToCheck = Lists.newArrayList();

        if (otherLesions != null) {
            lesionsToCheck.addAll(otherLesions);
        }

        if (biopsyLocation != null) {
            lesionsToCheck.add(biopsyLocation);
        }

        for (String lesion : lesionsToCheck) {
            String reformatted = CurationUtil.capitalizeFirstLetterOnly(lesion);
            Set<LesionLocationConfig> configs = find(database.lesionLocationConfigs(), reformatted);
            for (LesionLocationConfig config : configs) {
                if (config.category() != null) {
                    matches.add(config.category());
                }
            }
        }

        if (matches.isEmpty()) {
            return tumorDetails;
        }

        ImmutableTumorDetails.Builder builder = ImmutableTumorDetails.builder().from(tumorDetails);
        if (matches.contains(LesionLocationCategory.BRAIN)) {
            if (tumorDetails.hasBrainLesions() != null && !tumorDetails.hasBrainLesions()) {
                LOGGER.debug("  Overriding presence of brain lesions");
            }
            builder.hasBrainLesions(true);
        }

        if (matches.contains(LesionLocationCategory.CNS)) {
            if (tumorDetails.hasCnsLesions() != null && !tumorDetails.hasCnsLesions()) {
                LOGGER.debug("  Overriding presence of CNS lesions");
            }
            builder.hasCnsLesions(true);
        }

        if (matches.contains(LesionLocationCategory.LIVER)) {
            if (tumorDetails.hasLiverLesions() != null && !tumorDetails.hasLiverLesions()) {
                LOGGER.debug("  Overriding presence of liver lesions");
            }
            builder.hasLiverLesions(true);
        }

        if (matches.contains(LesionLocationCategory.BONE)) {
            if (tumorDetails.hasBoneLesions() != null && !tumorDetails.hasBoneLesions()) {
                LOGGER.debug("  Overriding presence of bone lesions");
            }
            builder.hasBoneLesions(true);
        }

        if (matches.contains(LesionLocationCategory.BONE)) {
            if (tumorDetails.hasBoneLesions() != null && !tumorDetails.hasBoneLesions()) {
                LOGGER.debug("  Overriding presence of bone lesions");
            }
            builder.hasBoneLesions(true);
        }

        if (matches.contains(LesionLocationCategory.LUNG)) {
            if (tumorDetails.hasLungLesions() != null && !tumorDetails.hasLungLesions()) {
                LOGGER.debug("  Overriding presence of lung lesions");
            }
            builder.hasLungLesions(true);
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
            Set<OncologicalHistoryConfig> configs = find(database.oncologicalHistoryConfigs(), input);
            if (configs.isEmpty()) {
                // Same input is curated twice, so need to check if used at other place.
                if (!input.trim().isEmpty() && find(database.secondPrimaryConfigs(), input).isEmpty()) {
                    LOGGER.warn(" Could not find oncological history config for input '{}'", input);
                }
            } else {
                for (OncologicalHistoryConfig config : configs) {
                    if (!config.ignore()) {
                        priorTumorTreatments.add(config.curated());
                    }
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
            Set<SecondPrimaryConfig> configs = find(database.secondPrimaryConfigs(), input);
            if (configs.isEmpty() && !input.trim().isEmpty()) {
                // Same input is curated twice, so need to check if used at other place.
                if (find(database.oncologicalHistoryConfigs(), input).isEmpty()) {
                    LOGGER.warn(" Could not find second primary config for input '{}'", input);
                }
            }

            for (SecondPrimaryConfig config : configs) {
                if (!config.ignore()) {
                    priorSecondPrimaries.add(config.curated());
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
            Set<NonOncologicalHistoryConfig> configs = find(database.nonOncologicalHistoryConfigs(), input);
            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find non-oncological history config for input '{}'", input);
            }

            for (NonOncologicalHistoryConfig config : configs) {
                if (!config.ignore() && config.curated() instanceof PriorOtherCondition) {
                    priorOtherConditions.add((PriorOtherCondition) config.curated());
                }
            }
        }

        return priorOtherConditions;
    }

    @NotNull
    public List<PriorMolecularTest> curatePriorMolecularTests(@Nullable List<String> inputs) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<PriorMolecularTest> priorMolecularTests = Lists.newArrayList();
        for (String input : inputs) {
            Set<MolecularTestConfig> configs = find(database.molecularTestConfigs(), input);
            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find molecular test config for input '{}'", input);
            }

            for (MolecularTestConfig config : configs) {
                if (!config.ignore()) {
                    priorMolecularTests.add(config.curated());
                }
            }
        }

        return priorMolecularTests;
    }

    @NotNull
    public List<Complication> curateComplications(@Nullable List<String> inputs) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<Complication> complications = Lists.newArrayList();
        for (String input : inputs) {
            String reformatted = CurationUtil.capitalizeFirstLetterOnly(input);
            Set<ComplicationConfig> configs = find(database.complicationConfigs(), reformatted);

            if (configs.isEmpty()) {
                complications.add(ImmutableComplication.builder().name(reformatted).build());
            }

            for (ComplicationConfig config : configs) {
                complications.add(ImmutableComplication.builder().from(config.curated()).build());
            }
        }

        // Add an entry if there is nothing known about complications.
        if (complications.isEmpty()) {
            complications.add(ImmutableComplication.builder().name("Unknown").build());
        }

        return complications;
    }

    @NotNull
    public List<Toxicity> curateQuestionnaireToxicities(@Nullable List<String> inputs, @NotNull LocalDate date) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<Toxicity> toxicities = Lists.newArrayList();
        for (String input : inputs) {
            Set<ToxicityConfig> configs = find(database.toxicityConfigs(), input);
            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find toxicity config for input '{}'", input);
            }

            for (ToxicityConfig config : configs) {
                if (!config.ignore()) {
                    toxicities.add(ImmutableToxicity.builder()
                            .name(config.name())
                            .evaluatedDate(date)
                            .source(ToxicitySource.QUESTIONNAIRE)
                            .grade(config.grade())
                            .build());
                }
            }
        }

        return toxicities;
    }

    @Nullable
    public ECG curateECG(@Nullable ECG input) {
        if (input == null) {
            return null;
        }

        Set<ECGConfig> configs = find(database.ecgConfigs(), input.aberrationDescription());

        // Assume ECGs can also be pass-through.
        if (configs.isEmpty()) {
            return input;
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple ECG configs matched to '{}'", input.aberrationDescription());
            return null;
        }

        ECGConfig config = configs.iterator().next();
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
    }

    @Nullable
    public InfectionStatus curateInfectionStatus(@Nullable InfectionStatus input) {
        if (input == null) {
            return null;
        }

        Set<InfectionConfig> configs = find(database.infectionConfigs(), input.description());

        // Assume infections can also be pass-through.
        if (configs.isEmpty()) {
            return input;
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple infection configs matched to '{}'", input.description());
            return null;
        }

        InfectionConfig config = configs.iterator().next();
        return ImmutableInfectionStatus.builder().from(input).description(config.interpretation()).build();
    }

    @Nullable
    public Double determineLVEF(@Nullable List<String> inputs) {
        if (inputs == null) {
            return null;
        }

        for (String input : inputs) {
            Set<NonOncologicalHistoryConfig> configs = find(database.nonOncologicalHistoryConfigs(), input);
            for (NonOncologicalHistoryConfig config : configs) {
                if (!config.ignore() && config.curated() instanceof Double) {
                    return (Double) config.curated();
                }
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
            Set<LesionLocationConfig> configs = find(database.lesionLocationConfigs(), reformatted);
            if (configs.isEmpty()) {
                curatedOtherLesions.add(reformatted);
            }

            for (LesionLocationConfig config : configs) {
                if (config.category() == null && !config.location().isEmpty()) {
                    // We only want to include lesions from the other lesions in actual other lesions if there is no category assigned.
                    curatedOtherLesions.add(config.location());
                }
            }
        }

        return curatedOtherLesions;
    }

    @Nullable
    public String curateBiopsyLocation(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String reformatted = CurationUtil.capitalizeFirstLetterOnly(input);

        Set<LesionLocationConfig> configs = find(database.lesionLocationConfigs(), reformatted);
        if (configs.isEmpty()) {
            return reformatted;
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple lesion location configs matched for biopsy location '{}'", reformatted);
            return null;
        }

        return configs.iterator().next().location();
    }

    @Nullable
    public Medication curateMedicationDosage(@NotNull String input) {
        Set<MedicationDosageConfig> configs = find(database.medicationDosageConfigs(), input);

        if (configs.isEmpty()) {
            // TODO: Change to warn once the medications are more final.
            LOGGER.debug(" Could not find medication dosage config for '{}'", input);
            return null;
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple medication dosage configs matched to '{}'", input);
            return null;
        }

        MedicationDosageConfig config = configs.iterator().next();
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

    @Nullable
    public String curateMedicationName(@NotNull String input) {
        if (input.isEmpty()) {
            return null;
        }

        Set<MedicationNameConfig> configs = find(database.medicationNameConfigs(), input);
        if (configs.isEmpty()) {
            LOGGER.warn(" Could not find medication name config for '{}'", input);
            return null;
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple medication name configs founds for medication input '{}'", input);
            return null;
        }

        MedicationNameConfig config = configs.iterator().next();
        return !config.ignore() ? config.name() : null;
    }

    @Nullable
    public MedicationStatus curateMedicationStatus(@NotNull String status) {
        if (status.isEmpty()) {
            return null;
        }

        if (status.equalsIgnoreCase("active")) {
            return MedicationStatus.ACTIVE;
        } else if (status.equalsIgnoreCase("on-hold")) {
            return MedicationStatus.ON_HOLD;
        } else if (status.equalsIgnoreCase("kuur geannuleerd")) {
            return MedicationStatus.CANCELLED;
        } else {
            LOGGER.warn(" Could not interpret medication status: {}", status);
            return MedicationStatus.UNKNOWN;
        }
    }

    @NotNull
    public Medication annotateWithMedicationCategory(@NotNull Medication medication) {
        return ImmutableMedication.builder().from(medication).categories(lookupCategories(medication.name())).build();
    }

    @NotNull
    private Set<String> lookupCategories(@NotNull String medication) {
        Set<MedicationCategoryConfig> configs = find(database.medicationCategoryConfigs(), medication);

        if (configs.isEmpty()) {
            LOGGER.warn(" Could not find medication category config for '{}'", medication);
            return Sets.newHashSet();
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple category configs found for medication with name '{}'", medication);
            return Sets.newHashSet();
        }

        return configs.iterator().next().categories();
    }

    @NotNull
    public Intolerance curateIntolerance(@NotNull Intolerance intolerance) {
        String reformatted = CurationUtil.capitalizeFirstLetterOnly(intolerance.name());

        Set<IntoleranceConfig> configs = find(database.intoleranceConfigs(), reformatted);

        String name = reformatted;
        ImmutableIntolerance.Builder builder = ImmutableIntolerance.builder().from(intolerance);
        if (configs.isEmpty()) {
            LOGGER.warn(" Could not find intolerance config for '{}'", reformatted);
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple intolerance configs for intolerance with name '{}'", reformatted);
        } else {
            IntoleranceConfig config = configs.iterator().next();
            name = config.name();
            builder.name(name).doids(config.doids());
        }

        if (intolerance.category().equalsIgnoreCase("medication")) {
            builder.subcategories(lookupCategories(name));
        }

        return builder.build();
    }

    @NotNull
    public LabValue translateLabValue(@NotNull LabValue input) {
        LaboratoryTranslation translation = findLaboratoryTranslation(input);

        if (translation == null) {
            return input;
        }

        evaluatedTranslations.put(LaboratoryTranslation.class, translation);
        return ImmutableLabValue.builder().from(input).code(translation.translatedCode()).name(translation.translatedName()).build();
    }

    @Nullable
    private LaboratoryTranslation findLaboratoryTranslation(@NotNull LabValue input) {
        String trimmedName = input.name().trim();
        for (LaboratoryTranslation entry : database.laboratoryTranslations()) {
            if (entry.code().equals(input.code()) && entry.name().equals(trimmedName)) {
                return entry;
            }
        }

        LOGGER.warn(" Could not find laboratory translation for lab value with code '{}' and name '{}'", input.code(), trimmedName);
        return null;
    }

    @NotNull
    public BloodTransfusion translateBloodTransfusion(@NotNull BloodTransfusion input) {
        BloodTransfusionTranslation translation = findBloodTransfusionTranslation(input);

        if (translation == null) {
            return input;
        }

        evaluatedTranslations.put(BloodTransfusionTranslation.class, translation);
        return ImmutableBloodTransfusion.builder().from(input).product(translation.translatedProduct()).build();
    }

    @Nullable
    private BloodTransfusionTranslation findBloodTransfusionTranslation(@NotNull BloodTransfusion input) {
        String trimmedProduct = input.product().trim();
        for (BloodTransfusionTranslation entry : database.bloodTransfusionTranslations()) {
            if (entry.product().equals(trimmedProduct)) {
                return entry;
            }
        }

        LOGGER.warn(" Could not find blood transfusion translation for blood transfusion with product '{}'", trimmedProduct);
        return null;
    }

    public void evaluate() {
        int warnCount = 0;
        for (Map.Entry<Class<? extends CurationConfig>, Collection<String>> entry : evaluatedCurationInputs.asMap().entrySet()) {
            List<? extends CurationConfig> configs = configsForClass(entry.getKey());
            Collection<String> evaluated = entry.getValue();
            for (CurationConfig config : configs) {
                // TODO: Raise warnings for unused medication dosage once more final
                if (!evaluated.contains(config.input()) && !(config instanceof ImmutableMedicationDosageConfig)) {
                    warnCount++;
                    LOGGER.warn(" Curation key '{}' not used for class {}", config.input(), entry.getKey().getSimpleName());
                }
            }
        }

        for (Map.Entry<Class<? extends Translation>, Collection<Translation>> entry : evaluatedTranslations.asMap().entrySet()) {
            List<? extends Translation> translations = translationsForClass(entry.getKey());
            Collection<Translation> evaluated = entry.getValue();
            for (Translation translation : translations) {
                if (!evaluated.contains(translation) && !(translation instanceof ImmutableBloodTransfusionTranslation)) {
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
        } else if (classToLookUp == ImmutableOncologicalHistoryConfig.class) {
            return database.oncologicalHistoryConfigs();
        } else if (classToLookUp == ImmutableSecondPrimaryConfig.class) {
            return database.secondPrimaryConfigs();
        } else if (classToLookUp == ImmutableLesionLocationConfig.class) {
            return database.lesionLocationConfigs();
        } else if (classToLookUp == ImmutableNonOncologicalHistoryConfig.class) {
            return database.nonOncologicalHistoryConfigs();
        } else if (classToLookUp == ImmutableComplicationConfig.class) {
            return database.complicationConfigs();
        } else if (classToLookUp == ImmutableECGConfig.class) {
            return database.ecgConfigs();
        } else if (classToLookUp == ImmutableInfectionConfig.class) {
            return database.infectionConfigs();
        } else if (classToLookUp == ImmutableToxicityConfig.class) {
            return database.toxicityConfigs();
        } else if (classToLookUp == ImmutableMolecularTestConfig.class) {
            return database.molecularTestConfigs();
        } else if (classToLookUp == ImmutableMedicationNameConfig.class) {
            return database.medicationNameConfigs();
        } else if (classToLookUp == ImmutableMedicationDosageConfig.class) {
            return database.medicationDosageConfigs();
        } else if (classToLookUp == ImmutableMedicationCategoryConfig.class) {
            return database.medicationCategoryConfigs();
        } else if (classToLookUp == ImmutableIntoleranceConfig.class) {
            return database.intoleranceConfigs();
        }
        throw new IllegalStateException("Class not found in curation database: " + classToLookUp);
    }

    @NotNull
    private List<? extends Translation> translationsForClass(@NotNull Class<? extends Translation> classToLookup) {
        if (classToLookup == LaboratoryTranslation.class) {
            return database.laboratoryTranslations();
        } else if (classToLookup == BloodTransfusionTranslation.class) {
            return database.bloodTransfusionTranslations();
        }

        throw new IllegalStateException("Class not found in curation database: " + classToLookup);
    }

    @NotNull
    private <T extends CurationConfig> Set<T> find(@NotNull List<T> configs, @NotNull String input) {
        Set<T> results = Sets.newHashSet();
        if (!configs.isEmpty()) {
            String trimmed = CurationUtil.fullTrim(input);
            evaluatedCurationInputs.put(configs.get(0).getClass(), trimmed);
            for (T config : configs) {
                if (config.input().equals(trimmed)) {
                    results.add(config);
                }
            }
        }
        return results;
    }
}
