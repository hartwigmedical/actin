package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.PRIORTUMORTREATMENT;

import java.util.List;

import com.hartwig.actin.clinical.ClinicalRecord;
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
        context.truncate(PRIORTUMORTREATMENT).execute();
        context.execute("SET FOREIGN_KEY_CHECKS = 1;");
    }

    public void writeClinicalRecords(@NotNull List<ClinicalRecord> records) {
        for (ClinicalRecord record : records) {
            writeClinicalRecord(record);
        }
    }

    private void writeClinicalRecord(@NotNull ClinicalRecord record) {
        for (PriorTumorTreatment priorTumorTreatment : record.priorTumorTreatments()) {
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
                    .values(record.sampleId(),
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
