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
package com.h3xstream.findsecbugs;

import com.h3xstream.findbugs.test.BaseDetectorTest;
import com.h3xstream.findbugs.test.EasyBugReporter;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class UnvalidatedRedirectDetectorTest extends BaseDetectorTest {

    @Test
    public void detectUnvalidatedRedirect() throws Exception {
        //Locate test code
        String[] files = {
                getClassFilePath("testcode/UnvalidatedRedirectServlet")
        };

        //Run the analysis
        EasyBugReporter reporter = spy(new SecurityReporter());
        analyze(files, reporter);


        verify(reporter).doReportBug(
                bugDefinition()
                        .bugType("UNVALIDATED_REDIRECT")
                        .inClass("UnvalidatedRedirectServlet")
                        .inMethod("unvalidatedRedirect1")
                        .withPriority("High")
                        .build()
        );

        verify(reporter).doReportBug(
                bugDefinition()
                        .bugType("UNVALIDATED_REDIRECT")
                        .inClass("UnvalidatedRedirectServlet")
                        .inMethod("unvalidatedRedirect2")
                        .withPriority("Medium")
                        .build()
        );

        verify(reporter, times(2)).doReportBug(
                bugDefinition().bugType("UNVALIDATED_REDIRECT").build()
        );
    }

    @Test
    public void detectJakartaUnvalidatedRedirect() throws Exception {
        //Locate test code
        String[] files = {
                getClassFilePath("testcode/JakartaUnvalidatedRedirectServlet")
        };

        //Run the analysis
        EasyBugReporter reporter = spy(new SecurityReporter());
        analyze(files, reporter);


        verify(reporter).doReportBug(
                bugDefinition()
                        .bugType("UNVALIDATED_REDIRECT")
                        .inClass("JakartaUnvalidatedRedirectServlet")
                        .inMethod("unvalidatedRedirect1")
                        .withPriority("Medium")
                        .build()
        );
    }
}
