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

    public enum FuncType {
        pointSet,
        linear,
        polynomial
    }

    static protected FuncType funcType_default = FuncType.linear;
    protected FuncType funcType = funcType_default;
    
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

        tmpStr = Utils.getOption("FT", options);
        if (tmpStr.length() != 0) {
            setFuncType(readFuncTypeFromName(tmpStr));
        } //else {
          //  setFuncType(m_SeedDefault);
        //}

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

        result.add("-FT");
        result.add("" + getFuncType());

        Collections.addAll(result, super.getOptions());

        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the tip text for this property
     *
     * @return tip text for this property suitable for displaying in the
     * explorer/experimenter gui
     */
    public String funcTypeTipText() {
        return "The function's type for function-dependable clustering.";
    }

    /**
     * Sets the function's type for this clustering
     */
    public void setFuncType(FuncType funcType) {
        this.funcType = funcType;
    }

    public void setFuncType(String funcTypeName) {
        setFuncType(readFuncTypeFromName(funcTypeName));
    }
    
    private FuncType readFuncTypeFromName(String funcTypeName) {
        funcTypeName = funcTypeName.toLowerCase();
        switch (funcTypeName) {
            case ("pointset"):
                return FuncType.pointSet;
            case ("linear"):
                return FuncType.linear;
            case ("polynomial"):
                return FuncType.polynomial;
            default:
                throw new IllegalArgumentException("Неизвестное имя типа функции: \"" + funcTypeName + "\". Либо имя не поддерживается пока что.");
        }
    }

    /**
     * Gets the function's type for this clustering
     *
     * @return the function's type for this clustering
     */
    public FuncType getFuncType() {
        return funcType;
    }
    
    /**
     * Метод для инициализации точек функции. (Следует переопределить в наследниках).
     */
    public abstract void funcInit();
}