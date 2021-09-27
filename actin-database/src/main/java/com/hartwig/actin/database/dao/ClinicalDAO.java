package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.PRIORTUMORTREATMENT;
import static com.hartwig.actin.database.Tables.TUMOR;
import static com.hartwig.actin.database.tables.Patient.PATIENT;

import java.util.List;

import com.hartwig.actin.clinical.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
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
        context.truncate(PRIORTUMORTREATMENT).execute();
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
        writePriorTumorTreatments(sampleId, record.priorTumorTreatments());
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
                        tumor.stage().display(),
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
                        tumor.otherLesions())
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
                            priorTumorTreatment.surgeryType())
                    .execute();
        }
    }
}
