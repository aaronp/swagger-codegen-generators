package io.swagger.codegen.v3.generators.scala;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.codegen.v3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.swagger.codegen.v3.generators.scala.ScalaCaskCodegen.capitalise;
import static io.swagger.codegen.v3.generators.scala.ScalaCaskCodegen.consumesMimetype;

public class CrossClientCodegen extends AbstractScalaCodegen {

    static boolean debug = Boolean.parseBoolean(env("DEBUG", "false"));

    private static enum Platform {
        JVM,
        JS,
        SHARED
    }

    private String groupId;
    private String artifactId;
    private String basePackage;
    private String artifactVersion;
    private String appName;
    private String infoEmail;

    private String appPackage;
    private String apiPath;
    private String modelPath;
    private String sharedApiPath;
    private String sharedModelPath;
    private String jsClientPath;
    private String jvmClientPath;

    private static String SharedClientTemplateFile = "client.mustache";

    public CrossClientCodegen() {
        super();

        templateDir = "scala";
        embeddedTemplateDir = "cross-client";

        modelTemplateFiles.put("model.mustache", ".scala");
        modelTestTemplateFiles.put("modelTest.mustache", ".scala");
        apiTestTemplateFiles.put("jvmClientTest.mustache", "ClientTest.scala");
        reservedWordsMappings.put("package", "pckg");

        setReservedWordsLowerCase(
            Arrays.asList(
                "abstract", "continue", "for", "new", "switch", "assert",
                "default", "if", "package", "synchronized", "boolean", "do", "goto", "private",
                "this", "break", "double", "implements", "protected", "throw", "byte", "else",
                "import", "public", "throws", "case", "enum", "instanceof", "return", "transient",
                "catch", "extends", "int", "short", "try", "char", "final", "interface", "static",
                "void", "class", "finally", "long", "strictfp", "volatile", "const", "float",
                "native", "super", "while", "type")
        );

        defaultIncludes = new HashSet<String>(
            Arrays.asList("double",
                "Int",
                "Long",
                "Float",
                "Double",
                "char",
                "float",
                "String",
                "boolean",
                "Boolean",
                "Double",
                "Integer",
                "Long",
                "Float",
                "List",
                "Set",
                "Map")
        );

        importMapping = new HashMap<String, String>();
        importMapping.put("BigDecimal", "scala.math.BigDecimal");
        importMapping.put("UUID", "java.util.UUID");
        importMapping.put("File", "java.io.File");
        importMapping.put("Date", "java.time.LocalDate as Date");
        importMapping.put("Timestamp", "java.sql.Timestamp");
        importMapping.put("Map", "Map");
        importMapping.put("HashMap", "Map");
        importMapping.put("Array", "Seq");
        importMapping.put("ArrayList", "Seq");
        importMapping.put("List", "Seq");
        importMapping.put("DateTime", "java.time.LocalDateTime");
        importMapping.put("LocalDateTime", "java.time.LocalDateTime");
        importMapping.put("LocalDate", "java.time.LocalDate");
        importMapping.put("LocalTime", "java.time.LocalTime");
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }

