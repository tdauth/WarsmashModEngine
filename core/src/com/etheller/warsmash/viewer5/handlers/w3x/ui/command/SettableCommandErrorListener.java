package com.etheller.warsmash.viewer5.handlers.w3x.ui.command;

import com.etheller.warsmash.util.War3ID;

public class SettableCommandErrorListener implements CommandErrorListener {
	private CommandErrorListener delegate;

	@Override
	public void showCommandError(final int playerIndex, final String message) {
		this.delegate.showCommandError(playerIndex, message);
	}

	@Override
	public void showCantPlaceError(final int playerIndex) {
		this.delegate.showCantPlaceError(playerIndex);
	}

	@Override
	public void showCantTransportError(int playerIndex) {
		this.delegate.showCantTransportError(playerIndex);
	}

	@Override
	public void showNoFoodError(final int playerIndex) {
		this.delegate.showNoFoodError(playerIndex);
	}

	@Override
	public void showNoManaError(final int playerIndex) {
		this.delegate.showNoManaError(playerIndex);
	}

	@Override
	public void showInventoryFullError(final int playerIndex) {
		this.delegate.showInventoryFullError(playerIndex);
	}

	public void setDelegate(final CommandErrorListener delegate) {
		this.delegate = delegate;
	}

	@Override
	public void showUnableToFindCoupleTargetError(final int playerIndex) {
		this.delegate.showUnableToFindCoupleTargetError(playerIndex);
	}

	@Override
	public void showBlightRingFullError(final int playerIndex) {
		this.delegate.showBlightRingFullError(playerIndex);
	}

	@Override
	public void showUpgradeCompleteAlert(int playerIndex, War3ID queuedRawcode, int level) {
		this.delegate.showUpgradeCompleteAlert(playerIndex, queuedRawcode, level);
	}
}
