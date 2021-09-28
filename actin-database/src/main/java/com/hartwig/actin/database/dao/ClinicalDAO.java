package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.CANCERRELATEDCOMPLICATION;
import static com.hartwig.actin.database.Tables.PRIOROTHERCONDITION;
import static com.hartwig.actin.database.Tables.PRIORSECONDPRIMARY;
import static com.hartwig.actin.database.Tables.PRIORTUMORTREATMENT;
import static com.hartwig.actin.database.Tables.TUMOR;
import static com.hartwig.actin.database.tables.Clinicalstatus.CLINICALSTATUS;
import static com.hartwig.actin.database.tables.Patient.PATIENT;

import java.util.List;

import com.hartwig.actin.clinical.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

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
        context.truncate(CANCERRELATEDCOMPLICATION).execute();
        context.truncate(PRIORTUMORTREATMENT).execute();
        context.truncate(PRIORSECONDPRIMARY).execute();
        context.truncate(PRIOROTHERCONDITION).execute();
        context.execute("SET FOREIGN_KEY_CHECKS = 1;");
    }

    public void writeClinicalRecords(@NotNull List<ClinicalRecord> records) {
        for (ClinicalRecord record : records) {
            writeClinicalRecord(record);
        }
    }

    private void writeClinicalRecord(@NotNull ClinicalRecord record) {
        String sampleId = record.sampleId();

        writePatientDetails(sampleId, record.patient());
        writeTumorDetails(sampleId, record.tumor());
        writeClinicalStatus(sampleId, record.clinicalStatus());
        writeCancerRelatedComplications(sampleId, record.cancerRelatedComplications());
        writePriorTumorTreatments(sampleId, record.priorTumorTreatments());
        writePriorSecondPrimaries(sampleId, record.priorSecondPrimaries());
        writePriorOtherConditions(sampleId, record.priorOtherConditions());
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
                TUMOR.OTHERLESIONS)
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
                        tumor.otherLesions()).execute();
    }

    private void writeClinicalStatus(@NotNull String sampleId, @NotNull ClinicalStatus clinicalStatus) {
        context.insertInto(CLINICALSTATUS,
                CLINICALSTATUS.SAMPLEID,
                CLINICALSTATUS.WHO,
                CLINICALSTATUS.HASCURRENTINFECTION,
                CLINICALSTATUS.INFECTIONDESCRIPTION,
                CLINICALSTATUS.HASSIGABERRATIONLATESTECG,
                CLINICALSTATUS.ECGABERRATIONDESCRIPTION)
                .values(sampleId,
                        clinicalStatus.who(),
                        DataUtil.toByte(clinicalStatus.hasCurrentInfection()),
                        clinicalStatus.infectionDescription(),
                        DataUtil.toByte(clinicalStatus.hasSigAberrationLatestEcg()),
                        clinicalStatus.ecgAberrationDescription())
                .execute();
    }

    private void writeCancerRelatedComplications(@NotNull String sampleId,
            @NotNull List<CancerRelatedComplication> cancerRelatedComplications) {
        for (CancerRelatedComplication cancerRelatedComplication : cancerRelatedComplications) {
            context.insertInto(CANCERRELATEDCOMPLICATION, CANCERRELATEDCOMPLICATION.SAMPLEID, CANCERRELATEDCOMPLICATION.NAME)
                    .values(sampleId, cancerRelatedComplication.name())
                    .execute();
        }
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
                    PRIORTUMORTREATMENT.STEMCELLTRANSTYPE,
                    PRIORTUMORTREATMENT.RADIOTHERAPYTYPE,
                    PRIORTUMORTREATMENT.SURGERYTYPE)
                    .values(sampleId,
                            priorTumorTreatment.name(),
                            priorTumorTreatment.year(),
                            priorTumorTreatment.category(),
                            DataUtil.toByte(priorTumorTreatment.isSystemic()),
                            priorTumorTreatment.chemoType(),
                            priorTumorTreatment.immunoType(),
                            priorTumorTreatment.targetedType(),
                            priorTumorTreatment.hormoneType(),
                            priorTumorTreatment.stemCellTransType(),
                            priorTumorTreatment.radiotherapyType(),
                            priorTumorTreatment.surgeryType()).execute();
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
                    PRIORSECONDPRIMARY.YEAR,
                    PRIORSECONDPRIMARY.ISSECONDPRIMARYCURED,
                    PRIORSECONDPRIMARY.CUREDDATE)
                    .values(sampleId,
                            priorSecondPrimary.tumorLocation(),
                            priorSecondPrimary.tumorSubLocation(),
                            priorSecondPrimary.tumorType(),
                            priorSecondPrimary.tumorSubType(),
                            DataUtil.concat(priorSecondPrimary.doids()),
                            priorSecondPrimary.year(),
                            DataUtil.toByte(priorSecondPrimary.isSecondPrimaryCured()),
                            priorSecondPrimary.curedDate()).execute();

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

}
