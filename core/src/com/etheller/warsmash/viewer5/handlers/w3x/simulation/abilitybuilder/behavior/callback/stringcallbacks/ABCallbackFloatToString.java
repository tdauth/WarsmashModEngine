package com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.behavior.callback.stringcallbacks;

import java.util.Map;

import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CSimulation;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CUnit;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.behavior.callback.floatcallbacks.ABFloatCallback;

public class ABCallbackFloatToString extends ABStringCallback {
	
	private ABFloatCallback value;
	
	@Override
	public String callback(CSimulation game, CUnit caster, Map<String, Object> localStore, final int castId) {
		return "" + value.callback(game, caster, localStore, castId);
	}

}
