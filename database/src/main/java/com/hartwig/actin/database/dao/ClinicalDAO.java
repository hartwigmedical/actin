package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.ALLERGY;
import static com.hartwig.actin.database.Tables.BLOODTRANSFUSION;
import static com.hartwig.actin.database.Tables.BODYWEIGHT;
import static com.hartwig.actin.database.Tables.CANCERRELATEDCOMPLICATION;
import static com.hartwig.actin.database.Tables.CLINICALSTATUS;
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

import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;
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
        context.truncate(CANCERRELATEDCOMPLICATION).execute();
        context.truncate(LABVALUE).execute();
        context.truncate(TOXICITY).execute();
        context.truncate(ALLERGY).execute();
        context.truncate(SURGERY).execute();
        context.truncate(BODYWEIGHT).execute();
        context.truncate(VITALFUNCTION).execute();
        context.truncate(BLOODTRANSFUSION).execute();
        context.truncate(MEDICATION).execute();
        context.execute("SET FOREIGN_KEY_CHECKS = 1;");
    }

    public void writeClinicalRecord(@NotNull ClinicalRecord record) {
        String sampleId = record.sampleId();

        writePatientDetails(sampleId, record.patient());
        writeTumorDetails(sampleId, record.tumor());
        writeClinicalStatus(sampleId, record.clinicalStatus());
        writePriorTumorTreatments(sampleId, record.priorTumorTreatments());
        writePriorSecondPrimaries(sampleId, record.priorSecondPrimaries());
        writePriorOtherConditions(sampleId, record.priorOtherConditions());
        writePriorMolecularTests(sampleId, record.priorMolecularTests());
        writeCancerRelatedComplications(sampleId, record.cancerRelatedComplications());
        writeLabValues(sampleId, record.labValues());
        writeToxicities(sampleId, record.toxicities());
        writeAllergies(sampleId, record.allergies());
        writeSurgeries(sampleId, record.surgeries());
        writeBodyWeights(sampleId, record.bodyWeights());
        writeVitalFunctions(sampleId, record.vitalFunctions());
        writeBloodTransfusions(sampleId, record.bloodTransfusions());
        writeMedications(sampleId, record.medications());
    }

    private void writePatientDetails(@NotNull String sampleId, @NotNull PatientDetails patient) {
        context.insertInto(PATIENT,
                PATIENT.SAMPLEID,
                PATIENT.BIRTHYEAR,
                PATIENT.GENDER,
                PATIENT.REGISTRATIONDATE,
                PATIENT.QUESTIONNAIREDATE)
                .values(sampleId, patient.birthYear(), patient.gender().display(), patient.registrationDate(), patient.questionnaireDate())
                .execute();
    }

    private void writeTumorDetails(@NotNull String sampleId, @NotNull TumorDetails tumor) {
        TumorStage stage = tumor.stage();
        context.insertInto(TUMOR,
                TUMOR.SAMPLEID,
                TUMOR.PRIMARYTUMORLOCATION,
                TUMOR.PRIMARYTUMORSUBLOCATION,
                TUMOR.PRIMARYTUMORTYPE,
                TUMOR.PRIMARYTUMORSUBTYPE,
                TUMOR.PRIMARYTUMOREXTRADETAILS,
                TUMOR.DOIDS,
                TUMOR.STAGE,
                TUMOR.HASMEASURABLELESIONRECIST,
                TUMOR.HASBRAINLESIONS,
                TUMOR.HASACTIVEBRAINLESIONS,
                TUMOR.HASSYMPTOMATICBRAINLESIONS,
                TUMOR.HASCNSLESIONS,
                TUMOR.HASACTIVECNSLESIONS,
                TUMOR.HASSYMPTOMATICCNSLESIONS,
                TUMOR.HASBONELESIONS,
                TUMOR.HASLIVERLESIONS,
                TUMOR.HASOTHERLESIONS,
                TUMOR.OTHERLESIONS,
                TUMOR.BIOPSYLOCATION)
                .values(sampleId,
                        tumor.primaryTumorLocation(),
                        tumor.primaryTumorSubLocation(),
                        tumor.primaryTumorType(),
                        tumor.primaryTumorSubType(),
                        tumor.primaryTumorExtraDetails(),
                        DataUtil.concat(tumor.doids()),
                        stage != null ? stage.display() : null,
                        DataUtil.toByte(tumor.hasMeasurableLesionRecist()),
                        DataUtil.toByte(tumor.hasBrainLesions()),
                        DataUtil.toByte(tumor.hasActiveBrainLesions()),
                        DataUtil.toByte(tumor.hasSymptomaticBrainLesions()),
                        DataUtil.toByte(tumor.hasCnsLesions()),
                        DataUtil.toByte(tumor.hasActiveCnsLesions()),
                        DataUtil.toByte(tumor.hasSymptomaticCnsLesions()),
                        DataUtil.toByte(tumor.hasBoneLesions()),
                        DataUtil.toByte(tumor.hasLiverLesions()),
                        DataUtil.toByte(tumor.hasOtherLesions()),
                        DataUtil.concat(tumor.otherLesions()),
                        tumor.biopsyLocation())
                .execute();
    }

    private void writeClinicalStatus(@NotNull String sampleId, @NotNull ClinicalStatus clinicalStatus) {
        InfectionStatus infectionStatus = clinicalStatus.infectionStatus();
        ECG ecg = clinicalStatus.ecg();

        context.insertInto(CLINICALSTATUS,
                CLINICALSTATUS.SAMPLEID,
                CLINICALSTATUS.WHO,
                CLINICALSTATUS.HASACTIVEINFECTION,
                CLINICALSTATUS.ACTIVEINFECTIONDESCRIPTION,
                CLINICALSTATUS.HASSIGABERRATIONLATESTECG,
                CLINICALSTATUS.ECGABERRATIONDESCRIPTION,
                CLINICALSTATUS.QTCFVALUE,
                CLINICALSTATUS.QTCFUNIT,
                CLINICALSTATUS.LVEF)
                .values(sampleId,
                        clinicalStatus.who(),
                        DataUtil.toByte(infectionStatus != null ? infectionStatus.hasActiveInfection() : null),
                        infectionStatus != null ? infectionStatus.description() : null,
                        DataUtil.toByte(ecg != null ? ecg.hasSigAberrationLatestECG() : null),
                        ecg != null ? ecg.aberrationDescription() : null,
                        ecg != null ? ecg.qtcfValue() : null,
                        ecg != null ? ecg.qtcfUnit() : null,
                        clinicalStatus.lvef())
                .execute();
    }

    private void writePriorTumorTreatments(@NotNull String sampleId, @NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        for (PriorTumorTreatment priorTumorTreatment : priorTumorTreatments) {
            context.insertInto(PRIORTUMORTREATMENT,
                    PRIORTUMORTREATMENT.SAMPLEID,
                    PRIORTUMORTREATMENT.NAME,
                    PRIORTUMORTREATMENT.YEAR,
                    PRIORTUMORTREATMENT.MONTH,
                    PRIORTUMORTREATMENT.CATEGORIES,
                    PRIORTUMORTREATMENT.ISSYSTEMIC,
                    PRIORTUMORTREATMENT.CHEMOTYPE,
                    PRIORTUMORTREATMENT.IMMUNOTYPE,
                    PRIORTUMORTREATMENT.TARGETEDTYPE,
                    PRIORTUMORTREATMENT.HORMONETYPE,
                    PRIORTUMORTREATMENT.RADIOTYPE,
                    PRIORTUMORTREATMENT.TRANSPLANTTYPE,
                    PRIORTUMORTREATMENT.SUPPORTIVETYPE,
                    PRIORTUMORTREATMENT.TRIALACRONYM)
                    .values(sampleId,
                            priorTumorTreatment.name(),
                            priorTumorTreatment.year(),
                            priorTumorTreatment.month(),
                            TreatmentCategoryResolver.toStringList(priorTumorTreatment.categories()),
                            DataUtil.toByte(priorTumorTreatment.isSystemic()),
                            priorTumorTreatment.chemoType(),
                            priorTumorTreatment.immunoType(),
                            priorTumorTreatment.targetedType(),
                            priorTumorTreatment.hormoneType(),
                            priorTumorTreatment.radioType(),
                            priorTumorTreatment.transplantType(),
                            priorTumorTreatment.supportiveType(),
                            priorTumorTreatment.trialAcronym())
                    .execute();
        }
    }

    private void writePriorSecondPrimaries(@NotNull String sampleId, @NotNull List<PriorSecondPrimary> priorSecondPrimaries) {
        for (PriorSecondPrimary priorSecondPrimary : priorSecondPrimaries) {
            context.insertInto(PRIORSECONDPRIMARY,
                    PRIORSECONDPRIMARY.SAMPLEID,
                    PRIORSECONDPRIMARY.TUMORLOCATION,
                    PRIORSECONDPRIMARY.TUMORSUBLOCATION,
                    PRIORSECONDPRIMARY.TUMORTYPE,
                    PRIORSECONDPRIMARY.TUMORSUBTYPE,
                    PRIORSECONDPRIMARY.DOIDS,
                    PRIORSECONDPRIMARY.DIAGNOSEDYEAR,
                    PRIORSECONDPRIMARY.DIAGNOSEDMONTH,
                    PRIORSECONDPRIMARY.TREATMENTHISTORY,
                    PRIORSECONDPRIMARY.ISACTIVE)
                    .values(sampleId,
                            priorSecondPrimary.tumorLocation(),
                            priorSecondPrimary.tumorSubLocation(),
                            priorSecondPrimary.tumorType(),
                            priorSecondPrimary.tumorSubType(),
                            DataUtil.concat(priorSecondPrimary.doids()),
                            priorSecondPrimary.diagnosedYear(),
                            priorSecondPrimary.diagnosedMonth(),
                            priorSecondPrimary.treatmentHistory(),
                            DataUtil.toByte(priorSecondPrimary.isActive()))
                    .execute();
        }
    }

    private void writePriorOtherConditions(@NotNull String sampleId, @NotNull List<PriorOtherCondition> priorOtherConditions) {
        for (PriorOtherCondition priorOtherCondition : priorOtherConditions) {
            context.insertInto(PRIOROTHERCONDITION,
                    PRIOROTHERCONDITION.SAMPLEID,
                    PRIOROTHERCONDITION.NAME,
                    PRIOROTHERCONDITION.YEAR,
                    PRIOROTHERCONDITION.DOIDS,
                    PRIOROTHERCONDITION.CATEGORY)
                    .values(sampleId,
                            priorOtherCondition.name(),
                            priorOtherCondition.year(),
                            DataUtil.concat(priorOtherCondition.doids()),
                            priorOtherCondition.category())
                    .execute();
        }
    }

    private void writePriorMolecularTests(@NotNull String sampleId, @NotNull List<PriorMolecularTest> priorMolecularTests) {
        for (PriorMolecularTest priorMolecularTest : priorMolecularTests) {
            context.insertInto(PRIORMOLECULARTEST,
                    PRIORMOLECULARTEST.SAMPLEID,
                    PRIORMOLECULARTEST.TEST,
                    PRIORMOLECULARTEST.ITEM,
                    PRIORMOLECULARTEST.MEASURE,
                    PRIORMOLECULARTEST.SCORETEXT,
                    PRIORMOLECULARTEST.SCOREVALUE,
                    PRIORMOLECULARTEST.UNIT)
                    .values(sampleId,
                            priorMolecularTest.test(),
                            priorMolecularTest.item(),
                            priorMolecularTest.measure(),
                            priorMolecularTest.scoreText(),
                            priorMolecularTest.scoreValue(),
                            priorMolecularTest.unit())
                    .execute();
        }
    }

    private void writeCancerRelatedComplications(@NotNull String sampleId,
            @NotNull List<CancerRelatedComplication> cancerRelatedComplications) {
        for (CancerRelatedComplication cancerRelatedComplication : cancerRelatedComplications) {
            context.insertInto(CANCERRELATEDCOMPLICATION,
                    CANCERRELATEDCOMPLICATION.SAMPLEID,
                    CANCERRELATEDCOMPLICATION.NAME,
                    CANCERRELATEDCOMPLICATION.YEAR,
                    CANCERRELATEDCOMPLICATION.MONTH)
                    .values(sampleId,
                            cancerRelatedComplication.name(),
                            cancerRelatedComplication.year(),
                            cancerRelatedComplication.month())
                    .execute();
        }
    }

    private void writeLabValues(@NotNull String sampleId, @NotNull List<LabValue> labValues) {
        for (LabValue lab : labValues) {
            context.insertInto(LABVALUE,
                    LABVALUE.SAMPLEID,
                    LABVALUE.DATE,
                    LABVALUE.CODE,
                    LABVALUE.NAME,
                    LABVALUE.COMPARATOR,
                    LABVALUE.VALUE,
                    LABVALUE.UNIT,
                    LABVALUE.REFLIMITLOW,
                    LABVALUE.REFLIMITUP,
                    LABVALUE.ISOUTSIDEREF)
                    .values(sampleId,
                            lab.date(),
                            lab.code(),
                            lab.name(),
                            lab.comparator(),
                            lab.value(),
                            lab.unit().display(),
                            lab.refLimitLow(),
                            lab.refLimitUp(),
                            DataUtil.toByte(lab.isOutsideRef()))
                    .execute();
        }
    }

    private void writeToxicities(@NotNull String sampleId, @NotNull List<Toxicity> toxicities) {
        for (Toxicity toxicity : toxicities) {
            context.insertInto(TOXICITY, TOXICITY.SAMPLEID, TOXICITY.NAME, TOXICITY.EVALUATEDDATE, TOXICITY.SOURCE, TOXICITY.GRADE)
                    .values(sampleId, toxicity.name(), toxicity.evaluatedDate(), toxicity.source().display(), toxicity.grade())
                    .execute();
        }
    }

    private void writeAllergies(@NotNull String sampleId, @NotNull List<Allergy> allergies) {
        for (Allergy allergy : allergies) {
            context.insertInto(ALLERGY,
                    ALLERGY.SAMPLEID,
                    ALLERGY.NAME,
                    ALLERGY.DOIDS,
                    ALLERGY.CATEGORY,
                    ALLERGY.CLINICALSTATUS,
                    ALLERGY.VERIFICATIONSTATUS,
                    ALLERGY.CRITICALITY)
                    .values(sampleId,
                            allergy.name(),
                            DataUtil.concat(allergy.doids()),
                            allergy.category(),
                            allergy.clinicalStatus(),
                            allergy.verificationStatus(),
                            allergy.criticality())
                    .execute();
        }
    }

    private void writeSurgeries(@NotNull String sampleId, @NotNull List<Surgery> surgeries) {
        for (Surgery surgery : surgeries) {
            context.insertInto(SURGERY, SURGERY.SAMPLEID, SURGERY.ENDDATE).values(sampleId, surgery.endDate()).execute();
        }
    }

    private void writeBodyWeights(@NotNull String sampleId, @NotNull List<BodyWeight> bodyWeights) {
        for (BodyWeight bodyWeight : bodyWeights) {
            context.insertInto(BODYWEIGHT, BODYWEIGHT.SAMPLEID, BODYWEIGHT.DATE, BODYWEIGHT.VALUE, BODYWEIGHT.UNIT)
                    .values(sampleId, bodyWeight.date(), bodyWeight.value(), bodyWeight.unit())
                    .execute();
        }
    }

    private void writeVitalFunctions(@NotNull String sampleId, @NotNull List<VitalFunction> vitalFunctions) {
        for (VitalFunction vitalFunction : vitalFunctions) {
            context.insertInto(VITALFUNCTION,
                    VITALFUNCTION.SAMPLEID,
                    VITALFUNCTION.DATE,
                    VITALFUNCTION.CATEGORY,
                    VITALFUNCTION.SUBCATEGORY,
                    VITALFUNCTION.VALUE,
                    VITALFUNCTION.UNIT)
                    .values(sampleId,
                            vitalFunction.date(),
                            vitalFunction.category().display(),
                            vitalFunction.subcategory(),
                            vitalFunction.value(),
                            vitalFunction.unit())
                    .execute();
        }
    }

    private void writeBloodTransfusions(@NotNull String sampleId, @NotNull List<BloodTransfusion> bloodTransfusions) {
        for (BloodTransfusion bloodTransfusion : bloodTransfusions) {
            context.insertInto(BLOODTRANSFUSION, BLOODTRANSFUSION.SAMPLEID, BLOODTRANSFUSION.DATE, BLOODTRANSFUSION.PRODUCT)
                    .values(sampleId, bloodTransfusion.date(), bloodTransfusion.product())
                    .execute();
        }
    }

    private void writeMedications(@NotNull String sampleId, @NotNull List<Medication> medications) {
        for (Medication medication : medications) {
            context.insertInto(MEDICATION,
                    MEDICATION.SAMPLEID,
                    MEDICATION.NAME,
                    MEDICATION.CATEGORIES,
                    MEDICATION.DOSAGEMIN,
                    MEDICATION.DOSAGEMAX,
                    MEDICATION.DOSAGEUNIT,
                    MEDICATION.FREQUENCY,
                    MEDICATION.FREQUENCYUNIT,
                    MEDICATION.IFNEEDED,
                    MEDICATION.STARTDATE,
                    MEDICATION.STOPDATE,
                    MEDICATION.ACTIVE)
                    .values(sampleId,
                            medication.name(),
                            DataUtil.concat(medication.categories()),
                            medication.dosageMin(),
                            medication.dosageMax(),
                            medication.dosageUnit(),
                            medication.frequency(),
                            medication.frequencyUnit(),
                            DataUtil.toByte(medication.ifNeeded()),
                            medication.startDate(),
                            medication.stopDate(),
                            DataUtil.toByte(medication.active()))
                    .execute();
        }
    }
}