    @Override
    public void processOpts() {
        super.processOpts();

        outputFolder = env("OUTPUT_DIR", orElse(outputFolder, "generated-code/cross-client"));
        groupId = env("GROUP_ID", orElse(groupId, "io.swagger"));
        artifactId = env("ARTIFACT_ID", orElse(artifactId, "cross-client"));
        basePackage = env("PACKAGE", groupId);
        artifactVersion = env("VERSION", orElse(artifactVersion, "0.0.1"));
        appName = env("APP_NAME", orElse(appName, "Cross-Client"));
        infoEmail = env("INFO_EMAIL", "apiteam@swagger.io");
        apiPackage = env("API_PACKAGE", orElse(apiPackage, basePackage + ".server.api"));
        modelPackage = env("MODEL_PACKAGE", orElse(modelPackage, basePackage + ".server.model"));
        testPackage = basePackage;

        appPackage = env("APP_PACKAGE", basePackage);
        apiPath = apiPackage.replace('.', '/');
        modelPath = modelPackage.replace('.', '/');
        sharedApiPath = "client/shared/src/main/scala/" + apiPath;
        sharedModelPath = "client/shared/src/main/scala/" + modelPath;
        jsClientPath = "client/js/src/main/scala/" + apiPath;
        jvmClientPath = "client/jvm/src/main/scala/" + apiPath;


        {
            final Map<String, String> settings = new HashMap<String, String>();
            settings.put("modelPackage", modelPackage);
            settings.put("appName", appName);
            settings.put("appPackage", appPackage);
            settings.put("sourceFolder", sourceFolder);
            settings.put("apiPackage", apiPackage);
            settings.put("basePackage", basePackage);
            settings.put("artifactId", artifactId);
            settings.put("groupId", groupId);
            settings.put("outputFolder", outputFolder);
            System.out.println("" +
                "\uD83C\uDD82\uD83C\uDD72\uD83C\uDD70\uD83C\uDD7B\uD83C\uDD70 \uD83C\uDD72\uD83C\uDD81\uD83C\uDD7E\uD83C\uDD82\uD83C\uDD82-\uD83C\uDD72\uD83C\uDD7B\uD83C\uDD78\uD83C\uDD74\uD83C\uDD7D\uD83C\uDD83 \n" +
                ScalaCaskCodegen.formatMap(settings) + "\n\n"
            );
        }


        typeMapping.put("integer", "Int");
        typeMapping.put("long", "Long");
        //TODO binary should be mapped to byte array
        // mapped to String as a workaround
        typeMapping.put("binary", "String");

        additionalProperties.put("appName", appName);
        additionalProperties.put("appDescription", "A cask service");
        additionalProperties.put("infoUrl", "http://swagger.io");
        additionalProperties.put("infoEmail", infoEmail);
        additionalProperties.put("licenseInfo", "All rights reserved");
        additionalProperties.put("licenseUrl", "http://apache.org/licenses/LICENSE-2.0.html");
        additionalProperties.putIfAbsent(CodegenConstants.INVOKER_PACKAGE, basePackage);
        additionalProperties.putIfAbsent(CodegenConstants.GROUP_ID, groupId);
        additionalProperties.putIfAbsent(CodegenConstants.ARTIFACT_ID, artifactId);
        additionalProperties.putIfAbsent(CodegenConstants.ARTIFACT_VERSION, artifactVersion);
        additionalProperties.putIfAbsent(CodegenConstants.PACKAGE_NAME, basePackage);

        // we have different source directories for cross-client builds. here we treat 'model' as the shared source folder
        additionalProperties.put(CodegenConstants.SOURCE_FOLDER, sharedModelPath);

        apiTemplateFiles.put(SharedClientTemplateFile, "Client.scala");

        supportingFiles.add(new SupportingFile("README.mustache", "README.md"));

        // TODO - put this back in when you're done developing @Aaron
        supportingFiles.add(new SupportingFile(".swagger-codegen-ignore", ".swagger-codegen-ignore--todo-put-this-back"));

        supportingFiles.add(new SupportingFile("build.sbt.mustache", "build.sbt"));
        supportingFiles.add(new SupportingFile(".scalafmt.conf.mustache", ".scalafmt.conf"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", ".gitignore"));
        supportingFiles.add(new SupportingFile("project/build.properties", "project", "build.properties"));
        supportingFiles.add(new SupportingFile("project/plugins.sbt", "project", "plugins.sbt"));

        supportingFiles.add(new SupportingFile("jvmClient.mustache", jvmClientPath, "JVMClient.scala"));
        supportingFiles.add(new SupportingFile("jsClient.mustache", jsClientPath, "JSClient.scala"));

        supportingFiles.add(new SupportingFile("apiPackage.mustache", sharedApiPath, "package.scala"));
        supportingFiles.add(new SupportingFile("http.mustache", sharedApiPath, "http.scala"));


        supportingFiles.add(new SupportingFile("modelPackage.mustache", sharedModelPath, "package.scala"));

        instantiationTypes.put("array", "Seq");
        instantiationTypes.put("map", "Map");
    }

    @Override
    public String modelFileFolder() {
        Object src = additionalProperties.get(CodegenConstants.SOURCE_FOLDER);
        return outputFolder + File.separator + src;
    }

    @Override
    public String apiFileFolder() {
        return apiFileFolderForPlatform(sharedModelPath, Platform.JVM);
    }

    public String apiFileFolderForPlatform(String folder, Platform platform) {
//        String folder;
//        switch(platform) {
//            case JS :
//                folder = modelFileFolder();
//            break;
//            case JVM :
//                folder = k();
//                break;
//            case JS :
//                folder = modelFileFolder();
//                break;
//        }

        return outputFolder + File.separator + folder.replace("/shared/", "/" + platform.name().toLowerCase() + "/");
    }

    @Override
    public String modelTestFileFolder() {
        return modelFileFolder().replace("/main/", "/test/");
    }

    @Override
    public String apiTestFileFolder() {
        return apiFileFolder().replace("/main/", "/test/");
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultApi";
        }
        name = sanitizeName(name);
        return camelize(name);
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        final String suffix = apiTemplateFiles().get(templateName);
        final String fn = toApiFilename(tag);
        Platform platform = Platform.JVM;
        if (templateName.equals(SharedClientTemplateFile)) {
            platform = Platform.SHARED;
//        } else if (templateName.equals()) {
//            platform = "shared";
        }

        final String value = apiFileFolderForPlatform(sharedApiPath, platform) + '/' + fn + suffix;
        System.out.println("apiFileName(" + templateName + "," + tag + ") returning " + value);
        return value;
    }

    @Override
    public boolean checkAliasModel() {
        return super.checkAliasModel();
    }

    @Override
    public String getDefaultTemplateDir() {
        return "scala/cross-client";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "scala-cross-client";
    }

    @Override
    public String getHelp() {
        return "Generates a Scala cross-client application.";
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        List<Map<String, Object>> operations = getOperations(objs);
        for (int i = 0; i < operations.size(); i++) {
            operations.get(i).put("hasMore", i < operations.size() - 1);
        }
        objs.put("operations", operations);
        return super.postProcessSupportingFileData(objs);
    }

    // thanks FlaskConnectionCodeGen
    private static List<Map<String, Object>> getOperations(Map<String, Object> objs) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> apiInfo = (Map<String, Object>) objs.get("apiInfo");
        List<Map<String, Object>> apis = (List<Map<String, Object>>) apiInfo.get("apis");
        for (Map<String, Object> api : apis) {
            Map<String, Object> operations = (Map<String, Object>) api.get("operations");
            result.add(operations);
        }
        return result;
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        final Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        final List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");

        operationList.forEach((op) -> postProcessOperation(op));
        return objs;
    }

