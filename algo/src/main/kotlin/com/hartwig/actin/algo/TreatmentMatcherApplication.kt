package com.hartwig.actin.algo

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory.create
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory.create
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.clinical.util.ClinicalPrinter
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import com.hartwig.actin.molecular.util.MolecularPrinter
import com.hartwig.actin.treatment.serialization.TrialJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException

class TreatmentMatcherApplication private constructor(private val config: TreatmentMatcherConfig) {
    @Throws(IOException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading clinical record from {}", config.clinicalJson())
        val clinical = ClinicalRecordJson.read(config.clinicalJson())
        ClinicalPrinter.printRecord(clinical)
        LOGGER.info("Loading molecular record from {}", config.molecularJson())
        val molecular = MolecularRecordJson.read(config.molecularJson())
        MolecularPrinter.printRecord(molecular)
        val patient = PatientRecordFactory.fromInputs(clinical, molecular)
        LOGGER.info("Loading trials from {}", config.treatmentDatabaseDirectory())
        val trials = TrialJson.readFromDir(config.treatmentDatabaseDirectory())
        LOGGER.info(" Loaded {} trials", trials.size)
        LOGGER.info("Loading DOID tree from {}", config.doidJson())
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson())
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val referenceDateProvider = create(clinical, config.runHistorically())
        LOGGER.info("Matching patient to available trials")
        val matcher: TrialMatcher = TrialMatcher.Companion.create(doidModel, referenceDateProvider)
        val trialMatches = matcher.determineEligibility(patient, trials)
        val match: TreatmentMatch? = ImmutableTreatmentMatch.builder()
            .patientId(patient.patientId())
            .sampleId(patient.molecular().sampleId())
            .referenceDate(referenceDateProvider.date())
            .referenceDateIsLive(referenceDateProvider.isLive)
            .trialMatches(trialMatches)
            .build()
        TreatmentMatchPrinter.printMatch(match)
        TreatmentMatchJson.write(match, config.outputDirectory())
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TreatmentMatcherApplication::class.java)
        private val APPLICATION: String? = "ACTIN Treatment Matcher"
        private val VERSION = TreatmentMatcherApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = TreatmentMatcherConfig.Companion.createOptions()
            var config: TreatmentMatcherConfig? = null
            try {
                config = TreatmentMatcherConfig.Companion.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            TreatmentMatcherApplication(config).run()
        }
    }
}