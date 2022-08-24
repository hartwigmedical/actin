package com.hartwig.actin.clinical;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.StringJoiner;

import com.hartwig.actin.util.ApplicationConfig;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ReformatQuestionnaireApplication {

    private static final Logger LOGGER = LogManager.getLogger(ReformatQuestionnaireApplication.class);

    private static final String QUESTIONNAIRE = "questionnaire";
    private static final String APPLICATION = "ACTIN Questionnaire Reformat";

    public static void main(@NotNull String... args) throws IOException {
        Options options = new Options();
        options.addOption(QUESTIONNAIRE, true, "File containing the questionnaire txt");

        String questionnaireFile = null;
        try {
            CommandLine cmd = new DefaultParser().parse(options, args);
            questionnaireFile = ApplicationConfig.nonOptionalFile(cmd, QUESTIONNAIRE);
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new ReformatQuestionnaireApplication(questionnaireFile).run();
    }

    @NotNull
    private final String questionnaireFile;

    private ReformatQuestionnaireApplication(@NotNull final String questionnaireFile) {
        this.questionnaireFile = questionnaireFile;
    }

    public void run() throws IOException {
        List<String> lines = Files.readAllLines(new File(questionnaireFile).toPath());

        StringJoiner joiner = new StringJoiner("\\n\\n");
        for (String line : lines) {
            joiner.add(line.trim());
        }

        System.out.println(joiner);
    }
}
