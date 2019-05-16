package io.jenkins.plugins.Models;

import java.util.HashMap;

public class ComponentInfo {

    public static final String DataGenRegId        = "DevartDataGeneratorMSSql_is1";
    public static final String UnitTestRegId       = "DevartUnitTest_is1";
    public static final String SchemaCompareRegId  = "DevartSchemaCompareMSSql_is1";
    public static final String DocumenterRegId     = "DevartDocumenterMSSql_is1";
    public static final String registryPath        = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\"; //считаем что это общий часть пути для всех наших продуктов

    private static HashMap<String, ComponentInfo> PossibleComponents = new HashMap<>();

    static{

        PossibleComponents.put(SchemaCompareRegId,  new ComponentInfo(SchemaCompareRegId, "4.6.44", "dbForge Schema Compare for SQL Server"));
        PossibleComponents.put(UnitTestRegId,       new ComponentInfo(UnitTestRegId, "1.5.48", "dbForge Unit Test for SQL Server"));
        PossibleComponents.put(DataGenRegId,        new ComponentInfo(DataGenRegId, "4.2.52", "dbForge Data Generator for SQL Server"));
        PossibleComponents.put(DocumenterRegId,     new ComponentInfo(DocumenterRegId, "9.9.99", "dbForge Documenter for SQL Server"));
    }

    private String id;
    private String version;
    private String componentName;

    private ComponentInfo(String _id, String _version, String _componentName) {

        id = _id; version = _version; componentName = _componentName;
    }

    public static ComponentInfo GetComponentInfo(String Id){

          return PossibleComponents.get(Id);
    }

    public String[] GetPSCallingParams() {

        return new String[]{registryPath + id, "DisplayVersion", version};
    }

    public String GetComponentName() {

        return componentName;
    }

    public String GetComponentVersion() {

        return version;
    }

    public String GetId() {

        return id;
    }

}
