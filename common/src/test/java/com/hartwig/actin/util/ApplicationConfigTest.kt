package com.hartwig.actin.util;

import static org.junit.Assert.assertEquals;

import com.google.common.io.Resources;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class ApplicationConfigTest {

    private static final String CONFIG_DIRECTORY = Resources.getResource("config").getPath();
    private static final String CONFIG_FILE = Resources.getResource("config/file.empty").getPath();

    @Test
    public void canRetrieveDirectoryFromConfig() throws ParseException {
        Options options = new Options();
        options.addOption("directory", true, Strings.EMPTY);

        CommandLine cmd = new DefaultParser().parse(options, new String[] { "-directory", CONFIG_DIRECTORY });

        assertEquals(CONFIG_DIRECTORY, ApplicationConfig.nonOptionalDir(cmd, "directory"));
    }

    @Test(expected = ParseException.class)
    public void crashOnNonExistingDirectory() throws ParseException {
        Options options = new Options();
        options.addOption("directory", true, Strings.EMPTY);

        CommandLine cmd = new DefaultParser().parse(options, new String[] { "-directory", "does not exist" });

        ApplicationConfig.nonOptionalDir(cmd, "directory");
    }

    @Test
    public void canRetrieveFileFromConfig() throws ParseException {
        Options options = new Options();
        options.addOption("file", true, Strings.EMPTY);

        CommandLine cmd = new DefaultParser().parse(options, new String[] { "-file", CONFIG_FILE });

        assertEquals(CONFIG_FILE, ApplicationConfig.nonOptionalFile(cmd, "file"));
    }

    @Test(expected = ParseException.class)
    public void crashOnNonExistingFile() throws ParseException {
        Options options = new Options();
        options.addOption("file", true, Strings.EMPTY);

        CommandLine cmd = new DefaultParser().parse(options, new String[] { "-file", "does not exist" });

        ApplicationConfig.nonOptionalFile(cmd, "file");
    }

    @Test
    public void canRetrieveValueFromConfig() throws ParseException {
        Options options = new Options();
        options.addOption("value", true, Strings.EMPTY);

        CommandLine cmd = new DefaultParser().parse(options, new String[] { "-value", "value" });

        assertEquals("value", ApplicationConfig.nonOptionalValue(cmd, "value"));
    }

    @Test(expected = ParseException.class)
    public void crashOnNonExistingValue() throws ParseException {
        CommandLine cmd = new DefaultParser().parse(new Options(), new String[] {});

        ApplicationConfig.nonOptionalValue(cmd, "does not exist");
    }
}