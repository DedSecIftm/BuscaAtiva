package io.github.dedseciftm.buscaativa.service;

import io.github.dedseciftm.buscaativa.dto.CreateUserDto;
import io.github.dedseciftm.buscaativa.dto.LoginUserDto;
import io.github.dedseciftm.buscaativa.dto.RecoveryJwtTokenDto;
import io.github.dedseciftm.buscaativa.dto.RecoveryUserDto;
import io.github.dedseciftm.buscaativa.model.Role;
import io.github.dedseciftm.buscaativa.model.User;
import io.github.dedseciftm.buscaativa.repository.RoleRepository;
import io.github.dedseciftm.buscaativa.repository.UserRepository;
import io.github.dedseciftm.buscaativa.security.JwtTokenService;
import io.github.dedseciftm.buscaativa.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService,
                       UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método responsável por autenticar um usuário e retornar um token JWT
    public RecoveryJwtTokenDto authenticateUser(LoginUserDto loginUserDto) {
        // Cria um objeto de autenticação com o email e a senha do usuário
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                normalize(loginUserDto.email()), loginUserDto.password());

        // Autentica o usuário com as credenciais fornecidas (credenciais erradas lançam AuthenticationException)
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        // Obtém o objeto UserDetails do usuário autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Gera um token JWT para o usuário autenticado
        return new RecoveryJwtTokenDto(jwtTokenService.generateToken(userDetails));
    }

    // Método responsável por criar um usuário
    @Transactional
    public void createUser(CreateUserDto createUserDto) {
        String email = normalize(createUserDto.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com este e-mail.");
        }

        // Reaproveita o papel já cadastrado (o tutorial cria um papel novo a cada usuário)
        Role role = roleRepository.findByName(createUserDto.role())
                .orElseGet(() -> roleRepository.save(Role.builder().name(createUserDto.role()).build()));

        User newUser = User.builder()
                .email(email)
                // Codifica a senha do usuário com o algoritmo bcrypt
                .password(passwordEncoder.encode(createUserDto.password()))
                // Atribui ao usuário uma permissão específica
                .roles(List.of(role))
                .build();

        // Salva o novo usuário no banco de dados
        userRepository.save(newUser);
    }

    public RecoveryUserDto getUser(String email) {
        return RecoveryUserDto.de(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}