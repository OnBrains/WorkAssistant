package org.onbrains.view;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Naumov Oleg on 04.04.2015 20:39.
 */


@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {

    public AuthFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {

            // check whether session variable is set
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            HttpSession ses = req.getSession(false);
            //  allow user to proccede if url is login.xhtml or user logged in or user is accessing any page in //public folder
            String reqURI = req.getRequestURI();
            if ( reqURI.indexOf("/view/authorization/login.xhtml") >= 0 || (ses != null && ses.getAttribute("login") != null)
                    || reqURI.indexOf("/view/authorization/registration.xhtml") >= 0 || reqURI.contains("javax.faces.resource") )
                chain.doFilter(request, response);
            else   // user didn't log in but asking for a page that is not allowed so take user to login page
                res.sendRedirect(req.getContextPath() + "/view/authorization/login.xhtml");  // Anonymous user. Redirect to login page
        }
        catch(Throwable t) {
            System.out.println( t.getMessage());
        }
    }

    @Override
    public void destroy() {

    }
}