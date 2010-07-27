package org.dcache.webadmin.view.pages.cellservices;

import org.dcache.webadmin.controller.impl.StandardCellsService;
import org.junit.Before;
import org.junit.Test;
import org.dcache.webadmin.model.dataaccess.DAOFactory;
import org.dcache.webadmin.model.dataaccess.impl.DAOFactoryImplHelper;
import org.apache.wicket.util.tester.WicketTester;
import org.dcache.webadmin.view.WebAdminInterface;
import org.dcache.webadmin.view.pages.ApplicationFactoryHelper;

/**
 *
 * @author jans
 */
public class CellServicesTest {

    private StandardCellsService _cellsService;
    private WicketTester _tester;

    @Before
    public void setUp() {
        WebAdminInterface authenticatedWebApp =
                ApplicationFactoryHelper.createSignedInAsAdminApplication();
        DAOFactory daoFactory = new DAOFactoryImplHelper();
        _cellsService = new StandardCellsService(daoFactory);
        authenticatedWebApp.setCellsService(_cellsService);
        _tester = new WicketTester(authenticatedWebApp);
        _tester.startPage(CellServices.class);
    }

    @Test
    public void testBasicRender() {
        _tester.assertRenderedPage(CellServices.class);
    }
}