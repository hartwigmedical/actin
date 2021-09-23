package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.PRIORTUMORTREATMENT;
import static com.hartwig.actin.database.tables.Patient.PATIENT;

import java.util.List;

import com.hartwig.actin.clinical.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

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
        writePriorTumorTreatments(sampleId, record.priorTumorTreatments());
    }

    private void writePatientDetails(@NotNull String sampleId, @NotNull PatientDetails patient) {
        context.insertInto(PATIENT, PATIENT.SAMPLEID, PATIENT.BIRTHYEAR, PATIENT.SEX, PATIENT.REGISTRATIONDATE, PATIENT.QUESTIONNAIREDATE)
                .values(sampleId, patient.birthYear(), patient.sex().toString(), patient.registrationDate(), patient.questionnaireDate())
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
                            DatabaseUtil.toByte(priorTumorTreatment.isSystemic()),
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
