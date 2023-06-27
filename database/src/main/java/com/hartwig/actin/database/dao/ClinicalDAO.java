package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.BLOODTRANSFUSION;
import static com.hartwig.actin.database.Tables.BODYWEIGHT;
import static com.hartwig.actin.database.Tables.CLINICALSTATUS;
import static com.hartwig.actin.database.Tables.COMPLICATION;
import static com.hartwig.actin.database.Tables.INTOLERANCE;
import static com.hartwig.actin.database.Tables.LABVALUE;
import static com.hartwig.actin.database.Tables.MEDICATION;
import static com.hartwig.actin.database.Tables.PATIENT;
import static com.hartwig.actin.database.Tables.PRIORMOLECULARTEST;
import static com.hartwig.actin.database.Tables.PRIOROTHERCONDITION;
import static com.hartwig.actin.database.Tables.PRIORSECONDPRIMARY;
import static com.hartwig.actin.database.Tables.PRIORTUMORTREATMENT;
import static com.hartwig.actin.database.Tables.SURGERY;
import static com.hartwig.actin.database.Tables.TOXICITY;
import static com.hartwig.actin.database.Tables.TUMOR;
import static com.hartwig.actin.database.Tables.VITALFUNCTION;

import java.util.List;
import java.util.Optional;

import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ECGMeasure;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;

class ClinicalDAO {

    @NotNull
    private final DSLContext context;

    public ClinicalDAO(@NotNull final DSLContext context) {
        this.context = context;
    }

    public void clear() {
        context.execute("SET FOREIGN_KEY_CHECKS = 0;");
        context.truncate(PATIENT).execute();
        context.truncate(TUMOR).execute();
        context.truncate(CLINICALSTATUS).execute();
        context.truncate(PRIORTUMORTREATMENT).execute();
        context.truncate(PRIORSECONDPRIMARY).execute();
        context.truncate(PRIOROTHERCONDITION).execute();
        context.truncate(PRIORMOLECULARTEST).execute();
        context.truncate(COMPLICATION).execute();
        context.truncate(LABVALUE).execute();
        context.truncate(TOXICITY).execute();
        context.truncate(INTOLERANCE).execute();
        context.truncate(SURGERY).execute();
        context.truncate(BODYWEIGHT).execute();
        context.truncate(VITALFUNCTION).execute();
        context.truncate(BLOODTRANSFUSION).execute();
        context.truncate(MEDICATION).execute();
        context.execute("SET FOREIGN_KEY_CHECKS = 1;");
    }

    public void writeClinicalRecord(@NotNull ClinicalRecord record) {
        String patientId = record.patientId();

        writePatientDetails(patientId, record.patient());
        writeTumorDetails(patientId, record.tumor());
        writeClinicalStatus(patientId, record.clinicalStatus());
        writePriorTumorTreatments(patientId, record.priorTumorTreatments());
        writePriorSecondPrimaries(patientId, record.priorSecondPrimaries());
        writePriorOtherConditions(patientId, record.priorOtherConditions());
        writePriorMolecularTests(patientId, record.priorMolecularTests());
        writeComplications(patientId, record.complications());
        writeLabValues(patientId, record.labValues());
        writeToxicities(patientId, record.toxicities());
        writeIntolerances(patientId, record.intolerances());
        writeSurgeries(patientId, record.surgeries());
        writeBodyWeights(patientId, record.bodyWeights());
        writeVitalFunctions(patientId, record.vitalFunctions());
        writeBloodTransfusions(patientId, record.bloodTransfusions());
        writeMedications(patientId, record.medications());
    }

    private void writePatientDetails(@NotNull String patientId, @NotNull PatientDetails patient) {
        context.insertInto(PATIENT,
                        PATIENT.PATIENTID,
                        PATIENT.BIRTHYEAR,
                        PATIENT.GENDER,
                        PATIENT.REGISTRATIONDATE,
                        PATIENT.QUESTIONNAIREDATE,
                        PATIENT.OTHERMOLECULARPATIENTID)
                .values(patientId,
                        patient.birthYear(),
                        patient.gender().display(),
                        patient.registrationDate(),
                        patient.questionnaireDate(),
                        patient.otherMolecularPatientId())
                .execute();
    }

