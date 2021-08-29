/*



        @Override
        protected void onPostExecute(String s) {
            try {
                // Send the unique chords off to get the string layout
                // This will eventually be if guitar/ukulele/mandolin/piano/other
                // Custom chords don't get sent for retrieval as they are already defined
                String myinstr = preferences.getMyPreferenceString(getActivity(),"chordInstrument","g");
                for (int l = 0; l < unique_chords.size(); l++) {
                    boolean containsit = unique_chords.get(l).contains("$$$");
                    if (myinstr.equals("u") && !containsit) {
                        ChordDirectory.ukuleleChords(getActivity(),preferences,unique_chords.get(l));
                    } else if (myinstr.equals("m") && !containsit) {
                        ChordDirectory.mandolinChords(getActivity(),preferences,unique_chords.get(l));
                    } else if (myinstr.equals("g") && !containsit) {
                        ChordDirectory.guitarChords(getActivity(),preferences,unique_chords.get(l));
                    } else if (myinstr.equals("c") && !containsit) {
                        ChordDirectory.cavaquinhoChords(getActivity(),preferences,unique_chords.get(l));
                    } else if (myinstr.equals("b") && !containsit) {
                        ChordDirectory.banjo4stringChords(getActivity(),preferences,unique_chords.get(l));
                    } else if (myinstr.equals("B") && !containsit) {
                        ChordDirectory.banjo5stringChords(getActivity(),preferences,unique_chords.get(l));
                    }

                    // If chord is custom, prepare this prefix to the name
                    String iscustom = "";
                    if (unique_chords.get(l).contains("$$$")) {
                        iscustom = "\n" + getResources().getString(R.string.custom) + "";
                        StaticVariables.chordnotes = unique_chords.get(l);
                        StaticVariables.chordnotes = StaticVariables.chordnotes.replace("$$$", "");
                        unique_chords.set(l, unique_chords.get(l).replace("$$$", ""));
                        int startposcname = unique_chords.get(l).lastIndexOf("_");
                        if (startposcname != -1) {
                            unique_chords.set(l, unique_chords.get(l).substring(startposcname + 1));
                        }
                    }

                    // Prepare a new Horizontal Linear Layout for each chord
                    TableRow chordview = new TableRow(getActivity());
                    TableLayout.LayoutParams tableRowParams =
                            new TableLayout.LayoutParams
                                    (TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

                    int leftMargin = 10;
                    int topMargin = 10;
                    int rightMargin = 10;
                    int bottomMargin = 10;

                    tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

                    chordview.setLayoutParams(tableRowParams);
                    TextView chordname = new TextView(getActivity());
                    ImageView image1 = new ImageView(getActivity());
                    ImageView image2 = new ImageView(getActivity());
                    ImageView image3 = new ImageView(getActivity());
                    ImageView image4 = new ImageView(getActivity());
                    ImageView image5 = new ImageView(getActivity());
                    ImageView image6 = new ImageView(getActivity());
                    ImageView image0 = new ImageView(getActivity());

                    // Initialise 6 strings and frets
                    String string_6 = "";
                    String string_5 = "";
                    String string_4 = "";
                    String string_3 = "";
                    String string_2 = "";
                    String string_1 = "";
                    String fret = "";

                    switch (preferences.getMyPreferenceString(getActivity(),"chordInstrument","g")) {
                        case "g":

                            if (StaticVariables.chordnotes.length() > 0) {
                                string_6 = StaticVariables.chordnotes.substring(0, 1);
                            }
                            if (StaticVariables.chordnotes.length() > 1) {
                                string_5 = StaticVariables.chordnotes.substring(1, 2);
                            }
                            if (StaticVariables.chordnotes.length() > 2) {
                                string_4 = StaticVariables.chordnotes.substring(2, 3);
                            }
                            if (StaticVariables.chordnotes.length() > 3) {
                                string_3 = StaticVariables.chordnotes.substring(3, 4);
                            }
                            if (StaticVariables.chordnotes.length() > 4) {
                                string_2 = StaticVariables.chordnotes.substring(4, 5);
                            }
                            if (StaticVariables.chordnotes.length() > 5) {
                                string_1 = StaticVariables.chordnotes.substring(5, 6);
                            }
                            if (StaticVariables.chordnotes.length() > 7) {
                                fret = StaticVariables.chordnotes.substring(7, 8);
                            }

                            // Prepare string_6
                            switch (string_6) {
                                case "0":
                                    image6.setImageDrawable(l0);
                                    break;
                                case "1":
                                    image6.setImageDrawable(l1);
                                    break;
                                case "2":
                                    image6.setImageDrawable(l2);
                                    break;
                                case "3":
                                    image6.setImageDrawable(l3);
                                    break;
                                case "4":
                                    image6.setImageDrawable(l4);
                                    break;
                                case "5":
                                    image6.setImageDrawable(l5);
                                    break;
                                default:
                                    image6.setImageDrawable(lx);
                                    break;
                            }

                            // Prepare string_5
                            switch (string_5) {
                                case "0":
                                    image5.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image5.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image5.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image5.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image5.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image5.setImageDrawable(m5);
                                    break;
                                default:
                                    image5.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_4
                            switch (string_4) {
                                case "0":
                                    image4.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image4.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image4.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image4.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image4.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image4.setImageDrawable(m5);
                                    break;
                                default:
                                    image4.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_3
                            switch (string_3) {
                                case "0":
                                    image3.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image3.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image3.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image3.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image3.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image3.setImageDrawable(m5);
                                    break;
                                default:
                                    image3.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_2
                            switch (string_2) {
                                case "0":
                                    image2.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image2.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image2.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image2.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image2.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image2.setImageDrawable(m5);
                                    break;
                                default:
                                    image2.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_1
                            switch (string_1) {
                                case "0":
                                    image1.setImageDrawable(r0);
                                    break;
                                case "1":
                                    image1.setImageDrawable(r1);
                                    break;
                                case "2":
                                    image1.setImageDrawable(r2);
                                    break;
                                case "3":
                                    image1.setImageDrawable(r3);
                                    break;
                                case "4":
                                    image1.setImageDrawable(r4);
                                    break;
                                case "5":
                                    image1.setImageDrawable(r5);
                                    break;
                                default:
                                    image1.setImageDrawable(rx);
                                    break;
                            }

                            // Prepare fret
                            switch (fret) {
                                case "1":
                                    image0.setImageDrawable(f1);
                                    break;
                                case "2":
                                    image0.setImageDrawable(f2);
                                    break;
                                case "3":
                                    image0.setImageDrawable(f3);
                                    break;
                                case "4":
                                    image0.setImageDrawable(f4);
                                    break;
                                case "5":
                                    image0.setImageDrawable(f5);
                                    break;
                                case "6":
                                    image0.setImageDrawable(f6);
                                    break;
                                case "7":
                                    image0.setImageDrawable(f7);
                                    break;
                                case "8":
                                    image0.setImageDrawable(f8);
                                    break;
                                case "9":
                                    image0.setImageDrawable(f9);
                                    break;
                                default:
                                    image0 = null;
                                    break;
                            }

                            chordname.setPadding(0, 0, 12, 0);
                            chordview.addView(chordname);
                            if (image0 != null) {
                                chordview.addView(image0);
                            }
                            chordview.addView(image6);
                            chordview.addView(image5);
                            chordview.addView(image4);
                            chordview.addView(image3);
                            chordview.addView(image2);
                            chordview.addView(image1);

                            break;
                        case "B":

                            if (StaticVariables.chordnotes.length() > 0) {
                                string_5 = StaticVariables.chordnotes.substring(0, 1);
                            }
                            if (StaticVariables.chordnotes.length() > 1) {
                                string_4 = StaticVariables.chordnotes.substring(1, 2);
                            }
                            if (StaticVariables.chordnotes.length() > 2) {
                                string_3 = StaticVariables.chordnotes.substring(2, 3);
                            }
                            if (StaticVariables.chordnotes.length() > 3) {
                                string_2 = StaticVariables.chordnotes.substring(3, 4);
                            }
                            if (StaticVariables.chordnotes.length() > 4) {
                                string_1 = StaticVariables.chordnotes.substring(4, 5);
                            }
                            if (StaticVariables.chordnotes.length() > 6) {
                                fret = StaticVariables.chordnotes.substring(6, 7);
                            }

                            // Prepare string_5
                            switch (string_5) {
                                case "0":
                                    image5.setImageDrawable(l0);
                                    break;
                                case "1":
                                    image5.setImageDrawable(l1);
                                    break;
                                case "2":
                                    image5.setImageDrawable(l2);
                                    break;
                                case "3":
                                    image5.setImageDrawable(l3);
                                    break;
                                case "4":
                                    image5.setImageDrawable(l4);
                                    break;
                                case "5":
                                    image5.setImageDrawable(l5);
                                    break;
                                default:
                                    image5.setImageDrawable(lx);
                                    break;
                            }

                            // Prepare string_4
                            switch (string_4) {
                                case "0":
                                    image4.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image4.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image4.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image4.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image4.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image4.setImageDrawable(m5);
                                    break;
                                default:
                                    image4.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_3
                            switch (string_3) {
                                case "0":
                                    image3.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image3.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image3.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image3.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image3.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image3.setImageDrawable(m5);
                                    break;
                                default:
                                    image3.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_2
                            switch (string_2) {
                                case "0":
                                    image2.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image2.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image2.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image2.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image2.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image2.setImageDrawable(m5);
                                    break;
                                default:
                                    image2.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_1
                            switch (string_1) {
                                case "0":
                                    image1.setImageDrawable(r0);
                                    break;
                                case "1":
                                    image1.setImageDrawable(r1);
                                    break;
                                case "2":
                                    image1.setImageDrawable(r2);
                                    break;
                                case "3":
                                    image1.setImageDrawable(r3);
                                    break;
                                case "4":
                                    image1.setImageDrawable(r4);
                                    break;
                                case "5":
                                    image1.setImageDrawable(r5);
                                    break;
                                default:
                                    image1.setImageDrawable(rx);
                                    break;
                            }

                            // Prepare fret
                            switch (fret) {
                                case "1":
                                    image0.setImageDrawable(f1);
                                    break;
                                case "2":
                                    image0.setImageDrawable(f2);
                                    break;
                                case "3":
                                    image0.setImageDrawable(f3);
                                    break;
                                case "4":
                                    image0.setImageDrawable(f4);
                                    break;
                                case "5":
                                    image0.setImageDrawable(f5);
                                    break;
                                case "6":
                                    image0.setImageDrawable(f6);
                                    break;
                                case "7":
                                    image0.setImageDrawable(f7);
                                    break;
                                case "8":
                                    image0.setImageDrawable(f8);
                                    break;
                                case "9":
                                    image0.setImageDrawable(f9);
                                    break;
                                default:
                                    image0 = null;
                                    break;
                            }

                            chordview.addView(chordname);
                            if (image0 != null) {
                                chordview.addView(image0);
                            }
                            chordview.addView(image5);
                            chordview.addView(image4);
                            chordview.addView(image3);
                            chordview.addView(image2);
                            chordview.addView(image1);

                            break;
                        case "u":
                        case "m":
                        case "c":
                        case "b":
                            if (StaticVariables.chordnotes.length() > 0) {
                                string_4 = StaticVariables.chordnotes.substring(0, 1);
                            }
                            if (StaticVariables.chordnotes.length() > 1) {
                                string_3 = StaticVariables.chordnotes.substring(1, 2);
                            }
                            if (StaticVariables.chordnotes.length() > 2) {
                                string_2 = StaticVariables.chordnotes.substring(2, 3);
                            }
                            if (StaticVariables.chordnotes.length() > 3) {
                                string_1 = StaticVariables.chordnotes.substring(3, 4);
                            }
                            if (StaticVariables.chordnotes.length() > 5) {
                                fret = StaticVariables.chordnotes.substring(5, 6);
                            }

                            // Prepare string_4
                            switch (string_4) {
                                case "0":
                                    image4.setImageDrawable(l0);
                                    break;
                                case "1":
                                    image4.setImageDrawable(l1);
                                    break;
                                case "2":
                                    image4.setImageDrawable(l2);
                                    break;
                                case "3":
                                    image4.setImageDrawable(l3);
                                    break;
                                case "4":
                                    image4.setImageDrawable(l4);
                                    break;
                                default:
                                    image4.setImageDrawable(lx);
                                    break;
                            }

                            // Prepare string_3
                            switch (string_3) {
                                case "0":
                                    image3.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image3.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image3.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image3.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image3.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image3.setImageDrawable(m5);
                                    break;
                                default:
                                    image3.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_2
                            switch (string_2) {
                                case "0":
                                    image2.setImageDrawable(m0);
                                    break;
                                case "1":
                                    image2.setImageDrawable(m1);
                                    break;
                                case "2":
                                    image2.setImageDrawable(m2);
                                    break;
                                case "3":
                                    image2.setImageDrawable(m3);
                                    break;
                                case "4":
                                    image2.setImageDrawable(m4);
                                    break;
                                case "5":
                                    image2.setImageDrawable(m5);
                                    break;
                                default:
                                    image2.setImageDrawable(mx);
                                    break;
                            }

                            // Prepare string_1
                            switch (string_1) {
                                case "0":
                                    image1.setImageDrawable(r0);
                                    break;
                                case "1":
                                    image1.setImageDrawable(r1);
                                    break;
                                case "2":
                                    image1.setImageDrawable(r2);
                                    break;
                                case "3":
                                    image1.setImageDrawable(r3);
                                    break;
                                case "4":
                                    image1.setImageDrawable(r4);
                                    break;
                                case "5":
                                    image1.setImageDrawable(r5);
                                    break;
                                default:
                                    image1.setImageDrawable(rx);
                                    break;
                            }

                            // Prepare fret
                            switch (fret) {
                                case "1":
                                    image0.setImageDrawable(f1);
                                    break;
                                case "2":
                                    image0.setImageDrawable(f2);
                                    break;
                                case "3":
                                    image0.setImageDrawable(f3);
                                    break;
                                case "4":
                                    image0.setImageDrawable(f4);
                                    break;
                                case "5":
                                    image0.setImageDrawable(f5);
                                    break;
                                case "6":
                                    image0.setImageDrawable(f6);
                                    break;
                                case "7":
                                    image0.setImageDrawable(f7);
                                    break;
                                case "8":
                                    image0.setImageDrawable(f8);
                                    break;
                                case "9":
                                    image0.setImageDrawable(f9);
                                    break;
                                default:
                                    image0 = null;
                                    break;
                            }

                            chordview.addView(chordname);
                            if (image0 != null) {
                                chordview.addView(image0);
                            }
                            chordview.addView(image4);
                            chordview.addView(image3);
                            chordview.addView(image2);
                            chordview.addView(image1);
                            break;
                    }

                    if (StaticVariables.chordnotes != null && !StaticVariables.chordnotes.contains("xxxx_") && !StaticVariables.chordnotes.contains("xxxxxx_")) {
                        chordimageshere.addView(chordview);
                        String text;
                        if (unique_chords.get(l) == null) {
                            text = "" + iscustom;
                        } else {
                            text = unique_chords.get(l) + iscustom;
                        }
                        chordname.setText(text);
                        chordname.setTextColor(0xffffffff);
                        chordname.setTextSize(20);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            StaticVariables.showCapoInChordsFragment = false;
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (prepare_chords!=null) {
            prepare_chords.cancel(true);
        }
        this.dismiss();
    }

}*/
