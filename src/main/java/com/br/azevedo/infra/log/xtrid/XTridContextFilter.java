package com.br.azevedo.infra.log.xtrid;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Slf4j
public class XTridContextFilter implements Filter {

    public static final String HEADER_TRID = "transactionid";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        String trid = verificaTRID(httpRequest.getHeader(HEADER_TRID));
        httpResponse.setHeader(HEADER_TRID, trid);

        MDC.put(HEADER_TRID, trid);
        MDC.put("transaction.id", "[transactionID - " + trid + "]");

        chain.doFilter(request, response);
    }

    private String verificaTRID(String trid) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(trid)) {
            trid = UUID.randomUUID().toString();
        } else {
            // Used encoding to avoid security vulnerabilities
            trid = java.net.URLEncoder.encode(trid, java.nio.charset.StandardCharsets.UTF_8.displayName());
        }
        return trid;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
