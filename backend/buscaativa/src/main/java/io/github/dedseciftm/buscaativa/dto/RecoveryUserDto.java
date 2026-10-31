package io.github.dedseciftm.buscaativa.dto;

import io.github.dedseciftm.buscaativa.model.RoleName;
import io.github.dedseciftm.buscaativa.model.User;

import java.util.List;

public record RecoveryUserDto(

        Long id,
        String email,
        List<RoleName> roles

) {

    public static RecoveryUserDto de(User user) {
        return new RecoveryUserDto(user.getId(), user.getEmail(),
                user.getRoles().stream().map(role -> role.getName()).toList());
    }
}
