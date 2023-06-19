package com.hartwig.actin.clinical

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.hartwig.actin.clinical.curation.CurationModel
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.ImmutableToxicityEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicityEvaluation
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableSurgicalTreatment
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableSurgeryHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.lab.LabExtraction
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionExtraction
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator
import com.hartwig.actin.clinical.sort.MedicationByNameComparator
import org.apache.logging.log4j.LogManager

class ClinicalRecordsFactory(feed: FeedModel, curation: CurationModel) {
    private val feed: FeedModel
    private val curation: CurationModel

    init {
        this.feed = feed
        this.curation = curation
    }

    fun create(): List<ClinicalRecord> {
        val records: MutableList<ClinicalRecord> = Lists.newArrayList<ClinicalRecord>()
        val processedPatientIds: MutableSet<String> = HashSet()
        val extraction = QuestionnaireExtraction(curation.questionnaireRawEntryMapper())
        LOGGER.info("Creating clinical model")
        for (subject in feed.subjects()) {
            val patientId = toPatientId(subject)
            check(!processedPatientIds.contains(patientId)) { "Cannot create clinical records. Duplicate patientId: $patientId" }
            processedPatientIds.add(patientId)
            LOGGER.info(" Extracting data for patient {}", patientId)
            val questionnaire: Questionnaire? = extraction.extract(feed.latestQuestionnaireEntry(subject))
            val extractedToxicities = extractToxicities(subject, questionnaire)
            val toxicityEvaluations: List<ToxicityEvaluation> = extractedToxicities
                .map { toxicity: Toxicity ->
                    ImmutableToxicityEvaluation.builder()
                        .toxicities(
                            setOf(
                                ImmutableObservedToxicity.builder()
                                    .name(toxicity.name())
                                    .addAllCategories(toxicity.categories())
                                    .grade(toxicity.grade())
                                    .build()
                            )
                        )
                        .evaluatedDate(toxicity.evaluatedDate())
                        .source(toxicity.source())
                        .build()
                }
            records.add(
                ImmutableClinicalRecord.builder()
                    .patientId(patientId)
                    .patient(extractPatientDetails(subject, questionnaire))
                    .tumor(extractTumorDetails(questionnaire))
                    .clinicalStatus(extractClinicalStatus(questionnaire))
                    .priorTumorTreatments(extractPriorTumorTreatments(questionnaire))
                    .priorSecondPrimaries(extractPriorSecondPrimaries(questionnaire))
                    .priorOtherConditions(extractPriorOtherConditions(questionnaire))
                    .priorMolecularTests(extractPriorMolecularTests(questionnaire))
                    .complications(extractComplications(questionnaire))
                    .labValues(extractLabValues(subject))
                    .toxicities(extractedToxicities)
                    .toxicityEvaluations(toxicityEvaluations)
                    .intolerances(extractIntolerances(subject))
                    .surgeries(extractSurgeries(subject))
                    .surgicalTreatments(extractSurgicalTreatments(subject))
                    .bodyWeights(extractBodyWeights(subject))
                    .vitalFunctions(extractVitalFunctions(subject))
                    .bloodTransfusions(extractBloodTransfusions(subject))
                    .medications(extractMedications(subject))
                    .build()
            )
        }
        records.sortWith(ClinicalRecordComparator())
        LOGGER.info("Evaluating curation database")
        curation.evaluate()
        return records
    }

    private fun extractPatientDetails(subject: String, questionnaire: Questionnaire?): PatientDetails {
        val patient: PatientEntry = feed.patientEntry(subject)
        return ImmutablePatientDetails.builder()
            .gender(patient.gender)
            .birthYear(patient.birthYear)
            .registrationDate(patient.periodStart)
            .questionnaireDate(questionnaire?.date)
            .otherMolecularPatientId(questionnaire?.genayaSubjectNumber)
            .build()
    }

    private fun extractTumorDetails(questionnaire: Questionnaire?): TumorDetails {
        if (questionnaire == null) {
            return ImmutableTumorDetails.builder().build()
        }
        val biopsyLocation: String? = questionnaire.biopsyLocation
        val otherLesions: List<String>? = questionnaire.otherLesions
        val curatedOtherLesions: List<String>? = curation.curateOtherLesions(otherLesions)
        val tumorDetails: TumorDetails = ImmutableTumorDetails.builder()
            .from(curation.curateTumorDetails(questionnaire.tumorLocation, questionnaire.tumorType))
            .biopsyLocation(curation.curateBiopsyLocation(biopsyLocation))
            .stage(questionnaire.stage)
            .hasMeasurableDisease(questionnaire.hasMeasurableDisease)
            .hasBrainLesions(questionnaire.hasBrainLesions)
            .hasActiveBrainLesions(questionnaire.hasActiveBrainLesions)
            .hasCnsLesions(questionnaire.hasCnsLesions)
            .hasActiveCnsLesions(questionnaire.hasActiveCnsLesions)
            .hasBoneLesions(questionnaire.hasBoneLesions)
            .hasLiverLesions(questionnaire.hasLiverLesions)
            .otherLesions(curatedOtherLesions)
            .build()
        return curation.overrideKnownLesionLocations(tumorDetails, biopsyLocation, otherLesions)
    }

