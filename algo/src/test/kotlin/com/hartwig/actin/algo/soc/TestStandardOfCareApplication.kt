package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientPrinter
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import com.hartwig.actin.icd.serialization.IcdDeserializer
import com.hartwig.actin.medication.AtcTree
import org.apache.logging.log4j.LogManager
import java.io.File
import java.time.LocalDate
import kotlin.system.exitProcess

class TestStandardOfCareApplication {

    fun run(): Int {
        LOGGER.info("Running ACTIN Test SOC Application")

        LOGGER.info("Loading DOID tree from {}", DOID_JSON_PATH)
        val doidEntry: DoidEntry = DoidJson.readDoidOwlEntry(DOID_JSON_PATH)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel: DoidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ICD-11 tree from file {}", ICD_TSV_PATH)
        val icdNodes = IcdNode.create(IcdDeserializer.readFromFile(ICD_TSV_PATH))
        LOGGER.info(" Loaded {} nodes", icdNodes.size)
        val icdModel = IcdModel.create(icdNodes)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(TREATMENT_JSON_PATH)

        val recommendationEngine = RecommendationEngineFactory(
            RuleMappingResourcesTestFactory.create(doidModel, icdModel, AtcTree.createFromFile(ATC_TREE_PATH), treatmentDatabase)
        ).create()

        val patient = patient(treatmentDatabase)
        LOGGER.info("Generating recommendations for patient record")
        PatientPrinter.printRecord(patient)

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
            "actin-resources-private",
            "disease_ontology",
            "doid.json"
        ).joinToString(File.separator)

        private val ACTIN_RESOURCE_PATH = listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "actin-resources-private"
        ).joinToString(File.separator)

        private val TREATMENT_JSON_PATH = ACTIN_RESOURCE_PATH + File.separator + "treatment_db"

        private val ICD_TSV_PATH = listOf(
            ACTIN_RESOURCE_PATH, "international_disease_classification", "SimpleTabulation-ICD-11-MMS-en.tsv"
        ).joinToString(File.separator)

        private val ATC_TREE_PATH = listOf(ACTIN_RESOURCE_PATH, "atc_config", "atc_tree.tsv").joinToString(File.separator)

        private fun patient(treatmentDatabase: TreatmentDatabase): PatientRecord {
            val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
            val recentDate = LocalDate.now().minusDays(7)
            return base.copy(
                tumor = base.tumor.copy(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID), primaryTumorSubLocation = "sigmoid"),
                oncologicalHistory = listOf(CAPOX, CAPECITABINE, BEVACIZUMAB, TRIFLURIDINE_TIPIRACIL, FOLFIRI, PANITUMUMAB)
                    .map {
                        val treatment = treatmentDatabase.findTreatmentByName(it) ?: throw IllegalStateException("Treatment not found: $it")
                        TreatmentTestFactory.treatmentHistoryEntry(
                            setOf(TreatmentTestFactory.drugTreatment(treatment.name, treatment.categories().first())),
                            stopYear = recentDate.year,
                            stopMonth = recentDate.monthValue
                        )
                    }
            )
        }
    }
}

fun main(): Unit = exitProcess(TestStandardOfCareApplication().run())
