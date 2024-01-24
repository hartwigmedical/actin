package com.hartwig.actin.clinical.nki

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentStage
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class EhrDataFeed(
    private val directory: String,
    private val qtPrologatingRiskCuration: CurationDatabase<QTProlongatingConfig>,
    private val cypInteractionCuration: CurationDatabase<CypInteractionConfig>,
    private val treatmentDatabase: TreatmentDatabase,
    private val atcModel: AtcModel
) {

    fun ingest(): List<ClinicalRecord> {
        return Files.list(Paths.get(directory)).map {
            val ehrPatientRecord = feedJson.decodeFromString(EhrPatientRecord.serializer(), Files.readString(it))
            val patientDetails = patientDetails(ehrPatientRecord)
            val tumorDetails = tumorDetails(ehrPatientRecord)
            val clinicalStatus = clinicalStatus(ehrPatientRecord)
            val treatmentHistory = treatmentHistory(ehrPatientRecord)
            val priorMolecularTests = molecularTest(ehrPatientRecord)
            val priorOtherCondition = priorOtherConditions(ehrPatientRecord)
            val complications = complications(ehrPatientRecord)
            val toxicities = toxicities(ehrPatientRecord)
            val medications = medications(ehrPatientRecord)
            val labValues = labValues(ehrPatientRecord)
            val bloodTransfusions = bloodTransfusions(ehrPatientRecord)
            val vitalFunctions = vitalFunctions(ehrPatientRecord)
            val intolerances = intolerances(ehrPatientRecord)
            val surgeries = surgeries(ehrPatientRecord)

            ImmutableClinicalRecord.builder().patientId(ehrPatientRecord.patientDetails.patientId).patient(patientDetails)
                .tumor(tumorDetails).clinicalStatus(clinicalStatus).oncologicalHistory(treatmentHistory)
                .priorMolecularTests(priorMolecularTests)
                .priorOtherConditions(priorOtherCondition)
                .complications(complications)
                .toxicities(toxicities)
                .medications(medications)
                .labValues(labValues)
                .bloodTransfusions(bloodTransfusions)
                .vitalFunctions(vitalFunctions)
                .intolerances(intolerances)
                .surgeries(surgeries)
                .build()


        }.collect(Collectors.toList())
    }

    private fun surgeries(ehrPatientRecord: EhrPatientRecord): List<Surgery> {
        return ehrPatientRecord.surgeries.map {
            ImmutableSurgery.builder().endDate(it.endDate).status(SurgeryStatus.valueOf(it.status)).build()
        }
    }

    private fun intolerances(ehrPatientRecord: EhrPatientRecord): List<Intolerance> {
        return ehrPatientRecord.allergies.map {
            ImmutableIntolerance.builder().name(it.description).name(it.description).category("").type("").clinicalStatus("")
                .verificationStatus("").criticality("").build()
        }
    }

    private fun vitalFunctions(ehrPatientRecord: EhrPatientRecord): List<VitalFunction> {
        return ehrPatientRecord.vitalFunctions.map {
            ImmutableVitalFunction.builder().date(it.date.atStartOfDay()).category(VitalFunctionCategory.fromString(it.measure))
                .value(it.value).unit(it.unit).subcategory("")
                .valid(true).build()
        }
    }

    private fun bloodTransfusions(ehrPatientRecord: EhrPatientRecord): List<BloodTransfusion> {
        return ehrPatientRecord.bloodTransfusions.map {
            ImmutableBloodTransfusion.builder().product(it.product).date(it.dateTime.toLocalDate()).build()
        }
    }

    private fun labValues(ehrPatientRecord: EhrPatientRecord): List<LabValue> {
        return ehrPatientRecord.labValues.map {
            ImmutableLabValue.builder().date(it.dateTime.toLocalDate()).name(it.measure).unit(LabUnit.fromString(it.unit)).value(it.value)
                .code("").comparator("").build()
        }
    }

    private fun medications(ehrPatientRecord: EhrPatientRecord): List<Medication> {
        return ehrPatientRecord.medications.map {
            val atcClassification = atcModel.resolveByCode(it.atcCode)
            val curatedQT = CurationResponse.createFromConfigs(
                qtPrologatingRiskCuration.find(it.drugName),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.QT_PROLONGATION,
                it.drugName,
                "medication name",
                true
            )
            val curatedCyp = CurationResponse.createFromConfigs(
                cypInteractionCuration.find(it.drugName),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.CYP_INTERACTION,
                it.drugName,
                "medication name",
                true
            )
            ImmutableMedication.builder().name(it.drugName).administrationRoute(it.administrationRoute)
                .dosage(
                    ImmutableDosage.builder().dosageMax(it.dosage).dosageMin(it.dosage).dosageUnit(it.dosageUnit).frequency(it.frequency)
                        .frequencyUnit(it.frequencyUnit)
                        .periodBetweenValue(it.periodBetweenDosagesValue)
                        .periodBetweenUnit(it.periodBetweenDosagesUnit)
                        .ifNeeded(it.administrationOnlyIfNeeded)
                        .build()
                )
                .startDate(it.startDate)
                .stopDate(it.endDate)
                .atc(atcClassification)
                .qtProlongatingRisk(curatedQT.config()?.status ?: QTProlongatingRisk.UNKNOWN)
                .cypInteractions(curatedCyp.config()?.interactions ?: emptyList())
                .isTrialMedication(false)
                .isSelfCare(false)
                .build()
        }
    }

    private fun toxicities(ehrPatientRecord: EhrPatientRecord): List<Toxicity> {
        return ehrPatientRecord.toxicities.map {
            ImmutableToxicity.builder().name(it.name).grade(it.grade).categories(it.categories).evaluatedDate(it.evaluatedDate)
                .source(ToxicitySource.EHR)
                .build()
        }
    }

    private fun complications(ehrPatientRecord: EhrPatientRecord): List<Complication> {
        return ehrPatientRecord.complications.map {
            ImmutableComplication.builder().name(it.name).year(it.startDate.year).month(it.startDate.monthValue).build()
        }
    }

    private fun priorOtherConditions(ehrPatientRecord: EhrPatientRecord): List<PriorOtherCondition> {
        return ehrPatientRecord.priorOtherConditions.map {
            ImmutablePriorOtherCondition.builder().name(it.diagnosis).year(it.startDate.year).month(it.startDate.monthValue).category("")
                .isContraindicationForTherapy(false).build()
        }
    }

    private fun molecularTest(ehrPatientRecord: EhrPatientRecord): List<PriorMolecularTest> {
        return ehrPatientRecord.molecularTestHistory.map {
            ImmutablePriorMolecularTest.builder().test(it.type).item(it.measure).impliesPotentialIndeterminateStatus(false)
                .scoreText(it.measure).build()
        }
    }

    private fun treatmentHistory(ehrPatientRecord: EhrPatientRecord): List<TreatmentHistoryEntry> {
        return ehrPatientRecord.treatmentHistory.map {

            val treatment = treatmentDatabase.findTreatmentByName(it.treatmentName)

            val history = ImmutableTreatmentHistoryDetails.builder()
                .stopYear(it.endDate.year)
                .stopMonth(it.endDate.monthValue)
                .stopReason(StopReason.createFromString(it.stopReason))
                .bestResponse(TreatmentResponse.createFromString(it.response))
                .switchToTreatments(it.modifications.map { modification ->
                    ImmutableTreatmentStage.builder()
                        .treatment(treatmentDatabase.findTreatmentByName(modification) ?: throw IllegalArgumentException()).build()
                })
                .toxicities(listOf(ImmutableObservedToxicity.builder().name(it.grade2Toxicities).grade(2).build()))
                .cycles(it.administeredCycles)
                .build()

            ImmutableTreatmentHistoryEntry.builder().startYear(it.startDate.year)
                .startMonth(it.startDate.monthValue).intents(listOf(Intent.valueOf(it.intention)))
                .treatments(listOf(treatment))
                .treatmentHistoryDetails(history)
                .isTrial(it.administeredInStudy)

                .build()
        }
    }

    private fun clinicalStatus(ehrPatientRecord: EhrPatientRecord): ImmutableClinicalStatus {
        val mostRecentWho = ehrPatientRecord.whoEvaluations.maxBy { who -> who.evaluationDate }
        val clinicalStatus =
            ImmutableClinicalStatus.builder().who(mostRecentWho.status).hasComplications(ehrPatientRecord.complications.isNotEmpty())
                .build()
        return clinicalStatus
    }

    private fun patientDetails(ehrPatientRecord: EhrPatientRecord): ImmutablePatientDetails {
        val patientDetails = ImmutablePatientDetails.builder().gender(Gender.valueOf(ehrPatientRecord.patientDetails.gender))
            .birthYear(ehrPatientRecord.patientDetails.birthYear).registrationDate(ehrPatientRecord.patientDetails.registrationDate)
            .build()
        return patientDetails
    }

    private fun tumorDetails(ehrPatientRecord: EhrPatientRecord): ImmutableTumorDetails {
        val tumorDetails = ImmutableTumorDetails.builder().primaryTumorLocation(ehrPatientRecord.tumorDetails.tumorLocalization)
            .primaryTumorType(ehrPatientRecord.tumorDetails.tumorTypeDetails)
            .stage(TumorStage.valueOf(ehrPatientRecord.tumorDetails.tumorStage))
            .hasBoneLesions(true)
            .hasMeasurableDisease(ehrPatientRecord.tumorDetails.measurableDisease)
            .build()
        return tumorDetails
    }
}