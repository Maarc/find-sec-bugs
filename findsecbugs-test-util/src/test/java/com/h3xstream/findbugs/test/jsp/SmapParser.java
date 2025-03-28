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
package com.h3xstream.findbugs.test.jsp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.*;

/**
 * This class is a highly modified version of Michael Schierl's "SmapParser.java".
 * It was test with Jetty and WebLogic SMAP.
 *
 * =======
 *
 * SmapParser.java - Parse source debug extensions and
 * enhance stack traces.
 *
 * Copyright (c) 2012 Michael Schierl
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * - Neither name of the copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND THE CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR THE CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * Utility class to parse Source Debug Extensions and enhance stack traces.
 *
 * Note that only the first stratum is parsed and used.
 *
 * @author Michael Schierl
 */
public class SmapParser {


    private String javaFilename;
    private final Map<Integer, FileInfo> fileinfo = new HashMap<>();
    private final Map<Integer, int[]> java2jsp = new HashMap<>();

    private static final Pattern LINE_INFO_PATTERN = Pattern.compile("([0-9]+)(?:#([0-9]+))?(?:,([0-9]+))?:([0-9]+)(?:,([0-9]+))?");

    private static String getLine(BufferedReader reader) throws IOException {
        String s = reader.readLine();
        if (s == null) {
            throw new IOException("EOF parsing SMAP");
        }
        return s;
    }

    public SmapParser(String smap) throws IOException {
        //BufferedReader is use to support multiple types of line return
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(smap.getBytes())));

        String header = getLine(reader); //SMAP
        javaFilename = getLine(reader); //*****.java
        String jsp = getLine(reader); //JSP or alternative script
        String stratum = getLine(reader); //*S JSP
        String f = getLine(reader); //*F

        if (!header.equals("SMAP") || !stratum.startsWith("*S ") || !f.equals("*F"))
            throw new IllegalArgumentException("Unexpected SMAP file format");

        //Parse the file info section (*F)
        String line;
        while((line = getLine(reader)) != null && !line.equals("*L")) {
            String path = null;
            if (line.startsWith("+ ")) {
                path = getLine(reader);
                line = line.substring(2);
            }

            int pos = line.indexOf(" ");
            int fileNum = Integer.parseInt(line.substring(0, pos));
            String name = line.substring(pos + 1);
            fileinfo.put(fileNum, new FileInfo(name, path == null ? name : path));
        }

        //Parse the line number mapping section (*L)
        int lastLFI = 0;
        while((line = getLine(reader)) != null && !line.equals("*E")) {

            if (!line.startsWith("*")) {

                Matcher m = LINE_INFO_PATTERN.matcher(line);
                if (!m.matches())
                    throw new IllegalArgumentException(line);

                int inputStartLine = Integer.parseInt(m.group(1));
                int lineFileID = m.group(2) == null ? lastLFI : Integer.parseInt(m.group(2));
                int repeatCount = m.group(3) == null ? 1 : Integer.parseInt(m.group(3));
                int outputStartLine = Integer.parseInt(m.group(4));
                int outputLineIncrement = m.group(5) == null ? 1 : Integer.parseInt(m.group(5));

                for (int i = 0; i < repeatCount; i++) {
                    int[] inputMapping = new int[]{ lineFileID, inputStartLine + i};
                    int baseOL = outputStartLine + i * outputLineIncrement;
                    for (int ol = baseOL; ol < baseOL + outputLineIncrement; ol++) {
                        if (!java2jsp.containsKey(ol))
                            java2jsp.put(ol, inputMapping);
                    }
                }
                lastLFI = lineFileID;
            }
        }
    }

    public String getJavaFilename() {
        return javaFilename;
    }

    public String getScriptFilename(int fileIndex) {
        FileInfo f = fileinfo.get(fileIndex);
        return f.name;
    }

    /**
     * Java line --> Script line
     *
     * @param lineNo
     * @return
     */
    public int[] getScriptLineNumber(Integer lineNo) {
        return java2jsp.get(lineNo);
    }

    public SmapLocation getSmapLocation(Integer lineNo) {
        int[] origSource = java2jsp.get(lineNo);
        FileInfo info = fileinfo.get(origSource[0]);
        return new SmapLocation(info,origSource[1], origSource[0] == 0);
    }

    /**
     * Script line --> Java line
     *
     * @param lineNo
     * @return
     */
    public List<Integer> getOriginalLine(Integer lineNo) {
        //Reverse the reverse mapping .. o_O
        //FIXME : Reimplement the method above
        List<Integer> jspLines = new ArrayList<Integer>();
        for(Map.Entry<Integer,int[]> lineMap : java2jsp.entrySet()) {

            for(int value : lineMap.getValue()) {
                if(value == lineNo)
                    jspLines.add(lineMap.getKey());
            }
        }
        return jspLines;
    }

    public static class FileInfo {
        public final String name;
        public final String path;

        public FileInfo(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }

    public static class SmapLocation {
        public final FileInfo fileInfo;
        public final int line;
        public final boolean isPrimaryFile;

        public SmapLocation(FileInfo fileInfo, int line, boolean isPrimaryFile) {
            this.fileInfo = fileInfo;
            this.line = line;
            this.isPrimaryFile = isPrimaryFile;
        }

    }
}