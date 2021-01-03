package com.sen4ik.cfaapi.configs.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	// @Autowired
	// private HandlerExceptionResolver resolver;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
		log.info("Jwt authentication failed:" + authException);

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Jwt authentication failed");
		// resolver.resolveException(request, response, null, authException);
	}

}
