package org.dcache.webadmin.view.pages.activetransfers;

import java.util.List;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.dcache.webadmin.controller.ActiveTransfersService;
import org.dcache.webadmin.controller.exceptions.ActiveTransfersServiceException;
import org.dcache.webadmin.view.beans.ActiveTransfersBean;
import org.dcache.webadmin.view.pages.basepage.BasePage;
import org.dcache.webadmin.view.panels.activetransfers.ActiveTransfersPanel;
import org.dcache.webadmin.view.util.Role;
import org.dcache.webadmin.view.util.SelectableWrapper;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class ActiveTransfers extends BasePage {

    private static final String KILL_MOVERS_TEXT = "activeTransfers.killSelected";
    private static final Logger _log = LoggerFactory.getLogger(ActiveTransfers.class);
    private List<SelectableWrapper<ActiveTransfersBean>> _activeTransfers;

    public ActiveTransfers() {
        Form activeTransfersForm = new Form("activeTransfersForm");
        activeTransfersForm.add(new FeedbackPanel("feedback"));
        Label label = new Label("activeTransfers.killMoversLabel",
                getStringResource(KILL_MOVERS_TEXT));
        MetaDataRoleAuthorizationStrategy.authorize(label, RENDER, Role.ADMIN);
        activeTransfersForm.add(label);
        Button button = new SubmitButton("submit");
        MetaDataRoleAuthorizationStrategy.authorize(button, RENDER, Role.ADMIN);
        activeTransfersForm.add(button);
        getActiveTransfers();
        activeTransfersForm.add(new ActiveTransfersPanel("activeTransfersPanel",
                new PropertyModel(this, "_activeTransfers")));
        add(activeTransfersForm);
    }

    private ActiveTransfersService getActiveTransfersService() {
        return getWebadminApplication().getActiveTransfersService();
    }

    private void getActiveTransfers() {
        try {
            _log.debug("getActiveTransfers called");
            _activeTransfers = getActiveTransfersService().getActiveTransferBeans();
        } catch (ActiveTransfersServiceException ex) {
            this.error(getStringResource("error.getActiveTransfersFailed") + ex.getMessage());
            _log.debug("getActiveTransfers failed {}", ex.getMessage());
            _activeTransfers = null;
        }
    }

    private class SubmitButton extends Button {

        public SubmitButton(String id) {
            super(id);
        }

        @Override
        public void onSubmit() {
            try {
                _log.debug("Kill Movers submitted");
                getActiveTransfersService().killTransfers(_activeTransfers);
            } catch (ActiveTransfersServiceException e) {
                _log.info("couldn't kill some movers - jobIds: {}",
                        e.getMessage());
                error(getStringResource("error.notAllMoversKilled"));
            }
            getActiveTransfers();
        }
    }
}