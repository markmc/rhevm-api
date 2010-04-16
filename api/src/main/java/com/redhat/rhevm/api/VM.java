/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.json.BadgerFish;

/* FIXME: switch from BadgerFish to the Mapped convention */
/* FIXME: could probably drop @XmlAccessorType */

@BadgerFish
@XmlRootElement(name = "vm")
@XmlAccessorType(XmlAccessType.NONE)
public class VM
{
	/* FIXME:
	 * This is a giant hack so that JAX-B marshalls the
	 * superclass rather than the subclass - e.g. we want the type
	 * to be 'VM' not 'DummyVM' when it is passed to jyaml
	 */
	public VM(VM vm) {
		this.link       = vm.link;
		this.id         = vm.id;
		this.name       = vm.name;
		this.actions    = vm.actions;
		this.hostId     = vm.hostId;
		this.templateId = vm.templateId;
		this.clusterId  = vm.clusterId;
	}

	public VM() {
	}

	/* FIXME: can we make this attritbute be auto-generated
	 *        somehow?
	 */
	@XmlElementRef
	public Link getLink() {
		return link;
	}
	public void setLink(Link link) {
		this.link = link;
	}
	protected Link link;

	@XmlElement(name = "id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	protected String id;

	@XmlElement(name = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	protected String name;

	@XmlElement(name = "actions")
	public Actions getActions() {
		return actions;
	}
	public void setActions(Actions actions) {
		this.actions = actions;
	}
	protected Actions actions;

	@XmlElement(name = "host_id")
	public String getHostId() {
		return hostId;
	}
	public void setHostId(String hostId) {
		this.hostId = hostId;
	}
	private String hostId;

	@XmlElement(name = "template_id")
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	private String templateId;

	@XmlElement(name = "cluster_id")
	public String getClusterId() {
		return clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	private String clusterId;
}
