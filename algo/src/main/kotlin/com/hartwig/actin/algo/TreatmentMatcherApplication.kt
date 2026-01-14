package com.hartwig.actin.algo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.datamodel.trial.TrialConfig
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcherFactory
import com.hartwig.actin.trial.Either
import com.hartwig.actin.trial.EligibilityFactory
import com.hartwig.actin.trial.TrialIngestion
import com.hartwig.actin.trial.right
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class TreatmentMatcherApplication(private val config: TreatmentMatcherConfig) {

    fun run() = runBlocking {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading data")
        val inputData = InputDataLoader.load(config)
        LOGGER.info("Loading complete")

        val referenceDateProvider = ReferenceDateProviderFactory.create(inputData.patient, config.runHistorically)
        LOGGER.info("Matching patient to available trials")


        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val configuration = AlgoConfiguration.create(config.overridesYaml)
        LOGGER.info("Loaded algo config: $configuration")

        val resources = RuleMappingResources(
            referenceDateProvider = referenceDateProvider,
            doidModel = inputData.doidModel,
            icdModel = inputData.icdModel,
            atcTree = inputData.atcTree,
            treatmentDatabase = treatmentDatabase,
            treatmentEfficacyPredictionJson = config.treatmentEfficacyPredictionJson,
            algoConfiguration = configuration
        )
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(config.extendedEfficacyJson)

        val resistanceEvidenceMatcher =
            ResistanceEvidenceMatcher.create(
                doidModel = inputData.doidModel,
                tumorDoids = inputData.patient.tumor.doids.orEmpty().toSet(),
                evidences = inputData.serveRecord.evidences(),
                treatmentDatabase = treatmentDatabase,
                molecularTests = inputData.patient.molecularTests,
                actionabilityMatcher = ActionabilityMatcherFactory.create(inputData.serveRecord)
            )

        val trialsOrErrors = inputData.trials?.right() ?: TrialIngestion(EligibilityFactory(treatmentDatabase)).ingest(
            Gson().fromJson(
                Files.readString(config.trialConfigJson?.let { Path.of(it) }
                    ?: error("One of trial config or trial database must be specified.")),
                object : TypeToken<List<TrialConfig>>() {}.type
            )
        )

        val trials = when (trialsOrErrors) {
            is Either.Right -> trialsOrErrors.value
            is Either.Left -> throw IllegalArgumentException(
                "Failed to ingest trials. Unmappable trials found: ${trialsOrErrors.value.joinToString { it.trialId }}"
            )
        }

        val treatmentMatcher =
            TreatmentMatcher.create(resources, trials, evidenceEntries, resistanceEvidenceMatcher)
        val treatmentMatch = treatmentMatcher.run(inputData.patient)

        LOGGER.info("Printing treatment match")
        TreatmentMatchPrinter.printMatch(treatmentMatch)
        TreatmentMatchJson.write(treatmentMatch, config.outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        const val APPLICATION = "ACTIN Treatment Matcher"

        val LOGGER: Logger = LogManager.getLogger(TreatmentMatcherApplication::class.java)
        private val VERSION = TreatmentMatcherApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TreatmentMatcherConfig.createOptions()
    val config: TreatmentMatcherConfig
    try {
        config = TreatmentMatcherConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TreatmentMatcherApplication.LOGGER.error(exception)
        HelpFormatter().printHelp(TreatmentMatcherApplication.APPLICATION, options)
        exitProcess(1)
    }

    TreatmentMatcherApplication(config).run()
}
