/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.clusterers;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.clusterers.FuncDependableClusterer.FuncType;
import weka.core.Utils;

/**
 *
 * @author alexUnder
 */
public class MyClusterer extends FuncDependableClusterer {

    Point2D.Double[] funcPoints;
    
    protected int accuracyFactor=1000;
    protected Double xLeft=0d;
    protected Double xRight=1d;
    protected Double yLeft=0d;
    protected Double yRight=1d;
    
    HashMap<Instance, Integer> instanceToClusterNumber = new HashMap<>();
    HashMap<ArrayList<Instance>, Instance> clustersToCentroid = new HashMap<>();
    int curClusterNum = 0;
    ////////
    //ArrayList<Point> superMassCenters = new ArrayList<Point>();
    
    @Override
    public void buildClusterer(Instances data) throws Exception {
        // can clusterer handle the data?
        if (data.numAttributes() != 2) {
            throw new IllegalArgumentException("The clusterer " + this.getClass().getSimpleName() + " can handle only instances with 2 attributes. But " + data.numAttributes() + " received");
        }
        getCapabilities().testWithFail(data);
        //Построение функции:
        funcInit();
        //clustering:
        for (Instance inst : data) {
            //Ссылки на новый кластер,к которому стоит добавлять, и новый центр масс при добавлении
            ArrayList<Instance> addToThisCluster = null;
            Instance thisNewCentroid = null;
            //оценка ошибки (которую следует минимизировать):
            Double error = Double.POSITIVE_INFINITY; // = max(R(func,centroid[i]))
            //оценка добавления к сущ. кластеру:
            for (ArrayList<Instance> cluster : clustersToCentroid.keySet()) {
                Instance newCentroid = newCentroid(cluster, inst);
                Double localError = sqrDistForFunc(newCentroid); //оценка ошибки для данной итерации (как максимальное ис расстояний)
                for (ArrayList<Instance> anotherCluster : clustersToCentroid.keySet()) {
                    if (anotherCluster != cluster) {
                        Double R_anotherCluster = sqrDistForFunc(clustersToCentroid.get(anotherCluster));
                        if (R_anotherCluster > localError) {
                            localError = R_anotherCluster;
                        }
                    }
                }
                if (localError < error) {
                    error = localError;
                    addToThisCluster = cluster;
                    thisNewCentroid = newCentroid;
                }
            }
            //оценка добавления к нов. кластеру:
            Double localError = sqrDistForFunc(inst);
            for (ArrayList<Instance> anotherCluster : clustersToCentroid.keySet()) {
                Double R_anotherCluster = sqrDistForFunc(clustersToCentroid.get(anotherCluster));
                if (R_anotherCluster > localError) {
                    localError = R_anotherCluster;
                }
            }
            //если добавление к нов. кластеру оптимальнее, чем добаление к существующим 
            if (localError < error) {
                error = localError;
                ArrayList<Instance> newCluster = new ArrayList<Instance>();
                newCluster.add(inst);
                addToThisCluster = newCluster;
                thisNewCentroid = inst;
                instanceToClusterNumber.put(inst, curClusterNum++);
                clustersToCentroid.put(newCluster, thisNewCentroid);
                //clustersToCentroid.
            } else //иначе - обновить хешмапы и прочее другими значениями
            {
                clustersToCentroid.remove(addToThisCluster);    //Такой порядок
                addToThisCluster.add(inst);                     //обязателен !!
                clustersToCentroid.put(addToThisCluster, thisNewCentroid);
                instanceToClusterNumber.put(inst, instanceToClusterNumber.get(addToThisCluster.get(0)));
            }
        }
    }

    /**
     * Расстояние от заданного экземпляра до функции
     * @return возвращает квадрат евклидова расстояния от инстанса до лижайшей из точек функции
     */
    public double sqrDistForFunc(Instance inst) {
        Double sqDist = Double.POSITIVE_INFINITY;
        for (Point2D.Double fpoint : funcPoints) {
            double deltX = fpoint.x - inst.value(0);
            double deltY = fpoint.y - inst.value(1);
            double distIter = deltX * deltX + deltY * deltY;
            if (distIter < sqDist) {
                sqDist = distIter;
            }
        }
        return sqDist;
    }

