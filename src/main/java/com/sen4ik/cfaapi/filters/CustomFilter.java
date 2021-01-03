package com.sen4ik.cfaapi.filters;

import com.sen4ik.cfaapi.exceptions.InvalidJwtAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class CustomFilter implements Filter {

    // This is probably some bad looking, not a good practice implementation, but this is the best I could do.
    // This filter is needed basically to catch InvalidJwtAuthenticationException
    // exception and to replace the html 500 internal error page with json.

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        /*
        log.info(
                "Logging Request  {} : {}", req.getMethod(),
                req.getRequestURI());
        log.info(
                "Logging Response :{}",
                res.getContentType());
        */

        try {
            chain.doFilter(request, response);
        }
        catch(Exception e){
            if(e instanceof InvalidJwtAuthenticationException){
                JSONObject entity = new JSONObject();
                entity.put("status", "Error");
                entity.put("message", e.getLocalizedMessage().trim());
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.setStatus(401);
                res.getWriter().write(entity.toString());
            }
        }

    }

}
