package net.finmath.project;

import java.util.ArrayList;
import java.util.Map;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionInterface;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.model.AbstractModel;
import net.finmath.montecarlo.process.AbstractProcess;
import net.finmath.montecarlo.process.ProcessEulerScheme;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;

public class MonteCarloBlackScholesDiffusionModel extends AbstractModel implements AssetModelMonteCarloSimulationInterface {

	private final double initialValue;
	private final double riskFreeRate;		
	private final double volatility;
	private final double mean;
	private final double variance;
	private final double intensity;
	private final CompoundPoissonProcessLogNormal jumpProcess;
	private final ProcessEulerScheme jumpfreeprocess;
	
	private final int seed1 = 3141;
	private final int seed2 = 3141;

	private final RandomVariableInterface[]	initialValueVector	= new RandomVariableInterface[1];
	private final RandomVariableInterface	drift;
	private final RandomVariableInterface	volatilityOnPaths;

	/**
	 * Create a Monte-Carlo simulation using given time discretization.
	 * 
	 * @param timeDiscretization The time discretization.
	 * @param numberOfPaths The number of Monte-Carlo path to be used.
	 * @param initialValue Spot value, containing jumpProcess.
	 * @param riskFreeRate The risk free rate.
	 * @param volatility The log volatility.
	 */
	public MonteCarloBlackScholesDiffusionModel(
			TimeDiscretizationInterface timeDiscretization,
			int numberOfPaths,
			double initialValue,
			double riskFreeRate,
			double volatility,
			double intensity,
			double mean,
			double variance) {
		super();

		this.initialValue	= initialValue;
		this.riskFreeRate	= riskFreeRate;
		this.volatility		= volatility;
		this.mean 			= mean;
		this.variance       = variance;
		this.intensity      = intensity;

		// Create a corresponding MC process
		this.jumpProcess = new CompoundPoissonProcessLogNormal(intensity, mean, variance,
				timeDiscretization, numberOfPaths,seed2);
		this.jumpfreeprocess = new ProcessEulerScheme(new BrownianMotion(timeDiscretization, 1 /* numberOfFactors */, numberOfPaths, seed1));
		AbstractProcess DiffusionProcess = new DiffusionProcessEulerScheme(jumpfreeprocess,jumpProcess);
		/*
		 * The interface definition requires that we provide the initial value, the drift and the volatility in terms of random variables.
		 * We construct the corresponding random variables here and will return (immutable) references to them.
		 *
		 * Since the underlying process is configured to simulate log(S),
		 * the initial value and the drift are transformed accordingly.
		 *
		 */
		this.initialValueVector[0]	= DiffusionProcess.getBrownianMotion().getRandomVariableForConstant(Math.log(initialValue));
		this.drift					= DiffusionProcess.getBrownianMotion().getRandomVariableForConstant(riskFreeRate - volatility * volatility / 2.0);
		this.volatilityOnPaths		= DiffusionProcess.getBrownianMotion().getRandomVariableForConstant(volatility);

		// Link model and process for delegation
		DiffusionProcess.setModel(this);
		this.setProcess(DiffusionProcess);
	}
	
