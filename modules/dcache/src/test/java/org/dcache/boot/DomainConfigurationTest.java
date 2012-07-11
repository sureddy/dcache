package org.dcache.boot;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.io.StringReader;
import java.io.IOException;

import org.dcache.util.ConfigurationProperties;
import org.dcache.boot.Domain;
import dmg.cells.nucleus.CellShell;
import dmg.cells.nucleus.SystemCell;
import dmg.util.CommandException;
import dmg.util.Formats;

public class DomainConfigurationTest
{
    private final static String DOMAIN_NAME = "domain";
    private final static String SERVICE1_NAME = "service1";
    private final static String SERVICE2_NAME = "service2";

    private final static String DEFAULTS =
        "a=1\n" +
        "b=${a}\n" +
        "service1/c=2\n";

    private final static String CONFIGURATION =
        "a=2\n" +
        "c=1\n";

    private final static String SERVICE1_CONFIG =
        "a=3\n" +
        "c=4\n";

    private final static String SERVICE2_CONFIG =
        "a=3\n" +
        "b=2\n" +
        "c=5\n";

    private final static SystemCell system = new SystemCell(DOMAIN_NAME);

    private ConfigurationProperties defaults;
    private ConfigurationProperties configuration;

    @Before
    public void setup()
        throws IOException
    {
        defaults = new ConfigurationProperties(new Properties());
        defaults.load(new StringReader(DEFAULTS));

        configuration = new ConfigurationProperties(defaults);
        configuration.load(new StringReader(CONFIGURATION));
    }

    public void assertPropertyEquals(String expected, String variable, CellShell shell)
    {
        String value = shell.getReplacement(variable);
        if (value != null) {
            value = Formats.replaceKeywords(value, shell);
        }
        assertEquals(expected, value);
    }

    @Test
    public void testWithDefaults()
        throws CommandException
    {
        Domain domain = new Domain(DOMAIN_NAME, defaults);
        ConfigurationProperties service = domain.createService(SERVICE1_NAME);
        CellShell shell = domain.createShellForService(system, service);

        assertPropertyEquals("1", "a", shell);
        assertPropertyEquals("1", "b", shell);
        assertPropertyEquals("2", "c", shell);
        assertPropertyEquals(DOMAIN_NAME, "domain.name", shell);
        assertPropertyEquals(SERVICE1_NAME, "domain.service", shell);

        service = domain.createService(SERVICE2_NAME);
        shell = domain.createShellForService(system, service);

        assertPropertyEquals("1", "a", shell);
        assertPropertyEquals("1", "b", shell);
        assertPropertyEquals(null, "c", shell);
        assertPropertyEquals(DOMAIN_NAME, "domain.name", shell);
        assertPropertyEquals(SERVICE2_NAME, "domain.service", shell);
    }

    @Test
    public void testWithConfiguration()
        throws CommandException
    {
        Domain domain = new Domain(DOMAIN_NAME, configuration);

        ConfigurationProperties service = domain.createService(SERVICE1_NAME);
        CellShell shell = domain.createShellForService(system, service);

        assertPropertyEquals("2", "a", shell);
        assertPropertyEquals("2", "b", shell);
        assertPropertyEquals("2", "c", shell);
        assertPropertyEquals(DOMAIN_NAME, "domain.name", shell);
        assertPropertyEquals(SERVICE1_NAME, "domain.service", shell);

        service = domain.createService(SERVICE2_NAME);
        shell = domain.createShellForService(system, service);

        assertPropertyEquals("2", "a", shell);
        assertPropertyEquals("2", "b", shell);
        assertPropertyEquals("1", "c", shell);
        assertPropertyEquals(DOMAIN_NAME, "domain.name", shell);
        assertPropertyEquals(SERVICE2_NAME, "domain.service", shell);
    }

    @Test
    public void testWithPerServiceConfiguration()
        throws CommandException, IOException
    {
        Domain domain = new Domain(DOMAIN_NAME, configuration);

        ConfigurationProperties service = domain.createService(SERVICE1_NAME);
        service.load(new StringReader(SERVICE1_CONFIG));
        CellShell shell = domain.createShellForService(system, service);

        assertPropertyEquals("3", "a", shell);
        assertPropertyEquals("3", "b", shell);
        assertPropertyEquals("4", "c", shell);
        assertPropertyEquals(DOMAIN_NAME, "domain.name", shell);
        assertPropertyEquals(SERVICE1_NAME, "domain.service", shell);

        service = domain.createService(SERVICE2_NAME);
        service.load(new StringReader(SERVICE2_CONFIG));
        shell = domain.createShellForService(system, service);

        assertPropertyEquals("3", "a", shell);
        assertPropertyEquals("2", "b", shell);
        assertPropertyEquals("5", "c", shell);
        assertPropertyEquals(DOMAIN_NAME, "domain.name", shell);
        assertPropertyEquals(SERVICE2_NAME, "domain.service", shell);
    }

    @Test
    public void testWithRuntimeOverrides()
        throws CommandException, IOException
    {
        Domain domain = new Domain(DOMAIN_NAME, configuration);

        ConfigurationProperties service = domain.createService(SERVICE1_NAME);
        service.load(new StringReader(SERVICE1_CONFIG));
        CellShell shell = domain.createShellForService(system, service);

        assertPropertyEquals("3", "a", shell);
        assertPropertyEquals("3", "b", shell);
        assertPropertyEquals("4", "c", shell);

        shell.environment().put("a", "${c}");

        assertPropertyEquals("4", "a", shell);
        assertPropertyEquals("4", "b", shell);
        assertPropertyEquals("4", "c", shell);
    }
}