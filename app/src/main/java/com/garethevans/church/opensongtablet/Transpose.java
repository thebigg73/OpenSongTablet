package com.garethevans.church.opensongtablet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;

public class Transpose extends Activity {
	
	public static String oldchordformat;
	
	public static void doTranspose() throws IOException {
		// Go through each line and change each chord to $..$
		// This marks the bit to be changed

		FullscreenActivity.transposedLyrics = null;
		FullscreenActivity.transposedLyrics = "";
		FullscreenActivity.myTransposedLyrics = null;
		FullscreenActivity.myTransposedLyrics = FullscreenActivity.myLyrics.split("\n");

		oldchordformat = FullscreenActivity.oldchordformat;

		// Change the saved key
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("A#", "$.1.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("B#", "$.3.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("C#", "$.4.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("D#", "$.6.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("E#", "$.8.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("F#", "$.9.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("G#", "$.11.$");
		// Do the flats next
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("Ab", "$.11.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("Bb", "$.1.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("Cb", "$.2.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("Db", "$.4.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("Eb", "$.6.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("Fb", "$.7.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("Gb", "$.9.$");
		// Do the white notes next
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("A", "$.0.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("B", "$.2.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("C", "$.3.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("D", "$.5.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("E", "$.7.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("F", "$.8.$");
		FullscreenActivity.mKey = FullscreenActivity.mKey.replace("G", "$.10.$");
		
		for (int x = 0; x < FullscreenActivity.numrowstowrite; x++) {
			if (FullscreenActivity.myTransposedLyrics[x].indexOf(".") == 0) {
				// Since this line has chords, do the changing!
				// If chordFormat == 1 --> Standard chord notation
				// If chordFormat == 2 --> Eastern European (B/H)
				// If chordFormat == 3 --> Eastern European (B/H) and is/es/s
				// If chordFormat == 4 --> Doh a deer

				// Do the sharps first
				// A=0 A#/Bb=1 B=2 C=3 C#/Db=4 D=5
				// D#/Eb=6 E=7 F=8 F#/Gb=9 G=10 G#/Ab=11
				
				//Normal format chords
				switch (oldchordformat) {
					case "1":
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" A#m", "$.31.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".A#m", "$.51.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("A#", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" B#m", "$.33.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".B#m", "$.53.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("B#", "$.3.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" C#m", "$.34.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".C#m", "$.54.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("C#", "$.4.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" D#m", "$.36.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".D#m", "$.56.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("D#", "$.6.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" E#m", "$.38.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".E#m", "$.58.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("E#", "$.8.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" F#m", "$.39.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".F#m", "$.59.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("F#", "$.9.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" G#m", "$.41.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".G#m", "$.61.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("G#", "$.11.$");
						// Do the flats next
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Abm", "$.41.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Abm", "$.61.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Ab", "$.11.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Bbm", "$.31.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Bbm", "$.51.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Bb", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Cbm", "$.32.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Cbm", "$.52.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Cb", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Dbm", "$.34.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Dbm", "$.54.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Db", "$.4.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Ebm", "$.36.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Ebm", "$.56.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Eb", "$.6.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Fbm", "$.37.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Fbm", "$.57.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Fb", "$.7.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Gbm", "$.39.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Gbm", "$.59.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Gb", "$.9.$");
						// Do the white notes next
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Am", "$.30.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Am", "$.50.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("A", "$.0.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Bm", "$.32.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Bm", "$.52.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("B", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Cm", "$.33.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Cm", "$.53.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("C", "$.3.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Dm", "$.35.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Dm", "$.55.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("D", "$.5.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Em", "$.37.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Em", "$.57.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("E", "$.7.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Fm", "$.38.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Fm", "$.58.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("F", "$.8.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Gm", "$.40.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Gm", "$.60.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("G", "$.10.$");


						//Eastern European (B/H)
						break;
					case "2":
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" A#m", "$.31.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".A#m", "$.51.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("A#", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" B#m", "$.32.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".B#m", "$.52.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("B#", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" H#m", "$.33.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".H#m", "$.53.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("H#", "$.3.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" C#m", "$.34.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".C#m", "$.54.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("C#", "$.4.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" D#m", "$.36.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".D#m", "$.56.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("D#", "$.6.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" E#m", "$.38.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".E#m", "$.58.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("E#", "$.8.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" F#m", "$.39.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".F#m", "$.59.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("F#", "$.9.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" G#m", "$.41.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".G#m", "$.61.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("G#", "$.11.$");
						// Do the flats next
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Abm", "$.41.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Abm", "$.61.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Ab", "$.11.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Bbm", "$.30.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Bbm", "$.50.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Bb", "$.0.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Hbm", "$.31.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Hbm", "$.51.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Hb", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Cbm", "$.32.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Cbm", "$.52.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Cb", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Dbm", "$.34.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Dbm", "$.54.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Db", "$.4.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Ebm", "$.36.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Ebm", "$.56.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Eb", "$.6.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Fbm", "$.37.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Fbm", "$.57.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Fb", "$.7.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Gbm", "$.39.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Gbm", "$.59.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Gb", "$.9.$");
						// Do the white notes next
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Am", "$.30.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Am", "$.50.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("A", "$.0.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Bm", "$.31.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Bm", "$.51.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("B", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Hm", "$.32.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Hm", "$.52.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("H", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Cm", "$.33.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Cm", "$.53.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("C", "$.3.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Dm", "$.35.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Dm", "$.55.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("D", "$.5.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Em", "$.37.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Em", "$.57.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("E", "$.7.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Fm", "$.38.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Fm", "$.58.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("F", "$.8.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" Gm", "$.40.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".Gm", "$.60.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("G", "$.10.$");
						//Eastern European (B/H) and is/es
						break;
					case "3":
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Ais", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" ais", "$.31.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".ais", "$.51.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Bis", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" ais", "$.32.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".ais", "$.52.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("His", "$.3.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" his", "$.33.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".his", "$.53.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Cis", "$.4.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" cis", "$.34.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".cis", "$.54.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Dis", "$.6.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" dis", "$.36.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".dis", "$.56.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Eis", "$.8.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" eis", "$.38.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".eis", "$.58.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Fis", "$.9.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" fis", "$.39.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".fis", "$.59.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Gis", "$.11.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" gis", "$.41.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".gis", "$.61.$");
						// Do the flats next
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("As", "$.11.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" as", "$.41.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".as", "$.61.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Bes", "$.0.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" bes", "$.30.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".bes", "$.50.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Hes", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" hes", "$.31.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".hes", "$.51.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Ces", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" ces", "$.32.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".ces", "$.52.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Des", "$.4.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" des", "$.34.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".des", "$.54.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Es", "$.6.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" es", "$.36.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".es", "$.56.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Fes", "$.7.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" fes", "$.37.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".fes", "$.57.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Ges", "$.9.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" ges", "$.39.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".ges", "$.59.$");
						// Do the white notes next
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("A", "$.0.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" a", "$.30.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".a", "$.50.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("B", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" b", "$.31.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".b", "$.51.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("H", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" h", "$.32.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".h", "$.52.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("C", "$.3.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" c", "$.33.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".c", "$.53.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("D", "$.5.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" d", "$.35.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".d", "$.55.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("E", "$.7.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" e", "$.37.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".e", "$.57.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("F", "$.8.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" f", "$.38.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".f", "$.58.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("G", "$.10.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(" g", "$.40.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace(".g", "$.60.$");

						//Doh a deer

						break;
					case "4":
						//Do all sharps
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Do#", "$.4.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Re#", "$.6.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Mi#", "$.8.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Fa#", "$.9.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Sol#", "$.11.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("La#", "$.1.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Si#", "$.3.$");
						// Do the flats next
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Dob", "$.2.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Reb", "$.4.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Mib", "$.6.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Fab", "$.7.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Solb", "$.9.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Lab", "$.11.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Sib", "$.1.$");
						// Do the white notes next
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Do", "$.3.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Re", "$.5.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Mi", "$.7.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Fa", "$.8.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Sol", "$.10.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("La", "$.0.$");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("Si", "$.2.$");

						break;
				}
				
				
				
				
				if (FullscreenActivity.transposeDirection.equals("+1")) {
					// Put the numbers up by one.
					// Move num 11 up to 12 (if it goes to 0 it will be moved
					// later)
					// Last step then fixes 12 to be 0
					
					// Repeat this as often as required.
					for (int repeatTranspose = 0; repeatTranspose < FullscreenActivity.transposeTimes; repeatTranspose++) {
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "$.12.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.10.$", "$.11.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "$.10.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.8.$", "$.9.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.7.$", "$.8.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "$.7.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.5.$", "$.6.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "$.5.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.3.$", "$.4.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.2.$", "$.3.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "$.2.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.0.$", "$.1.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.12.$", "$.0.$");

					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", "$.42.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.40.$", "$.41.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", "$.40.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.38.$", "$.39.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.37.$", "$.38.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", "$.37.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.35.$", "$.36.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", "$.35.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.33.$", "$.34.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.32.$", "$.33.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", "$.32.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.30.$", "$.31.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.42.$", "$.30.$");

					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", "$.62.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.60.$", "$.61.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", "$.60.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.58.$", "$.59.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.57.$", "$.58.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", "$.57.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.55.$", "$.56.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", "$.55.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.53.$", "$.54.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.52.$", "$.53.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", "$.52.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.50.$", "$.51.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.62.$", "$.50.$");

					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.11.$", "$.12.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.10.$", "$.11.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.9.$", "$.10.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.8.$", "$.9.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.7.$", "$.8.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.6.$", "$.7.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.5.$", "$.6.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.4.$", "$.5.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.3.$", "$.4.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.2.$", "$.3.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.1.$", "$.2.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.0.$", "$.1.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.12.$", "$.0.$");


					}
				}

				if (FullscreenActivity.transposeDirection.equals("-1")) {
					// Put the numbers down by one.
					// Move num 0 down to -1 (if it goes to 11 it will be moved
					// later)
					// Last step then fixes -1 to be 11

					// Repeat this as often as required.
					for (int repeatTranspose = 0; repeatTranspose < FullscreenActivity.transposeTimes; repeatTranspose++) {
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.0.$", "$.-1.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "$.0.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.2.$", "$.1.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.3.$", "$.2.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "$.3.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.5.$", "$.4.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "$.5.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.7.$", "$.6.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.8.$", "$.7.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "$.8.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.10.$", "$.9.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "$.10.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.-1.$", "$.11.$");

				
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.30.$", "$.29.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", "$.30.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.32.$", "$.31.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.33.$", "$.32.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", "$.33.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.35.$", "$.34.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", "$.35.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.37.$", "$.36.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.38.$", "$.37.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", "$.38.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.40.$", "$.39.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", "$.40.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.29.$", "$.41.$");

					
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.50.$", "$.49.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", "$.50.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.52.$", "$.51.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.53.$", "$.52.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", "$.53.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.55.$", "$.54.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", "$.55.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.57.$", "$.56.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.58.$", "$.57.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", "$.58.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.60.$", "$.59.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", "$.60.$");
					FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.49.$", "$.61.$");

					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.0.$", "$.-1.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.1.$", "$.0.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.2.$", "$.1.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.3.$", "$.2.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.4.$", "$.3.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.5.$", "$.4.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.6.$", "$.5.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.7.$", "$.6.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.8.$", "$.7.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.9.$", "$.8.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.10.$", "$.9.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.11.$", "$.10.$");
					FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.-1.$", "$.11.$");

				
					}
				}


				
				// Put the correct key back
				FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.0.$", "A");
				FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.2.$", "B");
				FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.3.$", "C");
				FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.5.$", "D");
				FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.7.$", "E");
				FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.8.$", "F");
				FullscreenActivity.mKey = FullscreenActivity.mKey.replace("$.10.$", "G");

				if (FullscreenActivity.transposeStyle.equals("flats")) {
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.1.$", "Bb");
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.4.$", "Db");
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.6.$", "Eb");
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.9.$", "Gb");
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.11.$", "Ab");
				} else {
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.1.$", "A#");
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.4.$", "C#");
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.6.$", "D#");
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.9.$", "F#");
					FullscreenActivity.mKey = FullscreenActivity.mKey	.replace("$.11.$", "G#");
				}

				
				
				
				// Normal chord format
				switch (FullscreenActivity.chordFormat) {
					case "1":
						// Fix the naturals back to chords
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.0.$", "A");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.30.$", " Am");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.50.$", ".Am");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.2.$", "B");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.32.$", " Bm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.52.$", ".Bm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.3.$", "C");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.33.$", " Cm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.53.$", ".Cm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.5.$", "D");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.35.$", " Dm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.55.$", ".Dm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.7.$", "E");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.37.$", " Em");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.57.$", ".Em");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.8.$", "F");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.38.$", " Fm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.58.$", ".Fm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.10.$", "G");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.40.$", " Gm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.60.$", ".Gm");

						if (FullscreenActivity.transposeStyle.equals("flats")) {
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "Bb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", " Bbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", ".Bbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "Db");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", " Dbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", ".Dbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "Eb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", " Ebm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", ".Ebm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "Gb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", " Gbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", ".Gbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "Ab");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", " Abm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", ".Abm");
						} else {
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "A#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", " A#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", ".A#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "C#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", " C#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", ".C#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "D#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", " D#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", ".D#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "F#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", " F#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", ".F#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "G#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", " G#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", ".G#m");
						}


						// Eastern European B/H
						break;
					case "2":
						// Fix the naturals back to chords
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.0.$", "A");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.30.$", " Am");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.50.$", ".Am");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.2.$", "H");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.32.$", " Hm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.52.$", ".Hm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.3.$", "C");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.33.$", " Cm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.53.$", ".Cm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.5.$", "D");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.35.$", " Dm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.55.$", ".Dm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.7.$", "E");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.37.$", " Em");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.57.$", ".Em");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.8.$", "F");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.38.$", " Fm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.58.$", ".Fm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.10.$", "G");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.40.$", " Gm");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.60.$", ".Gm");

						if (FullscreenActivity.transposeStyle.equals("flats")) {
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "B");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", " Bm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", ".Bm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "Db");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", " Dbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", ".Dbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "Eb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", " Ebm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", ".Ebm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "Gb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", " Gbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", ".Gbm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "Ab");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", " Abm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", ".Abm");
						} else {
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "B");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", " Bm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", ".Bm");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "C#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", " C#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", ".C#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "D#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", " D#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", ".D#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "F#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", " F#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", ".F#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "G#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", " G#m");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", ".G#m");
						}


						// Eastern European B/H is/es
						break;
					case "3":
						// Fix the naturals back to chords
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.0.$", "A");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.30.$", " a");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.50.$", ".a");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.2.$", "H");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.32.$", " h");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.52.$", ".h");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.3.$", "C");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.33.$", " c");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.53.$", ".c");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.5.$", "D");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.35.$", " d");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.55.$", ".d");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.7.$", "E");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.37.$", " e");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.57.$", ".e");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.8.$", "F");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.38.$", " f");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.58.$", ".f");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.10.$", "G");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.40.$", " g");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.60.$", ".g");

						if (FullscreenActivity.transposeStyle.equals("flats")) {
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "B");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", " b");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", ".b");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "Des");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", " des");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", ".des");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "Es");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", " es");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", ".es");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "Ges");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", " ges");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", ".ges");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "As");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", " as");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", ".as");
						} else {
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "B");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", " b");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", ".b");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "Cis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", " cis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", ".cis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "Dis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", " dis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", ".dis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "Fis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", " fis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", ".fis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "Gis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", " gis");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", ".gis");
						}


						// Do a deer
						break;
					case "4":
						// Fix the naturals back to chords
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.0.$", "La");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.30.$", "La");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.50.$", "La");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.2.$", "Si");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.32.$", "Si");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.52.$", "Si");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.3.$", "Do");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.33.$", "Do");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.53.$", "Do");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.5.$", "Re");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.35.$", "Re");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.55.$", "Re");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.7.$", "Mi");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.37.$", "Mi");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.57.$", "Mi");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.8.$", "Fa");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.38.$", "Fa");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.58.$", "Fa");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.10.$", "Sol");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.40.$", "Sol");
						FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.60.$", "Sol");

						if (FullscreenActivity.transposeStyle.equals("flats")) {
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "Sib");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", "Sib");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", "Sib");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "Reb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", "Reb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", "Reb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "Mib");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", "Mib");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", "Mib");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "Solb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", "Solb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", "Solb");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "Lab");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", "Lab");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", "Lab");
						} else {
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.1.$", "La#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.31.$", "La#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.51.$", "La#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.4.$", "Do#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.34.$", "Do#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.54.$", "Do#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.6.$", "Re#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.36.$", "Re#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.56.$", "Re#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.9.$", "Fa#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.39.$", "Fa#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.59.$", "Fa#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.11.$", "Sol#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.41.$", "Sol#");
							FullscreenActivity.myTransposedLyrics[x] = FullscreenActivity.myTransposedLyrics[x].replace("$.61.$", "Sol#");
						}

						break;
				}


				
				
				

			}
			// Add all the lines back up as a string
			FullscreenActivity.transposedLyrics += FullscreenActivity.myTransposedLyrics[x]
					+ "\n";

		}


		// Now that the chords have been changed, replace the myTransposedLyrics
		// into the file
		FullscreenActivity.mynewXML = null;
		FullscreenActivity.mynewXML = "";
		
		// Write the new improved XML file
		FullscreenActivity.mLyrics = FullscreenActivity.transposedLyrics;
		
		String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		myNEWXML += "<song>\n";
		myNEWXML += "  <title>" + FullscreenActivity.mTitle.toString() + "</title>\n";
		myNEWXML += "  <author>" + FullscreenActivity.mAuthor + "</author>\n";
		myNEWXML += "  <copyright>" + FullscreenActivity.mCopyright + "</copyright>\n";
		myNEWXML += "  <presentation>" + FullscreenActivity.mPresentation + "</presentation>\n";
		myNEWXML += "  <hymn_number>" + FullscreenActivity.mHymnNumber + "</hymn_number>\n";
		myNEWXML += "  <capo print=\"" + FullscreenActivity.mCapoPrint + "\">" + FullscreenActivity.mCapo + "</capo>\n";
		myNEWXML += "  <tempo>" + FullscreenActivity.mTempo + "</tempo>\n";
		myNEWXML += "  <time_sig>" + FullscreenActivity.mTimeSig + "</time_sig>\n";
		myNEWXML += "  <duration>" + FullscreenActivity.mDuration + "</duration>\n";
		myNEWXML += "  <ccli>" + FullscreenActivity.mCCLI + "</ccli>\n";
		myNEWXML += "  <theme>" + FullscreenActivity.mTheme + "</theme>\n";
		myNEWXML += "  <alttheme>" + FullscreenActivity.mAltTheme + "</alttheme>\n";
		myNEWXML += "  <user1>" + FullscreenActivity.mUser1 + "</user1>\n";
		myNEWXML += "  <user2>" + FullscreenActivity.mUser2 + "</user2>\n";
		myNEWXML += "  <user3>" + FullscreenActivity.mUser3 + "</user3>\n";
		myNEWXML += "  <key>" + FullscreenActivity.mKey + "</key>\n";
		myNEWXML += "  <aka>" + FullscreenActivity.mAka + "</aka>\n";
		myNEWXML += "  <key_line>" + FullscreenActivity.mKeyLine + "</key_line>\n";
		myNEWXML += "  <books>" + FullscreenActivity.mBooks + "</books>\n";
		myNEWXML += "  <midi>" + FullscreenActivity.mMidi + "</midi>\n";
		myNEWXML += "  <midi_index>" + FullscreenActivity.mMidiIndex + "</midi_index>\n";
		myNEWXML += "  <pitch>" + FullscreenActivity.mPitch + "</pitch>\n";
		myNEWXML += "  <restrictions>" + FullscreenActivity.mRestrictions + "</restrictions>\n";
		myNEWXML += "  <notes>" + FullscreenActivity.mNotes + "</notes>\n";
        myNEWXML += "  <linked_songs>" + FullscreenActivity.mLinkedSongs + "</linked_songs>\n";
        myNEWXML += "  <pad_file>" + FullscreenActivity.mPadFile + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + FullscreenActivity.mCustomChords + "</custom_chords>\n";
		myNEWXML += "  <lyrics>" + FullscreenActivity.transposedLyrics + "</lyrics>\n";
        if (!FullscreenActivity.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff1 + "\n";
        }
        if (!FullscreenActivity.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

		FullscreenActivity.mynewXML = myNEWXML;

		// Makes sure all & are replaced with &amp;
		FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
		FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

		// Now write the modified song
		FileOutputStream overWrite = new FileOutputStream(
				FullscreenActivity.dir + "/" + FullscreenActivity.songfilename,
				false);
		overWrite.write(FullscreenActivity.mynewXML.getBytes());
		overWrite.flush();
		overWrite.close();
		FullscreenActivity.transposedLyrics = null;
		FullscreenActivity.transposedLyrics = "";
		Arrays.fill(FullscreenActivity.myTransposedLyrics, null);
		Arrays.fill(FullscreenActivity.myParsedLyrics, null);
		FullscreenActivity.myLyrics = null;
		FullscreenActivity.myLyrics = "";
		FullscreenActivity.mynewXML = null;
		FullscreenActivity.mynewXML = "";
		FullscreenActivity.myXML = null;
		FullscreenActivity.myXML = "";

		Preferences.savePreferences();
	}

	public static String capoTranspose() {
		// Go through each line and change each chord to $..$
		// This marks the bit to be changed
		// Since this line has chords, do the changing!
		// Do the sharps first
		// A=0 A#/Bb=1 B=2 C=3 C#/Db=4 D=5
		// D#/Eb=6 E=7 F=8 F#/Gb=9 G=10 G#/Ab=11
		
		// Normal chord format
		switch (FullscreenActivity.chordFormat) {
			case "1":
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("A#", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("C#", "$.4.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("D#", "$.6.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("F#", "$.9.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("G#", "$.11.$");
				// Do the flats next
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Ab", "$.11.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Bb", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Db", "$.4.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Eb", "$.6.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Gb", "$.9.$");
				// Do the white notes next
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("A", "$.0.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("B", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("C", "$.3.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("D", "$.5.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("E", "$.7.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("F", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("G", "$.10.$");


				// Eastern European B/H
				break;
			case "2":
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("A#", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("B#", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("H#", "$.3.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("C#", "$.4.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("D#", "$.6.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("E#", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("F#", "$.9.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("G#", "$.11.$");
				// Do the flats next
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Ab", "$.11.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Bb", "$.0.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Hb", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Cb", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Db", "$.4.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Eb", "$.6.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Gb", "$.9.$");
				// Do the white notes next
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("A", "$.0.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("B", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("H", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("C", "$.3.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("D", "$.5.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("E", "$.7.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("F", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("G", "$.10.$");

				// Eastern European B/H is/es/s
				break;
			case "3":
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Ais", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" ais", "$.31.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".ais", "$.51.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Bis", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" bis", "$.32.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".bis", "$.52.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("His", "$.3.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" his", "$.33.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".his", "$.53.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Cis", "$.4.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" cis", "$.34.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".cis", "$.54.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Dis", "$.6.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" dis", "$.36.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".dis", "$.56.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Eis", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" eis", "$.38.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".eis", "$.58.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Fis", "$.9.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" fis", "$.39.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".fis", "$.59.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Gis", "$.11.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" gis", "$.41.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".gis", "$.61.$");
				// Do the flats next
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("As", "$.11.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" as", "$.41.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".as", "$.61.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Bes", "$.0.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" bes", "$.30.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".bes", "$.50.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Hes", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" hes", "$.31.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".hes", "$.51.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Ces", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" ces", "$.32.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".ces", "$.52.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Des", "$.4.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" des", "$.34.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".des", "$.54.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Es", "$.6.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" es", "$.36.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".es", "$.56.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Ges", "$.9.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" ges", "$.39.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".ges", "$.59.$");
				// Do the white notes next
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("A", "$.0.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" a", "$.0.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".a", "$.0.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("B", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" b", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".b", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("H", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" h", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".h", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("C", "$.3.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" c", "$.3.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".c", "$.3.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("D", "$.5.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" d", "$.5.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".d", "$.5.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("E", "$.7.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" e", "$.7.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".e", "$.7.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("F", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" f", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".f", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("G", "$.10.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(" g", "$.10.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace(".g", "$.10.$");

				// Do a deer
				break;
			case "4":
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("La#", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Do#", "$.4.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Re#", "$.6.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Fa#", "$.9.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Sol#", "$.11.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Mi#", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Si#", "$.3.$");
				// Do the flats next
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Lab", "$.11.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Sib", "$.1.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Reb", "$.4.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Mib", "$.6.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Solb", "$.9.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Dob", "$.9.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Fab", "$.7.$");
				// Do the white notes next
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("La", "$.0.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Si", "$.2.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Do", "$.3.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Re", "$.5.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Mi", "$.7.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Fa", "$.8.$");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("Sol", "$.10.$");

				break;
		}

		
		


		// Try to do a sensible capo change.
		// Do a for loop for each capo chord changing it by one each time until the desired fret change
		int numtimes = Integer.parseInt(FullscreenActivity.mCapo);
		for (int s=0; s < numtimes; s++) {
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.0.$", "$.-1.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "$.0.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.2.$", "$.1.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.3.$", "$.2.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "$.3.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.5.$", "$.4.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "$.5.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.7.$", "$.6.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.8.$", "$.7.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "$.8.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.10.$", "$.9.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "$.10.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.-1.$", "$.11.$");
			
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.30.$", "$.29.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.31.$", "$.30.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.32.$", "$.31.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.33.$", "$.32.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.34.$", "$.33.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.35.$", "$.34.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.36.$", "$.35.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.37.$", "$.36.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.38.$", "$.37.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.39.$", "$.38.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.40.$", "$.39.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.41.$", "$.40.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.29.$", "$.41.$");

			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.50.$", "$.49.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.51.$", "$.50.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.52.$", "$.51.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.53.$", "$.52.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.54.$", "$.53.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.55.$", "$.54.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.56.$", "$.55.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.57.$", "$.56.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.58.$", "$.57.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.59.$", "$.58.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.60.$", "$.59.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.61.$", "$.60.$");
			FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.49.$", "$.61.$");
		}
		
		
		
		
		
		
		// If normal chords
		switch (FullscreenActivity.chordFormat) {
			case "1":
				// Fix the naturals back to chords
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.0.$", "A");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.2.$", "B");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.3.$", "C");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.5.$", "D");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.7.$", "E");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.8.$", "F");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.10.$", "G");

				if (FullscreenActivity.transposeStyle.equals("flats")) {
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "Bb");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "Db");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "Eb");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "Gb");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "Ab");
				} else {
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "A#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "C#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "D#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "F#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "G#");
				}

				// Eastern European B/H
				break;
			case "2":
				// Fix the naturals back to chords
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.0.$", "A");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.2.$", "H");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.3.$", "C");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.5.$", "D");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.7.$", "E");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.8.$", "F");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.10.$", "G");

				if (FullscreenActivity.transposeStyle.equals("flats")) {
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "B");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "Db");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "Eb");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "Gb");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "Ab");
				} else {
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "B");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "C#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "D#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "F#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "G#");
				}

				// Eastern European B/H is/es/s
				break;
			case "3":
				// Fix the naturals back to chords
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.0.$", "A");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.30.$", " a");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.50.$", ".a");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.2.$", "B");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.32.$", " b");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.52.$", ".b");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.3.$", "C");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.33.$", " c");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.53.$", ".c");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.5.$", "D");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.35.$", " d");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.55.$", ".d");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.7.$", "E");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.37.$", " e");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.57.$", ".e");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.8.$", "F");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.38.$", " f");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.58.$", ".f");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.10.$", "G");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.40.$", " g");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.60.$", ".g");

				if (FullscreenActivity.transposeStyle.equals("flats")) {
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "B");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.31.$", " b");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.51.$", ".b");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "Des");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.34.$", " des");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.54.$", ".des");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "Es");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.36.$", " es");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.56.$", ".es");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "Ges");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.39.$", " ges");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.59.$", ".ges");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "As");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.41.$", " as");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.61.$", ".as");
				} else {
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "B");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.31.$", " b");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.51.$", ".b");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "Cis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.34.$", " cis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.54.$", ".cis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "Dis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.36.$", " dis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.56.$", ".dis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "Fis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.39.$", " fis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.59.$", ".fis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "Gis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.41.$", " gis");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.61.$", ".gis");
				}

				// Do a deer
				break;
			case "4":
				// Fix the naturals back to chords
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.0.$", "La");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.2.$", "Si");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.3.$", "Do");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.5.$", "Re");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.7.$", "Mi");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.8.$", "Fa");
				FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.10.$", "Sol");

				if (FullscreenActivity.transposeStyle.equals("flats")) {
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "Sib");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "Reb");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "Mib");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "Solb");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "Lab");
				} else {
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.1.$", "La#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.4.$", "Do#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.6.$", "Re#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.9.$", "Fa#");
					FullscreenActivity.temptranspChords = FullscreenActivity.temptranspChords.replace("$.11.$", "Sol#");
				}

				break;
		}
		

		return FullscreenActivity.temptranspChords;
	}

}