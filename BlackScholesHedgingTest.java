package net.finmath.project;
import net.finmath.stochastic.RandomVariableInterface;





import java.text.DecimalFormat;

 


import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;
import net.finmath.functions.AnalyticFormulas;

public class BlackScholesHedgingTest {

	public static void main(String[] args) throws CalculationException {
		double drift = 0.05;
        double vola = 0.3;
        double maturity=2.0;
        double strike=100;
        double initialValue = 100;
        int numberOfPaths = 1000;
        double mu = -1.0/2 * 0.15 * 0.15;
		double sigma = 0.15;
		double intensity = 0.4;
		
        
        
        TimeDiscretizationInterface times = new TimeDiscretization(0.0, 2000, 0.001);
        AssetModelMonteCarloSimulationInterface jump = new JumpDiffusionMonteCarloBlackScholesModel(times,
				numberOfPaths, initialValue, drift, vola, intensity, mu, sigma);
		
		AssetModelMonteCarloSimulationInterface black = new MonteCarloBlackScholesModel(times, numberOfPaths, initialValue, drift, vola);
		AbstractAssetMonteCarloProduct product = new EuropeanOption(maturity,strike );
		
		
  		double valuejump = product.getValue(jump);
  		double value2 = product.getValue(black);
  		AssetModelMonteCarloSimulationInterface model =new MonteCarloBlackScholesModel(times,numberOfPaths,initialValue,drift,vola);
  		 RandomVariableInterface PayoffHedgedPortfolioDifference=model.getAssetValue(maturity, 0);
  		
        for (int numberofHedgingTimes=2;numberofHedgingTimes<times.getNumberOfTimeSteps()/2;numberofHedgingTimes=numberofHedgingTimes*2){
        BlackScholesHedgedPortfolioWithModifiedTimeDiscretization hedgingPortfolio=new BlackScholesHedgedPortfolioWithModifiedTimeDiscretization(maturity,strike,drift,vola,numberofHedgingTimes);
        RandomVariableInterface Profit/Loss=hedgingPortfolio.
       
     //   System.out.println(hedgingPortfolio.getValue(maturity,model).getAverage());
     
        
        }
	}

}
