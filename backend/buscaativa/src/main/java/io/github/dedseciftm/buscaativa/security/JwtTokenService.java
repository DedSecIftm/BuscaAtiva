package io.github.dedseciftm.buscaativa.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtTokenService {

    private static final String ISSUER = "buscaativa"; // Emissor do token

    private final Algorithm algorithm;
    private final Duration validity;

    // A chave vem da propriedade chave.secret-key (variável de ambiente SECRET_KEY), nunca fixa no código.
    public JwtTokenService(@Value("${chave.secret-key}") String secretKey,
                           @Value("${jwt.expiracao:PT4H}") Duration validity) {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException("A chave secreta do JWT (SECRET_KEY) precisa ter pelo menos 32 caracteres.");
        }
        this.algorithm = Algorithm.HMAC256(secretKey);
        this.validity = validity;
    }

    public String generateToken(UserDetailsImpl user) {
        try {
            Instant now = Instant.now();
            return JWT.create()
                    .withIssuer(ISSUER) // Define o emissor do token
                    .withIssuedAt(now) // Define a data de emissão do token
                    .withExpiresAt(now.plus(validity)) // Define a data de expiração do token
                    .withSubject(user.getUsername()) // Define o assunto do token (o e-mail do usuário)
                    .sign(algorithm); // Assina o token usando o algoritmo especificado
        } catch (JWTCreationException exception) {
            throw new JWTCreationException("Erro ao gerar token.", exception);
        }
    }

    public String getSubjectFromToken(String token) {
        try {
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token) // Verifica assinatura, emissor e validade do token
                    .getSubject(); // Obtém o assunto (o e-mail do usuário)
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Token inválido ou expirado.");
        }
    }
}