    private fun extractClinicalStatus(questionnaire: Questionnaire?): ClinicalStatus {
        return if (questionnaire == null) ImmutableClinicalStatus.builder().build() else ImmutableClinicalStatus.builder()
            .who(questionnaire.whoStatus)
            .infectionStatus(curation.curateInfectionStatus(questionnaire.infectionStatus))
            .ecg(curation.curateECG(questionnaire.ecg))
            .lvef(curation.determineLVEF(questionnaire.nonOncologicalHistory))
            .hasComplications(extractComplications(questionnaire)?.isNotEmpty())
            .build()
    }

    private fun extractPriorTumorTreatments(questionnaire: Questionnaire?): List<PriorTumorTreatment> {
        return if (questionnaire == null) emptyList() else listOf(
            questionnaire.treatmentHistoryCurrentTumor,
            questionnaire.otherOncologicalHistory
        )
            .flatMap { curation.curatePriorTumorTreatments(it) }
    }

    private fun extractPriorSecondPrimaries(questionnaire: Questionnaire?): List<PriorSecondPrimary> {
        return if (questionnaire == null) emptyList() else listOf(
            questionnaire.otherOncologicalHistory,
            questionnaire.secondaryPrimaries
        )
            .flatMap { curation.curatePriorSecondPrimaries(it) }
    }

    private fun extractPriorOtherConditions(questionnaire: Questionnaire?): List<PriorOtherCondition> {
        return if (questionnaire == null) emptyList() else curation.curatePriorOtherConditions(questionnaire.nonOncologicalHistory)
    }

    private fun extractPriorMolecularTests(questionnaire: Questionnaire?): List<PriorMolecularTest> {
        return if (questionnaire == null) emptyList() else listOf(
            Pair("IHC", questionnaire.ihcTestResults),
            Pair("PD-L1", questionnaire.pdl1TestResults)
        )
            .flatMap { (key, value) -> curation.curatePriorMolecularTests(key, value) }
    }

    private fun extractComplications(questionnaire: Questionnaire?): List<Complication>? {
        return if (questionnaire != null) curation.curateComplications(questionnaire.complications) else null
    }

    private fun extractLabValues(subject: String): List<LabValue> {
        return feed.labEntries(subject)
            .map { LabExtraction.extract(it) }
            .map { curation.translateLabValue(it) }
            .sortedWith(LabValueDescendingDateComparator())
    }

    private fun extractToxicities(subject: String, questionnaire: Questionnaire?): List<Toxicity> {
        val feedToxicities: List<Toxicity> = feed.toxicityEntries(subject).mapNotNull { toxicityEntry: DigitalFileEntry ->
            val grade = extractGrade(toxicityEntry)
            if (grade != null) {
                ImmutableToxicity.builder()
                    .name(toxicityEntry.itemText)
                    .evaluatedDate(toxicityEntry.authored)
                    .source(ToxicitySource.EHR)
                    .grade(grade)
                    .build()
            } else {
                null
            }
        }
            .map { curation.translateToxicity(it) }

        if (questionnaire != null) {
            return feedToxicities + curation.curateQuestionnaireToxicities(questionnaire.unresolvedToxicities, questionnaire.date)
        }
        return feedToxicities
    }

    private fun extractIntolerances(subject: String): List<Intolerance> {
        return feed.intoleranceEntries(subject)
            .map { entry: IntoleranceEntry ->
                ImmutableIntolerance.builder()
                    .name(CurationUtil.capitalizeFirstLetterOnly(entry.codeText))
                    .category(CurationUtil.capitalizeFirstLetterOnly(entry.category))
                    .type(CurationUtil.capitalizeFirstLetterOnly(entry.isSideEffect))
                    .clinicalStatus(CurationUtil.capitalizeFirstLetterOnly(entry.clinicalStatus))
                    .verificationStatus(CurationUtil.capitalizeFirstLetterOnly(entry.verificationStatus))
                    .criticality(CurationUtil.capitalizeFirstLetterOnly(entry.criticality))
                    .build()
            }
            .map { curation.curateIntolerance(it) }
    }

    private fun extractSurgeries(subject: String): List<Surgery> {
        return feed.uniqueSurgeryEntries(subject)
            .map { ImmutableSurgery.builder().endDate(it.periodEnd).status(resolveSurgeryStatus(it.encounterStatus)).build() }
    }