    private void writeTumorDetails(@NotNull String patientId, @NotNull TumorDetails tumor) {
        TumorStage stage = tumor.stage();
        context.insertInto(TUMOR,
                        TUMOR.PATIENTID,
                        TUMOR.PRIMARYTUMORLOCATION,
                        TUMOR.PRIMARYTUMORSUBLOCATION,
                        TUMOR.PRIMARYTUMORTYPE,
                        TUMOR.PRIMARYTUMORSUBTYPE,
                        TUMOR.PRIMARYTUMOREXTRADETAILS,
                        TUMOR.DOIDS,
                        TUMOR.STAGE,
                        TUMOR.HASMEASURABLEDISEASE,
                        TUMOR.HASBRAINLESIONS,
                        TUMOR.HASACTIVEBRAINLESIONS,
                        TUMOR.HASCNSLESIONS,
                        TUMOR.HASACTIVECNSLESIONS,
                        TUMOR.HASBONELESIONS,
                        TUMOR.HASLIVERLESIONS,
                        TUMOR.HASLUNGLESIONS,
                        TUMOR.HASLYMPHNODELESIONS,
                        TUMOR.OTHERLESIONS,
                        TUMOR.BIOPSYLOCATION)
                .values(patientId,
                        tumor.primaryTumorLocation(),
                        tumor.primaryTumorSubLocation(),
                        tumor.primaryTumorType(),
                        tumor.primaryTumorSubType(),
                        tumor.primaryTumorExtraDetails(),
                        DataUtil.concat(tumor.doids()),
                        stage != null ? stage.display() : null,
                        tumor.hasMeasurableDisease(),
                        tumor.hasBrainLesions(),
                        tumor.hasActiveBrainLesions(),
                        tumor.hasCnsLesions(),
                        tumor.hasActiveCnsLesions(),
                        tumor.hasBoneLesions(),
                        tumor.hasLiverLesions(),
                        tumor.hasLungLesions(),
                        tumor.hasLymphNodeLesions(),
                        DataUtil.concat(tumor.otherLesions()),
                        tumor.biopsyLocation())
                .execute();
    }

    private void writeClinicalStatus(@NotNull String patientId, @NotNull ClinicalStatus clinicalStatus) {
        InfectionStatus infectionStatus = clinicalStatus.infectionStatus();
        Optional<ECG> ecg = Optional.ofNullable(clinicalStatus.ecg());
        Optional<ECGMeasure> qtcfMeasure = ecg.map(ECG::qtcfMeasure);
        Optional<ECGMeasure> jtcMeasure = ecg.map(ECG::jtcMeasure);
        context.insertInto(CLINICALSTATUS,
                        CLINICALSTATUS.PATIENTID,
                        CLINICALSTATUS.WHO,
                        CLINICALSTATUS.HASACTIVEINFECTION,
                        CLINICALSTATUS.ACTIVEINFECTIONDESCRIPTION,
                        CLINICALSTATUS.HASSIGABERRATIONLATESTECG,
                        CLINICALSTATUS.ECGABERRATIONDESCRIPTION,
                        CLINICALSTATUS.QTCFVALUE,
                        CLINICALSTATUS.QTCFUNIT,
                        CLINICALSTATUS.JTCVALUE,
                        CLINICALSTATUS.JTCUNIT,
                        CLINICALSTATUS.LVEF,
                        CLINICALSTATUS.HASCOMPLICATIONS)
                .values(patientId,
                        clinicalStatus.who(),
                        infectionStatus != null ? infectionStatus.hasActiveInfection() : null,
                        infectionStatus != null ? infectionStatus.description() : null,
                        ecg.map(ECG::hasSigAberrationLatestECG).orElse(null),
                        ecg.map(ECG::aberrationDescription).orElse(null),
                        qtcfMeasure.map(ECGMeasure::value).orElse(null),
                        qtcfMeasure.map(ECGMeasure::unit).orElse(null),
                        jtcMeasure.map(ECGMeasure::value).orElse(null),
                        jtcMeasure.map(ECGMeasure::unit).orElse(null),
                        clinicalStatus.lvef(),
                        clinicalStatus.hasComplications())
                .execute();
    }

