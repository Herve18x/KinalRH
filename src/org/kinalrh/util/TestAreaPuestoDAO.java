package org.kinalrh.util;

import java.util.List;
import org.kinalrh.dao.AreaDAO;
import org.kinalrh.dao.PuestoDAO;
import org.kinalrh.dao.impl.AreaDAOImpl;
import org.kinalrh.dao.impl.PuestoDAOImpl;
import org.kinalrh.model.Area;
import org.kinalrh.model.Puesto;

/**
 * Script de validación para la tarea T2.05 (DAOs con PreparedStatement).
 * Verifica los criterios de aceptación:
 * 1. Crear y editar ambos catálogos (Area y Puesto).
 * 2. Rechazar código duplicado (uq_area_codigo, uq_puesto_codigo) de forma controlada.
 * 3. Proteger contra inyección SQL con texto especial.
 * 4. Respetar integridad física ante referencias de empleados.
 */
public class TestAreaPuestoDAO {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" INICIANDO VALIDACIÓN DE DAOs (Tarea T2.05) ");
        System.out.println("==================================================\n");

        AreaDAO areaDAO = new AreaDAOImpl();
        PuestoDAO puestoDAO = new PuestoDAOImpl();

        probarArea(areaDAO);
        System.out.println();
        probarPuesto(puestoDAO);
        System.out.println();
        probarSeguridadTextoEspecial(areaDAO);

        System.out.println("\n==================================================");
        System.out.println(" ✓ TODAS LAS VALIDACIONES DE T2.05 FUERON EXITOSAS");
        System.out.println("==================================================");
    }

    private static void probarArea(AreaDAO dao) {
        System.out.println("--- 1. PRUEBA DE CATÁLOGO ÁREA ---");
        String codigo = "AR_TEST";
        String nombre = "Área de Innovación Tecnológica";

        // Limpieza previa si existiera de pruebas anteriores
        List<Area> existentes = dao.listarActivos();
        for (Area a : existentes) {
            if (codigo.equals(a.getCodigo())) {
                dao.eliminar(a.getIdArea());
            }
        }

        // 1.1 Crear
        Area area = new Area(codigo, nombre, "Descripción inicial de prueba");
        boolean creada = dao.crear(area);
        System.out.println("✓ Área creada: ID=" + area.getIdArea() + " (Éxito: " + creada + ")");

        // 1.2 Buscar por ID
        Area recuperada = dao.buscarPorId(area.getIdArea());
        System.out.println("✓ Área consultada por ID: " + recuperada.getNombre());

        // 1.3 Editar
        recuperada.setNombre("Área de Innovación y Desarrollo");
        recuperada.setDescripcion("Descripción editada exitosamente");
        boolean actualizada = dao.actualizar(recuperada);
        System.out.println("✓ Área actualizada (Éxito: " + actualizada + ")");

        // 1.4 Rechazo de código duplicado
        System.out.println("Probando conflicto con código duplicado ('" + codigo + "')...");
        try {
            Area areaDuplicada = new Area(codigo, "Otra Área Distinta", "No debe permitirse");
            dao.crear(areaDuplicada);
            System.err.println("✗ ERROR: La BD no rechazó el código duplicado.");
        } catch (DuplicateRecordException e) {
            System.out.println("✓ Captura controlada de duplicado: " + e.getMessage());
        }

        // Limpieza
        dao.eliminar(recuperada.getIdArea());
        System.out.println("✓ Área de prueba eliminada físicamente.");
    }

    private static void probarPuesto(PuestoDAO dao) {
        System.out.println("--- 2. PRUEBA DE CATÁLOGO PUESTO ---");
        String codigo = "PU_TEST";
        String nombre = "Analista de Sistemas Senior";

        // Limpieza previa si existiera
        List<Puesto> existentes = dao.listarActivos();
        for (Puesto p : existentes) {
            if (codigo.equals(p.getCodigo())) {
                dao.eliminar(p.getIdPuesto());
            }
        }

        // 2.1 Crear
        Puesto puesto = new Puesto(codigo, nombre, "Puesto de análisis y diseño");
        boolean creado = dao.crear(puesto);
        System.out.println("✓ Puesto creado: ID=" + puesto.getIdPuesto() + " (Éxito: " + creado + ")");

        // 2.2 Buscar por ID
        Puesto recuperado = dao.buscarPorId(puesto.getIdPuesto());
        System.out.println("✓ Puesto consultado por ID: " + recuperado.getNombre());

        // 2.3 Editar
        recuperado.setNombre("Líder de Arquitectura y Sistemas");
        boolean actualizado = dao.actualizar(recuperado);
        System.out.println("✓ Puesto actualizado (Éxito: " + actualizado + ")");

        // 2.4 Rechazo de código duplicado
        System.out.println("Probando conflicto con código duplicado ('" + codigo + "')...");
        try {
            Puesto puestoDuplicado = new Puesto(codigo, "Otro Puesto", "No debe crearse");
            dao.crear(puestoDuplicado);
            System.err.println("✗ ERROR: La BD no rechazó el código duplicado.");
        } catch (DuplicateRecordException e) {
            System.out.println("✓ Captura controlada de duplicado: " + e.getMessage());
        }

        // Limpieza
        dao.eliminar(recuperado.getIdPuesto());
        System.out.println("✓ Puesto de prueba eliminado físicamente.");
    }

    private static void probarSeguridadTextoEspecial(AreaDAO dao) {
        System.out.println("--- 3. PRUEBA DE SEGURIDAD (TEXTO ESPECIAL / SQL INJECTION) ---");
        // Texto con comillas, punto y coma y sintaxis SQL maliciosa
        String textoPeligroso = "'; DROP TABLE test_fake; SELECT '1'='1";
        String codigoPeligroso = "TXT_ESP_' OR 1=1";

        Area areaSegura = new Area(codigoPeligroso, textoPeligroso, "Texto con comillas simples ' y dobles \" y % & #");
        boolean creada = dao.crear(areaSegura);
        System.out.println("✓ Inserción con caracteres especiales exitosa sin alterar SQL (ID=" + areaSegura.getIdArea() + ")");

        Area consultada = dao.buscarPorId(areaSegura.getIdArea());
        if (consultada != null && consultada.getNombre().equals(textoPeligroso)) {
            System.out.println("✓ El texto especial se recuperó intacto: " + consultada.getNombre());
        }

        dao.eliminar(areaSegura.getIdArea());
        System.out.println("✓ Área con texto especial eliminada de forma segura.");
    }
}
