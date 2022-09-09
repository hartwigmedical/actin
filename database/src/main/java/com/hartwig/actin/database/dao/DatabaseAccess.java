package com.hartwig.actin.database.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

public class DatabaseAccess {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseAccess.class);

    private static final String DEV_CATALOG = "actin_test";

    @NotNull
    private final ClinicalDAO clinicalDAO;
    @NotNull
    private final MolecularDAO molecularDAO;
    @NotNull
    private final TreatmentDAO treatmentDAO;
    @NotNull
    private final TreatmentMatchDAO treatmentMatchDAO;

    @NotNull
    public static DatabaseAccess fromCredentials(@NotNull String user, @NotNull String pass, @NotNull String url) throws SQLException {
        // Disable annoying jooq self-ad messages
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        String jdbcUrl = "jdbc:" + url;

        Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
        String catalog = conn.getCatalog();
        LOGGER.info("Connecting to database '{}'", catalog);

        DSLContext context = DSL.using(conn, SQLDialect.MYSQL, settings(catalog));
        return new DatabaseAccess(new ClinicalDAO(context),
                new MolecularDAO(context),
                new TreatmentDAO(context),
                new TreatmentMatchDAO(context));
    }

    @Nullable
    private static Settings settings(@NotNull String catalog) {
        return !catalog.equals(DEV_CATALOG)
                ? new Settings().withRenderMapping(new RenderMapping().withSchemata(new MappedSchema().withInput(DEV_CATALOG)
                .withOutput(catalog)))
                : null;
    }

    private DatabaseAccess(@NotNull final ClinicalDAO clinicalDAO, @NotNull final MolecularDAO molecularDAO,
            @NotNull final TreatmentDAO treatmentDAO, @NotNull final TreatmentMatchDAO treatmentMatchDAO) {
        this.clinicalDAO = clinicalDAO;
        this.molecularDAO = molecularDAO;
        this.treatmentDAO = treatmentDAO;
        this.treatmentMatchDAO = treatmentMatchDAO;
    }

    public void writeClinicalRecords(@NotNull List<ClinicalRecord> records) {
        LOGGER.info(" Clearing all clinical data");
        clinicalDAO.clear();
        for (ClinicalRecord record : records) {
            LOGGER.info(" Writing clinical data for {}", record.patientId());
            clinicalDAO.writeClinicalRecord(record);
        }
    }

    public void writeMolecularRecord(@NotNull MolecularRecord record) {
        LOGGER.info(" Clearing molecular data for {}", record.sampleId());
        molecularDAO.clear(record);

        LOGGER.info(" Writing molecular data for {}", record.sampleId());
        molecularDAO.writeMolecularRecord(record);
    }

    public void writeTrials(@NotNull List<Trial> trials) {
        LOGGER.info(" Clearing all trial data");
        treatmentDAO.clear();
        for (Trial trial : trials) {
            LOGGER.info(" Writing trial data for {}", trial.identification().acronym());
            treatmentDAO.writeTrial(trial);
        }
    }

    public void writeTreatmentMatch(@NotNull TreatmentMatch treatmentMatch) {
        LOGGER.info(" Clearing treatment match data for {}", treatmentMatch.sampleId());
        treatmentMatchDAO.clear(treatmentMatch);

        LOGGER.info(" Writing treatment match data for {}", treatmentMatch.sampleId());
        treatmentMatchDAO.writeTreatmentMatch(treatmentMatch);
    }
}
