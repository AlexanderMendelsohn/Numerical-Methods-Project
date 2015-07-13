package projectTest;
import net.finmath.stochastic.RandomVariableInterface;






import java.text.DecimalFormat;

 



import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariable;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.montecarlo.assetderivativevaluation.products.BlackScholesHedgedPortfolio.HedgeStrategy;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.assetderivativevaluation.products.BlackScholesHedgedPortfolio;
import net.finmath.montecarlo.assetderivativevaluation.products.BlackScholesHedgedPortfolio.HedgeStrategy;

public class BlackScholesHedgingTest2 {

	public static void main(String[] args) throws CalculationException {
		double drift = 0.05;
        double vola = 0.3;
        double maturity=2.0;
        double strike=100;
        double initialValue = 100;
        int numberOfPaths = 10;
        double mu = -1.0/2 * 0.15 * 0.15;
		double sigma = 0.55;
		double intensity = 0.4;
		
        
        
        TimeDiscretizationInterface times = new TimeDiscretization(0.0, 2000, 0.001);
        
        /*Jump Diffusion Model*/
        AssetModelMonteCarloSimulationInterface jump = new JumpDiffusionMonteCarloBlackScholesModel(times,
				numberOfPaths, initialValue, drift, vola, intensity, mu, sigma);
		
        /*Black Scholes Model*/
		AssetModelMonteCarloSimulationInterface black = new MonteCarloBlackScholesModel(times, numberOfPaths, initialValue, drift, vola);
		
		/*European Option -> insert model*/
		AbstractAssetMonteCarloProduct product = new EuropeanOption(maturity,strike );
		
		/*Price of European Option on Jump-Diffusion-Model at time 0*/
  		double EuropeanPriceJump = product.getValue(jump);
  		/*Price of European Option on Black-Scholes-Model at time 0*/
  		double EuropeanPriceBlackScholes = product.getValue(black);
  		
  		/*Value of Jump-Process at maturity*/
  		RandomVariableInterface valueJumpAtMaturity = jump.getAssetValue(maturity, 0);
  		/*Value of European Option in Jump-Process at maturity*/
  		RandomVariableInterface valueJumpEuropeanOptionAtMaturity = valueJumpAtMaturity.sub(strike).floor(0);
  		
  		/*Value of Black-Scholes-Model at maturity*/
  		RandomVariableInterface valueBlackScholesAtMaturity = black.getAssetValue(maturity,0);
  		/*Value of European Option on BlackScholesModel at maturity*/
  		RandomVariableInterface valueBlackScholesEuropeanOptionAtMaturity = valueBlackScholesAtMaturity.sub(strike).floor(0);
  		
  		int hedgingIncrement = 500;
  		
  		RandomVariableInterface[] relativePandLJump = new RandomVariableInterface[(int)times.getNumberOfTimeSteps()/hedgingIncrement]; 
  		RandomVariableInterface[] relativePandLBlackScholes = new RandomVariableInterface[(int)times.getNumberOfTimeSteps()/hedgingIncrement]; 
  		RandomVariableInterface[] differencePortfolioToOptionPriceJump = new RandomVariableInterface[(int)times.getNumberOfTimeSteps()/hedgingIncrement]; 
  		RandomVariableInterface[] differencePortfolioToOptionPriceBlackScholes = new RandomVariableInterface[(int)times.getNumberOfTimeSteps()/hedgingIncrement]; 
  		
  		
  		int numberOfHedgingTimes = 0;
  		/*Loop over numberOfHedging times to get portfolio values (for each hedging time this is a random variable with numberOfPaths paths)*/
        for (int i=0;numberOfHedgingTimes<times.getNumberOfTimeSteps()/2;i++){
        	numberOfHedgingTimes =  numberOfHedgingTimes + hedgingIncrement;
        	BlackScholesHedgedPortfolioWithModifiedTimeDiscretization hedgingPortfolioValue = new BlackScholesHedgedPortfolioWithModifiedTimeDiscretization(maturity,strike,drift,vola,numberOfHedgingTimes);
        	RandomVariableInterface portfolioValueJump = hedgingPortfolioValue.getValue(maturity, jump);
        	RandomVariableInterface portfolioValueBlackScholes = hedgingPortfolioValue.getValue(maturity, black);
        	differencePortfolioToOptionPriceJump[i] = portfolioValueJump.sub(valueJumpEuropeanOptionAtMaturity);
        	differencePortfolioToOptionPriceBlackScholes[i] = portfolioValueBlackScholes.sub(valueBlackScholesEuropeanOptionAtMaturity);
        	relativePandLJump[i] = differencePortfolioToOptionPriceJump[i].div(EuropeanPriceJump).mult(Math.exp(-drift * maturity));
        	relativePandLBlackScholes[i] = differencePortfolioToOptionPriceBlackScholes[i].div(EuropeanPriceBlackScholes).mult(Math.exp(-drift * maturity));
        	System.out.println("Printing for the hedgingtime: " + numberOfHedgingTimes);
        	/*for (int j = 0; j<numberOfPaths;j++){
        		System.out.println(relativePandLJump[i].get(j));
        	}*/
        	System.out.println("variance PandL for jump: " + relativePandLJump[i].getVariance());
        	System.out.println("variance PandL for b&s: " + relativePandLBlackScholes[i].getVariance());
        }
        
        
     //   System.out.println(hedgingPortfolio.getValue(maturity,model).getAverage());

       	BlackScholesHedgedPortfolio deltaGamma = new BlackScholesHedgedPortfolio(maturity, strike, drift, vola,
    			5, 110, HedgeStrategy.deltaGammaHedge);
       	RandomVariableInterface relativePandLBlackJumpDeltaGamma;
  		RandomVariableInterface differencePortfolioToOptionPriceJumpDeltaGamma;
  		RandomVariableInterface portfolioValueJump = deltaGamma.getValue(maturity, jump);
  		differencePortfolioToOptionPriceJumpDeltaGamma = portfolioValueJump.sub(valueJumpEuropeanOptionAtMaturity);
  		relativePandLBlackJumpDeltaGamma = differencePortfolioToOptionPriceJumpDeltaGamma.div(EuropeanPriceJump).mult(Math.exp(-drift * maturity));
  		for (int j = 0; j<numberOfPaths; j++){
  			System.out.println(relativePandLBlackJumpDeltaGamma.get(j));
  		}
        
    }
	

}
