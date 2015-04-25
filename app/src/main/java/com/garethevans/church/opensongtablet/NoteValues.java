package com.garethevans.church.opensongtablet;

public enum NoteValues{
	one("1"),
	two("2"),
	three("3"),
	four("4"),
	five("5"),
	six("6"),
	seven("7"),
	eight("8");
	
	private String noteValue;

	NoteValues(String noteValue) {
		this.noteValue = noteValue;
	}
	
	@Override public String toString() {
	    return noteValue;
	}
}