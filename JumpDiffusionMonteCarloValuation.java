package net.finmath.project;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
//import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.time.TimeDiscretization;
/*
 * Here we import the the JumpDiffusionBlackScholesModel to be created by modifying the class 
 * MonteCarloBlackScholesModel (It should implement AssetMonteCarloSimulationInterface
 * 
 */
//

public class JumpDiffusionMonteCarloValuation {
	
public static void main(String[] args) throws CalculationException {
		
//		AssetModelMonteCarloSimulationInterface model = new MonteCarloJumpDiffusionModel(new TimeDiscretization(0, 10, 0.5), 10000, 100, 0.05, 0.20);

		AbstractAssetMonteCarloProduct product = new EuropeanOption(2.0, 110);
		
//		double value = product.getValue(model);
		
//		System.out.println("The value is " + value);
	}


}
