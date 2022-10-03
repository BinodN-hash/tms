package com.tms.security.filter;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Order(1)
public class RequestThrottleFilter extends OncePerRequestFilter {

    private int MAX_REQUESTS_PER_SECOND = 500; // or whatever you want it to be

    private LoadingCache<String, Integer> requestCountsPerIpAddress;
    private LoadingCache<String, String> ipBlocked;

    public RequestThrottleFilter() {
        super();
        requestCountsPerIpAddress = CacheBuilder.newBuilder().
                expireAfterWrite(10, TimeUnit.SECONDS).build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });

        ipBlocked = CacheBuilder.newBuilder().expireAfterWrite(4, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        return "";
                    }
                });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIpAddress = getClientIP(request);
        if (isIpBlocked(clientIpAddress)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Your Ip is Blocked");
            return;
        }
        if (isMaximumRequestsPerSecondExceeded(clientIpAddress)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMaximumRequestsPerSecondExceeded(String clientIpAddress) {
        int requests = 0;
        try {
            requests = requestCountsPerIpAddress.get(clientIpAddress);
            if (requests > MAX_REQUESTS_PER_SECOND) {
                log.info("BLOCK REQUEST : " + requests);
                log.info("BLOCK CLIENT IP  : " + clientIpAddress);
                // requestCountsPerIpAddress.put(clientIpAddress, requests);
                ipBlocked.put(clientIpAddress, clientIpAddress);
                return true;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            requests = 0;
        }
        requests++;
        requestCountsPerIpAddress.put(clientIpAddress, requests);
        return false;
    }

    private boolean isIpBlocked(String clientIpAddress) {
        try {
            if (StringUtils.isNotEmpty(ipBlocked.get(clientIpAddress))) {
                return true;
            }
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

}
