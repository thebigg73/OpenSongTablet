package com.garethevans.church.opensongtablet;

public class ChordDirectory {

	static String chordtoworkon;
	
	public static void simplifyChords(String tempchord) {
		chordtoworkon = tempchord;
		
		if (FullscreenActivity.chordFormat.equals("2")) {
			//European Bb = B and B = H
			chordtoworkon = chordtoworkon.replace("$Bb","$A#");
			chordtoworkon = chordtoworkon.replace("$B","$A#");
			chordtoworkon = chordtoworkon.replace("$H", "$B");
		} else if (FullscreenActivity.chordFormat.equals("3")) {
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


		// Now change any chord that ends with major or maj (not maj7)
		if (chordtoworkon.endsWith("maj")) {
			chordtoworkon = chordtoworkon.replace("maj", "");
		} else if (chordtoworkon.endsWith("major")) {
			chordtoworkon = chordtoworkon.replace("major", "");
		}

        // Replace min chords with m
        chordtoworkon = chordtoworkon.replace("min", "m");

		// Replace all flat chords with sharp chords
		chordtoworkon = chordtoworkon.replace("Ab", "G#");
		chordtoworkon = chordtoworkon.replace("Bb", "A#");
		chordtoworkon = chordtoworkon.replace("Db", "C#");
		chordtoworkon = chordtoworkon.replace("Eb", "D#");
		chordtoworkon = chordtoworkon.replace("Gb", "F#");
		
		// Replace all sus4 chords with sus
		chordtoworkon = chordtoworkon.replace("sus4", "sus");
		
		// Replace all sus2, add2 with 2
		chordtoworkon = chordtoworkon.replace("sus2", "2");
		chordtoworkon = chordtoworkon.replace("add2", "2");

        // Replace all sus9 chords with 9  add9 stay as add9
        chordtoworkon = chordtoworkon.replace("sus9", "9");

		// Replace all dim7 with dim
		chordtoworkon = chordtoworkon.replace("dim7", "dim");

        // Replace all m7/b5 with m7b5
        chordtoworkon = chordtoworkon.replace("m7/b5", "m7b5");

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

        // MINOR 6 CHORDS
        else if (chordtoworkon.equals("Am6"))		{chordnotes = "x02212";}
        else if (chordtoworkon.equals("A#m6"))		{chordnotes = "x13021";}
        else if (chordtoworkon.equals("Bm6"))		{chordnotes = "x20102";}
        else if (chordtoworkon.equals("Cm6"))		{chordnotes = "x31213";}
        else if (chordtoworkon.equals("C#m6"))		{chordnotes = "x42124";}
        else if (chordtoworkon.equals("Dm6"))		{chordnotes = "xx0201";}
        else if (chordtoworkon.equals("D#m6"))		{chordnotes = "xx1312";}
        else if (chordtoworkon.equals("Em6"))		{chordnotes = "022020";}
        else if (chordtoworkon.equals("Fm6"))		{chordnotes = "133131";}
        else if (chordtoworkon.equals("F#m6"))		{chordnotes = "244242";}
        else if (chordtoworkon.equals("Gm6"))		{chordnotes = "310030";}
        else if (chordtoworkon.equals("G#m6"))		{chordnotes = "133131_4_g_G#m6";}

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
		
		// SUS2 / 2 CHORDS
		else if (chordtoworkon.equals("A2"))		{chordnotes = "x02200";}
		else if (chordtoworkon.equals("A#2"))		{chordnotes = "x13311";}
		else if (chordtoworkon.equals("B2"))		{chordnotes = "x24422";}
		else if (chordtoworkon.equals("C2"))		{chordnotes = "x30033";}
		else if (chordtoworkon.equals("C#2"))		{chordnotes = "x13311_4_g_C#2";}
		else if (chordtoworkon.equals("D2"))		{chordnotes = "xx0230";}
		else if (chordtoworkon.equals("D#2"))		{chordnotes = "xx1341";}
		else if (chordtoworkon.equals("E2"))		{chordnotes = "xx2452";}
		else if (chordtoworkon.equals("F2"))		{chordnotes = "xx3011";}
		else if (chordtoworkon.equals("F#2"))		{chordnotes = "xx4122";}
		else if (chordtoworkon.equals("G2"))		{chordnotes = "300033";}
		else if (chordtoworkon.equals("G#2"))		{chordnotes = "411144";}

        // 9 CHORDS
        else if (chordtoworkon.equals("A9"))		{chordnotes = "21212x_4_g_A9";}
        else if (chordtoworkon.equals("A#9"))		{chordnotes = "x10111";}
        else if (chordtoworkon.equals("B9"))		{chordnotes = "x2122x";}
        else if (chordtoworkon.equals("C9"))		{chordnotes = "x3233x";}
        else if (chordtoworkon.equals("C#9"))		{chordnotes = "x2122x_3_g_C#9";}
        else if (chordtoworkon.equals("D9"))		{chordnotes = "x2122x_4_g_D9";}
        else if (chordtoworkon.equals("D#9"))		{chordnotes = "xx1021";}
        else if (chordtoworkon.equals("E9"))		{chordnotes = "020102";}
        else if (chordtoworkon.equals("F9"))		{chordnotes = "101011";}
        else if (chordtoworkon.equals("F#9"))		{chordnotes = "21212x";}
        else if (chordtoworkon.equals("G9"))		{chordnotes = "32323x";}
        else if (chordtoworkon.equals("G#9"))		{chordnotes = "21212x_3_g_G#9";}

        // add9 CHORDS
        else if (chordtoworkon.equals("Aadd9"))		{chordnotes = "54242x";}
        else if (chordtoworkon.equals("A#add9"))	{chordnotes = "x10311";}
        else if (chordtoworkon.equals("Badd9"))		{chordnotes = "43131x_4_g_Badd9";}
        else if (chordtoworkon.equals("Cadd9"))		{chordnotes = "x32030";}
        else if (chordtoworkon.equals("C#add9"))	{chordnotes = "x43141";}
        else if (chordtoworkon.equals("Dadd9"))		{chordnotes = "x54252";}
        else if (chordtoworkon.equals("D#add9"))    {chordnotes = "x43141_3_g_D#add9";}
        else if (chordtoworkon.equals("Eadd9"))		{chordnotes = "022102";}
        else if (chordtoworkon.equals("Fadd9"))		{chordnotes = "103011";}
        else if (chordtoworkon.equals("F#add9"))	{chordnotes = "xx4324";}
        else if (chordtoworkon.equals("Gadd9"))		{chordnotes = "320203";}
        else if (chordtoworkon.equals("G#add9"))	{chordnotes = "43131x";}

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


    public static void ukuleleChords(String chord) {

        String chordnotes = "";
        simplifyChords(chord);

        // MAJOR CHORDS
        if      (chordtoworkon.equals("A"))			{chordnotes = "2100";}
        else if (chordtoworkon.equals("A#"))		{chordnotes = "3211";}
        else if (chordtoworkon.equals("B"))			{chordnotes = "4322";}
        else if (chordtoworkon.equals("C"))			{chordnotes = "0003";}
        else if (chordtoworkon.equals("C#"))		{chordnotes = "1114";}
        else if (chordtoworkon.equals("D"))			{chordnotes = "2220";}
        else if (chordtoworkon.equals("D#"))		{chordnotes = "3331";}
        else if (chordtoworkon.equals("E"))			{chordnotes = "1402";}
        else if (chordtoworkon.equals("F"))			{chordnotes = "2010";}
        else if (chordtoworkon.equals("F#"))		{chordnotes = "3121";}
        else if (chordtoworkon.equals("G"))			{chordnotes = "0232";}
        else if (chordtoworkon.equals("G#"))		{chordnotes = "1343";}

         // MAJOR 7 CHORDS
        else if (chordtoworkon.equals("Amaj7"))		{chordnotes = "1100";}
        else if (chordtoworkon.equals("A#maj7"))	{chordnotes = "2211";}
        else if (chordtoworkon.equals("Bmaj7"))		{chordnotes = "3322";}
        else if (chordtoworkon.equals("Cmaj7"))		{chordnotes = "0002";}
        else if (chordtoworkon.equals("C#maj7"))	{chordnotes = "1113";}
        else if (chordtoworkon.equals("Dmaj7"))		{chordnotes = "2224";}
        else if (chordtoworkon.equals("D#maj7"))	{chordnotes = "3335";}
        else if (chordtoworkon.equals("Emaj7"))		{chordnotes = "1302";}
        else if (chordtoworkon.equals("Fmaj7"))		{chordnotes = "2413";}
        else if (chordtoworkon.equals("F#maj7"))	{chordnotes = "3524";}
        else if (chordtoworkon.equals("Gmaj7"))		{chordnotes = "0222";}
        else if (chordtoworkon.equals("G#maj7"))	{chordnotes = "1333";}

        // DOMINANT 7 CHORDS
        else if (chordtoworkon.equals("A7"))		{chordnotes = "0100";}
        else if (chordtoworkon.equals("A#7"))		{chordnotes = "1211";}
        else if (chordtoworkon.equals("B7"))		{chordnotes = "2322";}
        else if (chordtoworkon.equals("C7"))		{chordnotes = "0001";}
        else if (chordtoworkon.equals("C#7"))		{chordnotes = "1112";}
        else if (chordtoworkon.equals("D7"))		{chordnotes = "2020";}
        else if (chordtoworkon.equals("D#7"))		{chordnotes = "3131";}
        else if (chordtoworkon.equals("E7"))		{chordnotes = "1202";}
        else if (chordtoworkon.equals("F7"))		{chordnotes = "2313";}
        else if (chordtoworkon.equals("F#7"))		{chordnotes = "3424";}
        else if (chordtoworkon.equals("G7"))		{chordnotes = "0212";}
        else if (chordtoworkon.equals("G#7"))		{chordnotes = "1323";}

        // MAJOR 6 CHORDS
        else if (chordtoworkon.equals("A6"))		{chordnotes = "2424";}
        else if (chordtoworkon.equals("A#6"))		{chordnotes = "1313_3_u_A#6";}
        else if (chordtoworkon.equals("B6"))		{chordnotes = "1313_4_u_B6";}
        else if (chordtoworkon.equals("C6"))		{chordnotes = "0000";}
        else if (chordtoworkon.equals("C#6"))		{chordnotes = "1111";}
        else if (chordtoworkon.equals("D6"))		{chordnotes = "2222";}
        else if (chordtoworkon.equals("D#6"))		{chordnotes = "1111_3_u_D#6";}
        else if (chordtoworkon.equals("E6"))		{chordnotes = "1102";}
        else if (chordtoworkon.equals("F6"))		{chordnotes = "2213";}
        else if (chordtoworkon.equals("F#6"))		{chordnotes = "3324";}
        else if (chordtoworkon.equals("G6"))		{chordnotes = "0202";}
        else if (chordtoworkon.equals("G#6"))		{chordnotes = "1313";}

         // MINOR CHORDS
        else if (chordtoworkon.equals("Am"))		{chordnotes = "2000";}
        else if (chordtoworkon.equals("A#m"))		{chordnotes = "3111";}
        else if (chordtoworkon.equals("Bm"))		{chordnotes = "4222";}
        else if (chordtoworkon.equals("Cm"))		{chordnotes = "0333";}
        else if (chordtoworkon.equals("C#m"))		{chordnotes = "1444";}
        else if (chordtoworkon.equals("Dm"))		{chordnotes = "2210";}
        else if (chordtoworkon.equals("D#m"))		{chordnotes = "3321";}
        else if (chordtoworkon.equals("Em"))		{chordnotes = "0402";}
        else if (chordtoworkon.equals("Fm"))		{chordnotes = "1513";}
        else if (chordtoworkon.equals("F#m"))		{chordnotes = "2120";}
        else if (chordtoworkon.equals("Gm"))		{chordnotes = "0231";}
        else if (chordtoworkon.equals("G#m"))		{chordnotes = "1342";}

        // MINOR 7 CHORDS
        else if (chordtoworkon.equals("Am7"))		{chordnotes = "2030";}
        else if (chordtoworkon.equals("A#m7"))		{chordnotes = "3141";}
        else if (chordtoworkon.equals("Bm7"))		{chordnotes = "4252";}
        else if (chordtoworkon.equals("Cm7"))		{chordnotes = "0332";}
        else if (chordtoworkon.equals("C#m7"))		{chordnotes = "1443";}
        else if (chordtoworkon.equals("Dm7"))		{chordnotes = "2010";}
        else if (chordtoworkon.equals("D#m7"))		{chordnotes = "3121";}
        else if (chordtoworkon.equals("Em7"))		{chordnotes = "0202";}
        else if (chordtoworkon.equals("Fm7"))		{chordnotes = "1313";}
        else if (chordtoworkon.equals("F#m7"))		{chordnotes = "2100";}
        else if (chordtoworkon.equals("Gm7"))		{chordnotes = "0211";}
        else if (chordtoworkon.equals("G#m7"))		{chordnotes = "1322";}

        // MINOR 6 CHORDS
        else if (chordtoworkon.equals("Am6"))		{chordnotes = "2423";}
        else if (chordtoworkon.equals("A#m6"))		{chordnotes = "0111";}
        else if (chordtoworkon.equals("Bm6"))		{chordnotes = "1222";}
        else if (chordtoworkon.equals("Cm6"))		{chordnotes = "2333";}
        else if (chordtoworkon.equals("C#m6"))		{chordnotes = "1101";}
        else if (chordtoworkon.equals("Dm6"))		{chordnotes = "2212";}
        else if (chordtoworkon.equals("D#m6"))		{chordnotes = "3323";}
        else if (chordtoworkon.equals("Em6"))		{chordnotes = "0102";}
        else if (chordtoworkon.equals("Fm6"))		{chordnotes = "1213";}
        else if (chordtoworkon.equals("F#m6"))		{chordnotes = "2324";}
        else if (chordtoworkon.equals("Gm6"))		{chordnotes = "0201";}
        else if (chordtoworkon.equals("G#m6"))		{chordnotes = "1312";}

         // SUS (SUS4) CHORDS
        else if (chordtoworkon.equals("Asus"))		{chordnotes = "2200";}
        else if (chordtoworkon.equals("A#sus"))		{chordnotes = "3311";}
        else if (chordtoworkon.equals("Bsus"))		{chordnotes = "4422";}
        else if (chordtoworkon.equals("Csus"))		{chordnotes = "0013";}
        else if (chordtoworkon.equals("C#sus"))		{chordnotes = "1124";}
        else if (chordtoworkon.equals("Dsus"))		{chordnotes = "0230";}
        else if (chordtoworkon.equals("D#sus"))		{chordnotes = "1341";}
        else if (chordtoworkon.equals("Esus"))		{chordnotes = "2452";}
        else if (chordtoworkon.equals("Fsus"))		{chordnotes = "3011";}
        else if (chordtoworkon.equals("F#sus"))		{chordnotes = "4122";}
        else if (chordtoworkon.equals("Gsus"))		{chordnotes = "0233";}
        else if (chordtoworkon.equals("G#sus"))		{chordnotes = "1344";}

        // SUS2 / 2 CHORDS
        else if (chordtoworkon.equals("A2"))		{chordnotes = "2452";}
        else if (chordtoworkon.equals("A#2"))		{chordnotes = "3011";}
        else if (chordtoworkon.equals("B2"))		{chordnotes = "4122";}
        else if (chordtoworkon.equals("C2"))		{chordnotes = "0233";}
        else if (chordtoworkon.equals("C#2"))		{chordnotes = "1344";}
        else if (chordtoworkon.equals("D2"))		{chordnotes = "2200";}
        else if (chordtoworkon.equals("D#2"))		{chordnotes = "3311";}
        else if (chordtoworkon.equals("E2"))		{chordnotes = "4422";}
        else if (chordtoworkon.equals("F2"))		{chordnotes = "0013";}
        else if (chordtoworkon.equals("F#2"))		{chordnotes = "1124";}
        else if (chordtoworkon.equals("G2"))		{chordnotes = "0230";}
        else if (chordtoworkon.equals("G#2"))		{chordnotes = "1341";}

        // 9 CHORDS
        else if (chordtoworkon.equals("A9"))		{chordnotes = "2132";}
        else if (chordtoworkon.equals("A#9"))		{chordnotes = "3243";}
        else if (chordtoworkon.equals("B9"))		{chordnotes = "4354";}
        else if (chordtoworkon.equals("C9"))		{chordnotes = "3001";}
        else if (chordtoworkon.equals("C#9"))		{chordnotes = "1312";}
        else if (chordtoworkon.equals("D9"))		{chordnotes = "5424";}
        else if (chordtoworkon.equals("D#9"))		{chordnotes = "0111";}
        else if (chordtoworkon.equals("E9"))		{chordnotes = "1222";}
        else if (chordtoworkon.equals("F9"))		{chordnotes = "2333";}
        else if (chordtoworkon.equals("F#9"))		{chordnotes = "3444";}
        else if (chordtoworkon.equals("G9"))		{chordnotes = "0552";}
        else if (chordtoworkon.equals("G#9"))		{chordnotes = "1021";}

        // add9 CHORDS
        else if (chordtoworkon.equals("Aadd9"))		{chordnotes = "2102";}
        else if (chordtoworkon.equals("A#add9"))	{chordnotes = "3213";}
        else if (chordtoworkon.equals("Badd9"))		{chordnotes = "4324";}
        else if (chordtoworkon.equals("Cadd9"))		{chordnotes = "0203";}
        else if (chordtoworkon.equals("C#add9"))	{chordnotes = "1314";}
        else if (chordtoworkon.equals("Dadd9"))		{chordnotes = "2425";}
        else if (chordtoworkon.equals("D#add9"))	{chordnotes = "0311";}
        else if (chordtoworkon.equals("Eadd9"))		{chordnotes = "1422";}
        else if (chordtoworkon.equals("Fadd9"))		{chordnotes = "0010";}
        else if (chordtoworkon.equals("F#add9"))	{chordnotes = "1121";}
        else if (chordtoworkon.equals("Gadd9"))		{chordnotes = "0252";}
        else if (chordtoworkon.equals("G#add9"))	{chordnotes = "2232";}

        // DIMINISHED 7 CHORDS
        else if (chordtoworkon.equals("Adim"))		{chordnotes = "2323";}
        else if (chordtoworkon.equals("A#dim"))		{chordnotes = "0101";}
        else if (chordtoworkon.equals("Bdim"))		{chordnotes = "1212";}
        else if (chordtoworkon.equals("Cdim"))		{chordnotes = "2323";}
        else if (chordtoworkon.equals("C#dim"))		{chordnotes = "0101";}
        else if (chordtoworkon.equals("Ddim"))		{chordnotes = "1212";}
        else if (chordtoworkon.equals("D#dim"))		{chordnotes = "2323";}
        else if (chordtoworkon.equals("Edim"))		{chordnotes = "0101";}
        else if (chordtoworkon.equals("Fdim"))		{chordnotes = "1212";}
        else if (chordtoworkon.equals("F#dim"))		{chordnotes = "2323";}
        else if (chordtoworkon.equals("Gdim"))		{chordnotes = "0101";}
        else if (chordtoworkon.equals("G#dim"))		{chordnotes = "1212";}

        // MINOR 7 FLAT 5 (HALF DIMINISHED)
        else if (chordtoworkon.equals("Am7b5"))		{chordnotes = "2333";}
        else if (chordtoworkon.equals("A#m7b5"))	{chordnotes = "1101";}
        else if (chordtoworkon.equals("Bm7b5"))		{chordnotes = "2212";}
        else if (chordtoworkon.equals("Cm7b5"))		{chordnotes = "3323";}
        else if (chordtoworkon.equals("C#m7b5"))	{chordnotes = "0102";}
        else if (chordtoworkon.equals("Dm7b5"))		{chordnotes = "1213";}
        else if (chordtoworkon.equals("D#m7b5"))	{chordnotes = "2324";}
        else if (chordtoworkon.equals("Em7b5"))		{chordnotes = "0201";}
        else if (chordtoworkon.equals("Fm7b5"))		{chordnotes = "1312";}
        else if (chordtoworkon.equals("F#m7b5"))	{chordnotes = "2423";}
        else if (chordtoworkon.equals("Gm7b5"))		{chordnotes = "0111";}
        else if (chordtoworkon.equals("G#m7b5"))	{chordnotes = "1222";}

        // AUGMENTED (#5)
        else if (chordtoworkon.equals("Aaug"))		{chordnotes = "2114";}
        else if (chordtoworkon.equals("A#aug")) 	{chordnotes = "3221";}
        else if (chordtoworkon.equals("Baug"))		{chordnotes = "4332";}
        else if (chordtoworkon.equals("Caug"))		{chordnotes = "1003";}
        else if (chordtoworkon.equals("C#aug"))	    {chordnotes = "2110";}
        else if (chordtoworkon.equals("Daug"))		{chordnotes = "3221";}
        else if (chordtoworkon.equals("D#aug"))	    {chordnotes = "0332";}
        else if (chordtoworkon.equals("Eaug"))		{chordnotes = "1003";}
        else if (chordtoworkon.equals("Faug"))		{chordnotes = "2110";}
        else if (chordtoworkon.equals("F#aug")) 	{chordnotes = "3221";}
        else if (chordtoworkon.equals("Gaug"))		{chordnotes = "0332";}
        else if (chordtoworkon.equals("G#aug")) 	{chordnotes = "1003";}

        else {chordnotes = "xxxx";}

        // Standard ukulele chords all start with u_ (ukulele) and end with _0 (fret to start with)
        if (!chordnotes.contains("_")) {
            chordnotes = chordnotes + "_0_u_"+chordtoworkon;
        }

        FullscreenActivity.chordnotes = chordnotes;
    }



    public static void mandolinChords(String chord) {

        String chordnotes = "";
        simplifyChords(chord);

        // MAJOR CHORDS
        if      (chordtoworkon.equals("A"))			{chordnotes = "2245";}
        else if (chordtoworkon.equals("A#"))		{chordnotes = "1134_3_m_A#";}
        else if (chordtoworkon.equals("B"))			{chordnotes = "1134_4_m_B";}
        else if (chordtoworkon.equals("C"))			{chordnotes = "0230";}
        else if (chordtoworkon.equals("C#"))		{chordnotes = "1341";}
        else if (chordtoworkon.equals("D"))			{chordnotes = "2002";}
        else if (chordtoworkon.equals("D#"))		{chordnotes = "0503";}
        else if (chordtoworkon.equals("E"))			{chordnotes = "1220";}
        else if (chordtoworkon.equals("F"))			{chordnotes = "2331";}
        else if (chordtoworkon.equals("F#"))		{chordnotes = "3442";}
        else if (chordtoworkon.equals("G"))			{chordnotes = "0023";}
        else if (chordtoworkon.equals("G#"))		{chordnotes = "1134";}

        // MAJOR 7 CHORDS
        else if (chordtoworkon.equals("Amaj7"))		{chordnotes = "2244";}
        else if (chordtoworkon.equals("A#maj7"))	{chordnotes = "3355";}
        else if (chordtoworkon.equals("Bmaj7"))		{chordnotes = "1133_4_m_Bmaj7";}
        else if (chordtoworkon.equals("Cmaj7"))		{chordnotes = "4230";}
        else if (chordtoworkon.equals("C#maj7"))	{chordnotes = "5341";}
        else if (chordtoworkon.equals("Dmaj7"))		{chordnotes = "2042";}
        else if (chordtoworkon.equals("D#maj7"))	{chordnotes = "3153";}
        else if (chordtoworkon.equals("Emaj7"))		{chordnotes = "1120";}
        else if (chordtoworkon.equals("Fmaj7"))		{chordnotes = "5300";}
        else if (chordtoworkon.equals("F#maj7"))	{chordnotes = "3342";}
        else if (chordtoworkon.equals("Gmaj7"))		{chordnotes = "0022";}
        else if (chordtoworkon.equals("G#maj7"))	{chordnotes = "1133";}

        // DOMINANT 7 CHORDS
        else if (chordtoworkon.equals("A7"))		{chordnotes = "2243";}
        else if (chordtoworkon.equals("A#7"))		{chordnotes = "3354";}
        else if (chordtoworkon.equals("B7"))		{chordnotes = "1132_4_m_B7";}
        else if (chordtoworkon.equals("C7"))		{chordnotes = "1132_5_m_C7";}
        else if (chordtoworkon.equals("C#7"))		{chordnotes = "1132_6_m_C#7";}
        else if (chordtoworkon.equals("D7"))		{chordnotes = "2032";}
        else if (chordtoworkon.equals("D#7"))		{chordnotes = "3143";}
        else if (chordtoworkon.equals("E7"))		{chordnotes = "1024";}
        else if (chordtoworkon.equals("F7"))		{chordnotes = "2135";}
        else if (chordtoworkon.equals("F#7"))		{chordnotes = "2135_2_m_F#7";}
        else if (chordtoworkon.equals("G7"))		{chordnotes = "0021";}
        else if (chordtoworkon.equals("G#7"))		{chordnotes = "1132";}

        // MAJOR 6 CHORDS
        else if (chordtoworkon.equals("A6"))		{chordnotes = "2202";}
        else if (chordtoworkon.equals("A#6"))		{chordnotes = "3313";}
        else if (chordtoworkon.equals("B6"))		{chordnotes = "4424";}
        else if (chordtoworkon.equals("C6"))		{chordnotes = "2230";}
        else if (chordtoworkon.equals("C#6"))		{chordnotes = "3341";}
        else if (chordtoworkon.equals("D6"))		{chordnotes = "2022";}
        else if (chordtoworkon.equals("D#6"))		{chordnotes = "3133";}
        else if (chordtoworkon.equals("E6"))		{chordnotes = "4244";}
        else if (chordtoworkon.equals("F6"))		{chordnotes = "3052";}
        else if (chordtoworkon.equals("F#6"))		{chordnotes = "2122_5_m_F#6";}
        else if (chordtoworkon.equals("G6"))		{chordnotes = "0020";}
        else if (chordtoworkon.equals("G#6"))		{chordnotes = "1131";}

        // MINOR CHORDS
        else if (chordtoworkon.equals("Am"))		{chordnotes = "2235";}
        else if (chordtoworkon.equals("A#m"))		{chordnotes = "1124_3_m_A#m";}
        else if (chordtoworkon.equals("Bm"))		{chordnotes = "1124_4_m_Bm";}
        else if (chordtoworkon.equals("Cm"))		{chordnotes = "3341_3_m_Cm";}
        else if (chordtoworkon.equals("C#m"))		{chordnotes = "3341_4_m_C#m";}
        else if (chordtoworkon.equals("Dm"))		{chordnotes = "2001";}
        else if (chordtoworkon.equals("D#m"))		{chordnotes = "3112";}
        else if (chordtoworkon.equals("Em"))		{chordnotes = "0220";}
        else if (chordtoworkon.equals("Fm"))		{chordnotes = "1331";}
        else if (chordtoworkon.equals("F#m"))		{chordnotes = "2442";}
        else if (chordtoworkon.equals("Gm"))		{chordnotes = "0013";}
        else if (chordtoworkon.equals("G#m"))		{chordnotes = "1124";}

        // MINOR 7 CHORDS
        else if (chordtoworkon.equals("Am7"))		{chordnotes = "2233";}
        else if (chordtoworkon.equals("A#m7"))		{chordnotes = "3344";}
        else if (chordtoworkon.equals("Bm7"))		{chordnotes = "4455";}
        else if (chordtoworkon.equals("Cm7"))		{chordnotes = "1122_5_m_Cm7";}
        else if (chordtoworkon.equals("C#m7"))		{chordnotes = "1122_6_m_C#m7";}
        else if (chordtoworkon.equals("Dm7"))		{chordnotes = "2031";}
        else if (chordtoworkon.equals("D#m7"))		{chordnotes = "3142";}
        else if (chordtoworkon.equals("Em7"))		{chordnotes = "4253";}
        else if (chordtoworkon.equals("Fm7"))		{chordnotes = "3142_3_m_Fm7";}
        else if (chordtoworkon.equals("F#m7"))		{chordnotes = "3142_4_m_F#m7";}
        else if (chordtoworkon.equals("Gm7"))		{chordnotes = "0011";}
        else if (chordtoworkon.equals("G#m7"))		{chordnotes = "1122";}

        // MINOR 6 CHORDS
        else if (chordtoworkon.equals("Am6"))		{chordnotes = "2232";}
        else if (chordtoworkon.equals("A#m6"))		{chordnotes = "3343";}
        else if (chordtoworkon.equals("Bm6"))		{chordnotes = "4454";}
        else if (chordtoworkon.equals("Cm6"))		{chordnotes = "1121_5_m_Cm6";}
        else if (chordtoworkon.equals("C#m6"))		{chordnotes = "1121_6_m_C#m6";}
        else if (chordtoworkon.equals("Dm6"))		{chordnotes = "2021";}
        else if (chordtoworkon.equals("D#m6"))		{chordnotes = "3132";}
        else if (chordtoworkon.equals("Em6"))		{chordnotes = "4243";}
        else if (chordtoworkon.equals("Fm6"))		{chordnotes = "3132_3_m_Fm6";}
        else if (chordtoworkon.equals("F#m6"))		{chordnotes = "3132_4_m_F#m6";}
        else if (chordtoworkon.equals("Gm6"))		{chordnotes = "0010";}
        else if (chordtoworkon.equals("G#m6"))		{chordnotes = "1121";}

        // SUS (SUS4) CHORDS
        else if (chordtoworkon.equals("Asus"))		{chordnotes = "2255";}
        else if (chordtoworkon.equals("A#sus"))		{chordnotes = "1144_3_m_A#sus";}
        else if (chordtoworkon.equals("Bsus"))		{chordnotes = "1144_4_m_Bsus";}
        else if (chordtoworkon.equals("Csus"))		{chordnotes = "0330";}
        else if (chordtoworkon.equals("C#sus"))		{chordnotes = "1441";}
        else if (chordtoworkon.equals("Dsus"))		{chordnotes = "2003";}
        else if (chordtoworkon.equals("D#sus"))		{chordnotes = "3114";}
        else if (chordtoworkon.equals("Esus"))		{chordnotes = "4225";}
        else if (chordtoworkon.equals("Fsus"))		{chordnotes = "3331";}
        else if (chordtoworkon.equals("F#sus"))		{chordnotes = "4442";}
        else if (chordtoworkon.equals("Gsus"))		{chordnotes = "0033";}
        else if (chordtoworkon.equals("G#sus"))		{chordnotes = "1144";}

        // SUS2 / 2 CHORDS
        else if (chordtoworkon.equals("A2"))		{chordnotes = "2225";}
        else if (chordtoworkon.equals("A#2"))		{chordnotes = "3331";}
        else if (chordtoworkon.equals("B2"))		{chordnotes = "4442";}
        else if (chordtoworkon.equals("C2"))		{chordnotes = "5033";}
        else if (chordtoworkon.equals("C#2"))		{chordnotes = "3331_4_m_C#2";}
        else if (chordtoworkon.equals("D2"))		{chordnotes = "2000";}
        else if (chordtoworkon.equals("D#2"))		{chordnotes = "3111";}
        else if (chordtoworkon.equals("E2"))		{chordnotes = "4222";}
        else if (chordtoworkon.equals("F2"))		{chordnotes = "5333";}
        else if (chordtoworkon.equals("F#2"))		{chordnotes = "3111_4_m_F#2";}
        else if (chordtoworkon.equals("G2"))		{chordnotes = "0003";}
        else if (chordtoworkon.equals("G#2"))		{chordnotes = "1114";}

        // 9 CHORDS
        else if (chordtoworkon.equals("A9"))		{chordnotes = "4245";}
        else if (chordtoworkon.equals("A#9"))		{chordnotes = "3031";}
        else if (chordtoworkon.equals("B9"))		{chordnotes = "4142";}
        else if (chordtoworkon.equals("C9"))		{chordnotes = "5030";}
        else if (chordtoworkon.equals("C#9"))		{chordnotes = "4142_3_m_C#9";}
        else if (chordtoworkon.equals("D9"))		{chordnotes = "4100_4_m_D9";}
        else if (chordtoworkon.equals("D#9"))		{chordnotes = "4142_5_m_D#9";}
        else if (chordtoworkon.equals("E9"))		{chordnotes = "4140_6_m_E9";}
        else if (chordtoworkon.equals("F9"))		{chordnotes = "x303";}
        else if (chordtoworkon.equals("F#9"))		{chordnotes = "x414";}
        else if (chordtoworkon.equals("G9"))		{chordnotes = "0325";}
        else if (chordtoworkon.equals("G#9"))		{chordnotes = "x414_3_m_G#9";}

        // add9 CHORDS
        else if (chordtoworkon.equals("Aadd9"))		{chordnotes = "4245";}
        else if (chordtoworkon.equals("A#add9"))	{chordnotes = "3031";}
        else if (chordtoworkon.equals("Badd9"))		{chordnotes = "4142";}
        else if (chordtoworkon.equals("Cadd9"))		{chordnotes = "5030";}
        else if (chordtoworkon.equals("C#add9"))	{chordnotes = "4142_3_m_C#9";}
        else if (chordtoworkon.equals("Dadd9"))		{chordnotes = "4100_4_m_D9";}
        else if (chordtoworkon.equals("D#add9"))	{chordnotes = "4142_5_m_D#9";}
        else if (chordtoworkon.equals("Eadd9"))		{chordnotes = "4140_6_m_E9";}
        else if (chordtoworkon.equals("Fadd9"))		{chordnotes = "x303";}
        else if (chordtoworkon.equals("F#add9"))	{chordnotes = "x414";}
        else if (chordtoworkon.equals("Gadd9"))		{chordnotes = "0325";}
        else if (chordtoworkon.equals("G#add9"))	{chordnotes = "x414_3_m_G#9";}

        // DIMINISHED 7 CHORDS
        else if (chordtoworkon.equals("Adim"))		{chordnotes = "2132";}
        else if (chordtoworkon.equals("A#dim"))		{chordnotes = "3243";}
        else if (chordtoworkon.equals("Bdim"))		{chordnotes = "1021";}
        else if (chordtoworkon.equals("Cdim"))		{chordnotes = "2132";}
        else if (chordtoworkon.equals("C#dim"))		{chordnotes = "3243";}
        else if (chordtoworkon.equals("Ddim"))		{chordnotes = "1021";}
        else if (chordtoworkon.equals("D#dim"))		{chordnotes = "2132";}
        else if (chordtoworkon.equals("Edim"))		{chordnotes = "3241";}
        else if (chordtoworkon.equals("Fdim"))		{chordnotes = "1021";}
        else if (chordtoworkon.equals("F#dim"))		{chordnotes = "2132";}
        else if (chordtoworkon.equals("Gdim"))		{chordnotes = "3243";}
        else if (chordtoworkon.equals("G#dim"))		{chordnotes = "1021";}

        // MINOR 7 FLAT 5 (HALF DIMINISHED)
        else if (chordtoworkon.equals("Am7b5"))		{chordnotes = "2133";}
        else if (chordtoworkon.equals("A#m7b5"))	{chordnotes = "3244";}
        else if (chordtoworkon.equals("Bm7b5"))		{chordnotes = "2133_3_m_Bm7b6";}
        else if (chordtoworkon.equals("Cm7b5"))		{chordnotes = "2133_4_m_Cm7b5";}
        else if (chordtoworkon.equals("C#m7b5"))	{chordnotes = "2133_5_m_C#m7b5";}
        else if (chordtoworkon.equals("Dm7b5"))		{chordnotes = "1031";}
        else if (chordtoworkon.equals("D#m7b5"))	{chordnotes = "2142";}
        else if (chordtoworkon.equals("Em7b5"))		{chordnotes = "3253";}
        else if (chordtoworkon.equals("Fm7b5"))		{chordnotes = "5112_6_m_Fm7b5";}
        else if (chordtoworkon.equals("F#m7b5"))	{chordnotes = "5112_7_m_F#m7b5";}
        else if (chordtoworkon.equals("Gm7b5"))		{chordnotes = "0311";}
        else if (chordtoworkon.equals("G#m7b5"))	{chordnotes = "1022";}

        // AUGMENTED (#5)
        else if (chordtoworkon.equals("Aaug"))		{chordnotes = "2341";}
        else if (chordtoworkon.equals("A#aug")) 	{chordnotes = "3452";}
        else if (chordtoworkon.equals("Baug"))		{chordnotes = "4123";}
        else if (chordtoworkon.equals("Caug"))		{chordnotes = "5234";}
        else if (chordtoworkon.equals("C#aug"))	    {chordnotes = "4123_3_m_C#aug";}
        else if (chordtoworkon.equals("Daug"))		{chordnotes = "3012";}
        else if (chordtoworkon.equals("D#aug"))	    {chordnotes = "4123_5_m_D#aug";}
        else if (chordtoworkon.equals("Eaug"))		{chordnotes = "5234";}
        else if (chordtoworkon.equals("Faug"))		{chordnotes = "4123_7_m_Faug";}
        else if (chordtoworkon.equals("F#aug")) 	{chordnotes = "4123_8_m_F#aug";}
        else if (chordtoworkon.equals("Gaug"))		{chordnotes = "0123";}
        else if (chordtoworkon.equals("G#aug")) 	{chordnotes = "1230";}

        else {chordnotes = "xxxx";}

        // Standard mandolin chords all start with m_ (mandolin) and end with _0 (fret to start with)
        if (!chordnotes.contains("_")) {
            chordnotes = chordnotes + "_0_m_"+chordtoworkon;
        }

        FullscreenActivity.chordnotes = chordnotes;
    }

}