    private void writePriorTumorTreatments(@NotNull String patientId, @NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        for (PriorTumorTreatment priorTumorTreatment : priorTumorTreatments) {
            context.insertInto(PRIORTUMORTREATMENT,
                            PRIORTUMORTREATMENT.PATIENTID,
                            PRIORTUMORTREATMENT.NAME,
                            PRIORTUMORTREATMENT.STARTYEAR,
                            PRIORTUMORTREATMENT.STARTMONTH,
                            PRIORTUMORTREATMENT.STOPYEAR,
                            PRIORTUMORTREATMENT.STOPMONTH,
                            PRIORTUMORTREATMENT.CYCLES,
                            PRIORTUMORTREATMENT.BESTRESPONSE,
                            PRIORTUMORTREATMENT.STOPREASON,
                            PRIORTUMORTREATMENT.CATEGORIES,
                            PRIORTUMORTREATMENT.ISSYSTEMIC,
                            PRIORTUMORTREATMENT.CHEMOTYPE,
                            PRIORTUMORTREATMENT.IMMUNOTYPE,
                            PRIORTUMORTREATMENT.TARGETEDTYPE,
                            PRIORTUMORTREATMENT.HORMONETYPE,
                            PRIORTUMORTREATMENT.RADIOTYPE,
                            PRIORTUMORTREATMENT.CARTTYPE,
                            PRIORTUMORTREATMENT.TRANSPLANTTYPE,
                            PRIORTUMORTREATMENT.SUPPORTIVETYPE,
                            PRIORTUMORTREATMENT.TRIALACRONYM,
                            PRIORTUMORTREATMENT.ABLATIONTYPE)
                    .values(patientId,
                            priorTumorTreatment.name(),
                            priorTumorTreatment.startYear(),
                            priorTumorTreatment.startMonth(),
                            priorTumorTreatment.stopYear(),
                            priorTumorTreatment.stopMonth(),
                            priorTumorTreatment.cycles(),
                            priorTumorTreatment.bestResponse(),
                            priorTumorTreatment.stopReason(),
                            TreatmentCategoryResolver.toStringList(priorTumorTreatment.categories()),
                            priorTumorTreatment.isSystemic(),
                            priorTumorTreatment.chemoType(),
                            priorTumorTreatment.immunoType(),
                            priorTumorTreatment.targetedType(),
                            priorTumorTreatment.hormoneType(),
                            priorTumorTreatment.radioType(),
                            priorTumorTreatment.carTType(),
                            priorTumorTreatment.transplantType(),
                            priorTumorTreatment.supportiveType(),
                            priorTumorTreatment.trialAcronym(),
                            priorTumorTreatment.ablationType())
                    .execute();
        }
    }

    private void writePriorSecondPrimaries(@NotNull String patientId, @NotNull List<PriorSecondPrimary> priorSecondPrimaries) {
        for (PriorSecondPrimary priorSecondPrimary : priorSecondPrimaries) {
            context.insertInto(PRIORSECONDPRIMARY,
                            PRIORSECONDPRIMARY.PATIENTID,
                            PRIORSECONDPRIMARY.TUMORLOCATION,
                            PRIORSECONDPRIMARY.TUMORSUBLOCATION,
                            PRIORSECONDPRIMARY.TUMORTYPE,
                            PRIORSECONDPRIMARY.TUMORSUBTYPE,
                            PRIORSECONDPRIMARY.DOIDS,
                            PRIORSECONDPRIMARY.DIAGNOSEDYEAR,
                            PRIORSECONDPRIMARY.DIAGNOSEDMONTH,
                            PRIORSECONDPRIMARY.TREATMENTHISTORY,
                            PRIORSECONDPRIMARY.LASTTREATMENTYEAR,
                            PRIORSECONDPRIMARY.LASTTREATMENTMONTH,
                            PRIORSECONDPRIMARY.ISACTIVE)
                    .values(patientId,
                            priorSecondPrimary.tumorLocation(),
                            priorSecondPrimary.tumorSubLocation(),
                            priorSecondPrimary.tumorType(),
                            priorSecondPrimary.tumorSubType(),
                            DataUtil.concat(priorSecondPrimary.doids()),
                            priorSecondPrimary.diagnosedYear(),
                            priorSecondPrimary.diagnosedMonth(),
                            priorSecondPrimary.treatmentHistory(),
                            priorSecondPrimary.lastTreatmentYear(),
                            priorSecondPrimary.lastTreatmentMonth(),
                            priorSecondPrimary.isActive())
                    .execute();
        }
    }

    private void writePriorOtherConditions(@NotNull String patientId, @NotNull List<PriorOtherCondition> priorOtherConditions) {
        for (PriorOtherCondition priorOtherCondition : priorOtherConditions) {
            context.insertInto(PRIOROTHERCONDITION,
                            PRIOROTHERCONDITION.PATIENTID,
                            PRIOROTHERCONDITION.NAME,
                            PRIOROTHERCONDITION.YEAR,
                            PRIOROTHERCONDITION.MONTH,
                            PRIOROTHERCONDITION.DOIDS,
                            PRIOROTHERCONDITION.CATEGORY,
                            PRIOROTHERCONDITION.ISCONTRAINDICATIONFORTHERAPY)
                    .values(patientId,
                            priorOtherCondition.name(),
                            priorOtherCondition.year(),
                            priorOtherCondition.month(),
                            DataUtil.concat(priorOtherCondition.doids()),
                            priorOtherCondition.category(),
                            priorOtherCondition.isContraindicationForTherapy())
                    .execute();
        }
    }

