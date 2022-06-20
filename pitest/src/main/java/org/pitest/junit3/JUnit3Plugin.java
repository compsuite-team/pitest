package org.pitest.junit3;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.junit.NullConfiguration;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestPluginFactory;

import java.util.Collection;

/**
 * Handles simpler junit 3 classes. More complex cases fall through
 * to the standard junit 3 plugin, which is built on top of junit4.
 * <p>
 * This plugin works independently of junit, reimplementing junit3 behaviour.
 * This is faster, and works around corner case issues caused by the junit3
 * life cycle. Tests produced by this plugin are instantiated directly before use.
 * This means mutants will be active when their constructors are called.
 */
public class JUnit3Plugin implements TestPluginFactory {

    public static final String NAME = "junit3";

    @Override
    public String description() {
        return "JUnit 3 plugin";
    }

    @Override
    public Configuration createTestFrameworkConfiguration(TestGroupConfig config,
                                                          ClassByteArraySource source,
                                                          Collection<String> excludedRunners,
                                                          Collection<String> includedMethods) {
        if (junit3IsPresent()) {
            return new JUnit3Configuration();
        }
        return new NullConfiguration();
    }

    private boolean junit3IsPresent() {
        try {
            Class.forName("junit.framework.TestCase");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    @Override
    public String name() {
        return NAME;
    }

}