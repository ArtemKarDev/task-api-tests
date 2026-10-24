package ru.learning.taskapitests.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("ru.learning.taskapitests.tests")
@IncludeTags("regression")
public class RegressionTestSuite {
}