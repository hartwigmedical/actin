package com.hartwig.actin.database.dao

import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.trial.Trial
import org.apache.logging.log4j.LogManager
import org.jooq.SQLDialect
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.sql.DriverManager

class DatabaseAccess private constructor(
    private val clinicalDAO: ClinicalDAO, private val molecularDAO: MolecularDAO,
    private val trialDAO: TrialDAO, private val treatmentMatchDAO: TreatmentMatchDAO
) {

    fun writeClinicalRecords(records: List<ClinicalRecord>) {
        LOGGER.info(" Clearing all clinical data")
        clinicalDAO.clear()
        for (record in records) {
            LOGGER.info(" Writing clinical data for {}", record.patientId)
            clinicalDAO.writeClinicalRecord(record)
        }
    }

    fun writeMolecularRecord(patientId: String, record: MolecularRecord) {
        LOGGER.info(" Clearing molecular data for {}", record.sampleId)
        molecularDAO.clear(record)
        LOGGER.info(" Writing molecular data for {}", record.sampleId)
        molecularDAO.writeMolecularRecord(patientId, record)
    }

    fun writeTrials(trials: List<Trial>) {
        LOGGER.info(" Clearing all trial data")
        trialDAO.clear()
        for (trial in trials) {
            LOGGER.info(" Writing trial data for {}", trial.identification.acronym)
            trialDAO.writeTrial(trial)
        }
    }

    fun writeTreatmentMatch(treatmentMatch: TreatmentMatch) {
        LOGGER.info(" Clearing treatment match data for {}", treatmentMatch.patientId)
        treatmentMatchDAO.clear(treatmentMatch)
        LOGGER.info(" Writing treatment match data for {}", treatmentMatch.patientId)
        treatmentMatchDAO.writeTreatmentMatch(treatmentMatch)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(DatabaseAccess::class.java)
        private const val DEV_CATALOG = "actin_test"

        fun fromCredentials(user: String, pass: String, url: String): DatabaseAccess {
            // Disable annoying jooq self-ad messages
            System.setProperty("org.jooq.no-logo", "true")
            System.setProperty("org.jooq.no-tips", "true")
            val jdbcUrl = "jdbc:$url"
            val conn = DriverManager.getConnection(jdbcUrl, user, pass)
            val catalog = conn.catalog
            LOGGER.info("Connecting to database '{}'", catalog)
            val context = DSL.using(conn, SQLDialect.MYSQL, settings(catalog))
            return DatabaseAccess(
                ClinicalDAO(context),
                MolecularDAO(context),
                TrialDAO(context),
                TreatmentMatchDAO(context)
            )
        }

        private fun settings(catalog: String): Settings? {
            return if (catalog != DEV_CATALOG) {
                Settings().withRenderMapping(RenderMapping().withSchemata(MappedSchema().withInput(DEV_CATALOG).withOutput(catalog)))
            } else null
        }
    }
}