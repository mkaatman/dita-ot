/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.junit.Assert.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.KeyDef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestGenMapAndTopicListModule {

    private static final File resourceDir = TestUtils.getResourceDir(TestGenMapAndTopicListModule.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    
    private static File tempDir;
    private static File tempDirParallel;
    private static File tempDirAbove;

    @BeforeClass
    public static void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(TestGenMapAndTopicListModule.class);
        
        tempDirParallel = new File(tempDir, "parallel");
        tempDirParallel.mkdirs();
        final File inputDirParallel = new File("maps");
        final File inputMapParallel = new File(inputDirParallel, "root-map-01.ditamap");
        final File outDirParallel = new File(tempDirParallel, "out");
        generate(inputDirParallel, inputMapParallel, outDirParallel, tempDirParallel);
        
        tempDirAbove = new File(tempDir, "above");
        tempDirAbove.mkdirs();
        final File inputDirAbove = new File(".");
        final File inputMapAbove = new File(inputDirAbove, "root-map-02.ditamap");
        final File outDirAbove = new File(tempDirAbove, "out");
        generate(inputDirAbove, inputMapAbove, outDirAbove, tempDirAbove);
    }

    private static void generate(final File inputDir, final File inputMap, final File outDir, final File tempDir) throws DITAOTException {
        final PipelineHashIO pipelineInput = new PipelineHashIO();
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_INPUTMAP, inputMap.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_BASEDIR, srcDir.getAbsolutePath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, inputDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR, outDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_TEMPDIR, tempDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, new File("src" + File.separator + "main").getAbsolutePath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_DITAEXT, ".xml");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_INDEXTYPE, "xhtml");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ENCODING, "en-US");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_TARGETEXT, ".html");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE, Boolean.TRUE.toString());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, "1");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL, "warn");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP, "false");
        //pipelineInput.setAttribute("ditalist", new File(tempDir, FILE_NAME_DITA_LIST).getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_MAPLINKS, new File(tempDir, "maplinks.unordered").getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");

        final AbstractFacade facade = new PipelineFacade();
        facade.setLogger(new TestUtils.TestLogger());
        facade.execute("GenMapAndTopicList", pipelineInput);
    }
        
    @Test
    public void testFileContentParallel() throws Exception{
        final File e = new File(expDir, "parallel");
        
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "canditopics.list")));
        assertEquals(Collections.emptySet(), 
                readLines(new File(e, "coderef.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conref.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conrefpush.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conreftargets.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "copytosource.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "copytotarget2sourcemap.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "flagimage.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "maps/root-map-01.ditamap")),
                readLines(new File(e, "fullditamap.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "maps/root-map-01.ditamap",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "fullditamapandtopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "fullditatopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "hrefditatopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "hreftargets.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "html.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "image.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "keyref.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "outditafiles.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "relflagimage.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "resourceonly.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "skipchunk.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "subjectscheme.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "subtargets.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "maps/root-map-01.ditamap")),
                readLines(new File(e, "usr.input.file.list")));
        
        final Properties ditaProps = readProperties(new File(tempDirParallel, FILE_NAME_DITA_LIST));
        assertEquals(".." + File.separator, ditaProps.getProperty("uplevels"));

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new File(tempDirParallel+ File.separator + KEYDEF_LIST_FILE));
        final Element elem = document.getDocumentElement();
        final NodeList nodeList = elem.getElementsByTagName("keydef");
        final Map<String, List<String>> expKeyDef = new HashMap<String, List<String>>();
        expKeyDef.put("target_topic_2", Arrays.asList("target_topic_2", "topics" + UNIX_SEPARATOR + "target-topic-c.xml", "maps" + UNIX_SEPARATOR + "root-map-01.ditamap"));
        expKeyDef.put("target_topic_1", Arrays.asList("target_topic_1", "topics" + UNIX_SEPARATOR + "target-topic a.xml", "maps" + UNIX_SEPARATOR + "root-map-01.ditamap"));
        expKeyDef.put("target_topic_3", Arrays.asList("target_topic_3", "topics" + UNIX_SEPARATOR + "target-topic-c.xml", "maps" + UNIX_SEPARATOR + "root-map-01.ditamap"));
        expKeyDef.put("target_topic_4", Arrays.asList("target_topic_4", "http://www.example.com/?foo=bar&baz=qux#quxx", "maps" + UNIX_SEPARATOR + "root-map-01.ditamap"));
        for(int i = 0; i< nodeList.getLength();i++){
            final Element el = (Element) nodeList.item(i);
            final List<String> exp = expKeyDef.get(el.getAttribute("keys"));
            assertEquals(exp.get(0), el.getAttribute("keys"));
            assertEquals(exp.get(1), el.getAttribute("href"));
            assertEquals(exp.get(2), el.getAttribute("source"));
        }
    }
    
    @Test
    public void testFileContentAbove() throws Exception{
        final File e = new File(expDir, "above");
                
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml",
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml")),
                readLines(new File(e, "canditopics.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "coderef.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conref.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conrefpush.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conreftargets.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "copytosource.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "copytotarget2sourcemap.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "flagimage.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "root-map-02.ditamap")),
                readLines(new File(e, "fullditamap.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml",
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "root-map-02.ditamap")),
                readLines(new File(e, "fullditamapandtopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml",
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml")),
                readLines(new File(e, "fullditatopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "hrefditatopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml",
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml")),
                readLines(new File(e, "hreftargets.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "html.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "image.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "keyref.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "outditafiles.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "relflagimage.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "resourceonly.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "skipchunk.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "subjectscheme.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "subtargets.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "root-map-02.ditamap")),
                readLines(new File(e, "usr.input.file.list")));
                
        final Properties ditaProps = readProperties(new File(tempDirAbove, FILE_NAME_DITA_LIST));
        assertEquals("", ditaProps.getProperty("uplevels"));
        
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new File(tempDirAbove+ File.separator + KEYDEF_LIST_FILE));
        final Element elem = document.getDocumentElement();
        final NodeList nodeList = elem.getElementsByTagName("keydef");
        final Map<String, List<String>> expKeyDef = new HashMap<String, List<String>>();
        expKeyDef.put("target_topic_2", Arrays.asList("target_topic_2", "topics" + File.separator + "target-topic-c.xml", "root-map-02.ditamap"));
        expKeyDef.put("target_topic_1", Arrays.asList("target_topic_1", "topics" + File.separator + "target-topic a.xml", "root-map-02.ditamap"));
        expKeyDef.put("target_topic_3", Arrays.asList("target_topic_3", "topics" + File.separator + "target-topic-c.xml", "root-map-02.ditamap"));
        expKeyDef.put("target_topic_4", Arrays.asList("target_topic_4", "http://www.example.com/?foo=bar&baz=qux#quxx", "root-map-02.ditamap"));
        for(int i = 0; i< nodeList.getLength();i++){
            final Element el = (Element) nodeList.item(i);
            final List<String> exp = expKeyDef.get(el.getAttribute("keys"));
            assertEquals(exp.get(0), el.getAttribute("keys"));
            assertEquals(exp.get(1), el.getAttribute("href"));
            assertEquals(exp.get(2), el.getAttribute("source"));
        }
    }
        
    private Properties readProperties(final File f)
            throws IOException, FileNotFoundException {
        final Properties p = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(f);
            p.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return p;
    }
    
    private Set<String> readLines(final File f) throws IOException {
        final Set<String> lines = new HashSet<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return lines;
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
