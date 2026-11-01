package io.github.dedseciftm.buscaativa.config;

import io.github.dedseciftm.buscaativa.model.Role;
import io.github.dedseciftm.buscaativa.model.RoleName;
import io.github.dedseciftm.buscaativa.model.User;
import io.github.dedseciftm.buscaativa.repository.RoleRepository;
import io.github.dedseciftm.buscaativa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Na subida da aplicação garante os papéis e, se ADMIN_EMAIL e ADMIN_SENHA estiverem definidos e o
 * e-mail ainda não existir, cria o primeiro administrador (já que só admin cadastra usuários).
 */
@Component
public class InicializadorDeDados implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminSenha;

    public InicializadorDeDados(RoleRepository roleRepository, UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${admin.email:}") String adminEmail,
                                @Value("${admin.senha:}") String adminSenha) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminSenha = adminSenha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role papelAdmin = null;
        for (RoleName nome : RoleName.values()) {
            Role papel = roleRepository.findByName(nome)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(nome).build()));
            if (nome == RoleName.ROLE_ADMIN) {
                papelAdmin = papel;
            }
        }

        if (!adminEmail.isBlank() && !adminSenha.isBlank() && !userRepository.existsByEmail(adminEmail)) {
            userRepository.save(User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminSenha))
                    .roles(List.of(papelAdmin))
                    .build());
        }
    }
}
