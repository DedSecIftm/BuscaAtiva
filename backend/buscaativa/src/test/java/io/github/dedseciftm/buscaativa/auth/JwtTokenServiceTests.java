package io.github.dedseciftm.buscaativa.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.jwt.exceptions.JWTVerificationException;
import io.github.dedseciftm.buscaativa.model.User;
import io.github.dedseciftm.buscaativa.security.JwtTokenService;
import io.github.dedseciftm.buscaativa.security.UserDetailsImpl;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTests {

    private static final String CHAVE = "uma-chave-de-teste-com-mais-de-32-caracteres";
    private static final UserDetailsImpl USUARIO = new UserDetailsImpl(
            User.builder().email("a@b.com").password("x").roles(List.of()).build());

    @Test
    void tokenGeradoEValidadoComOMesmoEmail() {
        var servico = new JwtTokenService(CHAVE, Duration.ofHours(1));

        assertEquals("a@b.com", servico.getSubjectFromToken(servico.generateToken(USUARIO)));
    }

    @Test
    void tokenExpiradoEInvalido() {
        var servico = new JwtTokenService(CHAVE, Duration.ofSeconds(-10));
        String token = servico.generateToken(USUARIO);

        assertThrows(JWTVerificationException.class, () -> servico.getSubjectFromToken(token));
    }

    @Test
    void tokenAssinadoComOutraChaveEInvalido() {
        String token = new JwtTokenService(CHAVE + "-outra", Duration.ofHours(1)).generateToken(USUARIO);
        var servico = new JwtTokenService(CHAVE, Duration.ofHours(1));

        assertThrows(JWTVerificationException.class, () -> servico.getSubjectFromToken(token));
    }

    @Test
    void chaveCurtaEhRecusada() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenService("curta", Duration.ofHours(1)));
    }
}
