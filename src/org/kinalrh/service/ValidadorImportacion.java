package org.kinalrh.service;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import org.kinalrh.model.FilaImportacion;
import org.kinalrh.model.FilaImportacion.Error;

public class ValidadorImportacion {
    public static final List<String> ENCABEZADOS = List.of("dpi", "primer_nombre", "primer_apellido",
            "estado_codigo", "area_codigo", "puesto_codigo", "fecha_ingreso_kinal");

    public List<FilaImportacion> validar(List<String> encabezados, List<FilaImportacion> filas, Connection conn) throws SQLException {
        Map<String,Long> estados = catalogo(conn,"estado_empleado","id_estado_empleado");
        Map<String,Long> areas = catalogo(conn,"area","id_area");
        Map<String,Long> puestos = catalogo(conn,"puesto","id_puesto");
        Map<String,Integer> repetidos = new HashMap<>();
        for (FilaImportacion f : filas) if (!f.valor(0).isEmpty()) repetidos.merge(clave(f.valor(0)),1,Integer::sum);
        List<FilaImportacion> resultado = new ArrayList<>();
        List<FilaImportacion> entradas = filas.isEmpty() ? List.of(new FilaImportacion(1,List.of())) : filas;
        for (FilaImportacion fila : entradas) {
            List<Error> errores = new ArrayList<>(fila.getErrores());
            int n = fila.getNumeroFila();
            if (!ENCABEZADOS.equals(encabezados)) errores.add(new Error(1,"encabezados","Se requieren exactamente los siete encabezados de la plantilla, en su orden."));
            if (filas.isEmpty()) errores.add(new Error(1,"filas","El archivo no contiene filas de datos."));
            for (int i=0;i<4;i++) if (fila.valor(i).isBlank()) errores.add(new Error(n,ENCABEZADOS.get(i),"Campo obligatorio."));
            if (fila.valor(0).length()>20) errores.add(new Error(n,"dpi","Máximo 20 caracteres."));
            for (int i=1;i<=2;i++) if (fila.valor(i).length()>80) errores.add(new Error(n,ENCABEZADOS.get(i),"Máximo 80 caracteres."));
            if (repetidos.getOrDefault(clave(fila.valor(0)),0)>1) errores.add(new Error(n,"dpi","DPI repetido dentro del archivo."));
            if (!fila.valor(6).isEmpty()) {
                try {
                    if (!fila.valor(6).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw new DateTimeParseException("Formato", "", 0);
                    LocalDate fecha = LocalDate.parse(fila.valor(6));
                    if (fecha.getYear()<1000) throw new DateTimeParseException("Rango", "", 0);
                } catch (DateTimeParseException ex) { errores.add(new Error(n,"fecha_ingreso_kinal","Fecha ISO inválida; use AAAA-MM-DD entre 1000 y 9999.")); }
            }
            Long estado = referencia(fila,3,estados,errores);
            Long area = referencia(fila,4,areas,errores);
            Long puesto = referencia(fila,5,puestos,errores);
            resultado.add(new FilaImportacion(n,fila.getValores(),errores,
                    errores.isEmpty()?FilaImportacion.Accion.NUEVO:FilaImportacion.Accion.ERROR,null,estado,area,puesto));
        }
        return List.copyOf(resultado);
    }
    private Long referencia(FilaImportacion f,int columna,Map<String,Long> catalogo,List<Error> errores) {
        if (f.valor(columna).isEmpty()) return null;
        Long id = catalogo.get(clave(f.valor(columna)));
        if (id==null) errores.add(new Error(f.getNumeroFila(),ENCABEZADOS.get(columna),"Código desconocido o inactivo."));
        return id;
    }
    private Map<String,Long> catalogo(Connection conn,String tabla,String id) throws SQLException {
        Map<String,Long> mapa = new HashMap<>();
        try (PreparedStatement s=conn.prepareStatement("SELECT " + id + ", codigo FROM " + tabla + " WHERE activo=TRUE"); ResultSet r=s.executeQuery()) {
            while(r.next()) mapa.put(clave(r.getString("codigo")),r.getLong(id));
        }
        return mapa;
    }
    private String clave(String valor) {
        return java.text.Normalizer.normalize(valor,java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}","").toUpperCase(Locale.ROOT);
    }
}