    public Instance newCentroid(Instances cluster, Instance addingInst) throws Exception {
        Instance oldCentroid = clustersToCentroid.get(cluster);
        if (oldCentroid != null) {
            if (oldCentroid.equalHeaders(addingInst)) {
                int newSize = cluster.size() + 1;
                Instance newCentroid = new DenseInstance(oldCentroid);
                //цикл по всем аттрибутам:
                for (int i = 0; i < oldCentroid.numAttributes(); i++) {
                    double delt = (addingInst.value(i) - oldCentroid.value(i)) / newSize;
                    newCentroid.setValue(i, oldCentroid.value(i) + delt);
                }
                return newCentroid;
            } else {
                throw new Exception("The headers are not equals for old centroid and given new instance. " + oldCentroid.equalHeadersMsg(addingInst));
            }
        } else {
            throw new Exception("The centroid corresponding to given cluster is null");
        }
    }

    public Instance newCentroid(ArrayList<Instance> cluster, Instance addingInst) throws Exception {
        Instance oldCentroid = clustersToCentroid.get(cluster);
        if (oldCentroid != null) {
            //if (oldCentroid.enumerateAttributes().equals(addingInst.enumerateAttributes())) {
                int newSize = cluster.size() + 1;
                Instance newCentroid = new DenseInstance(oldCentroid);
                //цикл по всем аттрибутам:
                for (int i = 0; i < oldCentroid.numAttributes(); i++) {
                    double delt = (addingInst.value(i) - oldCentroid.value(i)) / newSize;
                    newCentroid.setValue(i, oldCentroid.value(i) + delt);
                }
                return newCentroid;
            //} else {
            //    throw new Exception("The attributes enumerations are not equal for old centroid and given new instance. ");
            //}
        } else {
            throw new Exception("The centroid corresponding to given cluster is null");
        }
    }
    
    /**
     * Classifies a given instance.
     *
     * @param instance the instance to be assigned to a cluster
     * @return the number of the assigned cluster as an interger if the class is
     * enumerated, otherwise the predicted value
     * @throws Exception if instance could not be classified successfully
     */
    @Override
    public int clusterInstance(Instance instance) throws Exception {
        return instanceToClusterNumber.get(instance);
    }

    @Override
    public int numberOfClusters() throws Exception {
        return clustersToCentroid.size();
    }

    @Override
    public Capabilities getCapabilities() {
        Capabilities res = new Capabilities(this);
        res.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
        res.enable(Capabilities.Capability.NO_CLASS);
        return res;
    }
    
    @Override
    public void funcInit(){
        switch (funcType){
            case linear:
                funcInitLinear();
                break;
            default:
                throw new IllegalArgumentException("Тип функции \"" + funcType+"\" пока не поддерживается");
        }
    }
    
    public void funcInitLinear(){
        funcPoints = new Point2D.Double[accuracyFactor];
        Double xStep=(xRight-xLeft)/accuracyFactor;
        Double yStep=(yRight-yLeft)/accuracyFactor;
        for (int i = 0; i < funcPoints.length; i++) {
            funcPoints[i] = new Point2D.Double(xLeft+i*xStep, yLeft+i*yStep);
        }
    }
    
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

        tmpStr = Utils.getOption("xl", options);
        if (tmpStr.length() != 0) {
            setXLeft(Double.valueOf(tmpStr));
        }
        tmpStr = Utils.getOption("xr", options);
        if (tmpStr.length() != 0) {
            setXRight(Double.valueOf(tmpStr));
        }
        tmpStr = Utils.getOption("yl", options);
        if (tmpStr.length() != 0) {
            setYLeft(Double.valueOf(tmpStr));
        }
        tmpStr = Utils.getOption("yr", options);
        if (tmpStr.length() != 0) {
            setYRight(Double.valueOf(tmpStr));
        }
        tmpStr = Utils.getOption("AF", options);
        if (tmpStr.length() != 0) {
            setAccuracyFactor(Integer.valueOf(tmpStr));
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

        result.add("-xl");
        result.add("" + getXLeft());
        result.add("-xr");
        result.add("" + getXRight());
        result.add("-yl");
        result.add("" + getYLeft());
        result.add("-yr");
        result.add("" + getYRight());
        result.add("-AF");
        result.add("" +getAccuracyFactor());

        Collections.addAll(result, super.getOptions());

        return result.toArray(new String[result.size()]);
    }
    
    public int getAccuracyFactor(){
        return accuracyFactor;
    }
    public Double getXLeft(){
        return xLeft;
    }
    public Double getXRight(){
        return xRight;
    }
    public Double getYLeft(){
        return yLeft;
    }
    public Double getYRight(){
        return yRight;
    }
    public void setAccuracyFactor(int newAccFact){
        accuracyFactor=newAccFact;
    }
    public void setXLeft(Double newXLeft){
        xLeft= newXLeft;
    }
    public void setXRight(Double newXRight){
        xRight = newXRight;
    }
    public void setYLeft(Double newYLeft){
        yLeft= newYLeft;
    }
    public void setYRight(Double newYRight){
        yRight=newYRight;
    }
}