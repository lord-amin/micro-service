package com.peykasa.audit.auditserver.test;

import com.peykasa.audit.common.oauth.OAuthUser;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Yaser(amin) Sadeghi
 */
public class BaseTest {


    protected void securitySimulated(String username) throws IOException, ServletException {
        OAuthUser oAuthUser = new OAuthUser();
        oAuthUser.setId(1L);
        oAuthUser.setUsername(username);

//        doAnswer(invocationOnMock -> {
//            System.err.println("Security simulated");
//            ServletRequest s1 = invocationOnMock.getArgument(0);
//            ServletResponse s2 = invocationOnMock.getArgument(1);
//            FilterChain argumentAt = invocationOnMock.getArgument(2);
//            s1.setAttribute(SecurityFilter.AUTHENTICATION_USER_KEY, oAuthUser);
//            argumentAt.doFilter(s1, s2);
//            return true;
//        }).when(securityFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    protected void securitySimulatedAdmin() throws IOException, ServletException {
        securitySimulated("admin");
    }

}
