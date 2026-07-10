package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final RepositoryUsuario userRepository;

    private static final int MAX_INTENTOS = 5;
    private static final int TIEMPO_BLOQUEO_MINUTOS = 15;

    @Override
    @Transactional // Importante: actualiza el contador y el estado de bloqueo
    public void registrarIntentoFallido(String email) {
        userRepository.findByEmail(email).ifPresentOrElse(usuario -> {
            int nuevosIntentos = (usuario.getIntentosFallidos() != null ? usuario.getIntentosFallidos() : 0) + 1;
            usuario.setIntentosFallidos(nuevosIntentos);

            log.warn(" INTENTO FALLIDO | Email: {} | {}/{}", email, nuevosIntentos, MAX_INTENTOS);

            if (nuevosIntentos >= MAX_INTENTOS) {
                bloquearUsuario(usuario);
            }

            userRepository.save(usuario);
        }, () -> log.warn(" Intento de login fallido para email no registrado: {}", email));
    }

    @Override
    @Transactional // Necesario porque puede disparar un "auto-desbloqueo" (escritura)
    public boolean estaCuentaBloqueada(String email) {
        return userRepository.findByEmail(email)
                .map(usuario -> {
                    if (!usuario.isBloqueado()) return false;

                    // Verificar si el tiempo de bloqueo ya pasó
                    if (usuario.getBloqueadoHasta() != null && LocalDateTime.now().isAfter(usuario.getBloqueadoHasta())) {
                        log.info(" AUTO-DESBLOQUEO | Tiempo expirado para: {}", email);
                        limpiarIntentosYDesbloquear(usuario);
                        return false;
                    }

                    log.warn(" ACCESO DENEGADO | Cuenta bloqueada: {}", email);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void desbloquearCuenta(String email) {
        userRepository.findByEmail(email).ifPresent(usuario -> {
            limpiarIntentosYDesbloquear(usuario);
            log.info(" DESBLOQUEO MANUAL | Realizado por sistema/admin para: {}", email);
        });
    }

    // --- Métodos privados para reutilizar lógica ---

    private void bloquearUsuario(ModelUsuario usuario) {
        usuario.setBloqueado(true);
        usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(TIEMPO_BLOQUEO_MINUTOS));
        log.error(" CUENTA BLOQUEADA | Usuario: {} hasta {}", usuario.getEmail(), usuario.getBloqueadoHasta());
    }

    private void limpiarIntentosYDesbloquear(ModelUsuario usuario) {
        usuario.setBloqueado(false);
        usuario.setBloqueadoHasta(null);
        usuario.setIntentosFallidos(0);
        userRepository.save(usuario);
    }
}