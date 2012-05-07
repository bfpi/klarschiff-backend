package de.fraunhofer.igd.klarschiff.web;

import java.util.List;

import de.fraunhofer.igd.klarschiff.service.security.Role;

@SuppressWarnings("serial")
public class VorgangFeedCommand extends VorgangSuchenCommand {

	private List<Role> zustaendigkeiten;

	public List<Role> getZustaendigkeiten() {
		return zustaendigkeiten;
	}

	public void setZustaendigkeiten(List<Role> zustaendigkeiten) {
		this.zustaendigkeiten = zustaendigkeiten;
	}
}
