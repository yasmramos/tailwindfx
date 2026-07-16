package io.github.yasmramos.tailwindfx.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TailwindCssMojoTest {

    @TempDir
    Path tempDir;

    @Test
    public void testMojoExecution() throws Exception {
        TailwindCssMojo mojo = new TailwindCssMojo();
        
        // Configurar directorios temporales usando reflexión (como hace Maven)
        File sourceDir = tempDir.resolve("src").toFile();
        sourceDir.mkdirs();
        File outputDir = tempDir.resolve("output").toFile();
        
        java.lang.reflect.Field sourceField = TailwindCssMojo.class.getDeclaredField("sourceDirectory");
        sourceField.setAccessible(true);
        sourceField.set(mojo, sourceDir);
        
        java.lang.reflect.Field outputField = TailwindCssMojo.class.getDeclaredField("outputDirectory");
        outputField.setAccessible(true);
        outputField.set(mojo, outputDir);

        // Ejecutar el mojo
        assertDoesNotThrow(() -> mojo.execute());

        // Verificar que se creó el archivo CSS
        File cssFile = new File(outputDir, "tailwindfx-generated.css");
        assertTrue(cssFile.exists(), "El archivo CSS debería existir");
    }

    @Test
    public void testOutputDirectoryCreation() throws Exception {
        TailwindCssMojo mojo = new TailwindCssMojo();
        
        File sourceDir = tempDir.resolve("src").toFile();
        sourceDir.mkdirs();
        File outputDir = tempDir.resolve("new-output").toFile();
        
        java.lang.reflect.Field sourceField = TailwindCssMojo.class.getDeclaredField("sourceDirectory");
        sourceField.setAccessible(true);
        sourceField.set(mojo, sourceDir);
        
        java.lang.reflect.Field outputField = TailwindCssMojo.class.getDeclaredField("outputDirectory");
        outputField.setAccessible(true);
        outputField.set(mojo, outputDir);

        mojo.execute();

        assertTrue(outputDir.exists(), "El directorio de salida debería crearse");
        assertTrue(outputDir.isDirectory(), "Debería ser un directorio");
    }
}
