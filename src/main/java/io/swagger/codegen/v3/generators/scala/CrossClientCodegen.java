package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrossClientCodegen extends AbstractScalaCodegen {

    static boolean debug = Boolean.parseBoolean(env("DEBUG", "false"));

    private String groupId;
    private String artifactId;
    private String basePackage;
    private String artifactVersion;
    private String appName;
    private String infoEmail;

    public CrossClientCodegen() {
        super();

        templateDir = "scala";
        embeddedTemplateDir = "cross-client";

        modelTemplateFiles.put("model.mustache", ".scala");
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

//        final String sourceDir = ensureSuffix(env("SCALA_SRC", orElse(sourceFolder, "src/main/scala/")), "/");
        final String appPackage = env("APP_PACKAGE", basePackage);
        final String appPath = appPackage.replace('.', '/');
        final String modelPath = "client/shared/src/main/scala/" + appPath;
        final String jsClientPath = "client/js/src/main/scala/" + appPath;
        final String jvmClientPath = "client/jvm/src/main/scala/" + appPath;


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
        Object oldSrc = additionalProperties.get(CodegenConstants.SOURCE_FOLDER);
        if (oldSrc != null) {
            System.out.println("WARNING: replacing " + oldSrc + " '' with " + modelPath);
        }
        additionalProperties.put(CodegenConstants.SOURCE_FOLDER, modelPath);

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
        supportingFiles.add(new SupportingFile("commonClient.mustache", modelPath, "Client.scala"));

        instantiationTypes.put("array", "Seq");
        instantiationTypes.put("map", "Map");
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
        return apiFileFolder() + '/' + fn + suffix;
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

    static boolean consumesMimetype(CodegenOperation op, String mimetype) {
        // people don't always/often specify the 'consumes' property, so we assume true when
        // the optional 'consumes' is null or empty
        boolean defaultRetValue = true;

        final List<Map<String, String>> consumes = op.consumes;
        if (consumes != null) {
            for (Map<String, String> c : consumes) {
                final String mt = c.get("mediaType");
                if (mt.equalsIgnoreCase(mimetype)) {
                    return true;
                }
            }
            return false;
        } else {
            return defaultRetValue;
        }
    }

    static String capitalise(String p) {
        if (p.length() < 2) {
            return p.toUpperCase();
        } else {
            String first = "" + p.charAt(0);
            return first.toUpperCase() + p.substring(1);
        }
    }

    static String nonParamPathPrefix(CodegenOperation op) {
        if (op.pathParams.isEmpty()) {
            return op.path;
        }

        final String firstParam = op.pathParams.stream().findFirst().get().paramName;
        final int i = op.path.indexOf(firstParam);
        final String path = chompSuffix(op.path.substring(0, i - 1), "/");
        return path;
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        final Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        final List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");

        operationList.forEach((op) -> postProcessOperation(op));
        return objs;
    }

    private static void postProcessOperation(CodegenOperation op) {
        // force http method to lower case
        op.httpMethod = op.httpMethod.toLowerCase();

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

        /** put in 'x-container-type' to help with unmarshalling from json */
        op.allParams.forEach((p) -> p.vendorExtensions.put("x-container-type", containerType(p.dataType)));
        op.bodyParams.forEach((p) -> p.vendorExtensions.put("x-container-type", containerType(p.dataType)));

        final String paramList = String.join(", ", op.allParams.stream().map((p) -> p.paramName).collect(Collectors.toList()));
        op.vendorExtensions.put("x-param-list", paramList);

        final Stream<String> typed = op.allParams.stream().map((p) -> p.paramName + " : " + asScalaDataType(p));
        final String typedParamList = String.join(", ", typed.collect(Collectors.toList()));
        op.vendorExtensions.put("x-param-list-typed", typedParamList);


        // for the declaration site
        op.vendorExtensions.put("x-cask-path-typed", routeArgs(op));
        op.vendorExtensions.put("x-query-args", queryArgs(op));

        op.vendorExtensions.put("x-response-type", enrichResponseType(op));
        String responseDebug = String.join("\n\n - - - - - - -\n\n", op.responses.stream().map(r -> ScalaCaskCodegen.inComment(pretty(r))).collect(Collectors.toList()));
        op.vendorExtensions.put("x-responses", responseDebug);
    }

    /**
     * The Service methods are decoupled from http Responses -- they return a typed 'ServiceResponse'.
     * This method tries to fill int the ServiceResponse type parameter
     * For example:
     * {{{
     * "200": {
     * "description": "successful operation",
     * "schema": {
     * "type": "string"
     * }
     * },
     * "400": {
     * "description": "Invalid username/password supplied"
     * }
     * }}}
     *
     * @param op
     * @return
     */
    private static String enrichResponseType(CodegenOperation op) {
        if (op.returnType != null && !op.returnType.isEmpty()) {
            return "ServiceResponse[" + op.returnType + "]";
        }
        Optional<CodegenResponse> successResponse = op.responses.stream().filter((r) -> r.code.startsWith("2")).findFirst();
        if (successResponse.isPresent()) {
            CodegenResponse r = successResponse.get();

            return "ServiceResponse[Unit] /**" +
                "containerType='" + r.containerType + "'\n" +
                "baseType='" + r.baseType + "'\n" +
                "dataType='" + r.dataType + "'\n" +
                "simpleType='" + r.getSimpleType() + "'\n" +
                "jsonSchema='" + r.jsonSchema + "'\n" +
                "schema='" + r.schema + "'\n" +
                "*/";
        } else {
            return "ServiceResponse[Unit]";
        }
    }

    private static CodegenParameter pathParamForName(CodegenOperation op, String pathParam) {
        final CodegenParameter param = op.pathParams.stream().filter(p -> p.paramName.equals(pathParam)).findFirst().get();
        if (param == null) {
            throw new RuntimeException("Bug: path param " + pathParam + " not found");
        }
        return param;
    }

    /**
     * The path placeholders as well as query parameters
     *
     * @param op the codegen operations
     * @return a list of both the path and query parameters as typed arguments (e.g. "aPathArg : Int, request: cask.Request, aQueryArg : Option[Long]")
     */
    private static String routeArgs(CodegenOperation op) {
        final Stream<String> pathParamNames = Arrays.stream(op.path.split("/", -1)).filter(p -> hasBrackets(p)).map(p -> {
            final CodegenParameter param = pathParamForName(op, chompBrackets(p));
            param.vendorExtensions.put("x-debug", ScalaCaskCodegen.inComment(pretty(param)));
            return param.paramName + " : " + asScalaDataType(param);
        });


        final List<String> pathList = pathParamNames.collect(Collectors.toList());

        // we always include the cask request
        pathList.add("request: cask.Request");

        final Stream<String> queryParams = op.queryParams.stream().map(p -> {
            p.vendorExtensions.put("x-default-value", defaultValue(p));
            return p.paramName + " : " + asScalaDataType(p);
        });
        pathList.addAll(queryParams.collect(Collectors.toList()));
        return pathList.isEmpty() ? "" : (String.join(", ", pathList));
    }

    private static String defaultValue(CodegenParameter p) {
        return ScalaCaskCodegen.pretty(p);
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

    private static String chompBrackets(String str) {
        return str.replace("{", "").replace("}", "");
    }

    private static String chompSuffix(String str, String suffix) {
        return str.endsWith(suffix) ? chompSuffix(str.substring(0, str.length() - suffix.length()), suffix) : str;
    }

    private static String ensureSuffix(String str, String suffix) {
        return str.endsWith(suffix) ? str : str + suffix;
    }

    private static boolean hasBrackets(String str) {
        return str.matches("^\\{(.*)\\}$");
    }

    static String containerType(String dataType) {
        String fixedForList = dataType.replaceAll(".*\\[(.*)\\]", "$1");
        // do we have to fix map?
        return fixedForList;
    }

    static String env(String key, String defaultValue) {
        return orElse(orElse(System.getenv().get(key), System.getProperty(key)), defaultValue);
    }

    static String orElse(String value, String valueWhenEmpty) {
        return (value == null || value.trim().isEmpty()) ? valueWhenEmpty : value;
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
