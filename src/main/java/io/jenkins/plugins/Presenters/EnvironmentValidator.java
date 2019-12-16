package io.jenkins.plugins.Presenters;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import io.jenkins.plugins.BaseStepBuilder;
import io.jenkins.plugins.Models.ComponentInfo;
import io.jenkins.plugins.Models.Constants;
import io.jenkins.plugins.Models.StepIds;
import io.jenkins.plugins.TestStepBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvironmentValidator {

    //наполнение словаря. Одна запись это id шага и список продуктов, для каждого из списка ComponentInfo - содержащий путь реестра, имя переменной по этому пути
    //и ее значение(на текуший момент - версия)
    static Map<StepIds, ArrayList<ComponentInfo>> stepComponentParams;

    public static boolean validate(List<Builder> builders, FilePath workspace, BuildListener listener) {

        stepComponentInitialize(builders);

        Map<String, ComponentInfo> checkComponents = new HashMap();

        for (int i = 0; i < builders.size();i++ )
        {
            if(builders.get(i) instanceof BaseStepBuilder) {
                BaseStepBuilder builder = (BaseStepBuilder) builders.get(i);
                StepIds si = builder.getStepId();

                if (stepComponentParams.keySet().contains(si)) {
                    ArrayList<ComponentInfo> listStepComponents = stepComponentParams.get(si);
                    for (int j = 0; j < listStepComponents.size(); j++) {
                        if (!checkComponents.containsKey(listStepComponents.get(j).GetId()))
                                checkComponents.put(listStepComponents.get(j).GetId(), listStepComponents.get(j));
                    }
                }
            }
        }

        boolean result = true;

        List<String> failedComponentName = new ArrayList<>();

        try {
            FilePath scriptLocation = copyResourceToWorkspace(workspace, Constants.psScriptsLocation);
            Launcher launcher = scriptLocation.createLauncher(listener);

            if(!PowerShellExecuter.getInstance().execute(launcher, listener, workspace, scriptLocation, "CheckPowerShellInstall",
                    new String[]{ComponentInfo.PowerShellModuleName, ComponentInfo.PowerShellModuleMinVersion})) {
                result = false;
                failedComponentName.add(String.format("%s, ver. %s", ComponentInfo.PowerShellModuleFullName, ComponentInfo.PowerShellModuleMinVersion));
            }

            for(Map.Entry<String, ComponentInfo> entry : checkComponents.entrySet()) {
                if(!PowerShellExecuter.getInstance().execute(launcher, listener, workspace, scriptLocation, "CheckRequiredComponentInstall",
                        entry.getValue().GetPSCallingParams())) {
                    result = false;
                    failedComponentName.add(String.format("%s, ver. %s", entry.getValue().GetComponentName(), entry.getValue().GetComponentVersion()));
                }
            }
        }
        catch (IOException e) {
            listener.error("Unexpected I/O exception executing cmdlet: " + e.getMessage());
            return false;
        }
        catch (InterruptedException e) {
            listener.error("Unexpected thread interruption executing cmdlet");
            return false;
        }

        if(failedComponentName.size() > 0) {
            listener.getLogger().println("The dbForge DevOps Automation for SQL Server Jenkins plugin requires the following components to be installed:");
            for (int i = 0; i < failedComponentName.size(); i++) {
                listener.getLogger().println("\t" + failedComponentName.get(i));
            }
            listener.getLogger().println("You need to download these products at:\n" +
                "https://www.devart.com/dbforge/sql/developer-bundle/download.html");
        }
        return result;
    }

    private static void stepComponentInitialize(List<Builder> builders){

        stepComponentParams = new HashMap();
        ArrayList<ComponentInfo> listComponents = new ArrayList();

        //обязательные компоненты для шагов
        ComponentInfo componentInfo = ComponentInfo.GetComponentInfo(ComponentInfo.SchemaCompareRegId);
        if(!listComponents.contains(componentInfo))
            listComponents.add(componentInfo);
        stepComponentParams.put(StepIds.Buid, listComponents);

        listComponents = new ArrayList();
        componentInfo = ComponentInfo.GetComponentInfo(ComponentInfo.UnitTestRegId);
        if(!listComponents.contains(componentInfo))
            listComponents.add(componentInfo);
        stepComponentParams.put(StepIds.Test, listComponents);

        //условно добавляемые компоненты
        for (int i = 0; i < builders.size();i++ ) {
            //датагенератор
            if (builders.get(i) instanceof TestStepBuilder) {
                TestStepBuilder builder = (TestStepBuilder)builders.get(i);
                StepIds si = builder.getStepId();
                if (builder.getGenerateTestData())
                    stepComponentParams.get(si).add(ComponentInfo.GetComponentInfo(ComponentInfo.DataGenRegId));
            }
        }
    }

    private static FilePath copyResourceToWorkspace(FilePath workspace, String relativeFilePath) throws IOException, InterruptedException {

        FilePath filePath;
        filePath = new FilePath(workspace, relativeFilePath);

        InputStream stream = EnvironmentValidator.class.getResourceAsStream("" + '/' + relativeFilePath);
        try {
            filePath.copyFrom(stream);
        }
        finally {
            if(stream != null)
                stream.close();
        }
        return filePath;
    }
}
