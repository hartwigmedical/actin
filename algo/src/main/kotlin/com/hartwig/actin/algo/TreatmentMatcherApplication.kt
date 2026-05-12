package com.hartwig.actin.algo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.datamodel.trial.TrialConfig
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcherFactory
import com.hartwig.actin.treatment.database.TreatmentDatabaseFactory
import com.hartwig.actin.trial.EligibilityFactory
import com.hartwig.actin.trial.TrialIngestion
import com.hartwig.actin.utils.monad.getOrNull
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

class TreatmentMatcherApplication(private val config: TreatmentMatcherConfig) {

    fun run() = runBlocking {
        logger.info { "Running $APPLICATION v$VERSION" }

        logger.info { "Loading data" }
        val inputData = InputDataLoader.load(config)
        logger.info { "Loading complete" }

        val referenceDateProvider = ReferenceDateProviderFactory.create(inputData.patient, config.runHistorically)
        logger.info { "Matching patient to available trials" }


        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val configuration = AlgoConfiguration.create(config.overridesYaml)
        logger.info { "Loaded algo config: $configuration" }

        val resources = RuleMappingResources(
            referenceDateProvider = referenceDateProvider,
            doidModel = inputData.doidModel,
            cuppaToDoidMapping = inputData.cuppaToDoidMapping,
            icdModel = inputData.icdModel,
            atcTree = inputData.atcTree,
            treatmentDatabase = treatmentDatabase,
            treatmentEfficacyPredictionJson = config.treatmentEfficacyPredictionJson,
            algoConfiguration = configuration
        )
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(config.extendedEfficacyJson)

        val resistanceEvidenceMatcher = ResistanceEvidenceMatcher.create(
            doidModel = inputData.doidModel,
            tumorDoids = inputData.patient.tumor.doids.orEmpty().toSet(),
            evidences = inputData.serveRecord.evidences(),
            treatmentDatabase = treatmentDatabase,
            molecularTests = inputData.patient.molecularTests,
            actionabilityMatcher = ActionabilityMatcherFactory.create(inputData.serveRecord)
        )

        val trials = inputData.trials ?: TrialIngestion(EligibilityFactory(treatmentDatabase)).ingest(
            Gson().fromJson(
                Files.readString(config.trialConfigJson?.let { Path.of(it) }
                    ?: error("One of trial config or trial database must be specified.")), object : TypeToken<List<TrialConfig>>() {}.type
            )
        ).mapLeft { unmappableTrials ->
            throw IllegalArgumentException(
                "Failed to ingest trials. Unmappable trials found: \n" + "${
                    unmappableTrials.map {
                        "Trial: ${it.trialId} Errors: ${it.mappingErrors.map { e -> "${e.inclusionRule}: ${e.error}\n" }} " +
                                "Cohorts: ${it.unmappableCohorts.map { c -> "Cohort: ${c.cohortId} Errors: ${c.mappingErrors.map { e -> "${e.inclusionRule} ${e.error}\n" }}" }}"
                    }
                }\n}")
        }.getOrNull()!!

        val treatmentMatcher = TreatmentMatcher.create(resources, trials, evidenceEntries, resistanceEvidenceMatcher)
        val treatmentMatch = treatmentMatcher.run(inputData.patient)

        logger.info { "Printing treatment match" }
        TreatmentMatchPrinter.printMatch(treatmentMatch)
        TreatmentMatchJson.write(treatmentMatch, config.outputDirectory)
        logger.info { "Done!" }
    }

    companion object {
        const val APPLICATION = "ACTIN Treatment Matcher"

        val logger = KotlinLogging.logger {}
        private val VERSION = TreatmentMatcherApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TreatmentMatcherConfig.createOptions()
    val config: TreatmentMatcherConfig
    try {
        config = TreatmentMatcherConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TreatmentMatcherApplication.logger.error(exception) { exception.message ?: "" }
        HelpFormatter().printHelp(TreatmentMatcherApplication.APPLICATION, options)
        exitProcess(1)
    }

    TreatmentMatcherApplication(config).run()
}
