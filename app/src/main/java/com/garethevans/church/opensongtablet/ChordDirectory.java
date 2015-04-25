package com.garethevans.church.opensongtablet;

public class ChordDirectory {

	static String chordtoworkon;
	
	public static void simplifyChords(String tempchord) {
		chordtoworkon = tempchord;
		
		if (FullscreenActivity.chordFormat=="2") {
			//European Bb = B and B = H
			chordtoworkon = chordtoworkon.replace("$Bb","$A#");
			chordtoworkon = chordtoworkon.replace("$B","$A#");
			chordtoworkon = chordtoworkon.replace("$H", "$B");
		} else if (FullscreenActivity.chordFormat=="3") {
			// Replace minors, flats and sharps
			chordtoworkon = chordtoworkon.replace("$As", "$Ab");
			chordtoworkon = chordtoworkon.replace("$Ais","$A#");
			chordtoworkon = chordtoworkon.replace("$as", "$Abm");
			chordtoworkon = chordtoworkon.replace("$ais","$A#m");
			chordtoworkon = chordtoworkon.replace("$a","$Am");

			chordtoworkon = chordtoworkon.replace("$Bes", "$A");
			chordtoworkon = chordtoworkon.replace("$Bis","$B");
			chordtoworkon = chordtoworkon.replace("$bes", "$Am");
			chordtoworkon = chordtoworkon.replace("$bis","$Bm");
			chordtoworkon = chordtoworkon.replace("$B","$Bb");
			chordtoworkon = chordtoworkon.replace("$b","$Bbm");

			chordtoworkon = chordtoworkon.replace("$Hs", "$Bb");
			chordtoworkon = chordtoworkon.replace("$His","$C");
			chordtoworkon = chordtoworkon.replace("$hs", "$Bbm");
			chordtoworkon = chordtoworkon.replace("$his","$Cm");
			chordtoworkon = chordtoworkon.replace("$H","$B");
			chordtoworkon = chordtoworkon.replace("$h","$Bm");
			
			chordtoworkon = chordtoworkon.replace("$Ces", "$B");
			chordtoworkon = chordtoworkon.replace("$Cis","$C#");
			chordtoworkon = chordtoworkon.replace("$ces", "$Bm");
			chordtoworkon = chordtoworkon.replace("$cis","$C#m");
			chordtoworkon = chordtoworkon.replace("$c","$Cm");
			
			chordtoworkon = chordtoworkon.replace("$Des", "$Db");
			chordtoworkon = chordtoworkon.replace("$Dis","$D#");
			chordtoworkon = chordtoworkon.replace("$des", "$Dbm");
			chordtoworkon = chordtoworkon.replace("$dis","$D#m");
			chordtoworkon = chordtoworkon.replace("$d","$Dm");
			
			chordtoworkon = chordtoworkon.replace("$Es", "$Eb");
			chordtoworkon = chordtoworkon.replace("$Eis","$F");
			chordtoworkon = chordtoworkon.replace("$es", "$Ebm");
			chordtoworkon = chordtoworkon.replace("$eis","$Fm");
			chordtoworkon = chordtoworkon.replace("$e","$Em");
			
			chordtoworkon = chordtoworkon.replace("$Fes", "$E");
			chordtoworkon = chordtoworkon.replace("$Fis","$F#");
			chordtoworkon = chordtoworkon.replace("$fes", "$Em");
			chordtoworkon = chordtoworkon.replace("$fis","$F#m");
			chordtoworkon = chordtoworkon.replace("$f","$Fm");
			
			chordtoworkon = chordtoworkon.replace("$Ges", "$Gb");
			chordtoworkon = chordtoworkon.replace("$Gis","$G#");
			chordtoworkon = chordtoworkon.replace("$ges", "$Gbm");
			chordtoworkon = chordtoworkon.replace("$gis","$G#m");
			chordtoworkon = chordtoworkon.replace("$g","$Gm");
		}
			
		// Now fix any silly chords that shouldn't exist
		chordtoworkon = chordtoworkon.replace("$B", "$C");
		chordtoworkon = chordtoworkon.replace("$Cb", "$B");
		chordtoworkon = chordtoworkon.replace("$E#", "$F");
		chordtoworkon = chordtoworkon.replace("$Fb", "$E");


		// Now change any chord that ends with major or maj
		if (chordtoworkon.endsWith("maj")) {
			chordtoworkon = chordtoworkon.replace("maj", "");
		} else if (chordtoworkon.endsWith("major")) {
			chordtoworkon = chordtoworkon.replace("major", "");
		}
		
		// Replace all flat chords with sharp chords
		chordtoworkon = chordtoworkon.replace("Ab", "G#");
		chordtoworkon = chordtoworkon.replace("Bb", "A#");
		chordtoworkon = chordtoworkon.replace("Db", "C#");
		chordtoworkon = chordtoworkon.replace("Eb", "D#");
		chordtoworkon = chordtoworkon.replace("Gb", "F#");
		
		// Replace all sus4 chords with sus
		chordtoworkon = chordtoworkon.replace("sus4", "sus");
		
		// Replace all sus2, add2 and add9 chords with 2
		chordtoworkon = chordtoworkon.replace("sus2", "2");
		chordtoworkon = chordtoworkon.replace("add2", "2");
		chordtoworkon = chordtoworkon.replace("add9", "2");

		// Replace all dim7 with dim
		chordtoworkon = chordtoworkon.replace("dim7", "dim");

		// Replace all #5 and + with aug
		chordtoworkon = chordtoworkon.replace("#5", "aug");
		chordtoworkon = chordtoworkon.replace("+", "aug");

		// Since we are only using chords, we can ignore slash chords (bass notes)
		chordtoworkon = chordtoworkon.replace("/A#", "");
		chordtoworkon = chordtoworkon.replace("/C#", "");
		chordtoworkon = chordtoworkon.replace("/D#", "");
		chordtoworkon = chordtoworkon.replace("/F#", "");
		chordtoworkon = chordtoworkon.replace("/G#", "");
		chordtoworkon = chordtoworkon.replace("/A", "");
		chordtoworkon = chordtoworkon.replace("/B", "");
		chordtoworkon = chordtoworkon.replace("/C", "");
		chordtoworkon = chordtoworkon.replace("/D", "");
		chordtoworkon = chordtoworkon.replace("/E", "");
		chordtoworkon = chordtoworkon.replace("/F", "");
		chordtoworkon = chordtoworkon.replace("/G", "");
		
		// Now we can remove the $ sign
		chordtoworkon = chordtoworkon.replace("$", "");

	}
	
	
	
