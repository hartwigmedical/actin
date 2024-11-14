package com.hartwig.actin.database.dao

import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver
import com.hartwig.actin.database.Tables
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.ECG
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.util.Optional
import org.jooq.DSLContext

internal class ClinicalDAO(private val context: DSLContext) {

    fun clear() {
        context.execute("SET FOREIGN_KEY_CHECKS = 0;")
        context.truncate(Tables.PATIENT).execute()
        context.truncate(Tables.TUMOR).execute()
        context.truncate(Tables.CLINICALSTATUS).execute()
        context.truncate(Tables.TREATMENTHISTORYENTRY).execute()
        context.truncate(Tables.PRIORSECONDPRIMARY).execute()
        context.truncate(Tables.PRIOROTHERCONDITION).execute()
        context.truncate(Tables.PRIORIHCTEST).execute()
        context.truncate(Tables.COMPLICATION).execute()
        context.truncate(Tables.LABVALUE).execute()
        context.truncate(Tables.TOXICITY).execute()
        context.truncate(Tables.INTOLERANCE).execute()
        context.truncate(Tables.SURGERY).execute()
        context.truncate(Tables.BODYWEIGHT).execute()
        context.truncate(Tables.VITALFUNCTION).execute()
        context.truncate(Tables.BLOODTRANSFUSION).execute()
        context.truncate(Tables.MEDICATION).execute()
        context.execute("SET FOREIGN_KEY_CHECKS = 1;")
    }

    fun writeClinicalRecord(record: ClinicalRecord) {
        val patientId = record.patientId
        writePatientDetails(patientId, record.patient)
        writeTumorDetails(patientId, record.tumor)
        writeClinicalStatus(patientId, record.clinicalStatus)
        writeTreatmentHistoryEntries(patientId, record.oncologicalHistory)
        writePriorSecondPrimaries(patientId, record.priorSecondPrimaries)
        writePriorOtherConditions(patientId, record.priorOtherConditions)
        writePriorMolecularTests(patientId, record.priorIHCTests)
        writeComplications(patientId, record.complications)
        writeLabValues(patientId, record.labValues)
        writeToxicities(patientId, record.toxicities)
        writeIntolerances(patientId, record.intolerances)
        writeSurgeries(patientId, record.surgeries)
        writeBodyWeights(patientId, record.bodyWeights)
        writeVitalFunctions(patientId, record.vitalFunctions)
        writeBloodTransfusions(patientId, record.bloodTransfusions)
        record.medications?.let { writeMedications(patientId, it) }
    }

    private fun writePatientDetails(patientId: String, patient: PatientDetails) {
        context.insertInto(
            Tables.PATIENT,
            Tables.PATIENT.PATIENTID,
            Tables.PATIENT.BIRTHYEAR,
            Tables.PATIENT.GENDER,
            Tables.PATIENT.REGISTRATIONDATE,
            Tables.PATIENT.QUESTIONNAIREDATE,
        )
            .values(
                patientId,
                patient.birthYear,
                patient.gender.display(),
                patient.registrationDate,
                patient.questionnaireDate
            )
            .execute()
    }

