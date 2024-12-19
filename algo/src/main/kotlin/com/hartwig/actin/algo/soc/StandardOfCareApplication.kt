package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientPrinter
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.serialization.CsvReader
import com.hartwig.actin.icd.serialization.IcdDeserializer
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class StandardOfCareApplication(private val config: StandardOfCareConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading patient record from from {}", config.patientJson)
        val patient = PatientRecordJson.read(config.patientJson)
        PatientPrinter.printRecord(patient)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry: DoidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel: DoidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ICD-11 tree from file {}", config.icdTsv)
        val icdNodes = IcdDeserializer.deserialize(CsvReader.readFromFile(config.icdTsv))
        LOGGER.info(" Loaded {} nodes", icdNodes.size)
        val icdModel = IcdModel.create(icdNodes)

        LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, config.runHistorically)
        val functionInputResolver = FunctionInputResolver(
            doidModel, icdModel, MolecularInputChecker.createAnyGeneValid(), treatmentDatabase, MedicationCategories.create(atcTree)
        )
        val configuration = EnvironmentConfiguration.create(config.overridesYaml).algo
        LOGGER.info(" Loaded algo config: $configuration")

        val resources = RuleMappingResources(
            referenceDateProvider,
            doidModel,
            icdModel,
            functionInputResolver,
            atcTree,
            treatmentDatabase,
            config.personalizationDataPath,
            configuration
        )
        val recommendationEngine = RecommendationEngineFactory(resources).create()

        LOGGER.info(recommendationEngine.provideRecommendations(patient))
        val requiredTreatments = recommendationEngine.determineRequiredTreatments(patient)
        requiredTreatments.forEach {
            val allMessages = it.evaluations.flatMap { evaluation ->
                with(evaluation) {
                    passGeneralMessages + warnGeneralMessages + undeterminedGeneralMessages
                }
            }
            LOGGER.debug("${it.treatmentCandidate.treatment.display()}: ${allMessages.joinToString(", ")}")
        }
        LOGGER.info("Standard of care has${if (requiredTreatments.isEmpty()) "" else " not"} been exhausted")
    }

    companion object {
        const val APPLICATION = "ACTIN Standard of Care"

        val LOGGER: Logger = LogManager.getLogger(StandardOfCareApplication::class.java)
        private val VERSION = StandardOfCareApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = StandardOfCareConfig.createOptions()

    try {
        val config = StandardOfCareConfig.createConfig(DefaultParser().parse(options, args))
        StandardOfCareApplication(config).run()
    } catch (exception: ParseException) {
        StandardOfCareApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(StandardOfCareApplication.APPLICATION, options)
        exitProcess(1)
    }
}
