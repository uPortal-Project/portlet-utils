package org.jasig.springframework.security.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.FilterChain;


/**
* Holds objects associated with a Portlet filter.<P>Guarantees the request and response are instances of
* <code>PortletRequest</code> and <code>PortletResponse</code>, and that there are no <code>null</code>
* objects.
* <p>
* Required so that security system classes can obtain access to the filter environment, as well as the request
* and response.
*
* @author Eric Dalquist
*/
public class PortletFilterInvocation {
   //~ Static fields ==================================================================================================
    static final FilterChain DUMMY_CHAIN = new FilterChain() {
        @Override
        public void doFilter(ActionRequest request, ActionResponse response) throws IOException, PortletException {
            throw new UnsupportedOperationException("Dummy filter chain");
        }
        @Override
        public void doFilter(EventRequest request, EventResponse response) throws IOException, PortletException {
            throw new UnsupportedOperationException("Dummy filter chain");
        }
        @Override
        public void doFilter(RenderRequest request, RenderResponse response) throws IOException, PortletException {
            throw new UnsupportedOperationException("Dummy filter chain");
        }
        @Override
        public void doFilter(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
            throw new UnsupportedOperationException("Dummy filter chain");
        }
    };

   //~ Instance fields ================================================================================================

   private FilterChain chain;
   private PortletRequest request;
   private PortletResponse response;

   //~ Constructors ===================================================================================================

   public PortletFilterInvocation(PortletRequest request, PortletResponse response, FilterChain chain) {
       if ((request == null) || (response == null) || (chain == null)) {
           throw new IllegalArgumentException("Cannot pass null values to constructor");
       }

       this.request = (PortletRequest) request;
       this.response = (PortletResponse) response;
       this.chain = chain;
   }

   //~ Methods ========================================================================================================

   public FilterChain getChain() {
       return chain;
   }

   public PortletRequest getRequest() {
       return request;
   }

   public PortletResponse getResponse() {
       return response;
   }

   public String toString() {
       return "PortletFilterInvocation: URL: " + request;
   }
}
