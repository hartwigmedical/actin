package com.hartwig.actin.clinical

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.Toxicity
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

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
            val questionnaire: Questionnaire = extraction.extract(feed.latestQuestionnaireEntry(subject))
            val extractedToxicities = extractToxicities(subject, questionnaire)
            val toxicityEvaluations: List<ToxicityEvaluation> = extractedToxicities.stream()
                .map(Function<Toxicity, Any> { toxicity: Toxicity ->
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
                })
                .collect(Collectors.toList<Any>())
            records.add(
                ImmutableClinicalRecord.builder()
                    .patientId(patientId)
                    .patient(extractPatientDetails(subject, questionnaire))
                    .tumor(extractTumorDetails(questionnaire))
                    .clinicalStatus(extractClinicalStatus(questionnaire))
                    .treatmentHistory(extractTreatmentHistory(questionnaire))
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
        records.sort(ClinicalRecordComparator())
        LOGGER.info("Evaluating curation database")
        curation.evaluate()
        return records
    }

    private fun extractPatientDetails(subject: String, questionnaire: Questionnaire?): PatientDetails {
        val patient: PatientEntry = feed.patientEntry(subject)
        return ImmutablePatientDetails.builder()
            .gender(patient.gender())
            .birthYear(patient.birthYear())
            .registrationDate(patient.periodStart())
            .questionnaireDate(if (questionnaire != null) questionnaire.date() else null)
            .otherMolecularPatientId(if (questionnaire != null) questionnaire.genayaSubjectNumber() else null)
            .build()
    }

    private fun extractTumorDetails(questionnaire: Questionnaire?): TumorDetails {
        if (questionnaire == null) {
            return ImmutableTumorDetails.builder().build()
        }
        val biopsyLocation: String = questionnaire.biopsyLocation()
        val otherLesions: List<String> = questionnaire.otherLesions()
        val curatedOtherLesions: List<String> = curation.curateOtherLesions(otherLesions)
        val tumorDetails: TumorDetails = ImmutableTumorDetails.builder()
            .from(curation.curateTumorDetails(questionnaire.tumorLocation(), questionnaire.tumorType()))
            .biopsyLocation(curation.curateBiopsyLocation(biopsyLocation))
            .stage(questionnaire.stage())
            .hasMeasurableDisease(questionnaire.hasMeasurableDisease())
            .hasBrainLesions(questionnaire.hasBrainLesions())
            .hasActiveBrainLesions(questionnaire.hasActiveBrainLesions())
            .hasCnsLesions(questionnaire.hasCnsLesions())
            .hasActiveCnsLesions(questionnaire.hasActiveCnsLesions())
            .hasBoneLesions(questionnaire.hasBoneLesions())
            .hasLiverLesions(questionnaire.hasLiverLesions())
            .otherLesions(curatedOtherLesions)
            .build()
        return curation.overrideKnownLesionLocations(tumorDetails, biopsyLocation, otherLesions)
    }

    private fun extractClinicalStatus(questionnaire: Questionnaire?): ClinicalStatus {
        return if (questionnaire == null) ImmutableClinicalStatus.builder().build() else ImmutableClinicalStatus.builder()
            .who(questionnaire.whoStatus())
            .infectionStatus(curation.curateInfectionStatus(questionnaire.infectionStatus()))
            .ecg(curation.curateECG(questionnaire.ecg()))
            .lvef(curation.determineLVEF(questionnaire.nonOncologicalHistory()))
            .hasComplications(
                Optional.ofNullable(extractComplications(questionnaire))
                    .map { complications: List<Complication>? -> !complications!!.isEmpty() }
                    .orElse(null))
            .build()
    }

    private fun extractTreatmentHistory(questionnaire: Questionnaire?): List<TreatmentHistoryEntry> {
        if (questionnaire == null) {
            return emptyList<TreatmentHistoryEntry>()
        }
        val fullHistoryInput =
            Stream.of<List<String?>>(questionnaire.treatmentHistoryCurrentTumor(), questionnaire.otherOncologicalHistory())
                .filter { obj: List<String?>? -> Objects.nonNull(obj) }
                .flatMap { obj: List<String?> -> obj.stream() }
                .collect(Collectors.toList<String>())
        return curation.curateTreatmentHistory(fullHistoryInput)
    }

    private fun extractPriorTumorTreatments(questionnaire: Questionnaire?): List<PriorTumorTreatment> {
        return if (questionnaire == null) emptyList<PriorTumorTreatment>() else Stream.of<List<String?>>(
            questionnaire.treatmentHistoryCurrentTumor(),
            questionnaire.otherOncologicalHistory()
        )
            .flatMap<PriorTumorTreatment>(Function<List<String?>, Stream<out PriorTumorTreatment>> { entry: List<String?>? ->
                curation.curatePriorTumorTreatments(
                    entry
                ).stream()
            })
            .collect(Collectors.toList<PriorTumorTreatment>())
    }

    private fun extractPriorSecondPrimaries(questionnaire: Questionnaire?): List<PriorSecondPrimary> {
        return if (questionnaire == null) emptyList<PriorSecondPrimary>() else Stream.of<List<String?>>(
            questionnaire.otherOncologicalHistory(),
            questionnaire.secondaryPrimaries()
        )
            .flatMap<PriorSecondPrimary>(Function<List<String?>, Stream<out PriorSecondPrimary>> { entry: List<String?>? ->
                curation.curatePriorSecondPrimaries(
                    entry
                ).stream()
            })
            .collect(Collectors.toList<PriorSecondPrimary>())
    }

    private fun extractPriorOtherConditions(questionnaire: Questionnaire?): List<PriorOtherCondition> {
        return if (questionnaire == null) emptyList<PriorOtherCondition>() else curation.curatePriorOtherConditions(questionnaire.nonOncologicalHistory())
    }

    private fun extractPriorMolecularTests(questionnaire: Questionnaire?): List<PriorMolecularTest> {
        return if (questionnaire == null) emptyList<PriorMolecularTest>() else Stream.of(
            Maps.immutableEntry("IHC", questionnaire.ihcTestResults()),
            Maps.immutableEntry<String, List<String>>("PD-L1", questionnaire.pdl1TestResults())
        )
            .flatMap<PriorMolecularTest>(Function<Map.Entry<String, List<String>>, Stream<out PriorMolecularTest>> { (key, value): Map.Entry<String, List<String>> ->
                curation.curatePriorMolecularTests(
                    key, value
                ).stream()
            })
            .collect(Collectors.toList<PriorMolecularTest>())
    }

    private fun extractComplications(questionnaire: Questionnaire?): List<Complication>? {
        return if (questionnaire != null) curation.curateComplications(questionnaire.complications()) else null
    }

    private fun extractLabValues(subject: String): List<LabValue> {
        return feed.labEntries(subject)
            .stream()
            .map<LabValue>(Function<LabEntry, LabValue> { obj: LabEntry? -> LabExtraction.extract() })
            .map<LabValue>(Function<LabValue, LabValue> { input: LabValue? -> curation.translateLabValue(input) })
            .sorted(LabValueDescendingDateComparator())
            .collect<List<LabValue>, Any>(Collectors.toList<LabValue>())
    }

    private fun extractToxicities(subject: String, questionnaire: Questionnaire?): List<Toxicity> {
        val toxicities: MutableList<Toxicity> = feed.toxicityEntries(subject).stream().flatMap<ImmutableToxicity>(
            Function<DigitalFileEntry, Stream<out ImmutableToxicity>> { toxicityEntry: DigitalFileEntry ->
                val grade = extractGrade(toxicityEntry)
                if (grade != null) {
                    return@flatMap Stream.of<ImmutableToxicity>(
                        ImmutableToxicity.builder()
                            .name(toxicityEntry.itemText())
                            .evaluatedDate(toxicityEntry.authored())
                            .source(ToxicitySource.EHR)
                            .grade(grade)
                            .build()
                    )
                } else {
                    return@flatMap Stream.empty<ImmutableToxicity>()
                }
            }).map<Toxicity>(Function<ImmutableToxicity, Toxicity> { input: ImmutableToxicity? -> curation.translateToxicity(input) })
            .collect<List<Toxicity>, Any>(Collectors.toList<Toxicity>())
        if (questionnaire != null) {
            toxicities.addAll(curation.curateQuestionnaireToxicities(questionnaire.unresolvedToxicities(), questionnaire.date()))
        }
        return toxicities
    }

    private fun extractIntolerances(subject: String): List<Intolerance> {
        return feed.intoleranceEntries(subject)
            .stream()
            .map<ImmutableIntolerance>(Function<IntoleranceEntry, ImmutableIntolerance> { entry: IntoleranceEntry ->
                ImmutableIntolerance.builder()
                    .name(CurationUtil.capitalizeFirstLetterOnly(entry.codeText()))
                    .category(CurationUtil.capitalizeFirstLetterOnly(entry.category()))
                    .type(CurationUtil.capitalizeFirstLetterOnly(entry.isSideEffect()))
                    .clinicalStatus(CurationUtil.capitalizeFirstLetterOnly(entry.clinicalStatus()))
                    .verificationStatus(CurationUtil.capitalizeFirstLetterOnly(entry.verificationStatus()))
                    .criticality(CurationUtil.capitalizeFirstLetterOnly(entry.criticality()))
                    .build()
            })
            .map<Intolerance>(Function<ImmutableIntolerance, Intolerance> { intolerance: ImmutableIntolerance? ->
                curation.curateIntolerance(
                    intolerance
                )
            })
            .collect<List<Intolerance>, Any>(Collectors.toList<Intolerance>())
    }

    private fun extractSurgeries(subject: String): List<Surgery> {
        return feed.uniqueSurgeryEntries(subject)
            .stream()
            .map<ImmutableSurgery>(Function<SurgeryEntry, ImmutableSurgery> { entry: SurgeryEntry ->
                ImmutableSurgery.builder()
                    .endDate(entry.periodEnd())
                    .status(resolveSurgeryStatus(entry.encounterStatus()))
                    .build()
            })
            .collect<List<Surgery>, Any>(Collectors.toList<Surgery>())
    }

    private fun extractSurgicalTreatments(subject: String): List<TreatmentHistoryEntry> {
        return feed.uniqueSurgeryEntries(subject)
            .stream()
            .map<Any>(Function<SurgeryEntry, Any> { encounterEntry: SurgeryEntry ->
                ImmutableTreatmentHistoryEntry.builder()
                    .treatments(setOf(ImmutableSurgicalTreatment.builder().name("extracted surgery").build()))
                    .surgeryHistoryDetails(
                        ImmutableSurgeryHistoryDetails.builder()
                            .endDate(encounterEntry.periodEnd())
                            .status(resolveSurgeryStatus(encounterEntry.encounterStatus()))
                            .build()
                    )
                    .build()
            })
            .collect<List<TreatmentHistoryEntry>, Any>(Collectors.toList<Any>())
    }

    private fun extractBodyWeights(subject: String): List<BodyWeight> {
        return feed.uniqueBodyWeightEntries(subject)
            .stream()
            .map<ImmutableBodyWeight>(Function<BodyWeightEntry, ImmutableBodyWeight> { entry: BodyWeightEntry ->
                ImmutableBodyWeight.builder()
                    .date(entry.effectiveDateTime())
                    .value(entry.valueQuantityValue())
                    .unit(entry.valueQuantityUnit())
                    .build()
            })
            .collect<List<BodyWeight>, Any>(Collectors.toList<BodyWeight>())
    }

    private fun extractVitalFunctions(subject: String): List<VitalFunction> {
        return feed.vitalFunctionEntries(subject)
            .stream()
            .map<ImmutableVitalFunction>(Function<VitalFunctionEntry, ImmutableVitalFunction> { entry: VitalFunctionEntry ->
                ImmutableVitalFunction.builder()
                    .date(entry.effectiveDateTime())
                    .category(VitalFunctionExtraction.determineCategory(entry.codeDisplayOriginal()))
                    .subcategory(entry.componentCodeDisplay())
                    .value(entry.quantityValue())
                    .unit(entry.quantityUnit())
                    .build()
            })
            .collect<List<VitalFunction>, Any>(Collectors.toList<VitalFunction>())
    }

    private fun extractBloodTransfusions(subject: String): List<BloodTransfusion> {
        return feed.bloodTransfusionEntries(subject)
            .stream()
            .map<ImmutableBloodTransfusion>(Function<DigitalFileEntry, ImmutableBloodTransfusion> { entry: DigitalFileEntry ->
                ImmutableBloodTransfusion.builder()
                    .date(entry.authored())
                    .product(entry.itemAnswerValueValueString())
                    .build()
            })
            .map<BloodTransfusion>(Function<ImmutableBloodTransfusion, BloodTransfusion> { input: ImmutableBloodTransfusion? ->
                curation.translateBloodTransfusion(
                    input
                )
            })
            .collect<List<BloodTransfusion>, Any>(Collectors.toList<BloodTransfusion>())
    }

    private fun extractMedications(subject: String): List<Medication> {
        val medications: MutableList<Medication> = Lists.newArrayList()
        for (entry in feed.medicationEntries(subject)) {
            val dosageCurated: Medication = curation.curateMedicationDosage(entry.dosageInstructionText())
            val builder: ImmutableMedication.Builder = ImmutableMedication.builder()
            if (dosageCurated != null) {
                builder.from(dosageCurated)
            }
            var name: String = CurationUtil.capitalizeFirstLetterOnly(entry.code5ATCDisplay())
            if (name.isEmpty()) {
                val input: String = CurationUtil.capitalizeFirstLetterOnly(entry.codeText())
                name = curation.curateMedicationName(input)
            }
            if (name != null && !name.isEmpty()) {
                val medication: Medication = builder.name(name)
                    .codeATC(curation.curateMedicationCodeATC(entry.code5ATCCode()))
                    .chemicalSubgroupAtc(entry.chemicalSubgroupDisplay())
                    .pharmacologicalSubgroupAtc(entry.pharmacologicalSubgroupDisplay())
                    .therapeuticSubgroupAtc(entry.therapeuticSubgroupDisplay())
                    .anatomicalMainGroupAtc(entry.anatomicalMainGroupDisplay())
                    .status(curation.curateMedicationStatus(entry.status()))
                    .administrationRoute(curation.translateAdministrationRoute(entry.dosageInstructionRouteDisplay()))
                    .startDate(entry.periodOfUseValuePeriodStart())
                    .stopDate(entry.periodOfUseValuePeriodEnd())
                    .build()
                medications.add(curation.annotateWithMedicationCategory(medication))
            }
        }
        medications.sort(MedicationByNameComparator())
        return medications
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ClinicalRecordsFactory::class.java)

        @JvmStatic
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
            val value: String = entry.itemAnswerValueValueString()
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