    private void writePriorMolecularTests(@NotNull String patientId, @NotNull List<PriorMolecularTest> priorMolecularTests) {
        for (PriorMolecularTest priorMolecularTest : priorMolecularTests) {
            context.insertInto(PRIORMOLECULARTEST,
                            PRIORMOLECULARTEST.PATIENTID,
                            PRIORMOLECULARTEST.TEST,
                            PRIORMOLECULARTEST.ITEM,
                            PRIORMOLECULARTEST.MEASURE,
                            PRIORMOLECULARTEST.SCORETEXT,
                            PRIORMOLECULARTEST.SCOREVALUEPREFIX,
                            PRIORMOLECULARTEST.SCOREVALUE,
                            PRIORMOLECULARTEST.SCOREVALUEUNIT,
                            PRIORMOLECULARTEST.IMPLIESPOTENTIALINDETERMINATESTATUS)
                    .values(patientId,
                            priorMolecularTest.test(),
                            priorMolecularTest.item(),
                            priorMolecularTest.measure(),
                            priorMolecularTest.scoreText(),
                            priorMolecularTest.scoreValuePrefix(),
                            priorMolecularTest.scoreValue(),
                            priorMolecularTest.scoreValueUnit(),
                            priorMolecularTest.impliesPotentialIndeterminateStatus())
                    .execute();
        }
    }

    private void writeComplications(@NotNull String patientId, @Nullable List<Complication> complications) {
        if (complications != null) {
            for (Complication complication : complications) {
                if (!complication.name().isEmpty()) {
                    context.insertInto(COMPLICATION,
                                    COMPLICATION.PATIENTID,
                                    COMPLICATION.NAME,
                                    COMPLICATION.CATEGORIES,
                                    COMPLICATION.YEAR,
                                    COMPLICATION.MONTH)
                            .values(patientId,
                                    complication.name(),
                                    DataUtil.concat(complication.categories()),
                                    complication.year(),
                                    complication.month())
                            .execute();
                }
            }
        }
    }

    private void writeLabValues(@NotNull String patientId, @NotNull List<LabValue> labValues) {
        for (LabValue lab : labValues) {
            context.insertInto(LABVALUE,
                            LABVALUE.PATIENTID,
                            LABVALUE.DATE,
                            LABVALUE.CODE,
                            LABVALUE.NAME,
                            LABVALUE.COMPARATOR,
                            LABVALUE.VALUE,
                            LABVALUE.UNIT,
                            LABVALUE.REFLIMITLOW,
                            LABVALUE.REFLIMITUP,
                            LABVALUE.ISOUTSIDEREF)
                    .values(patientId,
                            lab.date(),
                            lab.code(),
                            lab.name(),
                            lab.comparator(),
                            lab.value(),
                            lab.unit().display(),
                            lab.refLimitLow(),
                            lab.refLimitUp(),
                            lab.isOutsideRef())
                    .execute();
        }
    }

    private void writeToxicities(@NotNull String patientId, @NotNull List<Toxicity> toxicities) {
        for (Toxicity toxicity : toxicities) {
            context.insertInto(TOXICITY,
                            TOXICITY.PATIENTID,
                            TOXICITY.NAME,
                            TOXICITY.CATEGORIES,
                            TOXICITY.EVALUATEDDATE,
                            TOXICITY.SOURCE,
                            TOXICITY.GRADE)
                    .values(patientId,
                            toxicity.name(),
                            DataUtil.concat(toxicity.categories()),
                            toxicity.evaluatedDate(),
                            toxicity.source().display(),
                            toxicity.grade())
                    .execute();
        }
    }

