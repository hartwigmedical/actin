package com.hartwig.actin.system.example

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.TreatmentMatcher
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.serialization.CsvReader
import com.hartwig.actin.icd.serialization.IcdDeserializer
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.serialization.TrialJson
import java.io.File
import java.time.Period
import kotlin.system.exitProcess
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LocalExampleTreatmentMatchApplication {

    fun run(examplePatientRecordJson: String, exampleTrialDatabaseDir: String, outputDirectory: String) {
        LOGGER.info("Loading patient record from {}", examplePatientRecordJson)
        val patient = PatientRecordJson.read(examplePatientRecordJson)

        LOGGER.info("Loading trial data from {}", exampleTrialDatabaseDir)
        val trials = TrialJson.readFromDir(exampleTrialDatabaseDir)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, runHistorically = false)
        val resources = createExampleRuleMappingResources(referenceDateProvider)

        LOGGER.info("Matching patient ${patient.patientId} to available trials")

        val match = TreatmentMatcher
            .create(
                resources = resources,
                trials = trials,
                efficacyEvidence = emptyList(),
                resistanceEvidenceMatcher = createEmptyResistanceEvidenceMatcher(),
                maxMolecularTestAge = null
            )
            .evaluateAndAnnotateMatchesForPatient(patient)

        TreatmentMatchPrinter.printMatch(match)
        TreatmentMatchJson.write(match, outputDirectory)

        LOGGER.info("Done!")
    }

    private fun createExampleRuleMappingResources(referenceDateProvider: ReferenceDateProvider): RuleMappingResources {
        val resourceDirectory =
            listOf(System.getProperty("user.home"), "hmf", "repos", "actin-resources-private").joinToString(File.separator)

        val doidJson = listOf(resourceDirectory, "disease_ontology", "doid.json").joinToString(File.separator)
        val icdTsv = listOf(
            resourceDirectory, "icd", "SimpleTabulation-ICD-11-MMS-en.tsv"
        ).joinToString(File.separator)
        val atcTreeTsv = listOf(resourceDirectory, "atc_config", "atc_tree.tsv").joinToString(File.separator)
        val treatmentDatabaseDir = listOf(resourceDirectory, "treatment_db").joinToString(File.separator)

        LOGGER.info("Loading DOID tree from {}", doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ICD-11 tree from file {}", icdTsv)
        val icdNodes = IcdDeserializer.deserialize(CsvReader.readFromFile(icdTsv))
        LOGGER.info(" Loaded {} nodes", icdNodes.size)
        val icdModel = IcdModel.create(icdNodes)

        LOGGER.info("Creating ATC tree from file {}", atcTreeTsv)
        val atcTree = AtcTree.createFromFile(atcTreeTsv)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(treatmentDatabaseDir)

        val functionInputResolver =
            FunctionInputResolver(
                doidModel = doidModel,
                icdModel = icdModel,
                molecularInputChecker = MolecularInputChecker.createAnyGeneValid(),
                treatmentDatabase = treatmentDatabase,
                medicationCategories = MedicationCategories.create(atcTree)
            )

        val environmentConfiguration = ExampleFunctions.createExampleEnvironmentConfiguration()

        return RuleMappingResources(
            referenceDateProvider = referenceDateProvider,
            doidModel = doidModel,
            icdModel = icdModel,
            functionInputResolver = functionInputResolver,
            atcTree = atcTree,
            treatmentDatabase = treatmentDatabase,
            personalizationDataPath = null,
            algoConfiguration = environmentConfiguration.algo,
            maxMolecularTestAge = environmentConfiguration.algo.maxMolecularTestAgeInDays?.let {
                referenceDateProvider.date().minus(Period.ofDays(it))
            }
        )
    }

    private fun createEmptyResistanceEvidenceMatcher(): ResistanceEvidenceMatcher {
        return ResistanceEvidenceMatcher.create(
            doidModel = DoidModel(
                childToParentsMap = emptyMap(),
                termForDoidMap = emptyMap(),
                doidForLowerCaseTermMap = emptyMap(),
                doidManualConfig = DoidManualConfig.create()
            ),
            tumorDoids = emptySet(),
            evidences = emptyList(),
            treatmentDatabase = TreatmentDatabase(
                drugsByName = emptyMap(),
                treatmentsByName = emptyMap()
            ),
            molecularHistory = MolecularHistory(molecularTests = emptyList())
        )
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(LocalExampleTreatmentMatchApplication::class.java)
    }
}

private const val EXAMPLE_TO_RUN = LUNG_01_EXAMPLE

fun main() {
    LocalExampleTreatmentMatchApplication.LOGGER.info("Running ACTIN Example Treatment Matcher")
    try {
        val examplePatientRecordJson = ExampleFunctions.resolveExamplePatientRecordJson(EXAMPLE_TO_RUN)
        val exampleTrialDatabaseDir = ExampleFunctions.resolveExampleTrialDatabaseDirectory()
        val outputDirectory = System.getProperty("user.dir") + "/system/src/test/resources/example_treatment_match/"

        LocalExampleTreatmentMatchApplication().run(examplePatientRecordJson, exampleTrialDatabaseDir, outputDirectory)
    } catch (exception: ParseException) {
        LocalExampleTreatmentMatchApplication.LOGGER.warn(exception)
        exitProcess(1)
    }
}
