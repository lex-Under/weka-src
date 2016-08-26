/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.clusterers;

import java.util.Collections;
import java.util.Vector;
import weka.core.OptionHandler;
import weka.core.Utils;

/**
 * @author alexUnder
 */
public abstract class FuncDependableClusterer extends AbstractClusterer implements OptionHandler {

    static protected FunctionInfo functionInfo_default = (FunctionInfo)(new LinearFunctionInfo());
    protected FunctionInfo functionInfo = functionInfo_default;
    
    /**
     * Parses a given list of options. Valid options are:
     * <p>
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    @Override
    public void setOptions(String[] options) throws Exception {
        String tmpStr;

        tmpStr = Utils.getOption("FI", options);
        if (tmpStr.length() != 0) {
            String distFunctionClassSpec[] = Utils.splitOptions(tmpStr);
            if (distFunctionClassSpec.length == 0) {
                throw new Exception("Invalid FunctionInfo specification string.");
            }
            String className = distFunctionClassSpec[0];
            distFunctionClassSpec[0] = "";

            setFunctionInfo((FunctionInfo) Utils.forName(
                    FunctionInfo.class, className, distFunctionClassSpec));
        }

        super.setOptions(options);
    }

    /**
     * Gets the current settings of the classifier.
     *
     * @return an array of strings suitable for passing to setOptions
     */
    @Override
    public String[] getOptions() {

        Vector<String> result = new Vector<String>();
        
        result.add("-FI");
        result.add((functionInfo.getClass().getName() + " " + Utils
                .joinOptions(functionInfo.getOptions())).trim());

        Collections.addAll(result, super.getOptions());

        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String functionInfoTipText() {
        return "The function's type and general parameters for function-dependable clustering.";
    }

    /**
     * Sets the function's type for this clustering
     */
    public void setFunctionInfo(FunctionInfo newFunctionInfo) {
        this.functionInfo = newFunctionInfo;
    }
    
    /**
     * Gets the function for this clusterer
     *
     * @return the function for this clusterer
     */
    public FunctionInfo getFunctionInfo() {
        return functionInfo;
    }
    
    /**
     * Метод для инициализации точек функции. (Следует переопределить в наследниках).
     */
    public abstract void funcInit();
}