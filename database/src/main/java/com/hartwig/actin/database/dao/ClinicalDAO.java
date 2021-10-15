package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.ALLERGY;
import static com.hartwig.actin.database.Tables.BLOODPRESSURE;
import static com.hartwig.actin.database.Tables.BLOODTRANSFUSION;
import static com.hartwig.actin.database.Tables.CANCERRELATEDCOMPLICATION;
import static com.hartwig.actin.database.Tables.MEDICATION;
import static com.hartwig.actin.database.Tables.OTHERCOMPLICATION;
import static com.hartwig.actin.database.Tables.PRIOROTHERCONDITION;
import static com.hartwig.actin.database.Tables.PRIORSECONDPRIMARY;
import static com.hartwig.actin.database.Tables.PRIORTUMORTREATMENT;
import static com.hartwig.actin.database.Tables.SURGERY;
import static com.hartwig.actin.database.Tables.TOXICITY;
import static com.hartwig.actin.database.Tables.TUMOR;
import static com.hartwig.actin.database.tables.Clinicalstatus.CLINICALSTATUS;
import static com.hartwig.actin.database.tables.Labvalue.LABVALUE;
import static com.hartwig.actin.database.tables.Patient.PATIENT;

import java.util.List;

import com.hartwig.actin.datamodel.clinical.Allergy;
import com.hartwig.actin.datamodel.clinical.BloodPressure;
import com.hartwig.actin.datamodel.clinical.BloodTransfusion;
import com.hartwig.actin.datamodel.clinical.CancerRelatedComplication;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.ClinicalStatus;
import com.hartwig.actin.datamodel.clinical.Complication;
import com.hartwig.actin.datamodel.clinical.LabValue;
import com.hartwig.actin.datamodel.clinical.Medication;
import com.hartwig.actin.datamodel.clinical.PatientDetails;
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition;
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary;
import com.hartwig.actin.datamodel.clinical.PriorTumorTreatment;
import com.hartwig.actin.datamodel.clinical.Surgery;
import com.hartwig.actin.datamodel.clinical.Toxicity;
import com.hartwig.actin.datamodel.clinical.TumorDetails;

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
        context.truncate(CANCERRELATEDCOMPLICATION).execute();
        context.truncate(OTHERCOMPLICATION).execute();
        context.truncate(LABVALUE).execute();
        context.truncate(TOXICITY).execute();
        context.truncate(ALLERGY).execute();
        context.truncate(SURGERY).execute();
        context.truncate(BLOODPRESSURE).execute();
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
        writeCancerRelatedComplications(sampleId, record.cancerRelatedComplications());
        writeOtherComplications(sampleId, record.otherComplications());
        writeLabValues(sampleId, record.labValues());
        writeToxicities(sampleId, record.toxicities());
        writeAllergies(sampleId, record.allergies());
        writeSurgeries(sampleId, record.surgeries());
        writeBloodPressures(sampleId, record.bloodPressures());
        writeBloodTransfusions(sampleId, record.bloodTransfusions());
        writeMedications(sampleId, record.medications());
    }

    private void writePatientDetails(@NotNull String sampleId, @NotNull PatientDetails patient) {
        context.insertInto(PATIENT, PATIENT.SAMPLEID, PATIENT.BIRTHYEAR, PATIENT.SEX, PATIENT.REGISTRATIONDATE, PATIENT.QUESTIONNAIREDATE)
                .values(sampleId, patient.birthYear(), patient.sex().display(), patient.registrationDate(), patient.questionnaireDate())
                .execute();
    }

    private void writeTumorDetails(@NotNull String sampleId, @NotNull TumorDetails tumor) {
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
                        tumor.stage() != null ? tumor.stage().display() : null,
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
                        tumor.biopsyLocation()).execute();
    }

    private void writeClinicalStatus(@NotNull String sampleId, @NotNull ClinicalStatus clinicalStatus) {
        context.insertInto(CLINICALSTATUS,
                CLINICALSTATUS.SAMPLEID,
                CLINICALSTATUS.WHO,
                CLINICALSTATUS.HASACTIVEINFECTION,
                CLINICALSTATUS.HASSIGABERRATIONLATESTECG,
                CLINICALSTATUS.ECGABERRATIONDESCRIPTION)
                .values(sampleId,
                        clinicalStatus.who(),
                        DataUtil.toByte(clinicalStatus.hasActiveInfection()),
                        DataUtil.toByte(clinicalStatus.hasSigAberrationLatestEcg()),
                        clinicalStatus.ecgAberrationDescription())
                .execute();
    }

    private void writePriorTumorTreatments(@NotNull String sampleId, @NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        for (PriorTumorTreatment priorTumorTreatment : priorTumorTreatments) {
            context.insertInto(PRIORTUMORTREATMENT,
                    PRIORTUMORTREATMENT.SAMPLEID,
                    PRIORTUMORTREATMENT.NAME,
                    PRIORTUMORTREATMENT.YEAR,
                    PRIORTUMORTREATMENT.CATEGORY,
                    PRIORTUMORTREATMENT.ISSYSTEMIC,
                    PRIORTUMORTREATMENT.CHEMOTYPE,
                    PRIORTUMORTREATMENT.IMMUNOTYPE,
                    PRIORTUMORTREATMENT.TARGETEDTYPE,
                    PRIORTUMORTREATMENT.HORMONETYPE,
                    PRIORTUMORTREATMENT.STEMCELLTRANSTYPE)
                    .values(sampleId,
                            priorTumorTreatment.name(),
                            priorTumorTreatment.year(),
                            priorTumorTreatment.category(),
                            DataUtil.toByte(priorTumorTreatment.isSystemic()),
                            priorTumorTreatment.chemoType(),
                            priorTumorTreatment.immunoType(),
                            priorTumorTreatment.targetedType(),
                            priorTumorTreatment.hormoneType(),
                            priorTumorTreatment.stemCellTransType())
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
                    PRIORSECONDPRIMARY.ISSECONDPRIMARYACTIVE)
                    .values(sampleId,
                            priorSecondPrimary.tumorLocation(),
                            priorSecondPrimary.tumorSubLocation(),
                            priorSecondPrimary.tumorType(),
                            priorSecondPrimary.tumorSubType(),
                            DataUtil.concat(priorSecondPrimary.doids()),
                            priorSecondPrimary.diagnosedYear(),
                            DataUtil.toByte(priorSecondPrimary.isSecondPrimaryActive()))
                    .execute();
        }
    }

    private void writePriorOtherConditions(@NotNull String sampleId, @NotNull List<PriorOtherCondition> priorOtherConditions) {
        for (PriorOtherCondition priorOtherCondition : priorOtherConditions) {
            context.insertInto(PRIOROTHERCONDITION,
                    PRIOROTHERCONDITION.SAMPLEID,
                    PRIOROTHERCONDITION.NAME,
                    PRIOROTHERCONDITION.DOIDS,
                    PRIOROTHERCONDITION.CATEGORY)
                    .values(sampleId,
                            priorOtherCondition.name(),
                            DataUtil.concat(priorOtherCondition.doids()),
                            priorOtherCondition.category())
                    .execute();
        }
    }

    private void writeCancerRelatedComplications(@NotNull String sampleId,
            @NotNull List<CancerRelatedComplication> cancerRelatedComplications) {
        for (CancerRelatedComplication cancerRelatedComplication : cancerRelatedComplications) {
            context.insertInto(CANCERRELATEDCOMPLICATION, CANCERRELATEDCOMPLICATION.SAMPLEID, CANCERRELATEDCOMPLICATION.NAME)
                    .values(sampleId, cancerRelatedComplication.name())
                    .execute();
        }
    }

    private void writeOtherComplications(@NotNull String sampleId, @NotNull List<Complication> complications) {
        for (Complication complication : complications) {
            context.insertInto(OTHERCOMPLICATION,
                    OTHERCOMPLICATION.SAMPLEID,
                    OTHERCOMPLICATION.NAME,
                    OTHERCOMPLICATION.DOIDS,
                    OTHERCOMPLICATION.SPECIALTY,
                    OTHERCOMPLICATION.ONSETDATE,
                    OTHERCOMPLICATION.CATEGORY,
                    OTHERCOMPLICATION.STATUS)
                    .values(sampleId,
                            complication.name(),
                            DataUtil.concat(complication.doids()),
                            complication.specialty(),
                            complication.onsetDate(),
                            complication.category(),
                            complication.status())
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
                            lab.unit(),
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
            context.insertInto(ALLERGY, ALLERGY.SAMPLEID, ALLERGY.NAME, ALLERGY.CATEGORY, ALLERGY.CRITICALITY)
                    .values(sampleId, allergy.name(), allergy.category(), allergy.criticality())
                    .execute();
        }
    }

    private void writeSurgeries(@NotNull String sampleId, @NotNull List<Surgery> surgeries) {
        for (Surgery surgery : surgeries) {
            context.insertInto(SURGERY, SURGERY.SAMPLEID, SURGERY.ENDDATE).values(sampleId, surgery.endDate()).execute();
        }
    }

    private void writeBloodPressures(@NotNull String sampleId, @NotNull List<BloodPressure> bloodPressures) {
        for (BloodPressure bloodPressure : bloodPressures) {
            context.insertInto(BLOODPRESSURE,
                    BLOODPRESSURE.SAMPLEID,
                    BLOODPRESSURE.DATE,
                    BLOODPRESSURE.CATEGORY,
                    BLOODPRESSURE.VALUE,
                    BLOODPRESSURE.UNIT)
                    .values(sampleId, bloodPressure.date(), bloodPressure.category(), bloodPressure.value(), bloodPressure.unit())
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
                    MEDICATION.TYPE,
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
                            medication.type(),
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