    private fun writeTumorDetails(patientId: String, tumor: TumorDetails) {
        val stage = tumor.stage
        context.insertInto(
            Tables.TUMOR,
            Tables.TUMOR.PATIENTID,
            Tables.TUMOR.PRIMARYTUMORLOCATION,
            Tables.TUMOR.PRIMARYTUMORSUBLOCATION,
            Tables.TUMOR.PRIMARYTUMORTYPE,
            Tables.TUMOR.PRIMARYTUMORSUBTYPE,
            Tables.TUMOR.PRIMARYTUMOREXTRADETAILS,
            Tables.TUMOR.DOIDS,
            Tables.TUMOR.STAGE,
            Tables.TUMOR.HASMEASURABLEDISEASE,
            Tables.TUMOR.HASBRAINLESIONS,
            Tables.TUMOR.HASSUSPECTEDBRAINLESIONS,
            Tables.TUMOR.HASACTIVEBRAINLESIONS,
            Tables.TUMOR.HASCNSLESIONS,
            Tables.TUMOR.HASSUSPECTEDCNSLESIONS,
            Tables.TUMOR.HASACTIVECNSLESIONS,
            Tables.TUMOR.HASBONELESIONS,
            Tables.TUMOR.HASSUSPECTEDBONELESIONS,
            Tables.TUMOR.HASLIVERLESIONS,
            Tables.TUMOR.HASSUSPECTEDLIVERLESIONS,
            Tables.TUMOR.HASLUNGLESIONS,
            Tables.TUMOR.HASSUSPECTEDLUNGLESIONS,
            Tables.TUMOR.HASLYMPHNODELESIONS,
            Tables.TUMOR.HASSUSPECTEDLYMPHNODELESIONS,
            Tables.TUMOR.OTHERLESIONS,
            Tables.TUMOR.OTHERSUSPECTEDLESIONS,
            Tables.TUMOR.BIOPSYLOCATION
        )
            .values(
                patientId,
                tumor.primaryTumorLocation,
                tumor.primaryTumorSubLocation,
                tumor.primaryTumorType,
                tumor.primaryTumorSubType,
                tumor.primaryTumorExtraDetails,
                DataUtil.concat(tumor.doids),
                stage?.display(),
                tumor.hasMeasurableDisease,
                tumor.hasBrainLesions,
                tumor.hasSuspectedBrainLesions,
                tumor.hasActiveBrainLesions,
                tumor.hasCnsLesions,
                tumor.hasSuspectedCnsLesions,
                tumor.hasActiveCnsLesions,
                tumor.hasBoneLesions,
                tumor.hasSuspectedBoneLesions,
                tumor.hasLiverLesions,
                tumor.hasSuspectedLiverLesions,
                tumor.hasLungLesions,
                tumor.hasSuspectedLungLesions,
                tumor.hasLymphNodeLesions,
                tumor.hasSuspectedLymphNodeLesions,
                DataUtil.concat(tumor.otherLesions),
                DataUtil.concat(tumor.otherSuspectedLesions),
                tumor.biopsyLocation
            )
            .execute()
    }

    private fun writeClinicalStatus(patientId: String, clinicalStatus: ClinicalStatus) {
        val infectionStatus = clinicalStatus.infectionStatus
        val ecg = Optional.ofNullable(clinicalStatus.ecg)
        val qtcfMeasure = ecg.map(ECG::qtcfMeasure)
        val jtcMeasure = ecg.map(ECG::jtcMeasure)
        context.insertInto(
            Tables.CLINICALSTATUS,
            Tables.CLINICALSTATUS.PATIENTID,
            Tables.CLINICALSTATUS.WHO,
            Tables.CLINICALSTATUS.HASACTIVEINFECTION,
            Tables.CLINICALSTATUS.ACTIVEINFECTIONDESCRIPTION,
            Tables.CLINICALSTATUS.HASSIGABERRATIONLATESTECG,
            Tables.CLINICALSTATUS.ECGABERRATIONDESCRIPTION,
            Tables.CLINICALSTATUS.QTCFVALUE,
            Tables.CLINICALSTATUS.QTCFUNIT,
            Tables.CLINICALSTATUS.JTCVALUE,
            Tables.CLINICALSTATUS.JTCUNIT,
            Tables.CLINICALSTATUS.LVEF,
            Tables.CLINICALSTATUS.HASCOMPLICATIONS
        )
            .values(
                patientId,
                clinicalStatus.who,
                infectionStatus?.hasActiveInfection,
                infectionStatus?.description,
                ecg.map(ECG::hasSigAberrationLatestECG).orElse(null),
                ecg.map(ECG::aberrationDescription).orElse(null),
                qtcfMeasure.map { it?.value }.orElse(null),
                qtcfMeasure.map { it?.unit }.orElse(null),
                jtcMeasure.map { it?.value }.orElse(null),
                jtcMeasure.map { it?.unit }.orElse(null),
                clinicalStatus.lvef,
                clinicalStatus.hasComplications
            )
            .execute()
    }

