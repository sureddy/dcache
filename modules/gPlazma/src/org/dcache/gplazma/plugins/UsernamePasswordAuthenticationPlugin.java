package org.dcache.gplazma.plugins;

import java.security.Principal;
import java.util.Set;
import org.dcache.auth.Password;
import org.dcache.auth.UserNamePrincipal;
import org.dcache.auth.VerifiedUserPincipal;
import org.dcache.gplazma.AuthenticationException;
import org.dcache.gplazma.SessionAttribute;
import org.dcache.gplazma.SessionID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does the authentication Part that is needed for an Authenticationmechanism
 * with Username and Password. The UserNamePrincipal is
 * expected to be in the identifiedPrincipals. The Password is expected to be
 * in the privateCredentials.
 * The first Username/Password found is processed.
 * If there are more than one the surplus ones are ignored.
 * @author jans
 */
public abstract class UsernamePasswordAuthenticationPlugin implements
        GPlazmaAuthenticationPlugin, GPlazmaMappingPlugin, GPlazmaSessionPlugin {

    private static final Logger _log = LoggerFactory.getLogger(
            UsernamePasswordAuthenticationPlugin.class);

    public UsernamePasswordAuthenticationPlugin() {
    }

    public UsernamePasswordAuthenticationPlugin(String[] arguments) {
    }

    /*
     * searches for the first usernameprincipal and the Passwordcredential and
     * delegates the call to the specialised pluginclass. This throws an
     * AuthenticationException if the provided username and password cannot be
     * authenticated.
     */
    @Override
    public void authenticate(SessionID sID, Set<Object> publicCredentials,
            Set<Object> privateCredentials, Set<Principal> identifiedPrincipals)
            throws AuthenticationException {
        String user = getUser(identifiedPrincipals).getName();
        authenticate(user,
                getPassword(privateCredentials).getPassword().toCharArray());
        identifiedPrincipals.add(new VerifiedUserPincipal(user));
    }

    /*
     * Assumption is that the authentication step didn't fail, so that the
     * UserNamePrincipal in principals is already authenticated when this
     * is called
     */
    @Override
    public void map(SessionID sID, Set<Principal> principals,
            Set<Principal> authorizedPrincipals) throws AuthenticationException {
        _log.debug("map of username/pw is called");
        UserNamePrincipal user = new UserNamePrincipal(getVerifiedUser(principals).getName());
        authorizedPrincipals.add(user);
        map(user.getName(), principals, authorizedPrincipals);
    }

    /*
     * delegate to concrete implementation giving the username to get the
     * metadata
     */
    @Override
    public void session(SessionID sID, Set<Principal> authorizedPrincipals,
            Set<SessionAttribute> attrib) throws AuthenticationException {
        session(getUser(authorizedPrincipals).getName(), attrib);
    }

    /*
     * This method checks wheter the given username and password combination
     * exists and therefore is allowed to login. If this is not the case it
     * it throws an AuthenticationException
     */
    protected abstract void authenticate(String username, char[] password)
            throws AuthenticationException;

    /*
     * This method is responsible to add the GID and UID for the given user.
     * If this is not possible it throws an AuthenticationException
     */
    protected abstract void map(String username, Set<Principal> principals,
            Set<Principal> authorizedPrincipals) throws AuthenticationException;

    protected abstract void session(String username, Set<SessionAttribute> attrib)
            throws AuthenticationException;

    private VerifiedUserPincipal getVerifiedUser(Set<Principal> principals)
            throws AuthenticationException {
        for (Principal principal : principals) {
            if (principal instanceof VerifiedUserPincipal) {
                return (VerifiedUserPincipal) principal;
            }
        }
        throw new AuthenticationException("no verified user to map");
    }

    private UserNamePrincipal getUser(Set<Principal> identifiedPrincipals)
            throws AuthenticationException {
        for (Principal principal : identifiedPrincipals) {
            if (principal instanceof UserNamePrincipal) {
                return (UserNamePrincipal) principal;
            }
        }
        throw new AuthenticationException("no username provided");
    }

    private Password getPassword(Set<Object> privateCredentials)
            throws AuthenticationException {
        for (Object privateCredential : privateCredentials) {
            if (privateCredential instanceof Password) {
                return (Password) privateCredential;
            }
        }
        throw new AuthenticationException("no password provided");
    }
}