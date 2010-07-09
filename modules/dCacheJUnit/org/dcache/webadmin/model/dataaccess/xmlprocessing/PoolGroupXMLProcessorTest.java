package org.dcache.webadmin.model.dataaccess.xmlprocessing;

import java.util.Set;
import org.dcache.webadmin.model.dataaccess.impl.XMLDataGathererHelper;
import org.dcache.webadmin.model.exceptions.ParsingException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jans
 */
public class PoolGroupXMLProcessorTest {

    public PoolGroupXMLProcessor _processor = null;

    @Before
    public void setUp() {
        _processor = new PoolGroupXMLProcessor();
    }

    @Test
    public void testParsePoolGroupNamesDocument() throws ParsingException {
        Set<String> parsedPoolGroups = _processor.parsePoolGroupNamesDocument(
                _processor.createXMLDocument(XMLDataGathererHelper.poolGroupXmlcontent));
        assertTrue(parsedPoolGroups.contains(XMLDataGathererHelper.POOL1_POOLGROUP1));
    }
}