    private fun writeTreatmentHistoryEntries(patientId: String, treatmentHistoryEntries: List<TreatmentHistoryEntry>) {
        val records = treatmentHistoryEntries.flatMap { multiEntry: TreatmentHistoryEntry ->
            multiEntry.treatments.map { multiEntry.copy(treatments = setOf(it)) }
        }
            .map { entry: TreatmentHistoryEntry ->
                val treatment = entry.treatments.iterator().next()
                val intentString = DataUtil.concatObjects(entry.intents) ?: ""
                val drugTreatment = treatment as? DrugTreatment
                val radiotherapy = treatment as? Radiotherapy
                val details = entry.treatmentHistoryDetails
                val valueMap = mapOf(
                    "patientId" to patientId,
                    "name" to treatment.name,
                    "startYear" to entry.startYear,
                    "startMonth" to entry.startMonth,
                    "intents" to intentString,
                    "isTrial" to entry.isTrial,
                    "trialAcronym" to entry.trialAcronym,
                    "categories" to TreatmentCategoryResolver.toStringList(treatment.categories()),
                    "synonyms" to DataUtil.concat(treatment.synonyms),
                    "isSystemic" to treatment.isSystemic,
                    "drugs" to drugTreatment?.let { drugTx ->
                        DataUtil.concat(drugTx.drugs.map { "${it.name} (${it.drugTypes.sorted().joinToString(", ")})" })
                    },
                    "maxCycles" to drugTreatment?.maxCycles,
                    "isInternal" to radiotherapy?.isInternal,
                    "radioType" to radiotherapy?.radioType,
                    "stopYear" to details?.stopYear,
                    "stopMonth" to details?.stopMonth,
                    "ongoingAsOf" to details?.ongoingAsOf,
                    "cycles" to details?.cycles,
                    "bestResponse" to details?.bestResponse,
                    "stopReason" to details?.stopReason,
                    "stopReasonDetail" to details?.stopReasonDetail,
                    "toxicities" to details?.toxicities?.let { toxicities ->
                        DataUtil.concat(toxicities.map { "${it.name} grade ${it.grade} (${DataUtil.concat(it.categories)})" })
                    }
                )
                val maintenanceTreatmentMap = details?.maintenanceTreatment?.let { maintenanceTreatment ->
                    mapOf(
                        "maintenanceTreatment" to maintenanceTreatment.treatment.name,
                        "maintenanceTreatmentStartYear" to maintenanceTreatment.startYear,
                        "maintenanceTreatmentStartMonth" to maintenanceTreatment.startMonth,
                    )
                } ?: emptyMap()

                val switchToTreatmentMap = details?.switchToTreatments?.firstOrNull()?.let { switchToTreatment ->
                    mapOf(
                        "switchToTreatment" to switchToTreatment.treatment.name,
                        "switchToTreatmentStartYear" to switchToTreatment.startYear,
                        "switchToTreatmentStartMonth" to switchToTreatment.startMonth,
                        "switchToTreatmentCycles" to switchToTreatment.cycles
                    )
                } ?: emptyMap()

                val record = context.newRecord(Tables.TREATMENTHISTORYENTRY)
                record.fromMap(valueMap + maintenanceTreatmentMap + switchToTreatmentMap)
                record
            }
        context.batchInsert(records).execute()
    }

