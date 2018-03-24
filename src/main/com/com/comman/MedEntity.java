package com.comman;

public class MedEntity {
	@Override
	public String toString() {
		return "MedEntity [name=" + name + ", isenough=" + isenough + ", oid="
				+ oid + ", oidprice=" + oidprice + "]";
	}

	String name;
	String isenough;
	String oid;
	String oidprice;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIsenough() {
		return isenough;
	}

	public void setIsenough(String isenough) {
		this.isenough = isenough;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getOidprice() {
		return oidprice;
	}

	public void setOidprice(String oidprice) {
		this.oidprice = oidprice;
	}
}
