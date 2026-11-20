package io.github.dedseciftm.buscaativa.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.dedseciftm.buscaativa.model.Role;
import io.github.dedseciftm.buscaativa.model.RoleName;
import io.github.dedseciftm.buscaativa.model.User;
import io.github.dedseciftm.buscaativa.repository.RoleRepository;
import io.github.dedseciftm.buscaativa.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTests {

    private static final String EMAIL_USER = "professora@escola.com";
    private static final String EMAIL_ADMIN = "diretor@escola.com";
    private static final String SENHA = "senha-segura-123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void criaUsuarios() {
        criaUsuario(EMAIL_USER, RoleName.ROLE_USER);
        criaUsuario(EMAIL_ADMIN, RoleName.ROLE_ADMIN);
    }

    private void criaUsuario(String email, RoleName nome) {
        Role papel = roleRepository.findByName(nome)
                .orElseGet(() -> roleRepository.save(Role.builder().name(nome).build()));
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
        userRepository.saveAndFlush(User.builder().nome("Pessoa de Teste").email(email).password(passwordEncoder.encode(SENHA))
                .roles(List.of(papel)).build());
    }

    private String login(String email, String senha) throws Exception {
        var corpo = mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"" + email + "\", \"password\": \"" + senha + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return corpo.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    private String bearer(String email) throws Exception {
        return "Bearer " + login(email, SENHA);
    }

    @Test
    void loginCorretoPermiteAcessarRotaProtegida() throws Exception {
        mvc.perform(get("/api/regras").header(HttpHeaders.AUTHORIZATION, bearer(EMAIL_USER)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/users/test").header(HttpHeaders.AUTHORIZATION, bearer(EMAIL_USER)))
                .andExpect(status().isOk());
    }

    @Test
    void meDevolveOsDadosDoUsuarioDoToken() throws Exception {
        mvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(EMAIL_USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pessoa de Teste"))
                .andExpect(jsonPath("$.email").value(EMAIL_USER))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void loginAceitaEmailComMaiusculas() throws Exception {
        login("  Professora@Escola.com ", SENHA);
    }

    @Test
    void senhaErradaRetorna401() throws Exception {
        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"" + EMAIL_USER + "\", \"password\": \"errada\" }"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
    }

    @Test
    void usuarioInexistenteRetornaAMesmaMensagemDeSenhaErrada() throws Exception {
        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"ninguem@escola.com\", \"password\": \"qualquer-coisa\" }"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
    }

    @Test
    void loginSemCamposRetorna400() throws Exception {
        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.email").isNotEmpty())
                .andExpect(jsonPath("$.erros.password").isNotEmpty());
    }

    @Test
    void rotaProtegidaSemTokenRetorna401() throws Exception {
        mvc.perform(get("/api/regras"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/api/regras").header(HttpHeaders.AUTHORIZATION, "Bearer isto.nao.eum.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUsuarioRemovidoRetorna401() throws Exception {
        String token = bearer(EMAIL_USER);
        userRepository.delete(userRepository.findByEmail(EMAIL_USER).orElseThrow());
        userRepository.flush();

        mvc.perform(get("/api/regras").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void papelUserAcessaEndpointDeUserMasNaoDeAdmin() throws Exception {
        String token = bearer(EMAIL_USER);

        mvc.perform(get("/api/users/test/user").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/users/test/administrator").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden());
    }

    @Test
    void papelAdminAcessaEndpointDeAdmin() throws Exception {
        mvc.perform(get("/api/users/test/administrator").header(HttpHeaders.AUTHORIZATION, bearer(EMAIL_ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioComumNaoPodeCriarUsuario() throws Exception {
        mvc.perform(post("/api/users").header(HttpHeaders.AUTHORIZATION, bearer(EMAIL_USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nome\": \"Pessoa Nova\", \"email\": \"novo@escola.com\", \"password\": \"outra-senha-123\", \"role\": \"ROLE_USER\" }"))
                .andExpect(status().isForbidden());
    }

    @Test
    void criarUsuarioSemTokenRetorna401() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nome\": \"Pessoa Nova\", \"email\": \"novo@escola.com\", \"password\": \"outra-senha-123\", \"role\": \"ROLE_ADMIN\" }"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCriaUsuarioEOutroLoginFunciona() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nome\": \"Pessoa Nova\", \"email\": \"Novo@Escola.com\", \"password\": \"outra-senha-123\", \"role\": \"ROLE_USER\" }"))
                .andExpect(status().isCreated());

        login("novo@escola.com", "outra-senha-123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void criarUsuarioGuardaONomeSemEspacosNasPontas() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nome\": \"  Pessoa Nova  \", \"email\": \"novo@escola.com\", \"password\": \"outra-senha-123\", \"role\": \"ROLE_USER\" }"))
                .andExpect(status().isCreated());

        assertEquals("Pessoa Nova", userRepository.findByEmail("novo@escola.com").orElseThrow().getNome());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void criarUsuarioSemNomeRetorna400() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"novo@escola.com\", \"password\": \"outra-senha-123\", \"role\": \"ROLE_USER\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").value("Informe o nome."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void criarEmailRepetidoRetorna409() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nome\": \"Pessoa Nova\", \"email\": \"" + EMAIL_USER + "\", \"password\": \"outra-senha-123\", \"role\": \"ROLE_USER\" }"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void senhaCurtaRetorna400() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nome\": \"Pessoa Nova\", \"email\": \"novo@escola.com\", \"password\": \"123\", \"role\": \"ROLE_USER\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.password").isNotEmpty());
    }
}