    private fun extractSurgicalTreatments(subject: String): List<TreatmentHistoryEntry> {
        return feed.uniqueSurgeryEntries(subject).map { surgeryEntry: SurgeryEntry ->
            ImmutableTreatmentHistoryEntry.builder()
                .treatments(setOf(ImmutableSurgicalTreatment.builder().name("extracted surgery").build()))
                .surgeryHistoryDetails(
                    ImmutableSurgeryHistoryDetails.builder()
                        .endDate(surgeryEntry.periodEnd)
                        .status(resolveSurgeryStatus(surgeryEntry.encounterStatus))
                        .build()
                )
                .build()
        }
    }

    private fun extractBodyWeights(subject: String): List<BodyWeight> {
        return feed.uniqueBodyWeightEntries(subject).map { entry: BodyWeightEntry ->
            ImmutableBodyWeight.builder()
                .date(entry.effectiveDateTime)
                .value(entry.valueQuantityValue)
                .unit(entry.valueQuantityUnit)
                .build()
        }
    }

    private fun extractVitalFunctions(subject: String): List<VitalFunction> {
        return feed.vitalFunctionEntries(subject).map { entry: VitalFunctionEntry ->
            ImmutableVitalFunction.builder()
                .date(entry.effectiveDateTime)
                .category(VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal))
                .subcategory(entry.componentCodeDisplay)
                .value(entry.quantityValue)
                .unit(entry.quantityUnit)
                .build()
        }
    }

    private fun extractBloodTransfusions(subject: String): List<BloodTransfusion> {
        return feed.bloodTransfusionEntries(subject).map { entry: DigitalFileEntry ->
            ImmutableBloodTransfusion.builder()
                .date(entry.authored)
                .product(entry.itemAnswerValueValueString)
                .build()
        }
            .map { curation.translateBloodTransfusion(it) }
    }

    private fun extractMedications(subject: String): List<Medication> {
        val medications: MutableList<Medication> = Lists.newArrayList()
        for (entry in feed.medicationEntries(subject)) {
            val dosageCurated: Medication? = curation.curateMedicationDosage(entry.dosageInstructionText)
            val builder: ImmutableMedication.Builder = ImmutableMedication.builder()
            if (dosageCurated != null) {
                builder.from(dosageCurated)
            }
            val name: String? = CurationUtil.capitalizeFirstLetterOnly(entry.code5ATCDisplay).ifEmpty {
                curation.curateMedicationName(CurationUtil.capitalizeFirstLetterOnly(entry.codeText))
            }
            if (!name.isNullOrEmpty()) {
                val medication: Medication = builder.name(name)
                    .codeATC(curation.curateMedicationCodeATC(entry.code5ATCCode))
                    .chemicalSubgroupAtc(entry.chemicalSubgroupDisplay)
                    .pharmacologicalSubgroupAtc(entry.pharmacologicalSubgroupDisplay)
                    .therapeuticSubgroupAtc(entry.therapeuticSubgroupDisplay)
                    .anatomicalMainGroupAtc(entry.anatomicalMainGroupDisplay)
                    .status(curation.curateMedicationStatus(entry.status))
                    .administrationRoute(curation.translateAdministrationRoute(entry.dosageInstructionRouteDisplay))
                    .dosageUnit(curation.translateDosageUnit(entry.dosageInstructionDoseQuantityUnit))
                    .periodBetweenValue(entry.dosageInstructionPeriodBetweenDosagesValue)
                    .periodBetweenUnit(entry.dosageInstructionPeriodBetweenDosagesUnit)
                    .startDate(entry.periodOfUseValuePeriodStart)
                    .stopDate(entry.periodOfUseValuePeriodEnd)
                    .build()
                medications.add(curation.annotateWithMedicationCategory(medication))
            }
        }
        medications.sortWith(MedicationByNameComparator())
        return medications
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ClinicalRecordsFactory::class.java)

        @VisibleForTesting
        fun toPatientId(subject: String): String {
            var adjusted = subject
            // Subjects have been passed with unexpected subject IDs in the past (e.g. without ACTN prefix)
            if (subject.length == 10 && !subject.startsWith("ACTN")) {
                LOGGER.warn("Suspicious subject detected. Pre-fixing with 'ACTN': {}", subject)
                adjusted = "ACTN$subject"
            }
            return adjusted.replace("-".toRegex(), "")
        }

        private fun extractGrade(entry: DigitalFileEntry): Int? {
            val value: String = entry.itemAnswerValueValueString
            if (value.isEmpty()) {
                return null
            }
            val curated: String
            val notApplicableIndex = value.indexOf(". Not applicable")
            curated = if (notApplicableIndex > 0) {
                value.substring(0, notApplicableIndex)
            } else {
                value
            }
            return Integer.valueOf(curated)
        }

        private fun resolveSurgeryStatus(status: String): SurgeryStatus {
            val valueToFind = status.trim { it <= ' ' }.replace("-".toRegex(), "_")
            for (option in SurgeryStatus.values()) {
                if (option.toString().equals(valueToFind, ignoreCase = true)) {
                    return option
                }
            }
            LOGGER.warn("Could not resolve surgery status '{}'", status)
            return SurgeryStatus.UNKNOWN
        }
    }
}