    private fun writePriorSecondPrimaries(patientId: String, priorSecondPrimaries: List<PriorSecondPrimary>) {
        for (priorSecondPrimary in priorSecondPrimaries) {
            context.insertInto(
                Tables.PRIORSECONDPRIMARY,
                Tables.PRIORSECONDPRIMARY.PATIENTID,
                Tables.PRIORSECONDPRIMARY.TUMORLOCATION,
                Tables.PRIORSECONDPRIMARY.TUMORSUBLOCATION,
                Tables.PRIORSECONDPRIMARY.TUMORTYPE,
                Tables.PRIORSECONDPRIMARY.TUMORSUBTYPE,
                Tables.PRIORSECONDPRIMARY.DOIDS,
                Tables.PRIORSECONDPRIMARY.DIAGNOSEDYEAR,
                Tables.PRIORSECONDPRIMARY.DIAGNOSEDMONTH,
                Tables.PRIORSECONDPRIMARY.TREATMENTHISTORY,
                Tables.PRIORSECONDPRIMARY.LASTTREATMENTYEAR,
                Tables.PRIORSECONDPRIMARY.LASTTREATMENTMONTH,
                Tables.PRIORSECONDPRIMARY.STATUS
            )
                .values(
                    patientId,
                    priorSecondPrimary.tumorLocation,
                    priorSecondPrimary.tumorSubLocation,
                    priorSecondPrimary.tumorType,
                    priorSecondPrimary.tumorSubType,
                    DataUtil.concat(priorSecondPrimary.doids),
                    priorSecondPrimary.diagnosedYear,
                    priorSecondPrimary.diagnosedMonth,
                    priorSecondPrimary.treatmentHistory,
                    priorSecondPrimary.lastTreatmentYear,
                    priorSecondPrimary.lastTreatmentMonth,
                    priorSecondPrimary.status.toString()
                )
                .execute()
        }
    }

    private fun writePriorOtherConditions(patientId: String, priorOtherConditions: List<PriorOtherCondition>) {
        for (priorOtherCondition in priorOtherConditions) {
            context.insertInto(
                Tables.PRIOROTHERCONDITION,
                Tables.PRIOROTHERCONDITION.PATIENTID,
                Tables.PRIOROTHERCONDITION.NAME,
                Tables.PRIOROTHERCONDITION.YEAR,
                Tables.PRIOROTHERCONDITION.MONTH,
                Tables.PRIOROTHERCONDITION.DOIDS,
                Tables.PRIOROTHERCONDITION.CATEGORY,
                Tables.PRIOROTHERCONDITION.ISCONTRAINDICATIONFORTHERAPY
            )
                .values(
                    patientId,
                    priorOtherCondition.name,
                    priorOtherCondition.year,
                    priorOtherCondition.month,
                    DataUtil.concat(priorOtherCondition.doids),
                    priorOtherCondition.category,
                    priorOtherCondition.isContraindicationForTherapy
                )
                .execute()
        }
    }

    private fun writePriorMolecularTests(patientId: String, priorIHCTests: List<PriorIHCTest>) {
        for (priorIHCTest in priorIHCTests) {
            context.insertInto(
                Tables.PRIORIHCTEST,
                Tables.PRIORIHCTEST.PATIENTID,
                Tables.PRIORIHCTEST.TEST,
                Tables.PRIORIHCTEST.ITEM,
                Tables.PRIORIHCTEST.MEASURE,
                Tables.PRIORIHCTEST.MEASUREDATE,
                Tables.PRIORIHCTEST.SCORETEXT,
                Tables.PRIORIHCTEST.SCOREVALUEPREFIX,
                Tables.PRIORIHCTEST.SCOREVALUE,
                Tables.PRIORIHCTEST.SCOREVALUEUNIT,
                Tables.PRIORIHCTEST.IMPLIESPOTENTIALINDETERMINATESTATUS
            )
                .values(
                    patientId,
                    priorIHCTest.test,
                    priorIHCTest.item,
                    priorIHCTest.measure,
                    priorIHCTest.measureDate,
                    priorIHCTest.scoreText,
                    priorIHCTest.scoreValuePrefix,
                    priorIHCTest.scoreValue,
                    priorIHCTest.scoreValueUnit,
                    priorIHCTest.impliesPotentialIndeterminateStatus
                )
                .execute()
        }
    }

    private fun writeComplications(patientId: String, complications: List<Complication>?) {
        if (complications != null) {
            for (complication in complications) {
                if (complication.name.isNotEmpty()) {
                    context.insertInto(
                        Tables.COMPLICATION,
                        Tables.COMPLICATION.PATIENTID,
                        Tables.COMPLICATION.NAME,
                        Tables.COMPLICATION.CATEGORIES,
                        Tables.COMPLICATION.YEAR,
                        Tables.COMPLICATION.MONTH
                    )
                        .values(
                            patientId,
                            complication.name,
                            DataUtil.concat(complication.categories),
                            complication.year,
                            complication.month
                        )
                        .execute()
                }
            }
        }
    }

