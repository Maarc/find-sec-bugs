/*
 * Find Security Bugs
 * Copyright (c) Philippe Arteau, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.h3xstream.findsecbugs.xpath;

import com.h3xstream.findbugs.test.BaseDetectorTest;
import com.h3xstream.findbugs.test.EasyBugReporter;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class XPathInjectionJavaxDetectorTest extends BaseDetectorTest {

    @Test
    public void detectXPathInjectionJavax() throws Exception {
        //Locate test code
        String[] files = {
                getClassFilePath("testcode/xpath/XPathJavax")
        };

        //Run the analysis
        EasyBugReporter reporter = spy(new SecurityReporter());
        analyze(files, reporter);

        //Assertions
        for (Integer line : Arrays.asList(21, 29)) {
            verify(reporter).doReportBug(
                    bugDefinition().bugType("XPATH_INJECTION")
                            .inClass("XPathJavax").inMethod("main").atLine(line)
                            .build()
            );
        }

        //More than two means a false positive was trigger
        verify(reporter, times(2)).doReportBug(
                bugDefinition().bugType("XPATH_INJECTION").build());
    }


    @Test
    public void avoidFPXPathInjectionJavax() throws Exception {
        //Locate test code
        String[] files = {
                getClassFilePath("testcode/xpath/XPathJavaxSafe")
        };

        //Run the analysis
        EasyBugReporter reporter = spy(new SecurityReporter());
        analyze(files, reporter);

        //Assertions
        verify(reporter,never()).doReportBug(
                bugDefinition().bugType("XPATH_INJECTION")
                        .inClass("XPathJavaxSafe").inMethod("main")
                        .build()
        );

    }
}
