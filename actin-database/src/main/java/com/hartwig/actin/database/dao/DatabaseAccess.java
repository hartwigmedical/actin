package com.hartwig.actin.database.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.hartwig.actin.clinical.ClinicalRecord;

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

    public static DatabaseAccess fromCredentials(@NotNull String user, @NotNull String pass, @NotNull String url) throws SQLException {
        // Disable annoying jooq self-ad messages
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        String jdbcUrl = "jdbc:" + url;

        Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
        String catalog = conn.getCatalog();
        LOGGER.info("Connecting to database {}", catalog);

        DSLContext context = DSL.using(conn, SQLDialect.MYSQL, settings(catalog));
        return new DatabaseAccess(new ClinicalDAO(context));
    }

    @Nullable
    private static Settings settings(@NotNull String catalog) {
        return !catalog.equals(DEV_CATALOG)
                ? new Settings().withRenderMapping(new RenderMapping().withSchemata(new MappedSchema().withInput(DEV_CATALOG)
                .withOutput(catalog)))
                : null;
    }

    private DatabaseAccess(@NotNull final ClinicalDAO clinicalDAO) {
        this.clinicalDAO = clinicalDAO;
    }

    public void writeClinicalRecords(@NotNull List<ClinicalRecord> records) {
        clinicalDAO.clear();
        clinicalDAO.writeClinicalRecords(records);
    }
}