    private fun writeLabValues(patientId: String, labValues: List<LabValue>) {
        for (lab in labValues) {
            context.insertInto(
                Tables.LABVALUE,
                Tables.LABVALUE.PATIENTID,
                Tables.LABVALUE.DATE,
                Tables.LABVALUE.CODE,
                Tables.LABVALUE.NAME,
                Tables.LABVALUE.COMPARATOR,
                Tables.LABVALUE.VALUE,
                Tables.LABVALUE.UNIT,
                Tables.LABVALUE.REFLIMITLOW,
                Tables.LABVALUE.REFLIMITUP,
                Tables.LABVALUE.ISOUTSIDEREF
            )
                .values(
                    patientId,
                    lab.date,
                    lab.code,
                    lab.name,
                    lab.comparator,
                    lab.value,
                    lab.unit.display(),
                    lab.refLimitLow,
                    lab.refLimitUp,
                    lab.isOutsideRef
                )
                .execute()
        }
    }

    private fun writeToxicities(patientId: String, toxicities: List<Toxicity>) {
        for (toxicity in toxicities) {
            context.insertInto(
                Tables.TOXICITY,
                Tables.TOXICITY.PATIENTID,
                Tables.TOXICITY.NAME,
                Tables.TOXICITY.CATEGORIES,
                Tables.TOXICITY.EVALUATEDDATE,
                Tables.TOXICITY.SOURCE,
                Tables.TOXICITY.GRADE
            )
                .values(
                    patientId,
                    toxicity.name,
                    DataUtil.concat(toxicity.categories),
                    toxicity.evaluatedDate,
                    toxicity.source.display(),
                    toxicity.grade
                )
                .execute()
        }
    }

    private fun writeIntolerances(patientId: String, allergies: List<Intolerance>) {
        for (intolerance in allergies) {
            context.insertInto(
                Tables.INTOLERANCE,
                Tables.INTOLERANCE.PATIENTID,
                Tables.INTOLERANCE.NAME,
                Tables.INTOLERANCE.DOIDS,
                Tables.INTOLERANCE.CATEGORY,
                Tables.INTOLERANCE.SUBCATEGORIES,
                Tables.INTOLERANCE.TYPE,
                Tables.INTOLERANCE.CLINICALSTATUS,
                Tables.INTOLERANCE.VERIFICATIONSTATUS,
                Tables.INTOLERANCE.CRITICALITY
            )
                .values(
                    patientId,
                    intolerance.name,
                    DataUtil.concat(intolerance.doids),
                    intolerance.category,
                    DataUtil.concat(intolerance.subcategories),
                    intolerance.type,
                    intolerance.clinicalStatus,
                    intolerance.verificationStatus,
                    intolerance.criticality
                )
                .execute()
        }
    }

    private fun writeSurgeries(patientId: String, surgeries: List<Surgery>) {
        for (surgery in surgeries) {
            context.insertInto(Tables.SURGERY, Tables.SURGERY.PATIENTID, Tables.SURGERY.ENDDATE, Tables.SURGERY.STATUS)
                .values(patientId, surgery.endDate, surgery.status.toString())
                .execute()
        }
    }

    private fun writeBodyWeights(patientId: String, bodyWeights: List<BodyWeight>) {
        for (bodyWeight in bodyWeights) {
            context.insertInto(
                Tables.BODYWEIGHT,
                Tables.BODYWEIGHT.PATIENTID,
                Tables.BODYWEIGHT.DATE,
                Tables.BODYWEIGHT.VALUE,
                Tables.BODYWEIGHT.UNIT,
                Tables.BODYWEIGHT.VALID
            )
                .values(patientId, bodyWeight.date, bodyWeight.value, bodyWeight.unit, bodyWeight.valid)
                .execute()
        }
    }