	public MonteCarloBlackScholesDiffusionModel(
			double initialValue,
			double riskFreeRate,
			double volatility,
			ProcessEulerScheme process,
			CompoundPoissonProcessLogNormal jumpProcess) {
		super();
		
		this.jumpProcess = jumpProcess;
		this.jumpfreeprocess = process; 
		AbstractProcess DiffusionProcess = new DiffusionProcessEulerScheme(process,jumpProcess);

		this.initialValue	= initialValue;
		this.riskFreeRate	= riskFreeRate;
		this.volatility		= volatility;
		this.mean 			= jumpProcess.getMean();
		this.variance       = jumpProcess.getVariance();
		this.intensity      = jumpProcess.getIntensity();

		/*
		 * The interface definition requires that we provide the drift and the volatility in terms of random variables.
		 * We construct the corresponding random variables here and will return (immutable) references to them.
		 */
		this.initialValueVector[0]	= DiffusionProcess.getBrownianMotion().getRandomVariableForConstant(Math.log(initialValue));
		this.drift					= DiffusionProcess.getBrownianMotion().getRandomVariableForConstant(riskFreeRate - 0.5 * volatility*volatility);
		this.volatilityOnPaths		= DiffusionProcess.getBrownianMotion().getRandomVariableForConstant(volatility);
		
		// Link model and process for delegation
		DiffusionProcess.setModel(this);
		this.setProcess(DiffusionProcess);
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.model.AbstractModelInterface#getInitialState()
	 */
	@Override
	public RandomVariableInterface[] getInitialState() {
		return initialValueVector;
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.model.AbstractModelInterface#getDrift(int, net.finmath.stochastic.RandomVariableInterface[], net.finmath.stochastic.RandomVariableInterface[])
	 */
	@Override
	public RandomVariableInterface[] getDrift(int timeIndex, RandomVariableInterface[] realizationAtTimeIndex, RandomVariableInterface[] realizationPredictor) {
		return new RandomVariableInterface[] { drift };
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.model.AbstractModelInterface#getFactorLoading(int, int, net.finmath.stochastic.RandomVariableInterface[])
	 */
	@Override
	public RandomVariableInterface[] getFactorLoading(int timeIndex, int component, RandomVariableInterface[] realizationAtTimeIndex) {
		return new RandomVariableInterface[] { volatilityOnPaths };
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.model.AbstractModelInterface#applyStateSpaceTransform(int, net.finmath.stochastic.RandomVariableInterface)
	 */
	@Override
	public RandomVariableInterface applyStateSpaceTransform(int componentIndex, RandomVariableInterface randomVariable) {
		return randomVariable.exp();
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface#getAssetValue(double, int)
	 */
	@Override
	public RandomVariableInterface getAssetValue(double time, int assetIndex) throws CalculationException {
		return getAssetValue(getTimeIndex(time), assetIndex);
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface#getAssetValue(int, int)
	 */
	@Override
	public RandomVariableInterface getAssetValue(int timeIndex, int assetIndex) throws CalculationException {
		return getProcessValue(timeIndex, assetIndex);
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface#getNumeraire(int)
	 */
	@Override
	public RandomVariableInterface getNumeraire(int timeIndex) {
		double time = getTime(timeIndex);

		return getNumeraire(time);
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.model.AbstractModelInterface#getNumeraire(double)
	 */
	@Override
	public RandomVariableInterface getNumeraire(double time) {
		double numeraireValue = Math.exp(riskFreeRate * time);

		return getRandomVariableForConstant(numeraireValue);
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface#getRandomVariableForConstant(double)
	 */
	@Override
	public RandomVariableInterface getRandomVariableForConstant(double value) {
		return getProcess().getBrownianMotion().getRandomVariableForConstant(value);
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.model.AbstractModelInterface#getNumberOfComponents()
	 */
	@Override
	public int getNumberOfComponents() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationInterface#getNumberOfAssets()
	 */
	@Override
	public int getNumberOfAssets() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "\n" +
				"MonteCarloBlackScholesModel:\n" +
				"  initial value...:" + initialValue + "\n" +
				"  risk free rate..:" + riskFreeRate + "\n" +
				"  volatiliy.......:" + volatility;
	}

	/**
	 * Returns the risk free rate parameter of this model.
	 *
	 * @return Returns the riskFreeRate.
	 */
	public double getRiskFreeRate() {
		return riskFreeRate;
	}

	/**
	 * Returns the volatility parameter of this model.
	 * 
	 * @return Returns the volatility.
	 */
	public double getVolatility() {
		return volatility;
	}

	/**
	 * @return The number of paths.
	 * @see net.finmath.montecarlo.process.AbstractProcess#getNumberOfPaths()
	 */
	@Override
	public int getNumberOfPaths() {
		return getProcess().getNumberOfPaths();
	}

	@Override
	public AssetModelMonteCarloSimulationInterface getCloneWithModifiedSeed(
			int seed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RandomVariableInterface getMonteCarloWeights(double time)
			throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AssetModelMonteCarloSimulationInterface getCloneWithModifiedData(
			Map<String, Object> dataModified) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}
}
