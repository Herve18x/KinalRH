package org.kinalrh.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * Utilidades para componentes reutilizables (T2.07).
 */
public class ComponenteUtil {

    /**
     * Carga un ComboBox con los roles del sistema de forma estandarizada.
     * Puede ser reutilizado en cualquier formulario que requiera asignar roles.
     * 
     * @param combo El ComboBox a configurar.
     */
    public static void configurarSelectorRol(ComboBox<String> combo) {
        ObservableList<String> roles = FXCollections.observableArrayList(
            "ADMIN",
            "ENCARGADO",
            "GERENTEAREA",
            "GERENTEGENERAL",
            "JEFE",
            "VISOR"
        );
        combo.setItems(roles);
    }
}
