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
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslation;
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.ImmutableBloodTransfusionTranslation;
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation;
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslation;
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
            String inputPrimaryTumor = CurationUtil.fullTrim(inputTumorLocation + " | " + inputTumorType);

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
            Set<LesionLocationConfig> configs = find(database.lesionLocationConfigs(), lesion);
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
            String trimmedInput = CurationUtil.fullTrim(input);
            Set<OncologicalHistoryConfig> configs = find(database.oncologicalHistoryConfigs(), trimmedInput);
            if (configs.isEmpty()) {
                // Same input is curated twice, so need to check if used at other place.
                if (!trimmedInput.isEmpty() && find(database.secondPrimaryConfigs(), trimmedInput).isEmpty()) {
                    LOGGER.warn(" Could not find oncological history config for input '{}'", trimmedInput);
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
            String trimmedInput = CurationUtil.fullTrim(input);
            Set<SecondPrimaryConfig> configs = find(database.secondPrimaryConfigs(), trimmedInput);
            if (configs.isEmpty() && !trimmedInput.isEmpty()) {
                // Same input is curated twice, so need to check if used at other place.
                if (find(database.oncologicalHistoryConfigs(), trimmedInput).isEmpty()) {
                    LOGGER.warn(" Could not find second primary config for input '{}'", trimmedInput);
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
            String trimmedInput = CurationUtil.fullTrim(input);
            Set<NonOncologicalHistoryConfig> configs = find(database.nonOncologicalHistoryConfigs(), trimmedInput);
            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find non-oncological history config for input '{}'", trimmedInput);
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
    public List<PriorMolecularTest> curatePriorMolecularTests(@NotNull String type, @Nullable List<String> inputs) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<PriorMolecularTest> priorMolecularTests = Lists.newArrayList();
        for (String input : inputs) {
            String trimmedInput = CurationUtil.fullTrim(input);
            Set<MolecularTestConfig> configs = find(database.molecularTestConfigs(), trimmedInput);
            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find molecular test config for input '{}: {}'", type, trimmedInput);
            }

            for (MolecularTestConfig config : configs) {
                if (!config.ignore()) {
                    priorMolecularTests.add(config.curated());
                }
            }
        }

        return priorMolecularTests;
    }

    @Nullable
    public List<Complication> curateComplications(@Nullable List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return null;
        }

        List<Complication> complications = Lists.newArrayList();
        int unknownStateCount = 0;
        int validInputCount = 0;
        for (String input : inputs) {
            Set<ComplicationConfig> configs = find(database.complicationConfigs(), input);

            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find complication config for input '{}'", input);
            } else {
                validInputCount++;
            }

            if (hasConfigImplyingUnknownState(configs)) {
                unknownStateCount++;
            }

            for (ComplicationConfig config : configs) {
                if (!config.ignore()) {
                    complications.add(ImmutableComplication.builder().from(config.curated()).build());
                }
            }
        }

        // If there are complications but every single one of them implies an unknown state, return null
        if (unknownStateCount == validInputCount) {
            return null;
        }

        return complications;
    }

    private static boolean hasConfigImplyingUnknownState(@NotNull Set<ComplicationConfig> configs) {
        for (ComplicationConfig config : configs) {
            if (config.impliesUnknownComplicationState()) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public List<Toxicity> curateQuestionnaireToxicities(@Nullable List<String> inputs, @NotNull LocalDate date) {
        if (inputs == null) {
            return Lists.newArrayList();
        }

        List<Toxicity> toxicities = Lists.newArrayList();
        for (String input : inputs) {
            String trimmedInput = CurationUtil.fullTrim(input);
            Set<ToxicityConfig> configs = find(database.toxicityConfigs(), trimmedInput);
            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find toxicity config for input '{}'", trimmedInput);
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
        if (input == null || input.aberrationDescription() == null) {
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
            String description = !config.interpretation().isEmpty() ? config.interpretation() : null;
            return ImmutableECG.builder()
                    .from(input)
                    .aberrationDescription(description)
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
            Set<LesionLocationConfig> configs = find(database.lesionLocationConfigs(), lesion);
            if (configs.isEmpty()) {
                LOGGER.warn(" Could not find lesion config for input '{}'", lesion);
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

        Set<LesionLocationConfig> configs = find(database.lesionLocationConfigs(), input);
        if (configs.isEmpty()) {
            LOGGER.warn(" Could not find lesion config for biopsy location '{}'", input);
            return null;
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple lesion location configs matched for biopsy location '{}'", input);
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
        String trimmedInput = CurationUtil.fullTrim(input);

        if (trimmedInput.isEmpty()) {
            return null;
        }

        Set<MedicationNameConfig> configs = find(database.medicationNameConfigs(), trimmedInput);
        if (configs.isEmpty()) {
            LOGGER.warn(" Could not find medication name config for '{}'", trimmedInput);
            return null;
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple medication name configs founds for medication input '{}'", trimmedInput);
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
        return ImmutableMedication.builder()
                .from(medication)
                .categories(lookupMedicationCategories("medication", medication.name()))
                .build();
    }

    @NotNull
    private Set<String> lookupMedicationCategories(@NotNull String source, @NotNull String medication) {
        String trimmedMedication = CurationUtil.fullTrim(medication);
        Set<MedicationCategoryConfig> configs = find(database.medicationCategoryConfigs(), trimmedMedication);

        if (configs.isEmpty()) {
            LOGGER.warn(" Could not find medication category config for {} with name '{}'", source, trimmedMedication);
            return Sets.newHashSet();
        } else if (configs.size() > 1) {
            LOGGER.warn(" Multiple category configs found for {} with name '{}'", source, trimmedMedication);
            return Sets.newHashSet();
        }

        return configs.iterator().next().categories();
    }

    @Nullable
    public String translateAdministrationRoute(@Nullable String administrationRoute) {
        if (administrationRoute == null || administrationRoute.isEmpty()) {
            return null;
        }

        String trimmedAdministrationRoute = administrationRoute.trim();
        AdministrationRouteTranslation translation = findAdministrationRouteTranslation(trimmedAdministrationRoute);

        if (translation == null) {
            LOGGER.warn("No translation found for medication administration route: '{}'", trimmedAdministrationRoute);
            return null;
        }

        evaluatedTranslations.put(AdministrationRouteTranslation.class, translation);
        return !translation.translatedAdministrationRoute().isEmpty() ? translation.translatedAdministrationRoute() : null;
    }

    @Nullable
    private AdministrationRouteTranslation findAdministrationRouteTranslation(@NotNull String administrationRoute) {
        for (AdministrationRouteTranslation entry : database.administrationRouteTranslations()) {
            if (entry.administrationRoute().equals(administrationRoute)) {
                return entry;
            }
        }

        return null;
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
            builder.subcategories(lookupMedicationCategories("intolerance", name));
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
    public Toxicity translateToxicity(@NotNull Toxicity input) {
        ToxicityTranslation translation = findToxicityTranslation(input);

        if (translation == null) {
            LOGGER.warn("Could not find translation for toxicity with input '{}'", input.name());
            return input;
        }

        evaluatedTranslations.put(ToxicityTranslation.class, translation);
        return ImmutableToxicity.builder().from(input).name(translation.translatedToxicity()).build();
    }

    @Nullable
    private ToxicityTranslation findToxicityTranslation(@NotNull Toxicity input) {
        String trimmedToxicity = input.name().trim();
        for (ToxicityTranslation entry : database.toxicityTranslations()) {
            if (entry.toxicity().equals(trimmedToxicity)) {
                return entry;
            }
        }

        // No warn since not all toxicities need to be translated.
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
                if (!evaluated.contains(config.input().toLowerCase()) && !(config instanceof ImmutableMedicationDosageConfig)) {
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
        if (classToLookup == AdministrationRouteTranslation.class) {
            return database.administrationRouteTranslations();
        } else if (classToLookup == LaboratoryTranslation.class) {
            return database.laboratoryTranslations();
        } else if (classToLookup == ToxicityTranslation.class) {
            return database.toxicityTranslations();
        } else if (classToLookup == BloodTransfusionTranslation.class) {
            return database.bloodTransfusionTranslations();
        }

        throw new IllegalStateException("Class not found in curation database: " + classToLookup);
    }

    @NotNull
    private <T extends CurationConfig> Set<T> find(@NotNull List<T> configs, @NotNull String input) {
        Set<T> results = Sets.newHashSet();
        if (!configs.isEmpty()) {
            evaluatedCurationInputs.put(configs.get(0).getClass(), input.toLowerCase());
            for (T config : configs) {
                if (config.input().equalsIgnoreCase(input)) {
                    results.add(config);
                }
            }
        }
        return results;
    }
}
