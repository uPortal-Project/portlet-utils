package org.jasig.springframework.security.portlet.authentication;

import java.io.Serializable;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * A holder of selected portlet details related to a web authentication request.
 *
 * @author Eric Dalquist
 */
public class PortletAuthenticationDetails implements Serializable {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    //~ Instance fields ================================================================================================

    private final String remoteAddress;
    private final String sessionId;

    //~ Constructors ===================================================================================================

    /**
     * Records the remote address and will also set the session Id if a session
     * already exists (it won't create one).
     *
     * @param request that the authentication request was received from
     */
    public PortletAuthenticationDetails(PortletRequest request) {
        this.remoteAddress = request.getProperty("REMOTE_ADDR");

        PortletSession session = request.getPortletSession(false);
        this.sessionId = (session != null) ? session.getId() : null;
    }

    //~ Methods ========================================================================================================

    public boolean equals(Object obj) {
        if (obj instanceof PortletAuthenticationDetails) {
            PortletAuthenticationDetails rhs = (PortletAuthenticationDetails) obj;

            if ((remoteAddress == null) && (rhs.getRemoteAddress() != null)) {
                return false;
            }

            if ((remoteAddress != null) && (rhs.getRemoteAddress() == null)) {
                return false;
            }

            if (remoteAddress != null) {
                if (!remoteAddress.equals(rhs.getRemoteAddress())) {
                    return false;
                }
            }

            if ((sessionId == null) && (rhs.getSessionId() != null)) {
                return false;
            }

            if ((sessionId != null) && (rhs.getSessionId() == null)) {
                return false;
            }

            if (sessionId != null) {
                if (!sessionId.equals(rhs.getSessionId())) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Indicates the TCP/IP address the authentication request was received from.
     *
     * @return the address
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Indicates the <code>HttpSession</code> id the authentication request was received from.
     *
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    public int hashCode() {
        int code = 7654;

        if (this.remoteAddress != null) {
            code = code * (this.remoteAddress.hashCode() % 7);
        }

        if (this.sessionId != null) {
            code = code * (this.sessionId.hashCode() % 7);
        }

        return code;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(": ");
        sb.append("RemoteIpAddress: ").append(this.getRemoteAddress()).append("; ");
        sb.append("SessionId: ").append(this.getSessionId());

        return sb.toString();
    }

}
