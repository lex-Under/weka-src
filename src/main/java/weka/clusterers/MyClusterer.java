/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.clusterers;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author alexUnder
 */
public class MyClusterer extends FuncDependableClusterer {

    Point2D.Double[] funcPoints;
    ////////
    HashMap<Instance, Integer> instancesToClusterNumber = new HashMap<>();
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
                instancesToClusterNumber.put(inst, curClusterNum++);
                clustersToCentroid.put(newCluster, thisNewCentroid);
                //clustersToCentroid.
            } else //иначе - обновить хешмапы и прочее другими значениями
            {
                clustersToCentroid.remove(addToThisCluster);    //Такой порядок
                addToThisCluster.add(inst);                     //обязателен !!
                clustersToCentroid.put(addToThisCluster, thisNewCentroid);
                instancesToClusterNumber.put(inst, instancesToClusterNumber.get(addToThisCluster.get(0)));
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
        return instancesToClusterNumber.get(instance);
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
        if (functionInfo instanceof LinearFunctionInfo){
            funcInitLinear();
        }
        else throw new IllegalArgumentException("Метаданные о функции типа \"" + functionInfo+"\" не поддерживаются пока что.");
    }
    
    public void funcInitLinear(){
        LinearFunctionInfo lfi = (LinearFunctionInfo) functionInfo;
        int accuracyFactor = lfi.getAccuracyFactor();
        Double xMax = lfi.getXMax();
        Double xMin = lfi.getXMin();
        Double yMax = lfi.getYMax();
        Double yLeft = lfi.getYMin();
        funcPoints = new Point2D.Double[accuracyFactor];
        Double xStep=(xMax-xMin)/accuracyFactor;
        Double yStep=(yMax-yLeft)/accuracyFactor;
        for (int i = 0; i < funcPoints.length; i++) {
            funcPoints[i] = new Point2D.Double(xMin+i*xStep, yLeft+i*yStep);
        }
    }
}