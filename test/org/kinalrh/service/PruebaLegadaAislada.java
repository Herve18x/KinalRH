package org.kinalrh.service;

import java.nio.file.Path;
import org.kinalrh.PruebaBD;

/** Ejecuta pruebas históricas que usan Conexion exclusivamente sobre un esquema propio. */
public class PruebaLegadaAislada {
    public static void main(String[] args) throws Exception {
        Path raiz=Path.of(args[0]);
        if(!args[1].matches("org\\.kinalrh\\.service\\.[A-Za-z0-9]+Test")) throw new IllegalArgumentException("Clase de prueba inválida.");
        try(PruebaBD bd=new PruebaBD(raiz,true)) {
            bd.semilla(raiz);
            Class.forName(args[1]).getMethod("main",String[].class).invoke(null,(Object)new String[0]);
        }
    }
}
