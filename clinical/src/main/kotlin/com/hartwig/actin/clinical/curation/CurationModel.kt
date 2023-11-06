package com.hartwig.actin.clinical.curation

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.HashMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Multimap
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
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
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
import java.io.IOException
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

class CurationModel @VisibleForTesting internal constructor(
    private val database: CurationDatabase,
    val questionnaireRawEntryMapper: QuestionnaireRawEntryMapper
) {
    private val evaluatedCurationInputs: Multimap<Class<out CurationConfig>, String> =
        HashMultimap.create<Class<out CurationConfig>, String>()
    private val evaluatedAdministrationRouteTranslations = mutableSetOf<Translation>()
    private val evaluatedDosageUnitTranslations = mutableSetOf<Translation>()
    private val evaluatedLaboratoryTranslations = mutableSetOf<LaboratoryTranslation>()
    private val evaluatedToxicityTranslations = mutableSetOf<Translation>()
    private val warnings = mutableListOf<CurationWarning>()

    fun curateTumorDetails(patientId: String, inputTumorLocation: String?, inputTumorType: String?): TumorDetails {
        val primaryTumorConfig = if (inputTumorLocation == null && inputTumorType == null) null else {
            val inputPrimaryTumor = fullTrim(listOf(inputTumorLocation, inputTumorType).joinToString(" | ") { it ?: "" })
            findUniqueCurationConfig(
                inputPrimaryTumor, database.primaryTumorConfigs, patientId, CurationCategory.PRIMARY_TUMOR, "primary tumor"
            )
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
        val lesionsToCheck = (otherLesions ?: emptyList()) + listOfNotNull(biopsyLocation)

        val matches = lesionsToCheck.flatMap { find(database.lesionLocationConfigs, it).mapNotNull(LesionLocationConfig::category) }

        if (matches.isEmpty()) {
            return tumorDetails
        }

        val builder: ImmutableTumorDetails.Builder = ImmutableTumorDetails.builder().from(tumorDetails)
        overrideLesionPresence(
            matches, tumorDetails, builder, LesionLocationCategory.BONE, TumorDetails::hasBoneLesions,
            ImmutableTumorDetails.Builder::hasBoneLesions
        )
        overrideLesionPresence(
            matches, tumorDetails, builder, LesionLocationCategory.BRAIN, TumorDetails::hasBrainLesions,
            ImmutableTumorDetails.Builder::hasBrainLesions
        )
        overrideLesionPresence(
            matches, tumorDetails, builder, LesionLocationCategory.CNS, TumorDetails::hasCnsLesions,
            ImmutableTumorDetails.Builder::hasCnsLesions
        )
        overrideLesionPresence(
            matches, tumorDetails, builder, LesionLocationCategory.LIVER, TumorDetails::hasLiverLesions,
            ImmutableTumorDetails.Builder::hasLiverLesions
        )
        overrideLesionPresence(
            matches, tumorDetails, builder, LesionLocationCategory.LUNG, TumorDetails::hasLungLesions,
            ImmutableTumorDetails.Builder::hasLungLesions
        )
        overrideLesionPresence(
            matches, tumorDetails, builder, LesionLocationCategory.LYMPH_NODE, TumorDetails::hasLymphNodeLesions,
            ImmutableTumorDetails.Builder::hasLymphNodeLesions
        )
        return builder.build()
    }

    private fun overrideLesionPresence(
        matches: List<LesionLocationCategory>,
        tumorDetails: TumorDetails,
        builder: ImmutableTumorDetails.Builder,
        lesionLocationCategory: LesionLocationCategory,
        hasLesions: (TumorDetails) -> Boolean?,
        setLesions: (ImmutableTumorDetails.Builder, Boolean) -> ImmutableTumorDetails.Builder
    ) {
        if (matches.contains(lesionLocationCategory)) {
            if (hasLesions.invoke(tumorDetails) == false) {
                LOGGER.debug("  Overriding presence of ${lesionLocationCategory.name.lowercase()} lesions")
            }
            setLesions.invoke(builder, true)
        }
    }

    fun curateTreatmentHistoryEntry(patientId: String, entry: String): List<TreatmentHistoryEntry> {
        return findCurationConfigs(
            fullTrim(entry),
            database.treatmentHistoryEntryConfigs,
            patientId,
            CurationCategory.ONCOLOGICAL_HISTORY,
            "treatment history or second primary",
            database.secondPrimaryConfigs
        )
            .filterNot(CurationConfig::ignore)
            .map { it.curated!! }
    }

    fun curatePriorSecondPrimaries(patientId: String, inputs: List<String>?): List<PriorSecondPrimary> {
        return inputs?.flatMap {
            findCurationConfigs(
                fullTrim(it),
                database.secondPrimaryConfigs,
                patientId,
                CurationCategory.SECOND_PRIMARY,
                "second primary or treatment history",
                database.treatmentHistoryEntryConfigs
            )
        }
            ?.filterNot(CurationConfig::ignore)
            ?.map { it.curated!! }
            ?: emptyList()
    }

    fun curatePriorOtherConditions(patientId: String, inputs: List<String>?): List<PriorOtherCondition> {
        if (inputs == null) {
            return emptyList()
        }
        return inputs.flatMap {
            findRelevantCurationConfigs(
                fullTrim(it),
                database.nonOncologicalHistoryConfigs,
                patientId,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                "non-oncological history"
            )
        }
            .mapNotNull { obj: NonOncologicalHistoryConfig -> obj.priorOtherCondition.getOrNull() }
    }

    fun curatePriorMolecularTests(patientId: String, type: String, inputs: List<String>?): List<PriorMolecularTest> {
        return inputs?.flatMap {
            findRelevantCurationConfigs(
                fullTrim(it), database.molecularTestConfigs, patientId, CurationCategory.MOLECULAR_TEST, "$type molecular test"
            )
        }
            ?.map { it.curated!! }
            ?: emptyList()
    }

    fun curateComplications(patientId: String, inputs: List<String>?): List<Complication>? {
        if (inputs.isNullOrEmpty()) {
            return null
        }

        val complications: MutableList<Complication> = Lists.newArrayList()
        var unknownStateCount = 0
        var validInputCount = 0
        for (input in inputs) {
            val configs = findCurationConfigs(
                input, database.complicationConfigs, patientId, CurationCategory.COMPLICATION, "complication"
            )
            if (configs.isNotEmpty()) {
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
            findRelevantCurationConfigs(fullTrim(input), database.toxicityConfigs, patientId, CurationCategory.TOXICITY, "toxicity")
        }
            ?.map { config ->
                ImmutableToxicity.builder()
                    .name(config.name)
                    .categories(config.categories)
                    .evaluatedDate(date)
                    .source(ToxicitySource.QUESTIONNAIRE)
                    .grade(config.grade)
                    .build()
            } ?: emptyList()
    }

    fun curateECG(patientId: String, input: ECG?): ECG? {
        val configs = findCurationConfig(
            input?.aberrationDescription(), database.ecgConfigs, patientId, CurationCategory.ECG, "ECG"
        )
        when {
            configs?.isEmpty() == true -> {
                return input
            }

            configs?.size == 1 -> {
                val config = configs.first()
                if (!config.ignore) {
                    val description: String? = config.interpretation.ifEmpty { null }
                    return ImmutableECG.builder()
                        .from(input!!)
                        .aberrationDescription(description)
                        .qtcfMeasure(maybeECGMeasure(config.qtcfValue, config.qtcfUnit))
                        .jtcMeasure(maybeECGMeasure(config.jtcValue, config.jtcUnit))
                        .build()
                }
            }
        }
        return null
    }

    fun curateInfectionStatus(patientId: String, input: InfectionStatus?): InfectionStatus? {
        val configs = findCurationConfig(
            input?.description(), database.infectionConfigs, patientId, CurationCategory.INFECTION, "infection"
        )
        when {
            configs?.isEmpty() == true -> {
                return input
            }

            configs?.size == 1 -> {
                val config = configs.first()
                if (!config.ignore) {
                    val description: String? = config.interpretation.ifEmpty { null }
                    return ImmutableInfectionStatus.builder().from(input!!).description(description).build()
                }
            }
        }
        return null
    }

    fun curatePeriodBetweenUnit(patientId: String, input: String?): String? {
        val config = findUniqueCurationConfig(
            input, database.periodBetweenUnitConfigs, patientId, CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION,
            "period between unit"
        )
        return config?.interpretation
    }

    fun determineLVEF(inputs: List<String>?): Double? {
        return inputs?.flatMap { input: String -> find(database.nonOncologicalHistoryConfigs, input) }
            ?.filterNot { it.ignore }
            ?.map { it.lvef }
            ?.find { it.isPresent }
            ?.get()
    }

    fun curateOtherLesions(patientId: String, otherLesions: List<String>?): List<String>? {
        return otherLesions?.flatMap {
            findCurationConfigs(it, database.lesionLocationConfigs, patientId, CurationCategory.LESION_LOCATION, "lesion")
        }
            ?.filter { config ->
                // We only want to include lesions from the other lesions in actual other lesions
                // if it does not override an explicit lesion location
                val hasRealOtherLesion = config.category == null || config.category == LesionLocationCategory.LYMPH_NODE
                hasRealOtherLesion && config.location.isNotEmpty()
            }
            ?.map(LesionLocationConfig::location)
    }

    fun curateBiopsyLocation(patientId: String, input: String?): String? {
        val config = findUniqueCurationConfig(
            input, database.lesionLocationConfigs, patientId, CurationCategory.LESION_LOCATION, "biopsy location"
        )
        return config?.location
    }

    fun curateMedicationDosage(patientId: String, input: String): Dosage? {
        return findUniqueCurationConfig(
            input, database.medicationDosageConfigs, patientId, CurationCategory.MEDICATION_DOSAGE, "medication dosage"
        )?.let { config ->
            ImmutableDosage.builder()
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
    }

    fun curateMedicationName(patientId: String, input: String): String? {
        val config = findUniqueCurationConfig(
            fullTrim(input), database.medicationNameConfigs, patientId, CurationCategory.MEDICATION_NAME, "medication name"
        )
        return config?.name
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

    fun curateIntolerance(patientId: String, intolerance: Intolerance): Intolerance {
        val reformatted = CurationUtil.capitalizeFirstLetterOnly(intolerance.name())
        val config = findUniqueCurationConfig(
            reformatted, database.intoleranceConfigs, patientId, CurationCategory.INTOLERANCE, "intolerance"
        )
        val builder: ImmutableIntolerance.Builder = ImmutableIntolerance.builder().from(intolerance)
        if (config != null) {
            builder.name(config.name).doids(config.doids)
        }

        // TODO: add ATC code of medication to subcategories
        if (intolerance.category().equals("medication", ignoreCase = true)) {
            builder.subcategories(emptySet())
        }

        return builder.build()
    }

    private inline fun <reified T : CurationConfig> findUniqueCurationConfig(
        input: String?,
        curationConfigs: Map<String, Set<T>>,
        patientId: String,
        curationCategory: CurationCategory,
        categoryName: String
    ): T? {
        val configs = findCurationConfig(input, curationConfigs, patientId, curationCategory, categoryName)
        if (configs != null && configs.size == 1) {
            val config = configs.first()
            if (!config.ignore) {
                return config
            }
        }
        return null
    }

    private inline fun <reified T : CurationConfig> findCurationConfig(
        input: String?,
        curationConfigs: Map<String, Set<T>>,
        patientId: String,
        curationCategory: CurationCategory,
        categoryName: String
    ): Set<T>? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val configs = findCurationConfigs(input, curationConfigs, patientId, curationCategory, categoryName)
        if (configs.size > 1) {
            val trimmedInput = input.trim { it <= ' ' }
            warnings.add(
                CurationWarning(patientId, curationCategory, trimmedInput, "Multiple $categoryName configs found for input '$trimmedInput'")
            )
        }
        return configs
    }

    private inline fun <reified T : CurationConfig> findRelevantCurationConfigs(
        input: String,
        curationConfigs: Map<String, Set<T>>,
        patientId: String,
        curationCategory: CurationCategory,
        categoryName: String
    ): List<T> {
        return findCurationConfigs(input, curationConfigs, patientId, curationCategory, categoryName).filterNot(CurationConfig::ignore)
    }

    private inline fun <reified T : CurationConfig> findCurationConfigs(
        input: String,
        curationConfigs: Map<String, Set<T>>,
        patientId: String,
        curationCategory: CurationCategory,
        categoryName: String
    ): Set<T> {
        val trimmedInput = input.trim { it <= ' ' }
        val configs = find(curationConfigs, trimmedInput)
        if (configs.isEmpty()) {
            warnings.add(
                CurationWarning(patientId, curationCategory, trimmedInput, "Could not find $categoryName config for input '$trimmedInput'")
            )
        }
        return configs
    }

    private inline fun <reified T : CurationConfig, reified U : CurationConfig> findCurationConfigs(
        input: String,
        curationConfigs: Map<String, Set<T>>,
        patientId: String,
        curationCategory: CurationCategory,
        categoryName: String,
        secondaryConfigLookup: Map<String, Set<U>>
    ): Set<T> {
        val trimmedInput = input.trim { it <= ' ' }
        val configs = find(curationConfigs, trimmedInput)
        if (configs.isEmpty() && find(secondaryConfigLookup, trimmedInput).isEmpty()) {
            warnings.add(
                CurationWarning(patientId, curationCategory, trimmedInput, "Could not find $categoryName config for input '$trimmedInput'")
            )
        }
        return configs
    }

    private inline fun <reified T : CurationConfig> find(configs: Map<String, Set<T>>, input: String): Set<T> {
        if (configs.isNotEmpty()) {
            evaluatedCurationInputs.put(T::class.java, input.lowercase())
        }
        return configs[input.lowercase()] ?: emptySet()
    }

    fun translateLabValue(patientId: String, input: LabValue): LabValue {
        val translation: LaboratoryTranslation = findLaboratoryTranslation(patientId, input) ?: return input
        evaluatedLaboratoryTranslations.add(translation)
        return ImmutableLabValue.builder().from(input).code(translation.translatedCode).name(translation.translatedName).build()
    }

    private fun findLaboratoryTranslation(patientId: String, input: LabValue): LaboratoryTranslation? {
        val trimmedName: String = input.name().trim { it <= ' ' }
        val found = database.laboratoryTranslations[Pair(input.code(), trimmedName)]
        if (found == null) {
            warnings.add(
                CurationWarning(
                    patientId,
                    CurationCategory.LABORATORY_TRANSLATION,
                    input.code(),
                    "Could not find laboratory translation for lab value with code '${input.code()}' and name '$trimmedName'"
                )
            )
        }
        return found
    }

    fun translateAdministrationRoute(patientId: String, administrationRoute: String?): String? {
        val translation = findTranslation(
            administrationRoute, database.administrationRouteTranslations, patientId, CurationCategory.ADMISTRATION_ROUTE_TRANSLATION,
            "medication administration route", evaluatedAdministrationRouteTranslations
        )
        return translation?.translated?.ifEmpty { null }
    }

    fun translateToxicity(patientId: String, input: Toxicity): Toxicity {
        return findTranslation(
            input.name(), database.toxicityTranslations, patientId,
            CurationCategory.TOXICITY_TRANSLATION, "toxicity", evaluatedToxicityTranslations
        )?.let { translation ->
            ImmutableToxicity.builder().from(input).name(translation.translated).build()
        } ?: input
    }

    fun translateBloodTransfusion(patientId: String, input: BloodTransfusion): BloodTransfusion {
        return findTranslation(
            input.product(), database.bloodTransfusionTranslations, patientId,
            CurationCategory.BLOOD_TRANSFUSION_TRANSLATION, "blood transfusion with product"
        )?.let { translation ->
            ImmutableBloodTransfusion.builder().from(input).product(translation.translated).build()
        } ?: input
    }

    fun translateDosageUnit(patientId: String, dosageUnit: String?): String? {
        val translation = findTranslation(
            dosageUnit?.lowercase(), database.dosageUnitTranslations, patientId,
            CurationCategory.DOSAGE_UNIT_TRANSLATION, "medication dosage unit", evaluatedDosageUnitTranslations
        )
        return translation?.translated?.ifEmpty { null }
    }

    private fun findTranslation(
        input: String?,
        translations: Map<String, Translation>,
        patientId: String,
        curationCategory: CurationCategory,
        categoryName: String,
        evaluatedTranslations: MutableSet<Translation>? = null
    ): Translation? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val trimmedInput = input.trim { it <= ' ' }
        val translation: Translation? = translations[trimmedInput]
        if (translation == null) {
            warnings.add(
                CurationWarning(
                    patientId, curationCategory, trimmedInput, "No translation found for ${categoryName}: '$trimmedInput'"
                )
            )
            return null
        }
        evaluatedTranslations?.add(translation)
        return translation
    }

    fun getWarnings(patientId: String): List<CurationWarning> = warnings.filter { it.patientId == patientId }

    fun evaluate() {
        var warnCount = 0
        for ((key, evaluated) in evaluatedCurationInputs.asMap().entries) {
            val configs: List<CurationConfig> = configsForClass(key).values.flatten()
            for (config in configs) {
                // TODO: Raise warnings for unused medication dosage once more final
                if (!evaluated.contains(config.input.lowercase()) && isNotIgnored(config)) {
                    warnCount++
                    LOGGER.warn(" Curation key '{}' not used for class {}", config.input, key.simpleName)
                }
            }
        }
        listOf(
            database.administrationRouteTranslations to evaluatedAdministrationRouteTranslations,
            database.laboratoryTranslations to evaluatedLaboratoryTranslations,
            database.toxicityTranslations to evaluatedToxicityTranslations,
            database.dosageUnitTranslations to evaluatedDosageUnitTranslations
        )
            .flatMap { (allTranslations, evaluated) -> allTranslations.values.filterNot(evaluated::contains) }
            .forEach { translation ->
                warnCount++
                LOGGER.warn(" Translation '{}' not used", translation)
            }
    }

    private fun isNotIgnored(config: CurationConfig) =
        (config !is MedicationDosageConfig && config !is CypInteractionConfig && config !is QTProlongatingConfig)

    private fun configsForClass(classToLookUp: Class<out CurationConfig>): Map<String, Set<CurationConfig>> {
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
            return configs.any(ComplicationConfig::impliesUnknownComplicationState)
        }

        private fun maybeECGMeasure(value: Int?, unit: String?): ImmutableECGMeasure? {
            return if (value == null || unit == null) {
                null
            } else ImmutableECGMeasure.builder().value(value).unit(unit).build()
        }
    }
}