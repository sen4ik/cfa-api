package com.sen4ik.cfaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CfaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfaApiApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}

//below is needed when packaging to war
//@SpringBootApplication
//public class CfaApiApplication extends SpringBootServletInitializer {
//
//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		return application.sources(CfaApiApplication.class);
//	}
//
//	public static void main(String[] args) {
//		SpringApplication.run(CfaApiApplication.class, args);
//	}
//
//}

//@SpringBootApplication
//public class CfaApiApplication extends SpringBootServletInitializer {
//
//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//		return configureApplication(builder);
//	}
//
//	public static void main(String[] args) {
//		configureApplication(new SpringApplicationBuilder()).run(args);
//	}
//
//	private static SpringApplicationBuilder configureApplication(SpringApplicationBuilder builder) {
//		return builder.sources(CfaApiApplication.class).bannerMode(Banner.Mode.OFF);
//	}
//
//}
