package io.github.yasmramos.tailwindfx.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GenerateCssMojoTest {

    @TempDir
    Path tempDir;

    @Test
    public void testMojoExecution() throws Exception {
        GenerateCssMojo mojo = new GenerateCssMojo();
        
        // Configurar directorios temporales
        File outputDir = tempDir.resolve("output").toFile();
        mojo.outputDirectory = outputDir;
        
        // Ejecutar el mojo
        assertDoesNotThrow(() -> mojo.execute());
        
        // Verificar que se creó el archivo CSS
        File cssFile = new File(outputDir, "tailwind.css");
        assertTrue(cssFile.exists(), "El archivo tailwind.css debería existir");
    }

    @Test
    public void testOutputDirectoryCreation() throws Exception {
        GenerateCssMojo mojo = new GenerateCssMojo();
        
        File outputDir = tempDir.resolve("new-output").toFile();
        mojo.outputDirectory = outputDir;
        
        mojo.execute();
        
        assertTrue(outputDir.exists(), "El directorio de salida debería crearse");
        assertTrue(outputDir.isDirectory(), "Debería ser un directorio");
    }
}
