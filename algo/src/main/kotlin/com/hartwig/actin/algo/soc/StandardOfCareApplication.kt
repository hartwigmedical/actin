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
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import java.io.IOException
import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

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

        LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        LOGGER.info("Loading treatment data from {}", config.treatmentDirectory)
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, config.runHistorically)
        val functionInputResolver = FunctionInputResolver(
            doidModel, MolecularInputChecker.createAnyGeneValid(), treatmentDatabase, MedicationCategories.create(atcTree)
        )
        val environmentConfiguration =
            config.overridesYaml?.let { EnvironmentConfiguration.create(config.overridesYaml) } ?: EnvironmentConfiguration()
        val resources = RuleMappingResources(
            referenceDateProvider,
            doidModel,
            functionInputResolver,
            atcTree,
            treatmentDatabase,
            environmentConfiguration.algo
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
        val LOGGER: Logger = LogManager.getLogger(StandardOfCareApplication::class.java)
        const val APPLICATION = "ACTIN Standard of Care"
        private val VERSION = StandardOfCareApplication::class.java.getPackage().implementationVersion
    }
}

@Throws(IOException::class)
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