	public static void guitarChords(String chord) {
		
		String chordnotes = "";
		simplifyChords(chord);

		// MAJOR CHORDS
		if      (chordtoworkon.equals("A"))			{chordnotes = "002220";}
		else if (chordtoworkon.equals("A#"))		{chordnotes = "113331";}
		else if (chordtoworkon.equals("B"))			{chordnotes = "224442";}
		else if (chordtoworkon.equals("C"))			{chordnotes = "332010";}
		else if (chordtoworkon.equals("C#"))		{chordnotes = "113331_4_g_C#";}
		else if (chordtoworkon.equals("D"))			{chordnotes = "x00232";}
		else if (chordtoworkon.equals("D#"))		{chordnotes = "xx1343";}
		else if (chordtoworkon.equals("E"))			{chordnotes = "022100";}
		else if (chordtoworkon.equals("F"))			{chordnotes = "133211";}
		else if (chordtoworkon.equals("F#"))		{chordnotes = "244322";}
		else if (chordtoworkon.equals("G"))			{chordnotes = "320003";}
		else if (chordtoworkon.equals("G#"))		{chordnotes = "133211_4_g_G#";}
		
		// MAJOR 7 CHORDS
		else if (chordtoworkon.equals("Amaj7"))		{chordnotes = "002120";}
		else if (chordtoworkon.equals("A#maj7"))	{chordnotes = "113231";}
		else if (chordtoworkon.equals("Bmaj7"))		{chordnotes = "224342";}
		else if (chordtoworkon.equals("Cmaj7"))		{chordnotes = "332000";}
		else if (chordtoworkon.equals("C#maj7"))	{chordnotes = "113231_4_g_C#maj7";}
		else if (chordtoworkon.equals("Dmaj7"))		{chordnotes = "x00222";}
		else if (chordtoworkon.equals("D#maj7"))	{chordnotes = "xx1333";}
		else if (chordtoworkon.equals("Emaj7"))		{chordnotes = "021100";}
		else if (chordtoworkon.equals("Fmaj7"))		{chordnotes = "132211";}
		else if (chordtoworkon.equals("F#maj7"))	{chordnotes = "243322";}
		else if (chordtoworkon.equals("Gmaj7"))		{chordnotes = "320002";}
		else if (chordtoworkon.equals("G#maj7"))	{chordnotes = "132211_4_g_G#maj7";}

		// DOMINANT 7 CHORDS
		else if (chordtoworkon.equals("A7"))		{chordnotes = "002020";}
		else if (chordtoworkon.equals("A#7"))		{chordnotes = "113131";}
		else if (chordtoworkon.equals("B7"))		{chordnotes = "x21202";}
		else if (chordtoworkon.equals("C7"))		{chordnotes = "x32310";}
		else if (chordtoworkon.equals("C#7"))		{chordnotes = "113131_4_g_C#7";}
		else if (chordtoworkon.equals("D7"))		{chordnotes = "x00212";}
		else if (chordtoworkon.equals("D#7"))		{chordnotes = "xx1323";}
		else if (chordtoworkon.equals("E7"))		{chordnotes = "020100";}
		else if (chordtoworkon.equals("F7"))		{chordnotes = "131211";}
		else if (chordtoworkon.equals("F#7"))		{chordnotes = "242322";}
		else if (chordtoworkon.equals("G7"))		{chordnotes = "320001";}
		else if (chordtoworkon.equals("G#7"))		{chordnotes = "131211_4_g_G#7";}

		// MAJOR 6 CHORDS
		else if (chordtoworkon.equals("A6"))		{chordnotes = "002222";}
		else if (chordtoworkon.equals("A#6"))		{chordnotes = "x13333";}
		else if (chordtoworkon.equals("B6"))		{chordnotes = "x24444";}
		else if (chordtoworkon.equals("C6"))		{chordnotes = "x32210";}
		else if (chordtoworkon.equals("C#6"))		{chordnotes = "x13333_4_g_C#6";}
		else if (chordtoworkon.equals("D6"))		{chordnotes = "xx0202";}
		else if (chordtoworkon.equals("D#6"))		{chordnotes = "xx1313";}
		else if (chordtoworkon.equals("E6"))		{chordnotes = "022120";}
		else if (chordtoworkon.equals("F6"))		{chordnotes = "xx3231";}
		else if (chordtoworkon.equals("F#6"))		{chordnotes = "243342";}
		else if (chordtoworkon.equals("G6"))		{chordnotes = "320000";}
		else if (chordtoworkon.equals("G#6"))		{chordnotes = "xx3231_4_g_G#6";}

		// MINOR CHORDS
		else if (chordtoworkon.equals("Am"))		{chordnotes = "002210";}
		else if (chordtoworkon.equals("A#m"))		{chordnotes = "113321";}
		else if (chordtoworkon.equals("Bm"))		{chordnotes = "224432";}
		else if (chordtoworkon.equals("Cm"))		{chordnotes = "113321_3_g_Cm";}
		else if (chordtoworkon.equals("C#m"))		{chordnotes = "113321_4_g_C#m";}
		else if (chordtoworkon.equals("Dm"))		{chordnotes = "x00231";}
		else if (chordtoworkon.equals("D#m"))		{chordnotes = "xx1342";}
		else if (chordtoworkon.equals("Em"))		{chordnotes = "022000";}
		else if (chordtoworkon.equals("Fm"))		{chordnotes = "133111";}
		else if (chordtoworkon.equals("F#m"))		{chordnotes = "244222";}
		else if (chordtoworkon.equals("Gm"))		{chordnotes = "133111_3_g_Gm";}
		else if (chordtoworkon.equals("G#m"))		{chordnotes = "133111_4_g_G#m";}

		// MINOR 7 CHORDS
		else if (chordtoworkon.equals("Am7"))		{chordnotes = "002010";}
		else if (chordtoworkon.equals("A#m7"))		{chordnotes = "113121";}
		else if (chordtoworkon.equals("Bm7"))		{chordnotes = "224232";}
		else if (chordtoworkon.equals("Cm7"))		{chordnotes = "335343";}
		else if (chordtoworkon.equals("C#m7"))		{chordnotes = "113121_4_g_C#m7";}
		else if (chordtoworkon.equals("Dm7"))		{chordnotes = "x00211";}
		else if (chordtoworkon.equals("D#m7"))		{chordnotes = "xx1322";}
		else if (chordtoworkon.equals("Em7"))		{chordnotes = "020000";}
		else if (chordtoworkon.equals("Fm7"))		{chordnotes = "131111";}
		else if (chordtoworkon.equals("F#m7"))		{chordnotes = "242222";}
		else if (chordtoworkon.equals("Gm7"))		{chordnotes = "353333";}
		else if (chordtoworkon.equals("G#m7"))		{chordnotes = "131111_4_g_G#m7";}

		// SUS (SUS4) CHORDS
		else if (chordtoworkon.equals("Asus"))		{chordnotes = "002230";}
		else if (chordtoworkon.equals("A#sus"))		{chordnotes = "113341";}
		else if (chordtoworkon.equals("Bsus"))		{chordnotes = "224452";}
		else if (chordtoworkon.equals("Csus"))		{chordnotes = "332011";}
		else if (chordtoworkon.equals("C#sus"))		{chordnotes = "113341_4_g_C#sus";}
		else if (chordtoworkon.equals("Dsus"))		{chordnotes = "x00233";}
		else if (chordtoworkon.equals("D#sus"))		{chordnotes = "xx1344";}
		else if (chordtoworkon.equals("Esus"))		{chordnotes = "022200";}
		else if (chordtoworkon.equals("Fsus"))		{chordnotes = "133311";}
		else if (chordtoworkon.equals("F#sus"))		{chordnotes = "244422";}
		else if (chordtoworkon.equals("Gsus"))		{chordnotes = "320013";}
		else if (chordtoworkon.equals("G#sus"))		{chordnotes = "133311_4_g_G#sus";}
		
		// SUS2 / 2 / 9 CHORDS
		else if (chordtoworkon.equals("A2"))		{chordnotes = "002200";}
		else if (chordtoworkon.equals("A#2"))		{chordnotes = "113311";}
		else if (chordtoworkon.equals("B2"))		{chordnotes = "224422";}
		else if (chordtoworkon.equals("C2"))		{chordnotes = "332030";}
		else if (chordtoworkon.equals("C#2"))		{chordnotes = "113311_4_g_C#2";}
		else if (chordtoworkon.equals("D2"))		{chordnotes = "x00230";}
		else if (chordtoworkon.equals("D#2"))		{chordnotes = "xx1341";}
		else if (chordtoworkon.equals("E2"))		{chordnotes = "024100";}
		else if (chordtoworkon.equals("F2"))		{chordnotes = "xx3213";}
		else if (chordtoworkon.equals("F#2"))		{chordnotes = "xx4324";}
		else if (chordtoworkon.equals("G2"))		{chordnotes = "320203";}
		else if (chordtoworkon.equals("G#2"))		{chordnotes = "xx3213_4_g_G#2";}

		// DIMINISHED 7 CHORDS
		else if (chordtoworkon.equals("Adim"))		{chordnotes = "x01212";}
		else if (chordtoworkon.equals("A#dim"))		{chordnotes = "xx2323";}
		else if (chordtoworkon.equals("Bdim"))		{chordnotes = "xx3434";}
		else if (chordtoworkon.equals("Cdim"))		{chordnotes = "xx1212";}
		else if (chordtoworkon.equals("C#dim"))		{chordnotes = "xx2323";}
		else if (chordtoworkon.equals("Ddim"))		{chordnotes = "xx3434";}
		else if (chordtoworkon.equals("D#dim"))		{chordnotes = "xx1212";}
		else if (chordtoworkon.equals("Edim"))		{chordnotes = "xx2323";}
		else if (chordtoworkon.equals("Fdim"))		{chordnotes = "xx3434";}
		else if (chordtoworkon.equals("F#dim"))		{chordnotes = "xx1212";}
		else if (chordtoworkon.equals("Gdim"))		{chordnotes = "xx2323";}
		else if (chordtoworkon.equals("G#dim"))		{chordnotes = "xx3434";}
		
		// MINOR 7 FLAT 5 (HALF DIMINISHED)
		else if (chordtoworkon.equals("Am7b5"))		{chordnotes = "x01013";}
		else if (chordtoworkon.equals("A#m7b5"))	{chordnotes = "112124";}
		else if (chordtoworkon.equals("Bm7b5"))		{chordnotes = "112124_2_g_Bm7b5";}
		else if (chordtoworkon.equals("Cm7b5"))		{chordnotes = "112124_3_g_Cm7b5";}
		else if (chordtoworkon.equals("C#m7b5"))	{chordnotes = "112124_4_g_C#m7b5";}
		else if (chordtoworkon.equals("Dm7b5"))		{chordnotes = "xx0111";}
		else if (chordtoworkon.equals("D#m7b5"))	{chordnotes = "xx1222";}
		else if (chordtoworkon.equals("Em7b5"))		{chordnotes = "010030";}
		else if (chordtoworkon.equals("Fm7b5"))		{chordnotes = "121141";}
		else if (chordtoworkon.equals("F#m7b5"))	{chordnotes = "121141_2_g_F#m7b5";}
		else if (chordtoworkon.equals("Gm7b5"))		{chordnotes = "121141_3_g_Gm7b5";}
		else if (chordtoworkon.equals("G#m7b5"))	{chordnotes = "121141_4_g_G#m7b5";}
				
		// AUGMENTED (#5)
		else if (chordtoworkon.equals("Aaug"))		{chordnotes = "x03221";}
		else if (chordtoworkon.equals("A#aug"))		{chordnotes = "x14332";}
		else if (chordtoworkon.equals("Baug"))		{chordnotes = "x14332_2_g_Baug";}
		else if (chordtoworkon.equals("Caug"))		{chordnotes = "x32110";}
		else if (chordtoworkon.equals("C#aug"))		{chordnotes = "x14332_4_g_C#aug";}
		else if (chordtoworkon.equals("Daug"))		{chordnotes = "xx0332";}
		else if (chordtoworkon.equals("D#aug"))		{chordnotes = "xx1443";}
		else if (chordtoworkon.equals("Eaug"))		{chordnotes = "0321x0";}
		else if (chordtoworkon.equals("Faug"))		{chordnotes = "143221";}
		else if (chordtoworkon.equals("F#aug"))		{chordnotes = "143221_2_g_F#aug";}
		else if (chordtoworkon.equals("Gaug"))		{chordnotes = "321003";}
		else if (chordtoworkon.equals("G#aug"))		{chordnotes = "143221_4_g_G#aug";}
		
		else {chordnotes = "xxxxxx";}
		
		// Standard guitar chords all start with g_ (guitar) and end with _0 (fret to start with)
		if (!chordnotes.contains("_")) {
			chordnotes = chordnotes + "_0_g_"+chordtoworkon;
		}
		
		FullscreenActivity.chordnotes = chordnotes;
	}
}