    private fun writeVitalFunctions(patientId: String, vitalFunctions: List<VitalFunction>) {
        for (vitalFunction in vitalFunctions) {
            context.insertInto(
                Tables.VITALFUNCTION,
                Tables.VITALFUNCTION.PATIENTID,
                Tables.VITALFUNCTION.DATE,
                Tables.VITALFUNCTION.CATEGORY,
                Tables.VITALFUNCTION.SUBCATEGORY,
                Tables.VITALFUNCTION.VALUE,
                Tables.VITALFUNCTION.UNIT,
                Tables.VITALFUNCTION.VALID
            )
                .values(
                    patientId,
                    vitalFunction.date,
                    vitalFunction.category.display(),
                    vitalFunction.subcategory,
                    vitalFunction.value,
                    vitalFunction.unit,
                    vitalFunction.valid
                )
                .execute()
        }
    }

    private fun writeBloodTransfusions(patientId: String, bloodTransfusions: List<BloodTransfusion>) {
        for (bloodTransfusion in bloodTransfusions) {
            context.insertInto(
                Tables.BLOODTRANSFUSION,
                Tables.BLOODTRANSFUSION.PATIENTID,
                Tables.BLOODTRANSFUSION.DATE,
                Tables.BLOODTRANSFUSION.PRODUCT
            )
                .values(patientId, bloodTransfusion.date, bloodTransfusion.product)
                .execute()
        }
    }

    private fun writeMedications(patientId: String, medications: List<Medication>) {
        for (medication in medications) {
            val atc = medication.atc
            context.insertInto(
                Tables.MEDICATION,
                Tables.MEDICATION.PATIENTID,
                Tables.MEDICATION.NAME,
                Tables.MEDICATION.STATUS,
                Tables.MEDICATION.ADMINISTRATIONROUTE,
                Tables.MEDICATION.DOSAGEMIN,
                Tables.MEDICATION.DOSAGEMAX,
                Tables.MEDICATION.DOSAGEUNIT,
                Tables.MEDICATION.FREQUENCY,
                Tables.MEDICATION.FREQUENCYUNIT,
                Tables.MEDICATION.PERIODBETWEENVALUE,
                Tables.MEDICATION.PERIODBETWEENUNIT,
                Tables.MEDICATION.IFNEEDED,
                Tables.MEDICATION.STARTDATE,
                Tables.MEDICATION.STOPDATE,
                Tables.MEDICATION.CYPINTERACTIONS,
                Tables.MEDICATION.QTPROLONGATINGRISK,
                Tables.MEDICATION.ANATOMICALMAINGROUPATCNAME,
                Tables.MEDICATION.THERAPEUTICSUBGROUPATCNAME,
                Tables.MEDICATION.PHARMACOLOGICALSUBGROUPATCNAME,
                Tables.MEDICATION.CHEMICALSUBGROUPATCNAME,
                Tables.MEDICATION.CHEMICALSUBSTANCEATCCODE,
                Tables.MEDICATION.ISSELFCARE,
                Tables.MEDICATION.ISTRIALMEDICATION
            )
                .values(
                    patientId,
                    medication.name,
                    medication.status?.toString(),
                    medication.administrationRoute,
                    medication.dosage.dosageMin,
                    medication.dosage.dosageMax,
                    medication.dosage.dosageUnit,
                    medication.dosage.frequency,
                    medication.dosage.frequencyUnit,
                    medication.dosage.periodBetweenValue,
                    medication.dosage.periodBetweenUnit,
                    medication.dosage.ifNeeded,
                    medication.startDate,
                    medication.stopDate,
                    DataUtil.concat(medication.cypInteractions.map { "${it.strength} ${it.type} (${it.name})" }.toSet()),
                    medication.qtProlongatingRisk.toString(),
                    atc?.anatomicalMainGroup?.name,
                    atc?.therapeuticSubGroup?.name,
                    atc?.pharmacologicalSubGroup?.name,
                    atc?.chemicalSubGroup?.name,
                    atc?.chemicalSubstance?.code,
                    medication.isSelfCare,
                    medication.isTrialMedication
                ).execute()
        }
    }
}