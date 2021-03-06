package com.athaydes.osgimonitor.api.manage;

/**
 * Artifact search options.
 * User: Renato
 */
public enum SearchOption {
	/**
	 * Only return items matching exactly
	 */
	EXACT,
	/**
	 * Return items matching approximately
	 */
	APPROXIMATE,
	/**
	 * Return items whose partial name matches exactly
	 */
	CONTAINING_EXACT
}
