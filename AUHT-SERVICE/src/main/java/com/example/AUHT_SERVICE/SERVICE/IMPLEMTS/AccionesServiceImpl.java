package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.AccionesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccionesServiceImpl implements AccionesService {

    private final RepositoryUsuario userRepository;

    @Override
    @Transactional
    public void desactivarCuenta(String email) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        user.setActivo(false);
        userRepository.save(user);
        log.info("Cuenta desactivada en auth-service: {}", email);
    }

    @Override
    @Transactional
    public void reactivarCuenta(String email) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        user.setActivo(true);
        userRepository.save(user);
        log.info("Cuenta reactivada en auth-service: {}", email);
    }
}