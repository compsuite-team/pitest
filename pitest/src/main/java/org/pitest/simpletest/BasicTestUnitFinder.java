/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.pitest.simpletest;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.pitest.reflection.Reflection;
import org.pitest.simpletest.steps.CallStep;
import org.pitest.simpletest.steps.NoArgsInstantiateStep;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;


public class BasicTestUnitFinder implements TestUnitFinder {

  private final MethodFinder          testMethodFinder;

  public BasicTestUnitFinder(MethodFinder testMethodFinder) {
    this.testMethodFinder = testMethodFinder;
  }

  @Override
  public List<TestUnit> findTestUnits(final Class<?> testClass) {
    try {

      final List<TestUnit> units = new ArrayList<>();
      final TestStep instantiation = NoArgsInstantiateStep.instantiate(testClass);

      for (final TestMethod m : findTestMethods(testClass)) {
                units.add(createTestUnitForInstantiation(instantiation,
              "", testClass, m));
      }

      return units;

    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }
  }


  private TestUnit createTestUnitForInstantiation(
      final TestStep instantiationStep, final String namePrefix,
      final Class<?> testClass, final TestMethod testMethod) {

    final List<TestStep> steps = new ArrayList<>();

    steps.add(instantiationStep);
    steps.add(new CallStep(testMethod));

    final TestUnit unit = new SteppedTestUnit(new Description(namePrefix
        + testMethod.getName(), testClass), steps, testMethod.getExpected());
    return unit;

  }

  private Collection<TestMethod> findTestMethods(final Class<?> clazz) {
    final EqualitySet<TestMethod> set = new EqualitySet<>(
        new SignatureEqualityStrategy());
    final Consumer<Optional<TestMethod>> addToSet = a -> a.ifPresent(m -> set.add(m));
    final Collection<Method> methods = Reflection.allMethods(clazz);
    methods.stream().map(this.testMethodFinder).forEach(addToSet);

    return set.toCollection();
  }

}
