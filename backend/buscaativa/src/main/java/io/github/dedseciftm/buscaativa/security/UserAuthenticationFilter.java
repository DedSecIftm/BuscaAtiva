package io.github.dedseciftm.buscaativa.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import io.github.dedseciftm.buscaativa.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Autentica a requisição a partir do token JWT. Não é um {@code @Component} de propósito: o Spring Boot
 * registraria o filtro também fora da cadeia do Spring Security e a autenticação se perderia.
 * Ele é criado em {@link SecurityConfiguration}.
 *
 * <p>Diferente do tutorial, token ausente ou inválido não lança exceção (que viraria erro 500): a
 * requisição segue sem autenticação e o Spring Security responde 401 nas rotas protegidas.
 */
public class UserAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    public UserAuthenticationFilter(JwtTokenService jwtTokenService, UserRepository userRepository) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Verifica se o endpoint requer autenticação antes de processar a requisição
        if (checkIfEndpointIsNotPublic(request)) {
            String token = recoveryToken(request); // Recupera o token do cabeçalho Authorization
            if (token != null) {
                try {
                    String subject = jwtTokenService.getSubjectFromToken(token); // E-mail do usuário
                    userRepository.findByEmail(subject).ifPresent(user -> {
                        UserDetailsImpl userDetails = new UserDetailsImpl(user);
                        // Cria o objeto de autenticação e o coloca no contexto de segurança
                        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
                } catch (JWTVerificationException exception) {
                    SecurityContextHolder.clearContext(); // Token inválido ou expirado: segue sem autenticação
                }
            }
        }
        filterChain.doFilter(request, response); // Continua o processamento da requisição
    }

    // Recupera o token do cabeçalho Authorization da requisição
    private String recoveryToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }
        return null;
    }

    // Verifica se o endpoint requer autenticação antes de processar a requisição
    private boolean checkIfEndpointIsNotPublic(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return !Arrays.asList(SecurityConfiguration.ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED).contains(requestURI);
    }
}
