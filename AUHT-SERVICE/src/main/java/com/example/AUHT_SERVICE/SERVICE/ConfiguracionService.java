package com.example.AUHT_SERVICE.SERVICE;



import com.example.AUHT_SERVICE.MODEL.CategoriaConfig;
import com.example.AUHT_SERVICE.MODEL.ConfiguracionModel;

import java.util.List;
import java.util.Map;

public interface ConfiguracionService {

    /** Obtiene todas las configuraciones agrupadas por categoría. */
    Map<String, List<ConfiguracionModel>> getTodasAgrupadas();

    /** Obtiene configuraciones de una categoría. */
    List<ConfiguracionModel> getPorCategoria(CategoriaConfig categoria);

    /** Obtiene el valor de una clave. */
    String getValor(String clave);

    /** Obtiene el valor como número. */
    double getValorNumerico(String clave, double defaultVal);

    /** Obtiene el valor como booleano. */
    boolean getValorBooleano(String clave, boolean defaultVal);

    /** Actualiza una clave. Solo claves editables. */
    ConfiguracionModel actualizar(String clave, String valor, String emailAdmin);

    /** Actualiza múltiples claves en bloque. */
    List<ConfiguracionModel> actualizarBloque(Map<String, String> valores, String emailAdmin);
}
