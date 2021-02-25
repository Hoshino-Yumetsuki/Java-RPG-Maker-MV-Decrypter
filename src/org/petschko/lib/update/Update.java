package org.petschko.lib.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Author: Peter Dragicevic [peter&#064;petschko.org]
 * Authors-Website: http://petschko.org/
 * Date: 05.10.2017
 * Time: 14:37
 * Update: -
 * Version: 0.0.1
 *
 * Notes: Update Class
 */
public class Update {
	private URL checkVersionUrl;
	private URL whatsNewUrl = null;
	private URL downloadURL = null;
	private Version currentVersion;
	private Version newestVersion = null;
	private boolean hasNewVersion = false;

	/**
	 * Update Constructor
	 *
	 * @param checkVersionUrl - URL to get the newest Version-Number
	 * @param currentVersion - Current Version
	 * @throws IOException - IO-Exception
	 */
	public Update(String checkVersionUrl, String currentVersion) throws IOException {
		this.checkVersionUrl = new URL(checkVersionUrl);
		this.currentVersion = new Version(currentVersion);

		this.init();
	}

	/**
	 * Gets the Whats-New URL
	 *
	 * @return - Whats-New URL
	 */
	public URL getWhatsNewUrl() {
		return whatsNewUrl;
	}

	/**
	 * Initiates this instance
	 */
	private void init() throws IOException {
		this.getUpdateInfo();
		this.checkVersion();
	}

	/**
	 * Get all information from the File for the Update process
	 */
	private void getUpdateInfo() throws IOException {
		// Read the Update-URL
		InputStream content = this.checkVersionUrl.openStream();


		// Convert the read Content to Strings
		int c;
		int currentString = 0;
		StringBuilder version = new StringBuilder();
		StringBuilder downloadUrl = new StringBuilder();
		StringBuilder whatsNewUrl = new StringBuilder();

		while(true) {
			try {
				c = content.read();

				// Exit loop if file reaches end
				if(c == -1)
					break;

				if(c == (int) ';')
					currentString++;
				else {
					switch(currentString) {
						case 0:
							version.append((char) c);
							break;
						case 1:
							downloadUrl.append((char) c);
							break;
						case 2:
							whatsNewUrl.append((char) c);
							break;
						default:
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
				break;
			}
		}

		this.newestVersion = new Version(version.toString().trim());

		try {
			this.downloadURL = new URL(downloadUrl.toString().trim());
			this.whatsNewUrl = new URL(whatsNewUrl.toString().trim());
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the Version is the newest
	 */
	private void checkVersion() {
		if(this.newestVersion == null)
			return;

		this.hasNewVersion = ! this.currentVersion.versionsEqual(this.newestVersion);
	}

	/**
	 * Shows if the current Version is the newest
	 *
	 * @return - Is newest Version
	 */
	public boolean isHasNewVersion() {
		return this.hasNewVersion;
	}

	/**
	 * Get the newest Version-Number
	 *
	 * @return - Newest Version-Number or null if could not read Update-URL
	 */
	public String getNewestVersion() {
		return newestVersion.getVersion();
	}

	/**
	 * Get the current Version
	 *
	 * @return - Current Version
	 */
	public String getCurrentVersion() {
		return currentVersion.getVersion();
	}

	/**
	 * Starts the updater
	 */
	public void runUpdate() throws UpdateException {
		File updaterFile = new File("update.jar");

		if(this.newestVersion == null)
			throw new UpdateException("Newest Version is not set!", this.currentVersion);

		if(this.newestVersion.versionsEqual(new Version("")))
			throw new UpdateException("Newest Version is empty...", this.currentVersion);

		if(this.newestVersion.versionsEqual(this.currentVersion))
			throw new UpdateException("This Program is already up to date!", this.currentVersion, this.newestVersion);

		if(! updaterFile.exists() || updaterFile.isDirectory())
			throw new UpdateException("Updater not found!", this.currentVersion);

		String[] run = {
				"java",
				"-jar",
				"update.jar"
		};

		try {
			Runtime.getRuntime().exec(run);
		} catch (Exception e) {
			throw new UpdateException(e.getMessage(), this.currentVersion, e);
		}
		System.exit(0);
	}
}