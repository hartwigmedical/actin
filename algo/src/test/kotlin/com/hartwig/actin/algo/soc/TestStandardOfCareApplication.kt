package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.util.ClinicalPrinter
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.util.MolecularPrinter
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.system.exitProcess

class TestStandardOfCareApplication {

    fun run(): Int {
        val patient = patient()

        LOGGER.info("Running ACTIN Test SOC Application with clinical record")
        ClinicalPrinter.printRecord(patient.clinical())

        LOGGER.info("and molecular record")
        MolecularPrinter.printRecord(patient.molecular())

        LOGGER.info("Loading DOID tree from {}", DOID_JSON_PATH)
        val doidEntry: DoidEntry = DoidJson.readDoidOwlEntry(DOID_JSON_PATH)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)
        val doidModel: DoidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        val database = RecommendationDatabase(TreatmentDatabaseFactory.createFromPath(TREATMENT_JSON_PATH))
        StandardOfCareApplication.LOGGER.info("Loaded recommendation database for colorectal cancer with treatment candidates:")
        database.logRulesForDoidSet(setOf(DoidConstants.COLORECTAL_CANCER_DOID))
        val recommendationEngine =
            RecommendationEngine.create(doidModel, database, ReferenceDateProviderTestFactory.createCurrentDateProvider())
        val recommendationInterpreter = recommendationEngine.provideRecommendations(patient)
        LOGGER.info(recommendationInterpreter.summarize())
        LOGGER.info(recommendationInterpreter.csv())

        return 0
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TestStandardOfCareApplication::class.java)

        private val DOID_JSON_PATH = listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "common-resources-public",
            "disease_ontology",
            "doid.json"
        ).joinToString(File.separator)

        private val TREATMENT_JSON_PATH = listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "crunch-resources-private",
            "actin",
            "treatment_db"
        ).joinToString(File.separator)

        private fun patient(): PatientRecord {
            val tumorDetails: TumorDetails = ImmutableTumorDetails.builder().addDoids(DoidConstants.COLORECTAL_CANCER_DOID).build()
            val clinicalRecord: ClinicalRecord = ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createProperTestClinicalRecord())
                .tumor(tumorDetails)
                .build()
            val molecularRecord: MolecularRecord = TestMolecularFactory.createProperTestMolecularRecord()
            return PatientRecordFactory.fromInputs(clinicalRecord, molecularRecord)
        }

    }
}

fun main(): Unit = exitProcess(TestStandardOfCareApplication().run())
