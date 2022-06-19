//package com.peykasa.silo.central.apigatewayservice.filter;
//
//import lombok.NoArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.ServletException;
//import java.io.IOException;
//
///**
// * @author Taher Khorshidi
// */
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//@NoArgsConstructor
//public class SimpleCorsFilter extends OncePerRequestFilter {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCorsFilter.class);
////    @Override
////    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
////        HttpServletResponse response = (HttpServletResponse) res;
////        HttpServletRequest request = (HttpServletRequest) req;
////        response.setHeader("Access-Control-Allow-Origin", "*");
////        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
////        response.setHeader("Access-Control-Max-Age", "3600");
////        response.setHeader("Access-Control-Allow-Headers", "x-requested-with, authorization, content-auditor,content-type,accept,context,primary,event");
////        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
////            response.setStatus(HttpServletResponse.SC_OK);
////        } else {
////            chain.doFilter(req, res);
////        }
////    }
//
//    @Override
//    protected void doFilterInternal(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, javax.servlet.FilterChain filterChain) throws ServletException, IOException {
//        filterChain.doFilter(request, response);
//    }
//
////    @Override
////    public void init(FilterConfig filterConfig) {
////        LOGGER.debug("Init");
////    }
////
////    @Override
////    public void destroy() {
////        LOGGER.debug("Destroy");
////    }
//}