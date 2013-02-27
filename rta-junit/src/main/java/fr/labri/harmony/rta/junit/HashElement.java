package fr.labri.harmony.rta.junit;

import java.io.Serializable;

public class HashElement implements Serializable {
	
	@Override
	public String toString() {
		return "id=" + id + ", hash=" + hash + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public HashElement(String id, String hash) {
		super();
		this.id = id;
		this.hash = hash;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashElement other = (HashElement) obj;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	private String id;
	private String hash;
	
}
