package CAP4630Prog4;  
import java.io.*;
import java.util.*;
import weka.classifiers.Classifier;
import java.text.DecimalFormat;
import weka.core.*;

public class MulticlassPerceptron implements weka.classifiers.Classifier
{
    String fileName;
    int epoch = 0;
    int bias = 0;
    int weightUpdates = 0;
   //double learningRate = 0.0;
    double[] weights;

    public MulticlassPerceptron(String[] options)
    {
        System.out.println("University of Central Florida");
        System.out.println("CAP4630 Artificial Intelligence - Fall 2018");
        System.out.println("Multi-Class Perceptron Classifier");
        System.out.println("Author: Gianlouie Molinary");

        this.bias = 1;
        this.fileName = options[0];
        this.epoch = Integer.parseInt(options[1]);
    }

    public void buildClassifier(Instances data) throws Exception
    {
        int numInst = data.numInstances();

        if (numInst == 0)
        {
            return;
        }

        Instance firstInst = data.firstInstance();

        int numAttributes = firstInst.numAttributes();

        weights = new double[numAttributes];

        for (int i = 0; i < this.epoch; i++)
        {
            System.out.print("Epoch " + i + ": ");

            for (int j = 0; j < numInst; j++)
            {
                Instance inst = data.instance(j);

                int x = inst.numAttributes();

                double[] attributes = new double[x-1];

                for (int k = 0; k < x-1; k++)
                    attributes[k] = inst.value(k);

                double attributeSum = 0.0;

                for (int p = 0; p < x-1; p++)
                    attributeSum += inst.attribute(p).weight()*attributes[p];

                double biasWeight = inst.attribute(x-1).weight()*this.bias;

                double total = biasWeight + attributeSum;
                int biasTotal;

                if (total < 0)
                    biasTotal = -1;

                else
                    biasTotal = 1;

                int expectedTotal;
                double tempValue = inst.value(inst.attribute(x-1));

                if (tempValue == 1.0)
                    expectedTotal = -1;

                else
                    expectedTotal = 0;

                if (biasTotal != expectedTotal)
                {
                    System.out.print("0");

                    this.weightUpdates++;
                }

                else
                    System.out.print("1");
            }

            System.out.print("\n");
        }

        for (int i = 0; i < numAttributes; i++)
            this.weights[i] = data.attribute(i).weight();
    }

    public double[] distributionForInstance(Instance data)
    {
        double[] result = new double[data.numClasses()];
        result[predict(data)] = 1;
        return result;
    }

    int predict(Instance data)
    {
        int x = data.numAttributes();
        double[] attributes = new double[x-1];

        for (int i = 0; i < x-1; i++)
            attributes[i] = data.value(i);

        double sum = 0.0;

        for (int j = 0; j < x-1; j++)
            sum += data.attributes(j).weight() * (attributes[j]);
        
        double biasWeight = data.attribute(x-1).weight()*this.bias;
        double result = sum + biasWeight;
        int index;

        if (result >= 0.0)
        {
            index = 0;
        }

        else 
            index = 1;

        return index;
    }

    public Capabilities getCapabilities()
    {
        return null;
    }

    public double classifyInstance(Instance instance)
    {
        return 0;
    }

    public String toString()
    {
        String finalWeights = "";

        DecimalFormat df = new DecimalFormat("#0.000");

        for (int i = 0; i < this.weights.length; i++)
            finalWeights += (df.format(this.weights[i])+ "\n");
        
        System.out.println("Source file: " + this.fileName);
        System.out.println("Training epochs: " + this.epoch);
        System.out.println("Total # weight updates = " + this.weightUpdates);
        System.out.println("Final weights: \n" + finalWeights);

        return " ";
    }
}