    private static Set<String> recognizedHttpMethods = ImmutableSet.of("get", "post", "put", "delete", "patch");

    private static void postProcessOperation(CodegenOperation op) {
        // force http method to lower case
        op.httpMethod = op.httpMethod.toLowerCase();

        if (recognizedHttpMethods.contains(op.httpMethod)) {
            op.vendorExtensions.put("HttpRequestInstance", capitalise(op.httpMethod));
        } else {
            op.vendorExtensions.put("HttpRequestInstance", "Other(\"" + op.httpMethod + "\", ");
        }

        op.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(op)));
        op.allParams.forEach(p -> p.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(p))));
        op.bodyParams.forEach(p -> p.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(p))));
        op.pathParams.forEach(p -> p.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(p))));
        op.queryParams.forEach(p -> p.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(p))));
        op.headerParams.forEach(p -> p.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(p))));
        op.formParams.forEach(p -> p.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(p))));
        op.requiredParams.forEach(p -> p.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(p))));


        /** Put in 'x-consumes-json' and 'x-consumes-xml' */
        op.vendorExtensions.put("x-consumes-json", consumesMimetype(op, "application/json"));
        op.vendorExtensions.put("x-consumes-xml", consumesMimetype(op, "application/xml"));
        op.vendorExtensions.put("x-consumes-binary", consumesMimetype(op, "application/octet-stream"));

        op.bodyParams.stream().filter((b) -> b.getIsBodyParam()).forEach((p) -> {
            p.vendorExtensions.put("x-consumes-json", consumesMimetype(op, "application/json"));
            p.vendorExtensions.put("x-consumes-xml", consumesMimetype(op, "application/xml"));
            p.vendorExtensions.put("x-consumes-binary", consumesMimetype(op, "application/octet-stream"));
        });

        op.vendorExtensions.put("has-body-param", op.bodyParam != null);

        /** put in 'x-container-type' to help with unmarshalling from json */
        op.allParams.forEach((p) -> p.vendorExtensions.put("x-container-type", containerType(p.dataType)));
        op.bodyParams.forEach((p) -> p.vendorExtensions.put("x-container-type", containerType(p.dataType)));

        final String paramList = String.join(", ", op.allParams.stream().map((p) -> p.paramName).collect(Collectors.toList()));
        op.vendorExtensions.put("x-param-list", paramList);

        final String asStringTemplate = op.path.replace("{", "${");
        op.vendorExtensions.put("path-template", asStringTemplate);
        op.vendorExtensions.put("has-path-params", !op.pathParams.isEmpty());
        op.vendorExtensions.put("has-query-params", !op.queryParams.isEmpty());
        op.vendorExtensions.put("needs-path-query-separator", !op.pathParams.isEmpty() && !op.queryParams.isEmpty());
        op.vendorExtensions.put("has-url-params", !op.pathParams.isEmpty() || !op.queryParams.isEmpty());

        final Stream<String> typed = op.allParams.stream().map((p) -> p.paramName + " : " + asScalaDataType(p));
        final String typedParamList = String.join(", ", typed.collect(Collectors.toList()));
        op.vendorExtensions.put("x-param-list-typed", typedParamList);


        final List<String> examples = op.examples == null ? Collections.emptyList() : op.examples.stream().filter(x -> x != null)
            .map(x -> ScalaCaskCodegen.formatMap(x)).collect(Collectors.toList());
        op.vendorExtensions.put("example-list", ImmutableMap.of("asMap", examples));


        // for the declaration site
        op.vendorExtensions.put("x-query-args", queryArgs(op));

        op.vendorExtensions.put("x-response-type", ScalaCaskCodegen.enrichResponseType(op));

        op.vendorExtensions.put("responseTypes", responseTypes(op));

        String responseDebug = String.join("\n\n - - - - - - -\n\n", op.responses.stream().map(r -> ScalaCaskCodegen.inComment(pretty(r))).collect(Collectors.toList()));
        op.vendorExtensions.put("x-responses", responseDebug);
    }

    static String responseTypes(final CodegenOperation op) {
        if (op.returnType != null && !op.returnType.isEmpty()) {
            return op.returnType;
        }
        final Set<String> responseTypes = op.responses.stream().map(r -> {
            if (!StringUtils.isEmpty(r.dataType)) {
                return r.dataType;
            } else if (!StringUtils.isEmpty(r.baseType)) {
                return r.baseType;
            } else {
                return "Unit";
            }
        }).collect(Collectors.toSet());
        return String.join(" | ", responseTypes);
    }

    private static String queryArgs(final CodegenOperation op) {
        final List<String> list = op.queryParams.stream().map(p -> p.paramName).collect(Collectors.toList());
        final String prefix = list.isEmpty() ? "" : ", ";
        return prefix + String.join(", ", list);
    }

    private static String asScalaDataType(final CodegenParameter param) {
        String dataType = param.dataType;
        if (dataType.startsWith("List[")) {
            dataType = dataType.replace("List[", "Seq[");
        } else if (!param.required) {
            dataType = "Option[" + param.dataType + "]";
        }
        return dataType;
    }

    static String containerType(String dataType) {
        String fixedForList = dataType.replaceAll(".*\\[(.*)\\]", "$1");
        // do we have to fix map?
        return fixedForList;
    }

    static String env(String key, String defaultValue) {
        return ScalaCaskCodegen.env(key, defaultValue);
    }

    static String orElse(String value, String valueWhenEmpty) {
        return ScalaCaskCodegen.orElse(value, valueWhenEmpty);
    }

    static String pretty(CodegenResponse response) {
        return ScalaCaskCodegen.pretty(response);
    }

    static String pretty(CodegenParameter response) {
        return ScalaCaskCodegen.pretty(response);
    }

    static String pretty(CodegenOperation response) {
        return ScalaCaskCodegen.pretty(response);
    }

    static String pretty(CodegenProperty response) {
        return ScalaCaskCodegen.pretty(response);
    }
}
