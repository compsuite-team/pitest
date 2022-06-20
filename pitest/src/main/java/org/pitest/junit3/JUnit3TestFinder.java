package org.pitest.junit3;

import junit.framework.TestCase;
import org.pitest.reflection.Reflection;
import org.pitest.simpletest.EqualitySet;
import org.pitest.simpletest.SignatureEqualityStrategy;
import org.pitest.simpletest.SteppedTestUnit;
import org.pitest.simpletest.TestMethod;
import org.pitest.simpletest.TestStep;
import org.pitest.simpletest.steps.CallStep;
import org.pitest.simpletest.steps.NoArgsInstantiateStep;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.pitest.util.Unchecked.translateCheckedException;

public class JUnit3TestFinder implements TestUnitFinder {

    @Override
    public List<TestUnit> findTestUnits(final Class<?> testClass) {
        try {
            if (isSuitable(testClass)) {
                return Collections.emptyList();
            }
            final TestStep instantiation = NoArgsInstantiateStep.instantiate(testClass);

            return findTestMethods(testClass).stream()
                    .map(m -> createTestUnitForInstantiation(instantiation, testClass, m))
                    .collect(Collectors.toList());

        } catch (final Exception ex) {
            throw translateCheckedException(ex);
        }
    }

    private boolean isSuitable(Class<?> testClass) {
        return !isJUnit3Test(testClass) ||
                !hasSuitableConstructor(testClass) ||
                isAbstract(testClass) ||
                hasOverrides(testClass);
    }

    private boolean hasOverrides(Class<?> testClass) {
            Set<Method> methods = Reflection.allMethods(testClass);

        return overridesFor(methods, m -> m.getName().equals("runTest") && m.getParameterTypes().length == 0)
        || overridesFor(methods, m -> m.getName().equals("runBare") && m.getParameterTypes().length == 0);
    }

    private boolean overridesFor(Set<Method> methods, Predicate<Method> p) {
        List<Method> runMethods = methods
                .stream()
                .filter(p)
                .collect(Collectors.toList());
        return runMethods.size() != 1;
    }

    private boolean isAbstract(Class<?> testClass) {
        return Modifier.isAbstract(testClass.getModifiers());
    }

    private boolean hasSuitableConstructor(Class<?> testClass) {
        try {
            Constructor<?> noArgs = testClass.getConstructor();
            return Modifier.isPublic(noArgs.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean isJUnit3Test(Class<?> clazz) {
        return TestCase.class.isAssignableFrom(clazz);
    }

    private TestUnit createTestUnitForInstantiation(
            final TestStep instantiationStep,
            final Class<?> testClass, final TestMethod testMethod) {

        final List<TestStep> steps = asList(instantiationStep, new CallStep(testMethod));

        return new SteppedTestUnit(new Description(testMethod.getName(), testClass), steps, testMethod.getExpected());

    }

    private Collection<TestMethod> findTestMethods(final Class<?> clazz) {
        final EqualitySet<TestMethod> set = new EqualitySet<>(
                new SignatureEqualityStrategy());
        final Consumer<Optional<TestMethod>> addToSet = a -> a.ifPresent(m -> set.add(m));
        final Collection<Method> methods = Reflection.allMethods(clazz);
        methods.stream()
                .map(this::testMethod)

                .forEach(addToSet);

        return set.toCollection();
    }

    private Optional<TestMethod> testMethod(Method method) {
        if (method.getName().startsWith("test")) {
            return Optional.of(new TestMethod(method));
        }
        return Optional.empty();
    }

}
