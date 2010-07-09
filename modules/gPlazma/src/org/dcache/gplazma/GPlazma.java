package org.dcache.gplazma;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.dcache.commons.util.Strings;

import org.dcache.gplazma.configuration.ConfigurationLoadingStrategy;
import org.dcache.gplazma.configuration.ConfigurationItem;
import org.dcache.gplazma.configuration.ConfigurationItemControl;
import org.dcache.gplazma.configuration.ConfigurationItemType;

import org.dcache.gplazma.loader.PluginLoader;
import org.dcache.gplazma.loader.CachingPluginLoaderDecorator;
import org.dcache.gplazma.loader.XmlResourcePluginLoader;

import org.dcache.gplazma.strategies.StrategyFactory;
import org.dcache.gplazma.strategies.AuthenticationStrategy;
import org.dcache.gplazma.strategies.MappingStrategy;
import org.dcache.gplazma.strategies.AccountStrategy;
import org.dcache.gplazma.strategies.SessionStrategy;
import org.dcache.gplazma.strategies.GPlazmaPluginElement;

import org.dcache.gplazma.plugins.GPlazmaPlugin;
import org.dcache.gplazma.plugins.GPlazmaAuthenticationPlugin;
import org.dcache.gplazma.plugins.GPlazmaMappingPlugin;
import org.dcache.gplazma.plugins.GPlazmaAccountPlugin;
import org.dcache.gplazma.plugins.GPlazmaSessionPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author timur
 */
public class GPlazma {

    private static final Logger LOGGER =
        LoggerFactory.getLogger( GPlazma.class);



    private PluginLoader pluginLoader;


    private List<GPlazmaPluginElement<GPlazmaAuthenticationPlugin>>
            authenticationPluginElements;
    private List<GPlazmaPluginElement<GPlazmaMappingPlugin>>
            mappingPluginElements;
    private List<GPlazmaPluginElement<GPlazmaAccountPlugin>>
            accountPluginElements;
    private List<GPlazmaPluginElement<GPlazmaSessionPlugin>>
            sessionPluginElements;

   private final ConfigurationLoadingStrategy configurationLoadingStrategy;
   private AuthenticationStrategy authenticationStrategy;
   private MappingStrategy mappingStrategy;
   private AccountStrategy accountStrategy;
   private SessionStrategy sessionStrategy;

   public GPlazma(ConfigurationLoadingStrategy configurationLoadingStrategy) {
        this.configurationLoadingStrategy = configurationLoadingStrategy;
        loadPlugins();
        initStrategies();
    }

    public LoginReply login(Subject subject) throws AuthenticationException {
        if(subject == null) {
            throw new NullPointerException("subject is null");
        }
        AuthenticationStrategy currentAuthenticationStrategy;
        MappingStrategy currentMappingStrategy;
        AccountStrategy currentAccountStrategy;
        SessionStrategy currentSessionStrategy;

        synchronized (configurationLoadingStrategy) {
            if(configurationLoadingStrategy.hasUpdated()) {
                LOGGER.debug("configuration has been updated, reloading plugins");
                loadPlugins();
                initStrategies();
            }
            currentAuthenticationStrategy = this.authenticationStrategy;
            currentMappingStrategy = this.mappingStrategy;
            currentAccountStrategy = this.accountStrategy;
            currentSessionStrategy = this.sessionStrategy;
         }

         SessionID sessionId = null;
         Set<Principal> identifiedPrincipals = new HashSet();
         identifiedPrincipals.addAll(subject.getPrincipals());
         Set<Principal>  authorizedPrincipals = new HashSet();
         Set<SessionAttribute> attributes = new HashSet();
         currentAuthenticationStrategy.authenticate(
                 sessionId,
                 subject.getPublicCredentials(),
                 subject.getPrivateCredentials(),
                 identifiedPrincipals);
         currentMappingStrategy.map(
                 sessionId,
                 identifiedPrincipals,
                 authorizedPrincipals);
         currentAccountStrategy.account(
                 sessionId,
                 authorizedPrincipals);
         currentSessionStrategy.session(
                 sessionId,
                 authorizedPrincipals,
                 attributes);
         LoginReply reply = new LoginReply();
         Subject replySubject = new Subject(
                 false,
                 authorizedPrincipals,
                 subject.getPublicCredentials(),
                 subject.getPrivateCredentials());
         reply.setSubject(replySubject);
         reply.setSessionAttributes(attributes);
         return reply;
    }

    private void loadPlugins() {

        pluginLoader = new CachingPluginLoaderDecorator(
                XmlResourcePluginLoader.newPluginLoader());
        pluginLoader.init();


        authenticationPluginElements = new ArrayList();
        mappingPluginElements = new ArrayList();
        accountPluginElements = new ArrayList();
        sessionPluginElements = new ArrayList();

        for(ConfigurationItem configItem:
                configurationLoadingStrategy.
                        load().getConfigurationItemList()) {
            String pluginName = configItem.getPluginName();
            String configString = configItem.getPluginConfiguration();
            String[] pluginArguments = Strings.splitArgumentString(configString);
            ConfigurationItemType type = configItem.getType();
            ConfigurationItemControl control = configItem.getControl();
            GPlazmaPlugin plugin =
                    pluginLoader.newPluginByName(pluginName, pluginArguments);
            classifyPlugin(type, plugin, pluginName, control);

        }
    }

    private void initStrategies() {
        StrategyFactory factory =
                StrategyFactory.getInstanse();
        authenticationStrategy = factory.newAuthenticationStrategy();
        authenticationStrategy.setPlugins(authenticationPluginElements);
        mappingStrategy = factory.newMappingStrategy();
        mappingStrategy.setPlugins(mappingPluginElements);
        accountStrategy = factory.newAccountStrategy();
        accountStrategy.setPlugins(accountPluginElements);
        sessionStrategy = factory.newSessionStrategy();
        sessionStrategy.setPlugins(sessionPluginElements);
    }

    private void classifyPlugin(
            ConfigurationItemType type,
            GPlazmaPlugin plugin,
            String pluginName,
            ConfigurationItemControl control) throws IllegalArgumentException {
        if(!type.getType().isAssignableFrom(plugin.getClass())) {
                    throw new IllegalArgumentException(" plugin " + pluginName +
                            " (java class  " +
                            plugin.getClass().getCanonicalName() +
                            ") does not support being loaded as type " + type );
        }
        switch (type) {
            case AUTHENTICATION:
            {
                storePluginElement(plugin, control, authenticationPluginElements);
                break;
            }
            case MAPPING:
            {
                storePluginElement(plugin, control, mappingPluginElements);
                break;
            }
            case ACCOUNT:
            {
                storePluginElement(plugin, control, accountPluginElements);
                break;
            }
            case SESSION:
            {
                storePluginElement(plugin, control, sessionPluginElements);
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Unknown type " + type);
            }
        }
    }

    private static <T extends GPlazmaPlugin> void storePluginElement (
            GPlazmaPlugin plugin,
            ConfigurationItemControl control,
            List<GPlazmaPluginElement<T>> pluginElements)
            throws IllegalArgumentException {
        // we are forced to use unchecked cast here, as the generics do not support
        // instanceof, but we have checked the type before calling storePluginElement
        T authPlugin = (T) plugin;
        GPlazmaPluginElement<T> pluginElement = new GPlazmaPluginElement(authPlugin, control);
        pluginElements.add(pluginElement);
    }

}