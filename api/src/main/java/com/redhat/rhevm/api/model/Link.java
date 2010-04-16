/*
 * From Bill Burke's "RESTful Java with JAX-RS", license unknown
 */
package com.redhat.rhevm.api.model;

import java.net.URI;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "link")
@XmlAccessorType(XmlAccessType.NONE)
public class Link
{
	public Link()
	{
	}

	public Link(String rel, URI href)
	{
		this.rel  = rel;
		this.href = href.toString();
	}

	@XmlAttribute(name = "rel")
	public String getRel()
	{
		return rel;
	}

	public void setRel(String rel)
	{
		this.rel = rel;
	}
	private String rel;


	@XmlAttribute(name = "href")
	public String getHref()
	{
		return href;
	}

	public void setHref(String href)
	{
		this.href = href;
	}
	private String href;

	/**
	 * To write as link header
	 *
	 * @return
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder("<");
		builder.append(href).append(">; rel=").append(rel);
		return builder.toString();
	}

	private static Pattern parse = Pattern.compile("<(.+)>\\s*;\\s*(.+)");

	/**
	 * For unmarshalling Link Headers.
	 * Its not an efficient or perfect algorithm and does make a few assumptiosn
	 *
	 * @param val
	 * @return
	 */
	public static Link valueOf(String val)
	{
		Matcher matcher = parse.matcher(val);
		if (!matcher.matches()) throw new RuntimeException("Failed to parse link: " + val);
		Link link = new Link();
		link.href = matcher.group(1);
		String[] props = matcher.group(2).split(";");
		HashMap<String, String> map = new HashMap<String, String>();
		for (String prop : props)
		{
			String[] split = prop.split("=");
			map.put(split[0].trim(), split[1].trim());
		}
		if (map.containsKey("rel"))
		{
			link.rel = map.get("rel");
		}
		return link;
	}
}
