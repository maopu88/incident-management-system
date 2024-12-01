package com.maopu.incident.management.filter;


import com.maopu.incident.management.utils.RequestIdMdcUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@WebFilter(filterName = "LogFilter", urlPatterns = "/*")
@Component
public class LogFilter implements Filter {

    public static final String URL_SMOKE = "/smoke";

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse) res;
        final HttpServletRequest reqs = (HttpServletRequest) req;
        String requestId = reqs.getHeader(RequestIdMdcUtil.REQUEST_ID);

        if (isNotPrintRequestId(reqs)) {
            return;
        }

        if (StringUtils.isBlank(requestId)) {
            String newRequestId = RequestIdMdcUtil.getRequestId();
            MDC.put(RequestIdMdcUtil.REQUEST_ID, newRequestId);
            response.addHeader(RequestIdMdcUtil.REQUEST_ID, newRequestId);
        } else {
            MDC.put(RequestIdMdcUtil.REQUEST_ID, requestId);
        }

        log.info("LogFilter use request_id:{}", MDC.get(RequestIdMdcUtil.REQUEST_ID));
        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            log.error("LogFilter use request_id:{},failed", MDC.get(RequestIdMdcUtil.REQUEST_ID));
        } finally {
            MDC.remove(RequestIdMdcUtil.REQUEST_ID);
        }
    }

    private boolean isNotPrintRequestId(HttpServletRequest reqs) {
        return StringUtils.isNotBlank(reqs.getRequestURI()) && (reqs.getRequestURI().contains(URL_SMOKE));
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        log.info("LogFilter initialize");
    }

    @Override
    public void destroy() {
        MDC.remove(RequestIdMdcUtil.REQUEST_ID);
    }
}