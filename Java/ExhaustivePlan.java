import java.io.FileNotFoundException;
import java.util.List;

public class ExhaustivePlan extends ExperimentPlan {

    int experimentNumber;
    int sizeParameterSpace;

    public ExhaustivePlan() {
        experimentNumber = -1;
        sizeParameterSpace = 1;
    }

    public void addParameter(Parameter p) {
        super.addParameter(p);
        sizeParameterSpace = sizeParameterSpace * p.nbValues();
    }

    public int getExperimentNumber() {
        return experimentNumber;
    }


    @Override
    public List<Parameter> nextParametersSet() {
        experimentNumber++;
        if (experimentNumber != 0) {
            parameters.get(0).setNextValue();
            int previousDomainSpace = 1;
            for (int i = 1; i < parameters.size(); i++) {
                previousDomainSpace = previousDomainSpace * parameters.get(i - 1).nbValues();
                if ((experimentNumber % previousDomainSpace) == 0) {
                    parameters.get(i).setNextValue();
                }
            }
        }
        return parameters;
    }

    @Override
    public boolean hasNextParametersSet() {
        return experimentNumber < sizeParameterSpace - 1;
    }


    public static void main(String[] args) throws FileNotFoundException {
        ExhaustivePlan exp = new ExhaustivePlan();

        exp.setEperimentName("Headless");
        exp.setFinalStep(200);
        exp.setPath("/home/quan/gama_workspace/ProjectUSTH-LUP/models/Model_Complete.gaml");
        exp.setStopCondition("cycle > 200 or current_date.year = 2010");

        exp.addParameter(new Parameter("risk_control", "FLOAT", 0.5, 2.5, 0.5));
        exp.addParameter(new Parameter("land_price", "INT", 50000 , 200000, 50000));

        exp.addOutput(new Output("gdp", 1, "1"));

        double maxGDP = 0;
        List<Parameter> maxPs = null;


        while (exp.hasNextParametersSet()) {
            System.out.println("--------" + exp.getExperimentNumber() + "-----------");
            List<Parameter> ps = exp.nextParametersSet();
            ps.stream().forEach(p -> System.out.println(p));

            // absolute Path mandatory !!
            String XMLFilepath = "/home/quan/Downloads/USTH/GAML/farmer" + exp.getExperimentNumber();
            exp.writeXMLFile(XMLFilepath + ".xml");

            GAMACaller gama = new GAMACaller(XMLFilepath + ".xml", XMLFilepath);
            gama.runGAMA();

            XMLReader read = new XMLReader(XMLFilepath + "/simulation-outputs.xml");
            read.parseXmlFile();
            double gdp = Double.parseDouble(read.getFinalValueOf("gdp"));
            System.out.println("GDP: " + gdp);
            if (maxGDP < gdp) {
                maxGDP = gdp;
                maxPs = ps;
            }
        }
        System.out.println("Max GDP: " + maxGDP);
        System.out.println("min parameter set:");
        maxPs.stream().forEach(p -> System.out.println(p));
    }
}