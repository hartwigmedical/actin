package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientPrinter
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.medication.AtcTree
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.system.exitProcess

class TestStandardOfCareApplication {

    fun run(): Int {
        val patient = patient()

        LOGGER.info("Running ACTIN Test SOC Application with clinical record")
        PatientPrinter.printRecord(patient)

        LOGGER.info("Loading DOID tree from {}", DOID_JSON_PATH)
        val doidEntry: DoidEntry = DoidJson.readDoidOwlEntry(DOID_JSON_PATH)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel: DoidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(TREATMENT_JSON_PATH)

        val recommendationEngine = RecommendationEngineFactory(
            RuleMappingResourcesTestFactory.create(doidModel, AtcTree.createFromFile(ATC_TREE_PATH), treatmentDatabase)
        ).create()

        LOGGER.info(recommendationEngine.provideRecommendations(patient))
        val patientHasExhaustedStandardOfCare = recommendationEngine.patientHasExhaustedStandardOfCare(patient)
        LOGGER.info("Standard of care has${if (patientHasExhaustedStandardOfCare) "" else " not"} been exhausted")
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

        private val ACTIN_RESOURCE_PATH = listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "crunch-resources-private",
            "actin"
        ).joinToString(File.separator)

        private val TREATMENT_JSON_PATH = ACTIN_RESOURCE_PATH + File.separator + "treatment_db"

        private val ATC_TREE_PATH = listOf(ACTIN_RESOURCE_PATH, "atc_config", "atc_tree.tsv").joinToString(File.separator)

        private fun patient(): PatientRecord {
            val base = TestPatientFactory.createMinimalTestPatientRecord()
            return base.copy(
                tumor = base.tumor.copy(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID))
            )
        }
    }
}

fun main(): Unit = exitProcess(TestStandardOfCareApplication().run())
