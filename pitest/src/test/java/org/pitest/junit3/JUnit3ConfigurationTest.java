package org.pitest.junit3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import org.jmock.MockObjectTestCase;
import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.FindTestUnits;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.UnContainer;


public class JUnit3ConfigurationTest {

    private JUnit3Configuration testee = new JUnit3Configuration();


    private Container                          container = new UnContainer();
    private TestListener                       listener = Mockito.mock(TestListener.class);
    private Pitest                             pitest = new Pitest(listener);

    public static abstract class AbstractTest extends TestCase {
        public void testA() {

        }
    }

    @Test
    public void noTestInAbstractClass() {
        List<TestUnit> actual = find(AbstractTest.class);
        assertThat(actual).isEmpty();
    }

    public static class OverriddenRunTest extends TestCase {
        public void testA() {

        }

        @Override
        public void runTest() {

        }
    }

    @Test
    public void noTestWhenRunTestOverridden() {
        List<TestUnit> actual = find(OverriddenRunTest.class);
        assertThat(actual).isEmpty();
    }

    public static class OverriddenRunTestInParent extends OverriddenRunTest {
        public void testA() {

        }
    }

    @Test
    public void noTestWhenRunTestOverriddenInParent() {
        List<TestUnit> actual = find(OverriddenRunTestInParent.class);
        assertThat(actual).isEmpty();
    }

    public static class OverriddenRunBare extends TestCase {
        public void testA() {

        }

        @Override
        public void runBare() {

        }
    }

    @Test
    public void noTestWhenRunBareOverriddenInParent() {
        List<TestUnit> actual = find(OverriddenRunBare.class);
        assertThat(actual).isEmpty();
    }

    public static class JUnit3TestWithSingleStringConstructorAndJUnit4Annotations
            extends TestCase {

        private final String name;

        public JUnit3TestWithSingleStringConstructorAndJUnit4Annotations(
                final String name) {
            super(name);
            this.name = name;
        }

        @Test
        public void testOne() {
            assertEquals("testOne", this.name);
        }

        @Test
        public void testTwo() {
            assertEquals("testTwo", this.name);
        }

    }

    @Test
    public void shouldCallSingleStringArgumentsConstructorWithTestNameWithAnnotations() {
        run(JUnit3TestWithSingleStringConstructorAndJUnit4Annotations.class);
        verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    }

    public static class JUnit3TestWithSingleStringConstructor extends TestCase {

        private final String name;

        public JUnit3TestWithSingleStringConstructor(final String name) {
            super(name);
            this.name = name;
        }

        public void testOne() {
            assertEquals("testOne", this.name);
        }

        public void testTwo() {
            assertEquals("testTwo", this.name);
        }

    }

    @Test
    public void shouldCallSingleStringArgumentsConstructorWithTestName() {
        run(JUnit3TestWithSingleStringConstructor.class);
        verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    }

    public static class SimpleJUnit3Test extends TestCase {
        public void testOne() {

        }
    }

    @Test
    public void testFindJUnit3Tests() {
        run(SimpleJUnit3Test.class);
        verify(this.listener).onTestSuccess(any(TestResult.class));
    }

    public static class MixedJunit3And4Test extends TestCase {
        @Test
        public void testOne() {

        }
    }

    @Test
    public void shouldRunOnlyOneTestWhenMatchesBothJunit3And4Criteria() {
        run(MixedJunit3And4Test.class);
        verify(this.listener).onTestSuccess(any(TestResult.class));
    }

    public static class BaseTestCaseWithTest extends TestCase {
        public void testFoo() {

        }

        @Test
        public void testBar() {

        }
    }

    public static class InheritedTest extends BaseTestCaseWithTest {

    }

    public static class OverridesTestInParent extends BaseTestCaseWithTest {
        @Override
        public void testFoo() {

        }
    }

    @Test
    public void shouldRunTestsInheritedFromParent() {
        run(InheritedTest.class);
        verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    }

    @Test
    public void testOverriddenTestsCalledOnlyOnce() {
        run(OverridesTestInParent.class);
        verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    }


    static abstract class HideFromJUnit9 {

        public static class JMockTest extends MockObjectTestCase {
            org.jmock.Mock mock;

            @Override
            public void setUp() {
                this.mock = mock(Runnable.class);
                this.mock.expects(once()).method("run");
            }

            public void testFails() {

            }

            public void testPasses() {
                final Runnable r = (Runnable) this.mock.proxy();
                r.run();
            }
        }

    }

    @Test
    public void shouldRunJMock1Tests() {
        run(HideFromJUnit9.JMockTest.class);
        verify(this.listener).onTestSuccess(any(TestResult.class));
        verify(this.listener).onTestFailure(any(TestResult.class));
    }

    public static class JUnit3Test extends TestCase {
        public void testSomething() {

        }

        public void testSomethingElse() {

        }
    }

    public static class NoSuitableConstructor extends TestCase {
        public NoSuitableConstructor(final int i, final int j, final long l) {

        }

        public void testSomething() {

        }
    }

    @Test
    public void shouldNotFindTestsInJUnit3TestsWithoutASuitableConstructor() {
        final List<TestUnit> actual = find(NoSuitableConstructor.class);
        assertEquals(0, actual.size());
    }


    public static class NonPublicConstructor extends TestCase {
        NonPublicConstructor() {

        }

        public void testSomething() {

        }
    }

    @Test
    public void noTestsWhenConstructorNotPublic() {
        final List<TestUnit> actual = find(NonPublicConstructor.class);
        assertEquals(0, actual.size());
    }

    private void run(final Class<?> clazz) {
        this.pitest.run(this.container, this.testee, clazz);
    }

    private List<TestUnit> find(Class<?> clazz) {
        final FindTestUnits finder = new FindTestUnits(this.testee);
        return finder.findTestUnitsForAllSuppliedClasses(Arrays
                .asList(clazz));
    }

}
