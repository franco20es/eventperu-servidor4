package com.example.AUHT_SERVICE.REPOSITORY;


import com.example.AUHT_SERVICE.MODEL.CategoriaConfig;
import com.example.AUHT_SERVICE.MODEL.ConfiguracionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<ConfiguracionModel, String> {

    List<ConfiguracionModel> findByCategoriaOrderByClave(CategoriaConfig categoria);

    Optional<ConfiguracionModel> findByClave(String clave);
}
