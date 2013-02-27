package fr.labri.harmony.rta.junit;

import java.util.Comparator;

public class HashElementIdComparator implements Comparator<HashElement> {

	@Override
	public int compare(HashElement o1, HashElement o2) {
		// TODO Auto-generated method stub
		return o1.getId().compareTo(o2.getId());
	}

}
