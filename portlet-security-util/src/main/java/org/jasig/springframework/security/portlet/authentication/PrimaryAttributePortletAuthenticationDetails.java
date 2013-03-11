package org.jasig.springframework.security.portlet.authentication;

import java.util.Collection;

import javax.portlet.PortletRequest;

import org.jasig.springframework.security.portlet.authentication.PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails;
import org.springframework.security.core.GrantedAuthority;

/**
 * Extends the portlet pre-auth details to include a primaryAttribute value.
 * 
 * @author Eric Dalquist
 */
public class PrimaryAttributePortletAuthenticationDetails extends
        PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails {
    private static final long serialVersionUID = 1L;
    
    private final String primaryAttribute;

    public PrimaryAttributePortletAuthenticationDetails(PortletRequest request,
            Collection<? extends GrantedAuthority> authorities,
            String primaryAttribute) {
        super(request, authorities);
        
        this.primaryAttribute = primaryAttribute;
    }

    public String getPrimaryAttribute() {
        return primaryAttribute;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((primaryAttribute == null) ? 0 : primaryAttribute.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrimaryAttributePortletAuthenticationDetails other = (PrimaryAttributePortletAuthenticationDetails) obj;
        if (primaryAttribute == null) {
            if (other.primaryAttribute != null)
                return false;
        }
        else if (!primaryAttribute.equals(other.primaryAttribute))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PrimaryAttributePortletAuthenticationDetails [primaryAttribute=" + primaryAttribute
                + ", getGrantedAuthorities()=" + getGrantedAuthorities() + ", getRemoteAddress()=" + getRemoteAddress()
                + ", getSessionId()=" + getSessionId() + ", getUserInfo()=" + getUserInfo() + "]";
    }
}