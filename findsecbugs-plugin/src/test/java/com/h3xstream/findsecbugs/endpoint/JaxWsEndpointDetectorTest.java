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
package com.h3xstream.findsecbugs.endpoint;

import com.h3xstream.findbugs.test.BaseDetectorTest;
import com.h3xstream.findbugs.test.EasyBugReporter;
import org.testng.annotations.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class JaxWsEndpointDetectorTest extends BaseDetectorTest {

    @Test
    public void detectJaxWsEndpoint() throws Exception {
        //Locate test code
        String[] files = {
                getClassFilePath("testcode/endpoint/JaxWsService")
        };

        //Run the analysis
        EasyBugReporter reporter = spy(new SecurityReporter());
        analyze(files, reporter);

        //Assertions

        verify(reporter).doReportBug(
                bugDefinition()
                        .bugType("JAXWS_ENDPOINT")
                        .inClass("JaxWsService").inMethod("ping")
                        .build()
        );
        verify(reporter).doReportBug(
                bugDefinition()
                        .bugType("JAXWS_ENDPOINT")
                        .inClass("JaxWsService").inMethod("hello")
                        .build()
        );
    }

    @Test
    public void detectJakartaWsEndpoint() throws Exception {
        //Locate test code
        String[] files = {
                getClassFilePath("testcode/endpoint/JakartaWsService")
        };

        //Run the analysis
        EasyBugReporter reporter = spy(new SecurityReporter());
        analyze(files, reporter);

        //Assertions

        verify(reporter).doReportBug(
                bugDefinition()
                        .bugType("JAXWS_ENDPOINT")
                        .inClass("JakartaWsService").inMethod("ping")
                        .build()
        );
        verify(reporter).doReportBug(
                bugDefinition()
                        .bugType("JAXWS_ENDPOINT")
                        .inClass("JakartaWsService").inMethod("hello")
                        .build()
        );
    }
}
