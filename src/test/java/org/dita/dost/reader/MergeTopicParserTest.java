/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.reader;

import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static org.junit.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Method;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.junit.BeforeClass;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.writer.DitaWriter;
import org.junit.Test;

public class MergeTopicParserTest {

    final File resourceDir = TestUtils.getResourceDir(MergeTopicParserTest.class);
    private final File srcDir = new File(resourceDir, "src");
    private final File expDir = new File(resourceDir, "exp");

    private static SAXTransformerFactory stf;
    
    @BeforeClass
    public static void setup() {
        final TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(SAXTransformerFactory.FEATURE)) {
            throw new RuntimeException("SAX transformation factory not supported");
        }
        stf = (SAXTransformerFactory) tf;
        
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void testParse() throws Exception {
        final MergeTopicParser parser = new MergeTopicParser(new MergeUtils());
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final TransformerHandler s = stf.newTransformerHandler();
        s.getTransformer().setOutputProperty(OMIT_XML_DECLARATION , "yes");
        s.setResult(new StreamResult(output));
        parser.setContentHandler(s);
        parser.setLogger(new TestUtils.TestLogger());
        s.startDocument();
        parser.parse("test.xml", srcDir.getAbsolutePath());
        s.endDocument();
        assertXMLEqual(new InputSource(new File(expDir, "test.xml").toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));

        final Method method = MergeTopicParser.class.getDeclaredMethod("handleLocalHref", String.class);
        method.setAccessible(true);
        assertEquals("images/test.jpg", method.invoke(parser, "images/test.jpg"));
    }

    @Test
    public void testHandleLocalHref() throws Exception {
        final MergeTopicParser parser = new MergeTopicParser(new MergeUtils());
        parser.setContentHandler(new DefaultHandler());
        parser.setLogger(new TestUtils.TestLogger());
        final Method method = MergeTopicParser.class.getDeclaredMethod("handleLocalHref", String.class);
        method.setAccessible(true);
        
        parser.parse("test.xml", srcDir.getAbsolutePath());
        assertEquals("test.jpg", method.invoke(parser, "test.jpg"));
        assertEquals("test.jpg#foo", method.invoke(parser, "test.jpg#foo"));
        assertEquals("#foo", method.invoke(parser, "#foo"));
        assertEquals("images/test.jpg", method.invoke(parser, "images/test.jpg"));
        assertEquals("images/test.jpg#foo", method.invoke(parser, "images/test.jpg#foo"));
        assertEquals("../test.jpg", method.invoke(parser, "../test.jpg"));
        
        parser.parse("src/test.xml", resourceDir.getAbsolutePath());
        assertEquals("src/test.jpg", method.invoke(parser, "test.jpg"));
        assertEquals("src/images/test.jpg", method.invoke(parser, "images/test.jpg"));
        assertEquals("test.jpg", method.invoke(parser, "../test.jpg"));
    }
}