    private void writeIntolerances(@NotNull String patientId, @NotNull List<Intolerance> allergies) {
        for (Intolerance intolerance : allergies) {
            context.insertInto(INTOLERANCE,
                            INTOLERANCE.PATIENTID,
                            INTOLERANCE.NAME,
                            INTOLERANCE.DOIDS,
                            INTOLERANCE.CATEGORY,
                            INTOLERANCE.SUBCATEGORIES,
                            INTOLERANCE.TYPE,
                            INTOLERANCE.CLINICALSTATUS,
                            INTOLERANCE.VERIFICATIONSTATUS,
                            INTOLERANCE.CRITICALITY)
                    .values(patientId,
                            intolerance.name(),
                            DataUtil.concat(intolerance.doids()),
                            intolerance.category(),
                            DataUtil.concat(intolerance.subcategories()),
                            intolerance.type(),
                            intolerance.clinicalStatus(),
                            intolerance.verificationStatus(),
                            intolerance.criticality())
                    .execute();
        }
    }

    private void writeSurgeries(@NotNull String patientId, @NotNull List<Surgery> surgeries) {
        for (Surgery surgery : surgeries) {
            context.insertInto(SURGERY, SURGERY.PATIENTID, SURGERY.ENDDATE, SURGERY.STATUS)
                    .values(patientId, surgery.endDate(), surgery.status().toString())
                    .execute();
        }
    }

    private void writeBodyWeights(@NotNull String patientId, @NotNull List<BodyWeight> bodyWeights) {
        for (BodyWeight bodyWeight : bodyWeights) {
            context.insertInto(BODYWEIGHT, BODYWEIGHT.PATIENTID, BODYWEIGHT.DATE, BODYWEIGHT.VALUE, BODYWEIGHT.UNIT)
                    .values(patientId, bodyWeight.date(), bodyWeight.value(), bodyWeight.unit())
                    .execute();
        }
    }

    private void writeVitalFunctions(@NotNull String patientId, @NotNull List<VitalFunction> vitalFunctions) {
        for (VitalFunction vitalFunction : vitalFunctions) {
            context.insertInto(VITALFUNCTION,
                            VITALFUNCTION.PATIENTID,
                            VITALFUNCTION.DATE,
                            VITALFUNCTION.CATEGORY,
                            VITALFUNCTION.SUBCATEGORY,
                            VITALFUNCTION.VALUE,
                            VITALFUNCTION.UNIT)
                    .values(patientId,
                            vitalFunction.date(),
                            vitalFunction.category().display(),
                            vitalFunction.subcategory(),
                            vitalFunction.value(),
                            vitalFunction.unit())
                    .execute();
        }
    }

    private void writeBloodTransfusions(@NotNull String patientId, @NotNull List<BloodTransfusion> bloodTransfusions) {
        for (BloodTransfusion bloodTransfusion : bloodTransfusions) {
            context.insertInto(BLOODTRANSFUSION, BLOODTRANSFUSION.PATIENTID, BLOODTRANSFUSION.DATE, BLOODTRANSFUSION.PRODUCT)
                    .values(patientId, bloodTransfusion.date(), bloodTransfusion.product())
                    .execute();
        }
    }

    private void writeMedications(@NotNull String patientId, @NotNull List<Medication> medications) {
        for (Medication medication : medications) {
            context.insertInto(MEDICATION,
                            MEDICATION.PATIENTID,
                            MEDICATION.NAME,
                            MEDICATION.CODEATC,
                            MEDICATION.CATEGORIES,
                            MEDICATION.CHEMICALSUBGROUPATC,
                            MEDICATION.PHARMACOLOGICALSUBGROUPATC,
                            MEDICATION.THERAPEUTICSUBGROUPATC,
                            MEDICATION.ANATOMICALMAINGROUPATC,
                            MEDICATION.STATUS,
                            MEDICATION.ADMINISTRATIONROUTE,
                            MEDICATION.DOSAGEMIN,
                            MEDICATION.DOSAGEMAX,
                            MEDICATION.DOSAGEUNIT,
                            MEDICATION.FREQUENCY,
                            MEDICATION.FREQUENCYUNIT,
                            MEDICATION.IFNEEDED,
                            MEDICATION.STARTDATE,
                            MEDICATION.STOPDATE)
                    .values(patientId,
                            medication.name(),
                            medication.codeATC(),
                            DataUtil.concat(medication.categories()),
                            medication.chemicalSubgroupAtc(),
                            medication.pharmacologicalSubgroupAtc(),
                            medication.therapeuticSubgroupAtc(),
                            medication.anatomicalMainGroupAtc(),
                            medication.status() != null ? medication.status().toString() : null,
                            medication.administrationRoute(),
                            medication.dosageMin(),
                            medication.dosageMax(),
                            medication.dosageUnit(),
                            medication.frequency(),
                            medication.frequencyUnit(),
                            medication.ifNeeded(),
                            medication.startDate(),
                            medication.stopDate())
                    .execute();
        }
    }
}
