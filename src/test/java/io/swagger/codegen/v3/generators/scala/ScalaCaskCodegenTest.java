package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScalaCaskCodegenTest extends AbstractCodegenTest {
    private TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testPetstore() throws Exception {
//        folder.create();
//        final File output = folder.getRoot();
        final File output = Files.createDirectories(Paths.get("target/petstore-server-cask")).toFile();

        final CodegenConfigurator configurator = new CodegenConfigurator()
            .setLang("scala-cask")
            .setSkipOverwrite(false)
            .setApiPackage("the.api.packg")
//            .setModelNamePrefix("")
            .setInvokerPackage("in.voker.pckg")
            .setInputSpecURL("src/test/resources/3_0_0/petstore.yaml")
            .setOutputDir(output.getAbsolutePath());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();

        // the generator should simply complete w/o exception
        List<File> result = new DefaultGenerator().opts(clientOptInput).generate();
        Set<String> generatedNames = result.stream().map(f -> f.getName()).collect(Collectors.toSet());
        Assert.assertTrue(generatedNames.contains("DefaultService.scala"));
        Assert.assertTrue(generatedNames.contains(".gitignore"));
        Assert.assertTrue(generatedNames.contains("ServiceResponse.scala"));
        Assert.assertTrue(generatedNames.contains("Category.scala"));
        Assert.assertTrue(generatedNames.contains("App.scala"));
        Assert.assertTrue(generatedNames.contains("User.scala"));
        Assert.assertTrue(generatedNames.contains("DefaultRoutes.scala"));
        Assert.assertTrue(generatedNames.contains("Pet.scala"));
        Assert.assertTrue(generatedNames.contains("plugins.sbt"));
        Assert.assertTrue(generatedNames.contains("build.properties"));
        Assert.assertTrue(generatedNames.contains("PetRoutes.scala"));
        Assert.assertTrue(generatedNames.contains(".scalafmt.conf"));
        Assert.assertTrue(generatedNames.contains("UserService.scala"));
        Assert.assertTrue(generatedNames.contains("build.sbt"));
        Assert.assertTrue(generatedNames.contains("PetService.scala"));
        Assert.assertTrue(generatedNames.contains("Order.scala"));
        Assert.assertTrue(generatedNames.contains("UserRoutes.scala"));
        Assert.assertTrue(generatedNames.contains("StoreRoutes.scala"));
        Assert.assertTrue(generatedNames.contains("README.md"));
        Assert.assertTrue(generatedNames.contains("ApiResponse.scala"));
        Assert.assertTrue(generatedNames.contains("Test.scala"));
        Assert.assertTrue(generatedNames.contains("Tag.scala"));
        Assert.assertTrue(generatedNames.contains("Pet_petId_body.scala"));
        Assert.assertTrue(generatedNames.contains("StoreService.scala"));
        Assert.assertTrue(generatedNames.contains("VERSION"));
        Assert.assertTrue(generatedNames.contains("package.scala"));
    }
}