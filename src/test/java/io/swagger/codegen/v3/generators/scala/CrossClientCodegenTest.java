package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.junit.rules.TemporaryFolder;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CrossClientCodegenTest extends AbstractCodegenTest {
    private TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testPetstore() throws Exception {

        java.nio.file.Path path = java.nio.file.Paths.get("target/client-petstore");
        java.nio.file.Files.createDirectories(path);
        final File output = path.toFile();
//        folder.create();
//        final File output = folder.getRoot();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("scala-cross-client")
            .setSkipOverwrite(false)
            .setApiPackage("the.api.packg")
            .setModelNamePrefix("Model")
            .setInvokerPackage("in.voker.pckg")
            .setInputSpecURL("src/test/resources/3_0_0/petstore.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();

        // the generator should simply complete w/o exception
        List<File> result = new DefaultGenerator().opts(clientOptInput).generate();
        Set<String> generatedNames = result.stream().map(f -> f.getName()).collect(Collectors.toSet());
        Set<String> expectedFiles = new HashSet<>();
        expectedFiles.add(".gitignore");
//        Assert.assertEquals(generatedNames, expectedFiles);

    }

}