package com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.ability;

import java.util.List;
import java.util.Map;

import com.etheller.warsmash.util.War3ID;
import com.etheller.warsmash.util.WarsmashConstants;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CSimulation;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CUnit;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CUnitClassification;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CWidget;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilities.generic.AbstractGenericSingleIconNoSmartActiveAbility;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilities.targeting.AbilityPointTarget;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.behavior.CBehaviorAbilityBuilderBase;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.core.ABAction;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.core.ABCondition;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.core.ABLocalStoreKeys;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.parser.AbilityBuilderConfiguration;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilitybuilder.types.impl.CAbilityTypeAbilityBuilderLevelData;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.behaviors.CBehavior;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.orders.OrderIdUtils;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.players.CAllianceType;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.players.CPlayer;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.util.AbilityActivationReceiver;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.util.AbilityTargetCheckReceiver;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.util.ResourceType;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.util.AbilityTargetCheckReceiver.TargetType;

public class CAbilityAbilityBuilderActiveUnitTarget extends AbstractGenericSingleIconNoSmartActiveAbility implements AbilityBuilderAbility{

	List<CAbilityTypeAbilityBuilderLevelData> levelData;
	private AbilityBuilderConfiguration config;
	private Map<String, Object> localStore;
	private CBehaviorAbilityBuilderBase behavior;
	private int orderId;
	
	private float cooldown = 0;
	private int manaCost = 0;

	public CAbilityAbilityBuilderActiveUnitTarget(int handleId, War3ID alias,
			List<CAbilityTypeAbilityBuilderLevelData> levelData, AbilityBuilderConfiguration config,
			Map<String, Object> localStore) {
		super(handleId, alias);
		this.levelData = levelData;
		this.config = config;
		this.localStore = localStore;
		orderId = OrderIdUtils.getOrderId(config.getCastId());
	}

	@Override
	public void setLevel(int level) {
		super.setLevel(level);
		localStore.put(ABLocalStoreKeys.CURRENTLEVEL, level);
		CAbilityTypeAbilityBuilderLevelData levelDataLevel = this.levelData.get(this.getLevel() - 1);
		this.manaCost = levelDataLevel.getManaCost();
		this.cooldown = levelDataLevel.getCooldown();
	}

	@Override
	public int getBaseOrderId() {
		return orderId;
	}

	@Override
	public int getUIManaCost() {
		return this.manaCost;
	}

	@Override
	public boolean isToggleOn() {
		return false;
	}

	@Override
	public void onAdd(CSimulation game, CUnit unit) {
		this.behavior = new CBehaviorAbilityBuilderBase(unit, config, localStore, this);
		if (config.getOnAddAbility() != null) {
			for (ABAction action : config.getOnAddAbility()) {
				action.runAction(game, unit, localStore);
			}
		}
	}

	@Override
	public void onRemove(CSimulation game, CUnit unit) {
		if (config.getOnRemoveAbility() != null) {
			for (ABAction action : config.getOnRemoveAbility()) {
				action.runAction(game, unit, localStore);
			}
		}
	}

	@Override
	public void onTick(CSimulation game, CUnit unit) {
		if (config.getOnTickPreCast() != null) {
			for (ABAction action : config.getOnTickPreCast()) {
				action.runAction(game, unit, localStore);
			}
		}
	}

	@Override
	public void onDeath(CSimulation game, CUnit unit) {
		if (config.getOnDeathPreCast() != null) {
			for (ABAction action : config.getOnDeathPreCast()) {
				action.runAction(game, unit, localStore);
			}
		}
	}

	@Override
	public void onCancelFromQueue(CSimulation game, CUnit unit, int orderId) {
	}

	@Override
	public CBehavior begin(CSimulation game, CUnit caster, int orderId, CWidget target) {
		return this.behavior.reset(target);
	}

	@Override
	public CBehavior begin(CSimulation game, CUnit caster, int orderId, AbilityPointTarget point) {
		return null;
	}

	@Override
	public CBehavior beginNoTarget(CSimulation game, CUnit caster, int orderId) {
		return null;
	}

	@Override
	protected void innerCheckCanTarget(CSimulation game, CUnit unit, int orderId, CWidget target,
			AbilityTargetCheckReceiver<CWidget> receiver) {
		if ((target instanceof CUnit) && target.canBeTargetedBy(game, unit, this.levelData.get(this.getLevel()).getTargetsAllowed())) {
			if (!unit.isMovementDisabled() || unit.canReach(target, this.levelData.get(this.getLevel()).getCastRange())) {
				final CUnit targetUnit = (CUnit) target;
				final CPlayer player = game.getPlayer(unit.getPlayerIndex());
				
				if (this.config.getExtraTargetConditions() != null) {
					boolean result = true;
					for (ABCondition condition : config.getExtraTargetConditions()) {
						result = result && condition.evaluate(game, unit, localStore);
					}
					if (result) {
						receiver.targetOk(targetUnit);
					} else {
						receiver.targetTooComplicated();
					}
				} else {
					receiver.targetOk(targetUnit);
				}
			}
			else {
				receiver.targetOutsideRange();
			}
		}
		else {
			receiver.mustTargetType(TargetType.UNIT);
		}
	}

	@Override
	protected void innerCheckCanTarget(CSimulation game, CUnit unit, int orderId, AbilityPointTarget target,
			AbilityTargetCheckReceiver<AbilityPointTarget> receiver) {
		receiver.orderIdNotAccepted();
	}

	@Override
	protected void innerCheckCanTargetNoTarget(CSimulation game, CUnit unit, int orderId,
			AbilityTargetCheckReceiver<Void> receiver) {
		receiver.orderIdNotAccepted();
	}

	@Override
	protected void innerCheckCanUse(CSimulation game, CUnit unit, int orderId, AbilityActivationReceiver receiver) {
		float cooldownRemaining = unit.getCooldownForId(this.orderId);
		if (cooldownRemaining > 0) {
			receiver.cooldownNotYetReady(cooldownRemaining, this.cooldown);
		} else if (unit.getMana() < this.manaCost) {
			receiver.notEnoughResources(ResourceType.MANA);
		} else if (config.getExtraCastConditions() != null) {
			boolean result = true;
			for (ABCondition condition : config.getExtraCastConditions()) {
				result = result && condition.evaluate(game, unit, localStore);
			}
			if (result) {
				receiver.useOk();
			} else {
				receiver.unknownReasonUseNotOk();
			}
		} else {
			receiver.useOk();
		}
	}
	
	public void startCooldown(CUnit unit) {
		unit.addCooldown(this.orderId, this.cooldown);
	}

	@Override
	public List<CAbilityTypeAbilityBuilderLevelData> getLevelData() {
		return levelData;
	}

	@Override
	public AbilityBuilderConfiguration getConfig() {
		return config;
	}

	@Override
	public Map<String, Object> getLocalStore() {
		return localStore;
	}

}
