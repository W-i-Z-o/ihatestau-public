package de.tinf15b4.ihatestau.rest.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Base64;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.persistence.User;
import de.tinf15b4.ihatestau.util.PasswordsUtil;

@Provider
@PreMatching
public class AuthenticationFilter implements ContainerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

	@Inject
	private PersistenceBean persistenceBean;

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		SecurityContext newSecurityContext = checkProvidedCredentials(context);
		if (newSecurityContext != null)
			context.setSecurityContext(newSecurityContext);
	}

	private SecurityContext checkProvidedCredentials(ContainerRequestContext context) {
		String authHeader = context.getHeaderString("authorization");
		if (authHeader == null)
			return null;

		if (!authHeader.startsWith("Basic ") && !authHeader.startsWith("basic "))
			return null;

		String b64str = authHeader.substring("Basic ".length());
		String decoded;
		try {
			decoded = new String(Base64.getDecoder().decode(b64str), "UTF-8");
		} catch (UnsupportedEncodingException|IllegalArgumentException e) {
			return null;
		}

		String[] nameAndPw = decoded.split(":");
		if (nameAndPw.length != 2)
			return null;

		User userEntity = persistenceBean.selectById(User.class, nameAndPw[0]);
		if (userEntity == null)
			return null;

		if (!PasswordsUtil.isExpectedPassword(nameAndPw[1].toCharArray(), userEntity.getSalt(), userEntity.getPassword()))
			return null;

		return new SecurityContext() {
			@Override
			public Principal getUserPrincipal() {
				return new Principal() {
					@Override
					public String getName() {
						return nameAndPw[0];
					}
				};
			}

			@Override
			public boolean isUserInRole(String s) {
				return true; // FIXME!
			}

			@Override
			public boolean isSecure() {
				return false;
			}

			@Override
			public String getAuthenticationScheme() {
				return SecurityContext.BASIC_AUTH;
			}
		};
	}
}
