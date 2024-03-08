package com.hartwig.actin.algo

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory.create
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.medication.AtcTree
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.clinical.util.ClinicalPrinter
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import com.hartwig.actin.molecular.util.MolecularPrinter
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.serialization.TrialJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import kotlin.system.exitProcess

class TreatmentMatcherApplication(private val config: TreatmentMatcherConfig) {
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading clinical record from {}", config.clinicalJson)
        val clinical = ClinicalRecordJson.read(config.clinicalJson)
        ClinicalPrinter.printRecord(clinical)

        LOGGER.info("Loading molecular record from {}", config.molecularJson)
        val molecular = MolecularRecordJson.read(config.molecularJson)
        MolecularPrinter.printRecord(molecular)
        val patient = PatientRecordFactory.fromInputs(clinical, molecular)

        LOGGER.info("Loading trials from {}", config.trialDatabaseDirectory)
        val trials = TrialJson.readFromDir(config.trialDatabaseDirectory)
        LOGGER.info(" Loaded {} trials", trials.size)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        val referenceDateProvider = create(patient, config.runHistorically)
        LOGGER.info("Matching patient to available trials")

        // We assume we never check validity of a gene inside algo.
        val molecularInputChecker = MolecularInputChecker.createAnyGeneValid()
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val functionInputResolver = FunctionInputResolver(doidModel, molecularInputChecker, treatmentDatabase)
        val resources = RuleMappingResources(referenceDateProvider, doidModel, functionInputResolver, atcTree, treatmentDatabase)
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(config.extendedEfficacyJson)

        val match =
            TreatmentMatcher.create(resources, trials, evidenceEntries, config.trialSource).evaluateAndAnnotateMatchesForPatient(patient)

        TreatmentMatchPrinter.printMatch(match)
        TreatmentMatchJson.write(match, config.outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(TreatmentMatcherApplication::class.java)
        const val APPLICATION = "ACTIN Treatment Matcher"
        val VERSION: String = TreatmentMatcherApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

@Throws(IOException::class)
fun main(args: Array<String>) {
    val options: Options = TreatmentMatcherConfig.createOptions()
    try {
        val config = TreatmentMatcherConfig.createConfig(DefaultParser().parse(options, args))
        TreatmentMatcherApplication(config).run()
    } catch (exception: ParseException) {
        TreatmentMatcherApplication.LOGGER.error(exception)
        HelpFormatter().printHelp(TreatmentMatcherApplication.APPLICATION, options)
        exitProcess(1)
    }
}