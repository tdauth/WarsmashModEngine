package com.etheller.warsmash.viewer5.handlers.w3x.simulation.behaviors.skills.human.mountainking;

import com.etheller.warsmash.util.WarsmashConstants;
import com.etheller.warsmash.viewer5.handlers.w3x.SequenceUtils;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CSimulation;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CUnit;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CWidget;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilities.skills.human.mountainking.CAbilityThunderBolt;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilities.skills.util.CBuffStun;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilities.targeting.AbilityTarget;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilities.targeting.AbilityTargetStillAliveAndTargetableVisitor;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.abilities.targeting.AbilityTargetUnitVisitor;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.behaviors.CAbstractRangedBehavior;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.behaviors.CBehavior;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.combat.CAttackType;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.combat.projectile.CAbilityProjectileListener;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.orders.OrderIds;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.trigger.enumtypes.CWeaponSoundTypeJass;

public class CBehaviorThunderBolt extends CAbstractRangedBehavior implements CAbilityProjectileListener {
	private final CAbilityThunderBolt ability;
	private final AbilityTargetStillAliveAndTargetableVisitor stillAliveVisitor;
	private int castStartTick = 0;
	private boolean doneEffect = false;

	public CBehaviorThunderBolt(final CUnit unit, final CAbilityThunderBolt ability) {
		super(unit);
		this.ability = ability;
		this.stillAliveVisitor = new AbilityTargetStillAliveAndTargetableVisitor();
	}

	public CBehaviorThunderBolt reset(final CWidget target) {
		innerReset(target, false);
		this.castStartTick = 0;
		this.doneEffect = false;
		return this;
	}

	@Override
	public boolean isWithinRange(final CSimulation simulation) {
		final float castRange = this.ability.getCastRange();
		return this.unit.canReach(this.target, castRange);
	}

	@Override
	protected CBehavior update(final CSimulation simulation, final boolean withinFacingWindow) {
		this.unit.getUnitAnimationListener().playAnimation(false, null, SequenceUtils.SPELL_THROW, 1.0f, true);
		if (this.castStartTick == 0) {
			this.castStartTick = simulation.getGameTurnTick();
		}
		final int ticksSinceCast = simulation.getGameTurnTick() - this.castStartTick;
		final int castPointTicks = (int) (this.unit.getUnitType().getCastPoint()
				/ WarsmashConstants.SIMULATION_STEP_TIME);
		final int backswingTicks = (int) (this.unit.getUnitType().getCastBackswingPoint()
				/ WarsmashConstants.SIMULATION_STEP_TIME);
		if (!this.doneEffect && ((ticksSinceCast >= castPointTicks) || (ticksSinceCast >= backswingTicks))) {
			this.doneEffect = true;
			if (!this.unit.chargeMana(this.ability.getManaCost())) {
				simulation.getCommandErrorListener().showNoManaError(this.unit.getPlayerIndex());
				return this.unit.pollNextOrderBehavior(simulation);
			}
			if (this.target instanceof CUnit) {
				final CUnit targetUnit = (CUnit) this.target;
				simulation.createProjectile(targetUnit, ability.getAlias(), unit.getX(), unit.getY(),
						(float) unit.angleTo(targetUnit), ability.getProjectileSpeed(),
						ability.isProjectileHomingEnabled(), targetUnit, this);
				this.ability.setCooldownRemaining(this.ability.getCooldown());
				this.unit.fireCooldownsChangedEvent();
			}
		}
		if (ticksSinceCast >= backswingTicks) {
			return this.unit.pollNextOrderBehavior(simulation);
		}
		return this;
	}

	@Override
	public void onLaunch(final CSimulation game, final AbilityTarget target) {
	}

	@Override
	public void onHit(final CSimulation game, final AbilityTarget target) {
		final CUnit unitTarget = target.visit(AbilityTargetUnitVisitor.INSTANCE);
		if (unitTarget != null) {
			unitTarget.damage(game, this.unit, CAttackType.SPELLS, CWeaponSoundTypeJass.WHOKNOWS.name(),
					this.ability.getDamage());
			if (!unitTarget.isDead()) {
				unitTarget.add(game, new CBuffStun(game.getHandleIdAllocator().createId(), ability.getBuffId(),
						ability.getDuration(unitTarget)));
			}
		}
	}

	@Override
	protected CBehavior updateOnInvalidTarget(final CSimulation simulation) {
		return this.unit.pollNextOrderBehavior(simulation);
	}

	@Override
	protected boolean checkTargetStillValid(final CSimulation simulation) {
		return this.target.visit(this.stillAliveVisitor.reset(simulation, this.unit, this.ability.getTargetsAllowed()));
	}

	@Override
	protected void resetBeforeMoving(final CSimulation simulation) {
	}

	@Override
	public void begin(final CSimulation game) {
	}

	@Override
	public void end(final CSimulation game, final boolean interrupted) {
	}

	@Override
	public void endMove(final CSimulation game, final boolean interrupted) {
	}

	@Override
	public int getHighlightOrderId() {
		return OrderIds.thunderbolt;
	}
}
