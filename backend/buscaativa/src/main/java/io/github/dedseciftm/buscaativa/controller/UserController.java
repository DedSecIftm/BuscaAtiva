package io.github.dedseciftm.buscaativa.controller;

import io.github.dedseciftm.buscaativa.dto.CreateUserDto;
import io.github.dedseciftm.buscaativa.dto.LoginUserDto;
import io.github.dedseciftm.buscaativa.dto.RecoveryJwtTokenDto;
import io.github.dedseciftm.buscaativa.dto.RecoveryUserDto;
import io.github.dedseciftm.buscaativa.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDto> authenticateUser(@Valid @RequestBody LoginUserDto loginUserDto) {
        RecoveryJwtTokenDto token = userService.authenticateUser(loginUserDto);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        userService.createUser(createUserDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // Dados do dono do token; o frontend pode usar para validar a sessão salva
    @GetMapping("/me")
    public ResponseEntity<RecoveryUserDto> getMe(Authentication authentication) {
        return new ResponseEntity<>(userService.getUser(authentication.getName()), HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<String> getAuthenticationTest() {
        return new ResponseEntity<>("Autenticado com sucesso", HttpStatus.OK);
    }

    @GetMapping("/test/user")
    public ResponseEntity<String> getUserAuthenticationTest() {
        return new ResponseEntity<>("Usuário autenticado com sucesso", HttpStatus.OK);
    }

    @GetMapping("/test/administrator")
    public ResponseEntity<String> getAdminAuthenticationTest() {
        return new ResponseEntity<>("Administrador autenticado com sucesso", HttpStatus.OK);
    }
}
