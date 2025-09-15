package com.hartwig.actin.algo

import InputDataLoader
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Period
import kotlin.system.exitProcess

class TreatmentMatcherApplication(private val config: TreatmentMatcherConfig) {

    fun run() = runBlocking {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading data")
        val inputData = InputDataLoader.load(config)
        LOGGER.info("Loading complete")

        val referenceDateProvider = ReferenceDateProviderFactory.create(inputData.patient, config.runHistorically)
        LOGGER.info("Matching patient to available trials")

        // We assume we never check validity of a gene inside algo.
        val molecularInputChecker = MolecularInputChecker.createAnyGeneValid()
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val functionInputResolver =
            FunctionInputResolver(inputData.doidModel, inputData.icdModel, molecularInputChecker,
                treatmentDatabase, MedicationCategories.create(inputData.atcTree))
        val configuration = AlgoConfiguration.create(config.overridesYaml)
        LOGGER.info(" Loaded algo config: $configuration")

        val maxMolecularTestAge = configuration.maxMolecularTestAgeInDays?.let { referenceDateProvider.date().minus(Period.ofDays(it)) }
        val resources = RuleMappingResources(
            referenceDateProvider = referenceDateProvider,
            doidModel = inputData.doidModel,
            icdModel = inputData.icdModel,
            functionInputResolver = functionInputResolver,
            atcTree = inputData.atcTree,
            treatmentDatabase = treatmentDatabase,
            personalizationDataPath = config.personalizationDataPath,
            treatmentEfficacyPredictionJson = config.treatmentEfficacyPredictionJson,
            algoConfiguration = configuration,
            maxMolecularTestAge = maxMolecularTestAge
        )
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(config.extendedEfficacyJson)

        val resistanceEvidenceMatcher =
            ResistanceEvidenceMatcher.create(
                doidModel = inputData.doidModel,
                tumorDoids = inputData.patient.tumor.doids.orEmpty().toSet(),
                evidences = inputData.serveRecord.evidences(),
                treatmentDatabase = treatmentDatabase,
                molecularTests = inputData.patient.molecularTests,
                actionabilityMatcher = ActionabilityMatcher(inputData.serveRecord.evidences(), inputData.serveRecord.trials())
            )

        val treatmentMatcher = TreatmentMatcher.create(resources, inputData.trials, evidenceEntries, resistanceEvidenceMatcher, maxMolecularTestAge)
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