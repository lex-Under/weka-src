/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.clusterers;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

/**
 *
 * @author alexUnder
 */
public class LinearFunctionInfo extends FunctionInfo implements OptionHandler, Serializable {

    protected int accuracyFactor=1000;
    protected Double xMin=0d;
    protected Double xMax=1d;
    protected Double yMin=0d;
    protected Double yMax=1d;
    
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

        tmpStr = Utils.getOption("xmin", options);
        if (tmpStr.length() != 0) {
            setXMin(Double.valueOf(tmpStr));
        }
        tmpStr = Utils.getOption("xmax", options);
        if (tmpStr.length() != 0) {
            setXMax(Double.valueOf(tmpStr));
        }
        tmpStr = Utils.getOption("ymin", options);
        if (tmpStr.length() != 0) {
            setYMin(Double.valueOf(tmpStr));
        }
        tmpStr = Utils.getOption("ymax", options);
        if (tmpStr.length() != 0) {
            setYMax(Double.valueOf(tmpStr));
        }
        tmpStr = Utils.getOption("AF", options);
        if (tmpStr.length() != 0) {
            setAccuracyFactor(Integer.valueOf(tmpStr));
        }
    }

    /**
     * Gets the current settings of the classifier.
     *
     * @return an array of strings suitable for passing to setOptions
     */
    @Override
    public String[] getOptions() {

        Vector<String> result = new Vector<String>();

        result.add("-xmin");
        result.add("" + getXMin());
        result.add("-xmax");
        result.add("" + getXMax());
        result.add("-ymin");
        result.add("" + getYMin());
        result.add("-ymax");
        result.add("" + getYMax());
        result.add("-AF");
        result.add("" +getAccuracyFactor());

        return result.toArray(new String[result.size()]);
    }

    @Override
    public Enumeration<Option> listOptions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public int getAccuracyFactor(){
        return accuracyFactor;
    }
    public Double getXMin(){
        return xMin;
    }
    public Double getXMax(){
        return xMax;
    }
    public Double getYMin(){
        return yMin;
    }
    public Double getYMax(){
        return yMax;
    }
    public void setAccuracyFactor(int newAccFact){
        accuracyFactor=newAccFact;
    }
    public void setXMin(Double newXLeft){
        xMin= newXLeft;
    }
    public void setXMax(Double newXRight){
        xMax = newXRight;
    }
    public void setYMin(Double newYLeft){
        yMin= newYLeft;
    }
    public void setYMax(Double newYRight){
        yMax=newYRight;
    }
}
