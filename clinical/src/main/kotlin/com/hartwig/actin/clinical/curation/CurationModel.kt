package com.hartwig.actin.clinical.curation

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.HashMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.correction.QuestionnaireRawEntryMapper
import com.hartwig.actin.clinical.curation.CurationUtil.fullTrim
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslation
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation
import com.hartwig.actin.clinical.curation.translation.DosageUnitTranslation
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslation
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableECGMeasure
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.doid.DoidModel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Strings
import java.io.IOException
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

class CurationModel @VisibleForTesting internal constructor(
    private val database: CurationDatabase,
    questionnaireRawEntryMapper: QuestionnaireRawEntryMapper
) {
    private val questionnaireRawEntryMapper: QuestionnaireRawEntryMapper
    private val evaluatedCurationInputs: Multimap<Class<out CurationConfig>, String> =
        HashMultimap.create<Class<out CurationConfig>, String>()
    private val evaluatedTranslations: Multimap<Class<out Translation>, Translation> =
        HashMultimap.create<Class<out Translation>, Translation>()
    private val warnings = mutableListOf<CurationWarning>()

    init {
        this.questionnaireRawEntryMapper = questionnaireRawEntryMapper
    }

    fun curateTumorDetails(patientId: String, inputTumorLocation: String?, inputTumorType: String?): TumorDetails {
        var primaryTumorConfig: PrimaryTumorConfig? = null
        if (inputTumorLocation != null || inputTumorType != null) {
            val inputTumorLocationString = inputTumorLocation ?: Strings.EMPTY
            val inputTumorTypeString = inputTumorType ?: Strings.EMPTY
            val inputPrimaryTumor = fullTrim("$inputTumorLocationString | $inputTumorTypeString")
            val configs: Set<PrimaryTumorConfig?> = find(database.primaryTumorConfigs, inputPrimaryTumor)
            if (configs.isEmpty()) {
                warnings.add(
                    CurationWarning(
                        patientId,
                        CurationCategory.PRIMARY_TUMOR,
                        inputPrimaryTumor,
                        "Could not find primary tumor config for input '$inputPrimaryTumor'"
                    )
                )
            } else if (configs.size > 1) {
                warnings.add(
                    CurationWarning(
                        patientId,
                        CurationCategory.PRIMARY_TUMOR,
                        inputPrimaryTumor,
                        "Primary tumor '$inputPrimaryTumor' matched to multiple configs!"
                    )
                )
            } else {
                primaryTumorConfig = configs.iterator().next()
            }
        }

        return if (primaryTumorConfig == null) {
            ImmutableTumorDetails.builder().build()
        } else {
            ImmutableTumorDetails.builder()
                .primaryTumorLocation(primaryTumorConfig.primaryTumorLocation)
                .primaryTumorSubLocation(primaryTumorConfig.primaryTumorSubLocation)
                .primaryTumorType(primaryTumorConfig.primaryTumorType)
                .primaryTumorSubType(primaryTumorConfig.primaryTumorSubType)
                .primaryTumorExtraDetails(primaryTumorConfig.primaryTumorExtraDetails)
                .doids(primaryTumorConfig.doids)
                .build()
        }
    }

    fun overrideKnownLesionLocations(
        tumorDetails: TumorDetails, biopsyLocation: String?,
        otherLesions: List<String>?
    ): TumorDetails {
        val matches: MutableSet<LesionLocationCategory> = Sets.newHashSet<LesionLocationCategory>()
        val lesionsToCheck: MutableList<String> = Lists.newArrayList()
        if (otherLesions != null) {
            lesionsToCheck.addAll(otherLesions)
        }

        if (biopsyLocation != null) {
            lesionsToCheck.add(biopsyLocation)
        }

        for (lesion in lesionsToCheck) {
            val configs: Set<LesionLocationConfig> = find(database.lesionLocationConfigs, lesion)
            for (config in configs) {
                if (config.category != null) {
                    matches.add(config.category)
                }
            }
        }

        if (matches.isEmpty()) {
            return tumorDetails
        }

        val builder: ImmutableTumorDetails.Builder = ImmutableTumorDetails.builder().from(tumorDetails)
        if (matches.contains(LesionLocationCategory.BRAIN)) {
            if (tumorDetails.hasBrainLesions() == false) {
                LOGGER.debug("  Overriding presence of brain lesions")
            }
            builder.hasBrainLesions(true)
        }
        if (matches.contains(LesionLocationCategory.CNS)) {
            if (tumorDetails.hasCnsLesions() == false) {
                LOGGER.debug("  Overriding presence of CNS lesions")
            }
            builder.hasCnsLesions(true)
        }
        if (matches.contains(LesionLocationCategory.LIVER)) {
            if (tumorDetails.hasLiverLesions() == false) {
                LOGGER.debug("  Overriding presence of liver lesions")
            }
            builder.hasLiverLesions(true)
        }
        if (matches.contains(LesionLocationCategory.BONE)) {
            if (tumorDetails.hasBoneLesions() == false) {
                LOGGER.debug("  Overriding presence of bone lesions")
            }
            builder.hasBoneLesions(true)
        }
        if (matches.contains(LesionLocationCategory.LUNG)) {
            if (tumorDetails.hasLungLesions() == false) {
                LOGGER.debug("  Overriding presence of lung lesions")
            }
            builder.hasLungLesions(true)
        }
        if (matches.contains(LesionLocationCategory.LYMPH_NODE)) {
            if (tumorDetails.hasLymphNodeLesions() == false) {
                LOGGER.debug("  Overriding presence of lymph node lesions")
            }
            builder.hasLymphNodeLesions(true)
        }
        return builder.build()
    }

    fun curateTreatmentHistoryEntry(patientId: String, entry: String): List<TreatmentHistoryEntry> {
        val trimmedInput = fullTrim(entry)
        val treatmentHistoryEntryConfigs = find(database.treatmentHistoryEntryConfigs, trimmedInput)
        if (treatmentHistoryEntryConfigs.isEmpty()) {
            // Same input is curated twice, so need to check if used at other place.
            if (trimmedInput.isNotEmpty() && find(database.secondPrimaryConfigs, trimmedInput).isEmpty()) {
                warnings.add(
                    CurationWarning(
                        patientId,
                        CurationCategory.ONCOLOGICAL_HISTORY,
                        trimmedInput,
                        "Could not find treatment history or second primary config for input '$trimmedInput'"
                    )
                )
            }
        }
        return treatmentHistoryEntryConfigs.filter { !it.ignore }.mapNotNull { it.curated }
    }

    fun curatePriorSecondPrimaries(patientId: String, inputs: List<String>?): List<PriorSecondPrimary> {
        if (inputs == null) {
            return Lists.newArrayList<PriorSecondPrimary>()
        }

        val priorSecondPrimaries: MutableList<PriorSecondPrimary> = Lists.newArrayList<PriorSecondPrimary>()
        for (input in inputs) {
            val trimmedInput = fullTrim(input)
            val configs: Set<SecondPrimaryConfig> = find(database.secondPrimaryConfigs, trimmedInput)
            if (configs.isEmpty()) {
                // Same input is curated twice, so need to check if used at other place.
                if (trimmedInput.isNotEmpty() && find(database.treatmentHistoryEntryConfigs, trimmedInput).isEmpty()) {
                    warnings.add(
                        CurationWarning(
                            patientId,
                            CurationCategory.SECOND_PRIMARY,
                            trimmedInput,
                            "Could not find second primary or treatment history config for input '$trimmedInput'"
                        )
                    )
                }
            }
            for (config in configs) {
                if (!config.ignore) {
                    priorSecondPrimaries.add(config.curated!!)
                }
            }
        }
        return priorSecondPrimaries
    }

    fun curatePriorOtherConditions(patientId: String, inputs: List<String>?): List<PriorOtherCondition> {
        if (inputs == null) {
            return Lists.newArrayList<PriorOtherCondition>()
        }

        val priorOtherConditions: MutableList<PriorOtherCondition> = Lists.newArrayList<PriorOtherCondition>()
        for (input in inputs) {
            val trimmedInput = fullTrim(input)
            val configs: Set<NonOncologicalHistoryConfig> = find(database.nonOncologicalHistoryConfigs, trimmedInput)
            if (configs.isEmpty()) {
                warnings.add(
                    CurationWarning(
                        patientId,
                        CurationCategory.NON_ONCOLOGICAL_HISTORY,
                        trimmedInput,
                        "Could not find non-oncological history config for input '$trimmedInput'"
                    )
                )
            }
            configs
                .filter { config: NonOncologicalHistoryConfig -> !config.ignore }
                .mapNotNull { obj: NonOncologicalHistoryConfig -> obj.priorOtherCondition.getOrNull() }
                .forEach { e: PriorOtherCondition -> priorOtherConditions.add(e) }
        }
        return priorOtherConditions
    }

    fun curatePriorMolecularTests(patientId: String, type: String, inputs: List<String>?): List<PriorMolecularTest> {
        if (inputs == null) {
            return Lists.newArrayList<PriorMolecularTest>()
        }

        val priorMolecularTests: MutableList<PriorMolecularTest> = Lists.newArrayList<PriorMolecularTest>()
        for (input in inputs) {
            val trimmedInput = fullTrim(input)
            val configs: Set<MolecularTestConfig> = find(database.molecularTestConfigs, trimmedInput)
            if (configs.isEmpty()) {
                warnings.add(
                    CurationWarning(
                        patientId,
                        CurationCategory.MOLECULAR_TEST,
                        trimmedInput,
                        "Could not find molecular test config for type '$type' with input: '$trimmedInput'"
                    )
                )
            }
            for (config in configs) {
                if (!config.ignore) {
                    priorMolecularTests.add(config.curated!!)
                }
            }
        }
        return priorMolecularTests
    }

    fun curateComplications(patientId: String, inputs: List<String>?): List<Complication>? {
        if (inputs.isNullOrEmpty()) {
            return null
        }

        val complications: MutableList<Complication> = Lists.newArrayList()
        var unknownStateCount = 0
        var validInputCount = 0
        for (input in inputs) {
            val configs: Set<ComplicationConfig> = find(database.complicationConfigs, input)
            if (configs.isEmpty()) {
                warnings.add(
                    CurationWarning(
                        patientId,
                        CurationCategory.COMPLICATION,
                        input,
                        "Could not find complication config for input '$input'"
                    )
                )
            } else {
                validInputCount++
            }
            if (hasConfigImplyingUnknownState(configs)) {
                unknownStateCount++
            }
            for (config in configs) {
                if (!config.ignore) {
                    complications.add(ImmutableComplication.builder().from(config.curated!!).build())
                }
            }
        }

        // If there are complications but every single one of them implies an unknown state, return null
        return if (unknownStateCount == validInputCount) {
            null
        } else complications
    }

    fun curateQuestionnaireToxicities(patientId: String, inputs: List<String>?, date: LocalDate): List<Toxicity> {
        return inputs?.flatMap { input ->
            val trimmedInput = fullTrim(input)
            val configs: Set<ToxicityConfig> = find(database.toxicityConfigs, trimmedInput)
            if (configs.isEmpty()) {
                warnings.add(
                    CurationWarning(
                        patientId,
                        CurationCategory.TOXICITY,
                        trimmedInput,
                        "Could not find toxicity config for input '$trimmedInput'"
                    )
                )
            }
            configs.filterNot(ToxicityConfig::ignore)
                .map { config ->
                    ImmutableToxicity.builder()
                        .name(config.name)
                        .categories(config.categories)
                        .evaluatedDate(date)
                        .source(ToxicitySource.QUESTIONNAIRE)
                        .grade(config.grade)
                        .build()
                }
        } ?: emptyList()
    }

    fun curateECG(patientId: String, input: ECG?): ECG? {
        if (input?.aberrationDescription() == null) {
            return null
        }

        val configs: Set<ECGConfig> = find(database.ecgConfigs, input.aberrationDescription()!!)
        if (configs.isEmpty()) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.ECG,
                    input.aberrationDescription()!!,
                    "Could not find ECG config for input '${input.aberrationDescription()}'"
                )
            )
            return input
        } else if (configs.size > 1) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.ECG,
                    input.aberrationDescription()!!,
                    "Multiple ECG configs matched to '${input.aberrationDescription()}'"
                )
            )
            return null
        }
        val config: ECGConfig = configs.iterator().next()
        if (config.ignore) {
            return null
        }

        val description: String? = config.interpretation.ifEmpty { null }
        return ImmutableECG.builder()
            .from(input)
            .aberrationDescription(description)
            .qtcfMeasure(maybeECGMeasure(config.qtcfValue, config.qtcfUnit))
            .jtcMeasure(maybeECGMeasure(config.jtcValue, config.jtcUnit))
            .build()
    }

    fun curateInfectionStatus(patientId: String, input: InfectionStatus?): InfectionStatus? {
        if (input?.description() == null) {
            return null
        }

        val configs: Set<InfectionConfig> = find(database.infectionConfigs, input.description()!!)
        if (configs.isEmpty()) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.INFECTION,
                    input.description()!!,
                    "Could not find infection config for input '${input.description()}'"
                )
            )
            return input
        } else if (configs.size > 1) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.INFECTION,
                    input.description()!!,
                    "Multiple infection configs matched to '${input.description()}'"
                )
            )
            return null
        }
        val config: InfectionConfig = configs.iterator().next()
        if (config.ignore) {
            return null
        }
        val description: String? = config.interpretation.ifEmpty { null }

        return ImmutableInfectionStatus.builder().from(input).description(description).build()
    }

    fun curatePeriodBetweenUnit(patientId: String, input: String?): String? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val configs: Set<PeriodBetweenUnitConfig> = find(database.periodBetweenUnitConfigs, input)
        if (configs.isEmpty()) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION,
                    input,
                    "Could not find period between unit config for input '$input'"
                )
            )
            return input
        } else if (configs.size > 1) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION,
                    input,
                    "Multiple period between unit configs matched to '$input'"
                )
            )
            return null
        }
        val config: PeriodBetweenUnitConfig = configs.first()
        if (config.ignore) {
            return null
        }
        return config.interpretation
    }

    fun determineLVEF(inputs: List<String>?): Double? {
        return inputs?.flatMap { input: String -> find(database.nonOncologicalHistoryConfigs, input) }
            ?.filterNot { it.ignore }
            ?.map { it.lvef }
            ?.find { it.isPresent }
            ?.get()
    }

    fun curateOtherLesions(patientId: String, otherLesions: List<String>?): List<String>? {
        if (otherLesions == null) {
            return null
        }

        val curatedOtherLesions: MutableList<String> = Lists.newArrayList()
        for (lesion in otherLesions) {
            val configs: Set<LesionLocationConfig> = find(database.lesionLocationConfigs, lesion)
            if (configs.isEmpty()) {
                warnings.add(
                    CurationWarning(
                        patientId,
                        CurationCategory.LESION_LOCATION,
                        lesion,
                        "Could not find lesion config for input '$lesion'"
                    )
                )
            }
            for (config in configs) {
                // We only want to include lesions from the other lesions in actual other lesions
                // if it does not override an explicit lesion location
                val hasRealOtherLesion = config.category == null || config.category == LesionLocationCategory.LYMPH_NODE
                if (hasRealOtherLesion && config.location.isNotEmpty()) {
                    curatedOtherLesions.add(config.location)
                }
            }
        }
        return curatedOtherLesions
    }

    fun curateBiopsyLocation(patientId: String, input: String?): String? {
        if (input.isNullOrEmpty()) {
            return null
        }

        val configs: Set<LesionLocationConfig> = find(database.lesionLocationConfigs, input)
        if (configs.isEmpty()) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.LESION_LOCATION,
                    input,
                    "Could not find lesion config for biopsy location '$input'"
                )
            )
            return null
        } else if (configs.size > 1) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.LESION_LOCATION,
                    input,
                    "Multiple lesion location configs matched for biopsy location '$input'"
                )
            )
            return null
        }
        return configs.iterator().next().location
    }

    fun curateMedicationDosage(patientId: String, input: String): Dosage? {
        val configs: Set<MedicationDosageConfig> = find(database.medicationDosageConfigs, input)
        if (configs.isEmpty()) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.MEDICATION_DOSAGE,
                    input,
                    "Could not find medication dosage config for '$input'"
                )
            )
            return null
        } else if (configs.size > 1) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.MEDICATION_DOSAGE,
                    input,
                    "Multiple medication dosage configs matched to '$input'"
                )
            )
            return null
        }
        val config: MedicationDosageConfig = configs.first()

        return ImmutableDosage.builder()
            .dosageMin(config.dosageMin)
            .dosageMax(config.dosageMax)
            .dosageUnit(config.dosageUnit)
            .frequency(config.frequency)
            .frequencyUnit(config.frequencyUnit)
            .periodBetweenValue(config.periodBetweenValue)
            .periodBetweenUnit(config.periodBetweenUnit)
            .ifNeeded(config.ifNeeded)
            .build()
    }

    fun curateMedicationName(patientId: String, input: String): String? {
        val trimmedInput = fullTrim(input)
        if (trimmedInput.isEmpty()) {
            return null
        }

        val configs: Set<MedicationNameConfig> = find(database.medicationNameConfigs, trimmedInput)
        if (configs.isEmpty()) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.MEDICATION_NAME,
                    trimmedInput,
                    "Could not find medication name config for '$trimmedInput'"
                )
            )
            return null
        } else if (configs.size > 1) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.MEDICATION_NAME,
                    trimmedInput,
                    "Multiple medication name configs founds for medication input '$trimmedInput'"
                )
            )
            return null
        }
        val config: MedicationNameConfig = configs.iterator().next()

        return if (!config.ignore) config.name else null
    }

    fun curateMedicationStatus(patientId: String, status: String): MedicationStatus? {
        if (status.isEmpty()) {
            return null
        }

        return if (status.equals("active", ignoreCase = true)) {
            MedicationStatus.ACTIVE
        } else if (status.equals("on-hold", ignoreCase = true)) {
            MedicationStatus.ON_HOLD
        } else if (status.equals("kuur geannuleerd", ignoreCase = true)) {
            MedicationStatus.CANCELLED
        } else {
            LOGGER.warn("Could not interpret medication status: $status for patient $patientId")
            MedicationStatus.UNKNOWN
        }
    }

    fun curateMedicationCypInteractions(medicationName: String): List<CypInteraction> {
        return find(database.cypInteractionConfigs, medicationName).flatMap { it.interactions }
    }

    fun annotateWithQTProlongating(medicationName: String): QTProlongatingRisk {
        val riskConfigs = find(database.qtProlongingConfigs, medicationName)
        return if (riskConfigs.isEmpty()) {
            QTProlongatingRisk.NONE
        } else if (riskConfigs.size > 1) {
            throw IllegalStateException(
                "Multiple risk configurations found for one medication name [$medicationName]. " +
                        "Check the qt_prolongating.tsv for a duplicate"
            )
        } else {
            return riskConfigs.first().status
        }
    }

    fun translateAdministrationRoute(patientId: String, administrationRoute: String?): String? {
        if (administrationRoute.isNullOrEmpty()) {
            return null
        }

        val trimmedAdministrationRoute = administrationRoute.trim { it <= ' ' }
        val translation: AdministrationRouteTranslation? = findAdministrationRouteTranslation(trimmedAdministrationRoute)
        if (translation == null) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.ADMISTRATION_ROUTE_TRANSLATION,
                    trimmedAdministrationRoute,
                    "No translation found for medication administration route: '$trimmedAdministrationRoute'"
                )
            )
            return null
        }
        evaluatedTranslations.put(AdministrationRouteTranslation::class.java, translation)

        return translation.translatedAdministrationRoute.ifEmpty { null }
    }

    private fun findAdministrationRouteTranslation(administrationRoute: String): AdministrationRouteTranslation? {
        for (entry in database.administrationRouteTranslations) {
            if (entry.administrationRoute == administrationRoute) {
                return entry
            }
        }
        return null
    }

    fun curateIntolerance(patientId: String, intolerance: Intolerance): Intolerance {
        val reformatted = CurationUtil.capitalizeFirstLetterOnly(intolerance.name())
        val configs: Set<IntoleranceConfig> = find(database.intoleranceConfigs, reformatted)
        val builder: ImmutableIntolerance.Builder = ImmutableIntolerance.builder().from(intolerance)
        if (configs.isEmpty()) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.INTOLERANCE,
                    reformatted,
                    "Could not find intolerance config for '$reformatted'"
                )
            )
        } else if (configs.size > 1) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.INTOLERANCE,
                    reformatted,
                    "Multiple intolerance configs for intolerance with name '$reformatted'"
                )
            )
        } else {
            val config: IntoleranceConfig = configs.iterator().next()
            builder.name(config.name).doids(config.doids)
        }

        // TODO: add ATC code of medication to subcategories
        if (intolerance.category().equals("medication", ignoreCase = true)) {
            builder.subcategories(emptySet())
        }

        return builder.build()
    }

    fun translateLabValue(patientId: String, input: LabValue): LabValue {
        val translation: LaboratoryTranslation = findLaboratoryTranslation(patientId, input) ?: return input
        evaluatedTranslations.put(LaboratoryTranslation::class.java, translation)
        return ImmutableLabValue.builder().from(input).code(translation.translatedCode).name(translation.translatedName).build()
    }

    private fun findLaboratoryTranslation(patientId: String, input: LabValue): LaboratoryTranslation? {
        val trimmedName: String = input.name().trim { it <= ' ' }
        for (entry in database.laboratoryTranslations) {
            if (entry.code == input.code() && entry.name == trimmedName) {
                return entry
            }
        }
        warnings.add(
            CurationWarning(
                patientId,
                CurationCategory.LABORATORY_TRANSLATION,
                input.code(),
                "Could not find laboratory translation for lab value with code '${input.code()}' and name '$trimmedName'"
            )
        )
        return null
    }

    fun translateToxicity(patientId: String, input: Toxicity): Toxicity {
        val translation: ToxicityTranslation? = findToxicityTranslation(input.name())
        if (translation == null) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.TOXICITY_TRANSLATION,
                    input.name(),
                    "Could not find translation for toxicity with input '${input.name()}'"
                )
            )
            return input
        }
        evaluatedTranslations.put(ToxicityTranslation::class.java, translation)
        return ImmutableToxicity.builder().from(input).name(translation.translatedToxicity).build()
    }

    private fun findToxicityTranslation(toxicityName: String): ToxicityTranslation? {
        val trimmedToxicity = toxicityName.trim { it <= ' ' }
        for (entry in database.toxicityTranslations) {
            if (entry.toxicity == trimmedToxicity) {
                return entry
            }
        }

        // No warn since not all toxicities need to be translated.
        return null
    }

    fun translateBloodTransfusion(patientId: String, input: BloodTransfusion): BloodTransfusion {
        val translation: BloodTransfusionTranslation = findBloodTransfusionTranslation(patientId, input) ?: return input
        evaluatedTranslations.put(BloodTransfusionTranslation::class.java, translation)
        return ImmutableBloodTransfusion.builder().from(input).product(translation.translatedProduct).build()
    }

    private fun findBloodTransfusionTranslation(patientId: String, input: BloodTransfusion): BloodTransfusionTranslation? {
        val trimmedProduct: String = input.product().trim { it <= ' ' }
        for (entry in database.bloodTransfusionTranslations) {
            if (entry.product == trimmedProduct) {
                return entry
            }
        }
        warnings.add(
            CurationWarning(
                patientId,
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                trimmedProduct,
                "Could not find blood transfusion translation for blood transfusion with product '$trimmedProduct'"
            )
        )
        return null
    }

    fun translateDosageUnit(patientId: String, dosageUnit: String?): String? {
        if (dosageUnit.isNullOrEmpty()) {
            return null
        }
        val trimmedDosageUnit = dosageUnit.trim { it <= ' ' }
        val translation: DosageUnitTranslation? = findDosageUnitTranslation(trimmedDosageUnit)
        if (translation == null) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.DOSAGE_UNIT_TRANSLATION,
                    trimmedDosageUnit,
                    "No translation found for medication dosage unit: '$trimmedDosageUnit'"
                )
            )
            return null
        }
        evaluatedTranslations.put(DosageUnitTranslation::class.java, translation)
        return translation.translatedDosageUnit.ifEmpty { null }
    }

    private fun findDosageUnitTranslation(dosageUnit: String): DosageUnitTranslation? {
        return database.dosageUnitTranslations.firstOrNull { it.dosageUnit.lowercase() == dosageUnit.lowercase() }
    }

    fun getWarnings(patientId: String): List<CurationWarning> = warnings.filter { it.patientId == patientId }

    fun evaluate() {
        var warnCount = 0
        for ((key, evaluated) in evaluatedCurationInputs.asMap().entries) {
            val configs: List<CurationConfig> = configsForClass(key)
            for (config in configs) {
                // TODO: Raise warnings for unused medication dosage once more final
                if (!evaluated.contains(config.input.lowercase()) && isNotIgnored(config)) {
                    warnCount++
                    LOGGER.warn(" Curation key '{}' not used for class {}", config.input, key.simpleName)
                }
            }
        }
        for ((key, evaluated) in evaluatedTranslations.asMap().entries) {
            val translations: List<Translation> = translationsForClass(key)
            for (translation in translations) {
                if (!evaluated.contains(translation) && translation !is BloodTransfusionTranslation) {
                    warnCount++
                    LOGGER.warn(" Translation '{}' not used", translation)
                }
            }
        }
    }

    private fun isNotIgnored(config: CurationConfig) =
        (config !is MedicationDosageConfig && config !is CypInteractionConfig && config !is QTProlongatingConfig)

    fun questionnaireRawEntryMapper(): QuestionnaireRawEntryMapper {
        return questionnaireRawEntryMapper
    }

    private fun configsForClass(classToLookUp: Class<out CurationConfig>): List<CurationConfig> {
        when (classToLookUp) {
            PrimaryTumorConfig::class.java -> {
                return database.primaryTumorConfigs
            }

            TreatmentHistoryEntryConfig::class.java -> {
                return database.treatmentHistoryEntryConfigs
            }

            SecondPrimaryConfig::class.java -> {
                return database.secondPrimaryConfigs
            }

            LesionLocationConfig::class.java -> {
                return database.lesionLocationConfigs
            }

            NonOncologicalHistoryConfig::class.java -> {
                return database.nonOncologicalHistoryConfigs
            }

            ComplicationConfig::class.java -> {
                return database.complicationConfigs
            }

            ECGConfig::class.java -> {
                return database.ecgConfigs
            }

            InfectionConfig::class.java -> {
                return database.infectionConfigs
            }

            PeriodBetweenUnitConfig::class.java -> {
                return database.periodBetweenUnitConfigs
            }

            ToxicityConfig::class.java -> {
                return database.toxicityConfigs
            }

            MolecularTestConfig::class.java -> {
                return database.molecularTestConfigs
            }

            MedicationNameConfig::class.java -> {
                return database.medicationNameConfigs
            }

            MedicationDosageConfig::class.java -> {
                return database.medicationDosageConfigs
            }

            IntoleranceConfig::class.java -> {
                return database.intoleranceConfigs
            }

            CypInteractionConfig::class.java -> {
                return database.cypInteractionConfigs
            }

            QTProlongatingConfig::class.java -> {
                return database.qtProlongingConfigs
            }

            else -> throw IllegalStateException("Class not found in curation database: $classToLookUp")
        }
    }

    private fun translationsForClass(classToLookup: Class<out Translation>): List<Translation> {
        when (classToLookup) {
            AdministrationRouteTranslation::class.java -> {
                return database.administrationRouteTranslations
            }

            LaboratoryTranslation::class.java -> {
                return database.laboratoryTranslations
            }

            ToxicityTranslation::class.java -> {
                return database.toxicityTranslations
            }

            BloodTransfusionTranslation::class.java -> {
                return database.bloodTransfusionTranslations
            }

            DosageUnitTranslation::class.java -> {
                return database.dosageUnitTranslations
            }

            else -> throw IllegalStateException("Class not found in curation database: $classToLookup")
        }
    }

    private fun <T : CurationConfig> find(configs: List<T>, input: String): Set<T> {
        if (configs.isNotEmpty()) {
            evaluatedCurationInputs.put(configs[0].javaClass, input.lowercase())
            return configs.filter { config: T -> config.input.equals(input, ignoreCase = true) }.toSet()
        }
        return emptySet()
    }

    companion object {
        private val LOGGER = LogManager.getLogger(CurationModel::class.java)

        @Throws(IOException::class)
        fun create(clinicalCurationDirectory: String, doidModel: DoidModel, treatmentDatabase: TreatmentDatabase): CurationModel {
            val reader = CurationDatabaseReader(CurationValidator(doidModel), treatmentDatabase)
            val questionnaireRawEntryMapper: QuestionnaireRawEntryMapper =
                QuestionnaireRawEntryMapper.createFromCurationDirectory(clinicalCurationDirectory)
            return CurationModel(reader.read(clinicalCurationDirectory), questionnaireRawEntryMapper)
        }

        private fun hasConfigImplyingUnknownState(configs: Set<ComplicationConfig>): Boolean {
            for (config in configs) {
                if (config.impliesUnknownComplicationState) {
                    return true
                }
            }
            return false
        }

        private fun maybeECGMeasure(value: Int?, unit: String?): ImmutableECGMeasure? {
            return if (value == null || unit == null) {
                null
            } else ImmutableECGMeasure.builder().value(value).unit(unit).build()
        }
    }
}