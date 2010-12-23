package org.mpain.reaper.service.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "package", namespace = "http://www.idpf.org/2007/opf")
public class OPFPackage {
	@XmlAttribute(name = "version", namespace = "http://www.idpf.org/2007/opf")
	private int version;
	
	@XmlAttribute(name = "unique-identifier", namespace = "http://www.idpf.org/2007/opf")
	private String identifier;
	
	@XmlElement(name = "metadata", namespace = "http://www.idpf.org/2007/opf")
	private OPFMetadata metadata;
	
	@XmlElementWrapper(name = "manifest", namespace = "http://www.idpf.org/2007/opf")
	@XmlElement(name = "item", namespace = "http://www.idpf.org/2007/opf")
	private OPFManifestItem item;
	
	@XmlElement(name = "spine", namespace = "http://www.idpf.org/2007/opf")
	private OPFSpine spine;

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public OPFMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(OPFMetadata metadata) {
		this.metadata = metadata;
	}

	public OPFManifestItem getItem() {
		return item;
	}

	public void setItem(OPFManifestItem item) {
		this.item = item;
	}

	public OPFSpine getSpine() {
		return spine;
	}

	public void setSpine(OPFSpine spine) {
		this.spine = spine;
	}
}
