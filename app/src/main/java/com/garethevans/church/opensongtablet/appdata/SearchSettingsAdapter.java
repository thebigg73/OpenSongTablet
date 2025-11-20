package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.bible.BibleGatewayBottomSheet;
import com.garethevans.church.opensongtablet.bible.BibleOfflineBottomSheet;
import com.garethevans.church.opensongtablet.chords.ChordFingeringBottomSheet;
import com.garethevans.church.opensongtablet.chords.TransposeBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.midi.MidiSongBottomSheet;
import com.garethevans.church.opensongtablet.songprocessing.CreateSongBottomSheet;
import com.garethevans.church.opensongtablet.utilities.SoundLevelBottomSheet;
import com.garethevans.church.opensongtablet.utilities.TunerBottomSheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchSettingsAdapter extends RecyclerView.Adapter<SearchSettingsViewHolder> {

    // This holds the searchable settings with some keywords, seach phrases, etc.
    private final String TAG = "SearchSettings";
    private final Context c;
    private List<SettingItem> allItems;
    private final List<SettingItem> displayedItems;
    private final MainActivityInterface mainActivityInterface;
    private final SearchMenuFragment searchMenuFragment;

    public SearchSettingsAdapter(Context c, SearchMenuFragment searchMenuFragment) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.searchMenuFragment = searchMenuFragment;
        setupItems();
        displayedItems = new ArrayList<>(allItems);
    }

    // IMPORTANT: THIS IS ALL THE MENU OPTIONS - MUST BE KEPT UP TO DATE FOR SEARCH
    private void setupItems() {
        // Main settings menus
        // Each item as the title, description, list of keywords, navigation deeplink

        // Key words (alphabetical) - likely add to translation strings (will be used in lowercase for match)
        // Some will not need translation (marked as // * comment)
        String abc = "abc"; // *
        String about = c.getString(R.string.about);
        String acceleration = c.getString(R.string.acceleration);
        String action = c.getString(R.string.action);
        String actions = c.getString(R.string.actions);
        String actionbar = c.getString(R.string.actionbar_display);
        String add = c.getString(R.string.add);
        String addsongs = c.getString(R.string.add_songs);
        String advertise = c.getString(R.string.connections_advertise);
        String aeros = "Aeros"; // *
        String alphabetical = c.getString(R.string.alphabetical);
        String app = c.getString(R.string.app);
        String audio = c.getString(R.string.audio);
        String author = c.getString(R.string.author);
        String automatic = c.getString(R.string.automatic);
        String autohide = c.getString(R.string.autohide);
        String autoscale = c.getString(R.string.autoscale);
        String autoscroll = c.getString(R.string.autoscroll);
        String background = c.getString(R.string.background);
        String backing = c.getString(R.string.backing);
        String backup = c.getString(R.string.backup);
        String band = c.getString(R.string.band);
        String banjo = c.getString(R.string.banjo4);
        String battery = c.getString(R.string.battery);
        String bb = "BB"; // *
        String beatbuddy = "BeatBuddy"; // *
        String beats = c.getString(R.string.beats);
        String bible = c.getString(R.string.bible);
        String biblegateway = "BibleGateway"; // *
        String bigger = c.getString(R.string.bigger);
        String blank = c.getString(R.string.blank);
        String ble = "Ble"; // *
        String bluetooth = "Bluetooth"; // *
        String board = c.getString(R.string.board);
        String bold = c.getString(R.string.bold);
        String bottom = c.getString(R.string.bottom);
        String bpm = c.getString(R.string.bpm);
        String bracket = c.getString(R.string.brackets);
        String browser = c.getString(R.string.browser);
        String buddy = "Buddy"; // *
        String bulk = c.getString(R.string.bulk);
        String button = c.getString(R.string.button);
        String camera = c.getString(R.string.camera);
        String capo = c.getString(R.string.capo);
        String cast = "Cast"; // *
        String cavaquinho = c.getString(R.string.cavaquinho);
        String cc = "CC"; // *
        String ccli = c.getString(R.string.ccli);
        String change = c.getString(R.string.change);
        String chapter = c.getString(R.string.chapter);
        String checkbox = c.getString(R.string.checkbox);
        String chopro = "Chopro"; // *
        String chordie = "Chordie"; // *
        String chordpro = c.getString(R.string.chordpro); // *
        String chords = c.getString(R.string.chords);
        String chromecast = "Chromecast"; // *
        String church = c.getString(R.string.church);
        String clear = c.getString(R.string.clear);
        String click = c.getString(R.string.sound_click);
        String client = c.getString(R.string.client);
        String clock = c.getString(R.string.clock);
        String code = c.getString(R.string.code);
        String color = c.getString(R.string.color);
        String colour = c.getString(R.string.colour);
        String connect = c.getString(R.string.connect);
        String connected = c.getString(R.string.connected);
        String controller = c.getString(R.string.controller);
        String contribute = c.getString(R.string.contribute);
        String controls = c.getString(R.string.controls);
        String copy = c.getString(R.string.copy_of);
        String copyright = c.getString(R.string.copyright);
        String crash = c.getString(R.string.crash);
        String create = c.getString(R.string.create);
        String csv = "csv"; // *
        String curly = c.getString(R.string.curly);
        String custom = c.getString(R.string.custom);
        String dark = c.getString(R.string.dark);
        String database = c.getString(R.string.database);
        String decrease = c.getString(R.string.decrease);
        String delete = c.getString(R.string.delete);
        String device = c.getString(R.string.device);
        String diagram = c.getString(R.string.diagram);
        String discover = c.getString(R.string.connections_discover);
        String display = c.getString(R.string.display);
        String doc = "doc"; // *
        String docx = "docx"; // *
        //String donate = c.getString(R.string.donate);
        String doremi = "doremi"; // *
        String download = c.getString(R.string.download);
        String draw = c.getString(R.string.draw);
        String drawing = c.getString(R.string.drawing);
        String drum = c.getString(R.string.drum);
        String duplicate = c.getString(R.string.duplicate);
        String edit = c.getString(R.string.edit);
        String empty = c.getString(R.string.empty);
        String export = c.getString(R.string.export);
        String file = c.getString(R.string.file);
        String filter = c.getString(R.string.filters);
        String filtering = c.getString(R.string.filtering);
        String finger = c.getString(R.string.finger);
        String fingering = c.getString(R.string.fingering);
        String folder = c.getString(R.string.folder);
        String font = c.getString(R.string.font);
        String foot = c.getString(R.string.foot);
        String format = c.getString(R.string.format);
        String forum = c.getString(R.string.forum);
        String fret = c.getString(R.string.fret);
        String full = c.getString(R.string.full);
        String gesture = c.getString(R.string.gesture);
        String get = c.getString(R.string.get);
        String github = "GitHub"; // *
        String guide = c.getString(R.string.guide);
        String guitar = c.getString(R.string.guitar);
        String hardware = c.getString(R.string.hardware);
        String hdmi = "HDMI"; // *
        String help = c.getString(R.string.help);
        String hide = c.getString(R.string.hide);
        String highlighter = c.getString(R.string.highlight);
        String host = c.getString(R.string.host);
        String hot = c.getString(R.string.hot);
        String hotzones = c.getString(R.string.hot_zones);
        String image = c.getString(R.string.image);
        String immersive = c.getString(R.string.immersive);
        String import_string = c.getString(R.string.import_basic);
        String increase = c.getString(R.string.increase);
        String index = c.getString(R.string.index);
        String information = c.getString(R.string.information);
        String inline = c.getString(R.string.inline);
        String instrument = c.getString(R.string.instrument);
        String internet = c.getString(R.string.internet);
        String ios = "iOS"; // *
        String justchords = c.getString(R.string.justchords);
        String key = c.getString(R.string.key);
        String keyword = c.getString(R.string.keyword);
        String language = c.getString(R.string.language);
        String larger = c.getString(R.string.larger);
        String left = c.getString(R.string.pan_left);
        String level = c.getString(R.string.level);
        String license = c.getString(R.string.license);
        String licence = c.getString(R.string.licence);
        String light = c.getString(R.string.light);
        String link = c.getString(R.string.link);
        String load = c.getString(R.string.load);
        String logs = c.getString(R.string.logs);
        String looper = c.getString(R.string.looper);
        String lyrics = c.getString(R.string.lyrics);
        String main = c.getString(R.string.mainfoldername);
        String managesets = c.getString(R.string.set_manage);
        String mandolin = c.getString(R.string.mandolin);
        String manual = c.getString(R.string.manual);
        String margins = c.getString(R.string.margins);
        String master = c.getString(R.string.master);
        String max = "Max"; // *
        String maximum = c.getString(R.string.maximum);
        String menu = c.getString(R.string.menu);
        String message = c.getString(R.string.message);
        String messages = c.getString(R.string.messages);
        String meter = c.getString(R.string.meter);
        String metronome = c.getString(R.string.metronome);
        String midi = "MIDI"; // *
        String mode = c.getString(R.string.mode);
        String move = c.getString(R.string.move);
        String multiline = c.getString(R.string.multiline);
        String multiple = c.getString(R.string.multiple);
        String multitrack = c.getString(R.string.multitrack);
        String music = c.getString(R.string.music);
        String nashville = "Nashville"; // *
        String nearby = "Nearby"; // *
        String new_string = c.getString(R.string.new_something);
        String next = c.getString(R.string.next);
        String notation = c.getString(R.string.notation);
        String note = c.getString(R.string.note);
        String numeral = c.getString(R.string.numeral);
        String online = c.getString(R.string.online);
        String onsong = "OnSong"; // *
        String open = c.getString(R.string.open);
        String opensong = "OpenSong"; // *
        String opensongapp = "OpenSongApp"; // *
        String order = c.getString(R.string.order);
        String osb = "osb"; // *
        String osbs = "osbs"; // *
        String pad = c.getString(R.string.pad);
        String padding = c.getString(R.string.padding);
        String page = c.getString(R.string.page);
        String paypal = "PayPal"; // *
        String pc = "PC"; // *
        String pedal = c.getString(R.string.pedal);
        String pen = c.getString(R.string.pen);
        String pencil = c.getString(R.string.pencil);
        String pdf = c.getString(R.string.pdf);
        String performance = c.getString(R.string.performance);
        String permission = c.getString(R.string.permission);
        String photo = c.getString(R.string.photo);
        String piano = c.getString(R.string.piano);
        String picture = c.getString(R.string.picture);
        String player = c.getString(R.string.player);
        String popup = c.getString(R.string.popup);
        String position = c.getString(R.string.position);
        String preferences = c.getString(R.string.preferences);
        String presenter = c.getString(R.string.presenter);
        String presentation = c.getString(R.string.presentation);
        String previous = c.getString(R.string.previous);
        String profile = c.getString(R.string.profile);
        String program = c.getString(R.string.program);
        String rate_string = c.getString(R.string.rate_string);
        String rating = c.getString(R.string.rating);
        String record = c.getString(R.string.record);
        String recorder = c.getString(R.string.recorder);
        String remove = c.getString(R.string.remove);
        String rename = c.getString(R.string.rename);
        String reset = c.getString(R.string.reset);
        String restore = c.getString(R.string.restore);
        String review = c.getString(R.string.review);
        String right = c.getString(R.string.pan_right);
        String root = c.getString(R.string.root);
        String sample = c.getString(R.string.sample);
        String save = c.getString(R.string.save);
        String scale = c.getString(R.string.scale);
        String scaling = c.getString(R.string.scaling);
        String score = c.getString(R.string.score);
        String screen = c.getString(R.string.screen);
        String scripture = c.getString(R.string.scripture);
        String scroll = c.getString(R.string.scroll);
        String scrolling = c.getString(R.string.scrolling);
        String search = c.getString(R.string.search);
        String secondary = c.getString(R.string.secondary);
        String section = c.getString(R.string.section);
        String select = c.getString(R.string.select);
        String send = c.getString(R.string.send);
        String server = c.getString(R.string.server);
        String set = c.getString(R.string.set);
        String sets = c.getString(R.string.sets);
        String setlist = c.getString(R.string.set_list);
        String setlists = c.getString(R.string.set_lists);
        String settings = c.getString(R.string.settings);
        String shape = c.getString(R.string.shape);
        String share = c.getString(R.string.share);
        String show = c.getString(R.string.show);
        String signature = c.getString(R.string.signature);
        String size = c.getString(R.string.size);
        String slave = c.getString(R.string.slave);
        String slide = c.getString(R.string.slide);
        String smaller = c.getString(R.string.smaller);
        String song = c.getString(R.string.song);
        String songactions = c.getString(R.string.song_actions);
        String songs = c.getString(R.string.songs);
        String songselect = "SongSelect"; // *
        String songsheet = c.getString(R.string.songsheet);
        String sound = c.getString(R.string.sound);
        String source = c.getString(R.string.source);
        String space = c.getString(R.string.space);
        String spacing = c.getString(R.string.spacing);
        String spotify = c.getString(R.string.spotify);
        String stage = c.getString(R.string.stage);
        String standard = c.getString(R.string.standard);
        String stem = c.getString(R.string.stem);
        String stems = c.getString(R.string.stems);
        String sticky = c.getString(R.string.sticky);
        String storage = c.getString(R.string.storage);
        String style = c.getString(R.string.style);
        String subdirectory = c.getString(R.string.subdirectory);
        String subdirectories = c.getString(R.string.subdirectories);
        String subfolder = c.getString(R.string.subfolder);
        String swipe = c.getString(R.string.swipe_string);
        String sync = "Sync"; // *
        String synchronise = c.getString(R.string.synchronise);
        String synchronize = c.getString(R.string.synchronize);
        String system = c.getString(R.string.system);
        String tab = c.getString(R.string.tab);
        String tag = c.getString(R.string.tag);
        String tap = c.getString(R.string.tap);
        String tempo = c.getString(R.string.tempo);
        String text = c.getString(R.string.text);
        String theme = c.getString(R.string.theme);
        String tick = c.getString(R.string.tick);
        String time = c.getString(R.string.time);
        String title = c.getString(R.string.title);
        String titlebar = c.getString(R.string.titlebar);
        String tock = c.getString(R.string.tock);
        String tool = c.getString(R.string.tool);
        String top = c.getString(R.string.top);
        String track = c.getString(R.string.track);
        String translation = c.getString(R.string.translation);
        String transpose = c.getString(R.string.transpose);
        String trigger = c.getString(R.string.trigger);
        String trim = c.getString(R.string.trim);
        String tune = c.getString(R.string.tune);
        String tuner = c.getString(R.string.tuner);
        String txt = "txt"; // *
        String ultimate = "Ultimate"; // *
        String ug = "UG"; // *
        String ultimateguitar = "UltimateGuitar"; // *
        String usage = c.getString(R.string.usage);
        String user = c.getString(R.string.user);
        String utilities = c.getString(R.string.utilities);
        String utility = c.getString(R.string.utility);
        String value = c.getString(R.string.midi_value);
        String verse = c.getString(R.string.verse);
        String version = c.getString(R.string.version);
        String vl3 = "VL3"; // *
        String voicelive = "VoiceLive"; // *
        String volume = c.getString(R.string.volume);
        String web = c.getString(R.string.web);
        String webserver = c.getString(R.string.web_server);
        String website = c.getString(R.string.website);
        String where = c.getString(R.string.where);
        String width = c.getString(R.string.width);
        String word = "Word"; // *
        String worship = c.getString(R.string.worship);
        String youtube = c.getString(R.string.youtube);
        String zone = c.getString(R.string.zone);

        allItems = Arrays.asList(
                //App modes: Performance, Stage, Presenter
                new SettingItem(c.getString(R.string.choose_app_mode),performance+", "+stage+", "+presenter,
                        Arrays.asList(mode,performance,stage,presenter),
                        c.getString(R.string.deeplink_app_mode),
                        settings+"/"+c.getString(R.string.choose_app_mode)),

                //  Local storage (change locations, subfolders, move songs, etc.)
                new SettingItem(c.getString(R.string.storage_local),c.getString(R.string.storage_settings),
                        Arrays.asList(storage,folder,subfolder,main,move,root,subdirectory,subdirectories,subfolder,where),
                        c.getString(R.string.deeplink_manage_storage),
                        settings+"/"+storage+"/"+c.getString(R.string.storage_local)),

                // OpenChords
                new SettingItem(c.getString(R.string.openchords),c.getString(R.string.openchords_info),
                        Arrays.asList(c.getString(R.string.openchords),synchronise,synchronize,sync,justchords),
                        c.getString(R.string.deeplink_openchords),
                        settings+"/"+storage+"/"+c.getString(R.string.openchords)),

                // Theme
                new SettingItem(c.getString(R.string.theme_choose),c.getString(R.string.theme_edit),
                        Arrays.asList(theme,display,light,dark,custom,color,colour,style),
                        c.getString(R.string.deeplink_theme),
                        settings+"/"+display+"/"+c.getString(R.string.theme_edit)),

                // Fonts
                new SettingItem(c.getString(R.string.font_choose),c.getString(R.string.font_choose_description),
                        Arrays.asList(font,text,display,style,lyrics,chords),
                        c.getString(R.string.deeplink_fonts),
                        settings+"/"+display+"/"+c.getString(R.string.font_choose)),

                // Scaling
                new SettingItem(scaling,c.getString(R.string.scaling_info),
                        Arrays.asList(scaling,display,size,increase,decrease,bigger,smaller,larger,scale,automatic,autoscale,max,maximum,display),
                        c.getString(R.string.deeplink_scaling),
                        settings+"/"+display+"/"+scaling),

                // Song display advanced settings
                new SettingItem(c.getString(R.string.song_display),c.getString(R.string.extra_settings),
                        Arrays.asList(display,song,songsheet,next,previous,autohide,automatic,hide,show,bold,bracket,format,lyrics,chords,curly,multiline,order,presentation,pdf,section,trim,filter,filtering),
                        c.getString(R.string.deeplink_display_extra),
                        settings+"/"+display+"/"+c.getString(R.string.song_display)),

                // Connected display
                new SettingItem(c.getString(R.string.connected_display),c.getString(R.string.connected_display_description),
                        Arrays.asList(display,song,chords,connected,hdmi,cast,chromecast,secondary,background,information,margins),
                        c.getString(R.string.deeplink_connected_display),
                        settings+"/"+display+"/"+c.getString(R.string.connected_display)),

                // Actionbar
                new SettingItem(c.getString(R.string.actionbar_display),c.getString(R.string.actionbar_info),
                        Arrays.asList(display,actionbar,battery,titlebar,autohide,automatic,hide,song,title,author,tempo,battery,time,clock),
                        c.getString(R.string.deeplink_actionbar),
                        settings+"/"+display+"/"+c.getString(R.string.actionbar_display)),

                // Menu settings
                new SettingItem(c.getString(R.string.menu_settings), c.getString(R.string.menu_settings_description),
                        Arrays.asList(display,settings,song,set,menu,title,checkbox,alphabetical,index,size,popup),
                        c.getString(R.string.deeplink_menu_settings),
                        settings+"/"+display+"/"+c.getString(R.string.menu_settings)),

                // Inline sets
                new SettingItem(c.getString(R.string.set_inline), c.getString(R.string.set_inline_info),
                        Arrays.asList(set,inline,setlist,setlists,width,size,left),
                        c.getString(R.string.deeplink_inlineset),
                        settings+"/"+display+"/"+c.getString(R.string.set_inline)),

                // Margins
                new SettingItem(margins + " ("+display+")", c.getString(R.string.margins_info),
                        Arrays.asList(margins,left,right,bottom,top,spacing,padding,system,immersive,mode,screen,space,spacing,full),
                        c.getString(R.string.deeplink_margins),
                        settings+"/"+display+"/"+c.getString(R.string.margins)),

                // Hardware acceleration
                new SettingItem(c.getString(R.string.hardware_acceleration) + " ("+display+")",c.getString(R.string.hardware_acceleration_info),
                        Arrays.asList(hardware,acceleration),
                        "action_hardwareAcceleration",
                        settings+"/"+display),

                // Backup
                new SettingItem(backup + " ("+song+")",c.getString(R.string.backup_info),
                        Arrays.asList(song,songs,backup,storage,osb),
                        c.getString(R.string.deeplink_backup),
                        settings+"/"+songactions+"/"+backup),

                // Add songs
                new SettingItem(addsongs,c.getString(R.string.import_main),
                        Arrays.asList(song,songs,add,new_string,create,import_string),
                        c.getString(R.string.deeplink_song_actions),
                        settings+"/"+songactions+"/"+addsongs),

                // Create new song
                new SettingItem(c.getString(R.string.create_new_song),opensong,
                        Arrays.asList(song,songs,create,new_string,opensong,add,blank,empty),
                        "bottomSheet_CreateSongBottomSheet",
                        settings+"/"+songactions+"/"+addsongs+"/"+c.getString(R.string.create_new_song)),

                // Online song
                new SettingItem(c.getString(R.string.online) + " ("+song+")",c.getString(R.string.online_services),
                        Arrays.asList(song,songs,online,import_string,ultimate,guitar,ug,ultimateguitar,songselect,chordie,select,download,get),
                        c.getString(R.string.deeplink_import_online),
                        settings+"/"+songactions+"/"+addsongs+"/"+c.getString(R.string.online)),

                // Add song as file
                new SettingItem(file + " ("+song+")",c.getString(R.string.import_other),
                        Arrays.asList(song,songs,import_string,file,word,doc,docx,text,txt,chordpro,chopro,onsong,pdf,onsong),
                        "action_importFile",
                        settings+"/"+songactions+"/"+addsongs+"/"+file),

                // Multiple files
                new SettingItem(c.getString(R.string.import_bulk) + " ("+song+")",c.getString(R.string.import_bulk_info),
                        Arrays.asList(song,songs,import_string,file,word,doc,docx,text,txt,chordpro,chopro,onsong,pdf,onsong,multiple,bulk),
                        c.getString(R.string.deeplink_import_bulk),
                        settings+"/"+songactions+"/"+addsongs+"/"+c.getString(R.string.import_bulk)),

                // Camera
                new SettingItem(c.getString(R.string.camera) + " ("+song+")", c.getString(R.string.camera_info),
                        Arrays.asList(song,songs,import_string,camera,picture,image,photo),
                        "action_camera",
                        settings+"/"+songactions+"/"+addsongs+"/"+c.getString(R.string.camera)),

                // OpenSongApp backup import
                new SettingItem(opensongapp + " ("+song+")",c.getString(R.string.import_osb),
                        Arrays.asList(song,songs,backup,import_string,restore,osb),
                        c.getString(R.string.deeplink_import_osb),
                        settings+"/"+songactions+"/"+addsongs+"/"+opensongapp),

                // iOS/OnSong
                new SettingItem(c.getString(R.string.onsong) + " ("+song+")",c.getString(R.string.onsong_import),
                        Arrays.asList(song,songs,import_string,ios,onsong,backup),
                        "action_importIos",
                        settings+"/"+songactions+"/"+addsongs+"/"+c.getString(R.string.onsong)),

                // Church sample songs
                new SettingItem(c.getString(R.string.my_church),c.getString(R.string.respect_copyright),
                        Arrays.asList(song,songs,add,import_string,download,sample,church,worship),
                        "action_churchSample",
                        settings+"/"+songactions+"/"+addsongs+"/"+c.getString(R.string.my_church)),

                // Band sample songs
                new SettingItem(c.getString(R.string.my_band),c.getString(R.string.respect_copyright),
                        Arrays.asList(song,songs,add,import_string,download,sample,band),
                        "action_bandSample",
                        settings+"/"+songactions+"/"+addsongs+"/"+c.getString(R.string.my_band)),

                // Edit song
                new SettingItem(c.getString(R.string.edit) + " ("+song+")",c.getString(R.string.edit_song),
                        Arrays.asList(song,songs,edit,change,lyrics,chords,settings),
                        c.getString(R.string.deeplink_edit),
                        settings+"/"+songactions+"/"+c.getString(R.string.edit)),

                // Duplicate song
                new SettingItem(duplicate + " ("+song+")",c.getString(R.string.duplicate_song),
                        Arrays.asList(song,songs,duplicate,copy,new_string,create),
                        "bottomSheet_duplicateSong",
                        settings+"/"+songactions+"/"+duplicate),

                // Delete song
                new SettingItem(c.getString(R.string.delete) + " ("+song+")",mainActivityInterface.getSong().getTitle(),
                        Arrays.asList(song,songs,delete,clear,remove),
                        "action_deleteSong",
                        settings+"/"+songactions+"/"+c.getString(R.string.delete)),

                // Export
                new SettingItem(c.getString(R.string.export) + " ("+song+")",c.getString(R.string.export_current_song),
                        Arrays.asList(song,songs,export,share),
                        c.getString(R.string.deeplink_export),
                        settings+"/"+songactions+"/"+export),

                // Pad
                new SettingItem(pad + " ("+song+")",c.getString(R.string.pad_info),
                        Arrays.asList(song,songs,pad,audio,backing,track,music),
                        c.getString(R.string.deeplink_pads),
                        settings+"/"+songactions+"/"+pad),

                // Autoscroll
                new SettingItem(autoscroll + " ("+song+")",c.getString(R.string.autoscroll_info),
                        Arrays.asList(song,songs,autoscroll,automatic,scroll,scrolling),
                        c.getString(R.string.deeplink_autoscroll_settings),
                        settings+"/"+songactions+"/"+autoscroll),

                // Metronome
                new SettingItem(metronome + " ("+song+")",c.getString(R.string.metronome_info),
                        Arrays.asList(song,songs,metronome,click,drum,tempo,time,signature,bpm),
                        c.getString(R.string.deeplink_metronome),
                        settings+"/"+songactions+"/"+metronome),

                // Sticky notes
                new SettingItem(c.getString(R.string.song_notes),c.getString(R.string.song_notes_edit),
                        Arrays.asList(song,songs,sticky,note),
                        c.getString(R.string.deeplink_sticky_notes),
                        settings+"/"+songactions+"/"+c.getString(R.string.song_notes)),

                // Highlighter
                new SettingItem(highlighter + " ("+song+")",c.getString(R.string.highlight_info),
                        Arrays.asList(song,songs,note,pen,pencil,highlighter,draw,drawing),
                        c.getString(R.string.deeplink_highlighter),
                        settings+"/"+songactions+"/"+highlighter),

                // Link
                new SettingItem(c.getString(R.string.link) + " ("+song+")",c.getString(R.string.link_info),
                        Arrays.asList(song,songs,link,audio,youtube,doc,music,backing,track,pad,spotify,music,web,online,internet),
                        c.getString(R.string.deeplink_links),
                        settings+"/"+songactions+"/"+c.getString(R.string.link)),

                // Chords
                new SettingItem(c.getString(R.string.chords),c.getString(R.string.chord_settings),
                        Arrays.asList(song,songs,chords,finger,fingering,shape,custom,transpose,settings,capo,key,format,autohide,nashville,doremi,numeral,standard,fret,draw,drawing,diagram,position,piano,banjo,guitar,cavaquinho,mandolin),
                        c.getString(R.string.deeplink_chords),
                        settings+"/"+songactions+"/"+chords),

                // Chord fingering
                new SettingItem(c.getString(R.string.chords),c.getString(R.string.chord_fingering),
                        Arrays.asList(song,songs,chords,finger,fingering,shape,custom,fret,draw,drawing,position,guitar,piano,cavaquinho,mandolin,banjo),
                        "bottomSheet_chordFingering",
                        settings+"/"+songactions+"/"+chords+"/"+chords),

                // Custom chords
                new SettingItem(c.getString(R.string.custom_chords),c.getString(R.string.custom_chords_info),
                        Arrays.asList(song,songs,chords,custom,shape,finger,fingering,position,draw,drawing,fret,guitar,piano,cavaquinho,mandolin,banjo),
                        c.getString(R.string.deeplink_chords_custom),
                        settings+"/"+songactions+"/"+chords+"/"+chords),

                // Transpose
                new SettingItem(transpose + " ("+song+")",c.getString(R.string.transpose_info),
                        Arrays.asList(song,songs,chords,transpose,key,fret,capo,change),
                        "bottomSheet_transpose",
                        settings+"/"+songactions+"/"+chords+"/"+transpose),

                // Chord settings
                new SettingItem(c.getString(R.string.chord_settings),c.getString(R.string.pref_key_text),
                        Arrays.asList(song,songs,chords,transpose,settings,capo,key,format,autohide,nashville,doremi,numeral,standard,fret),
                        c.getString(R.string.deeplink_chords),
                        settings+"/"+songactions+"/"+chords),

                // Music score
                new SettingItem(c.getString(R.string.music_score) + " ("+song+")",c.getString(R.string.music_score_info),
                        Arrays.asList(song,songs,music,score,tab,abc,notation),
                        c.getString(R.string.deeplink_abc),
                        settings+"/"+songactions+"/"+c.getString(R.string.music_score)),

                // MIDI for song
                new SettingItem(c.getString(R.string.midi) + " ("+song+")",c.getString(R.string.midi_commands),
                        Arrays.asList(song,songs,midi,send,trigger,automatic,pc,cc,program,change,controller,message),
                        "bottomSheet_songMidi",
                        settings+"/"+songactions+"/"+c.getString(R.string.midi)),

                // Song tags
                new SettingItem(c.getString(R.string.tag_song),c.getString(R.string.tag_song_info),
                        Arrays.asList(song,songs,tag,theme,keyword),
                        c.getString(R.string.deeplink_tags),
                        settings+"/"+songactions+"/"+c.getString(R.string.tag_song)),

                // YouTube
                new SettingItem(youtube,search,
                        Arrays.asList(song,songs,youtube,link,search),
                        "action_youtube",
                        settings+"/"+songactions+"/"+youtube),

                // YouTube Music
                new SettingItem("YouTube Music",search,
                        Arrays.asList(song,songs,youtube,music,backing,background,track,pad,audio,link,search),
                        "action_youtube",
                        settings+"/"+songactions+"/"+youtube),

                // Spotify
                new SettingItem(spotify,search,
                        Arrays.asList(song,songs,spotify,link,search,music,backing,track,audio,pad),
                        "action_spotify",
                        settings+"/"+songactions+"/"+spotify),

                // Multitrack player - song
                new SettingItem(c.getString(R.string.multitrack_player) + " ("+song+")",c.getString(R.string.multitrack_player_info),
                        Arrays.asList(song,songs,track,music,multiple,multitrack,track,audio,stem,stems,player),
                        "action_multitrack",
                        settings+"/"+songactions+"/" + multitrack),

                // Manage your sets
                new SettingItem(managesets,c.getString(R.string.set_manage_info),
                        Arrays.asList(song,songs,set,sets,setlist,setlists,load,save,export,share,backup,osbs,restore,import_string,new_string,create,new_string,edit,rename,delete,bible,verse,chapter,scripture,note,custom,slide,image),
                        c.getString(R.string.deeplink_sets),
                        settings+"/"+managesets),

                // Create new set
                new SettingItem(c.getString(R.string.set_new),c.getString(R.string.set_new_info),
                        Arrays.asList(set,sets,setlist,setlists,create,new_string,blank,delete),
                        "bottomSheet_createNewSet",
                        settings+"/"+managesets+"/"+c.getString(R.string.set_manage)+"/"+c.getString(R.string.set_new)),

                // Load set
                new SettingItem(c.getString(R.string.load) + " ("+set+")",c.getString(R.string.set_load_info),
                        Arrays.asList(set,sets,setlist,setlists,load),
                        "action_setLoad",
                        settings+"/"+managesets+"/"+load),

                // Save set
                new SettingItem(c.getString(R.string.save) + " ("+set+")",c.getString(R.string.set_save_info),
                        Arrays.asList(set,sets,setlist,setlists,save),
                        "action_setSave",
                        settings+"/"+managesets+"/"+save),

                // Rename set
                new SettingItem(c.getString(R.string.rename) + " ("+set+")",c.getString(R.string.rename_info),
                        Arrays.asList(set,sets,setlist,setlists,rename,change),
                        "action_setRename",
                        settings+"/"+managesets+"/"+rename),

                // Delete set
                new SettingItem(delete + " ("+set+")",c.getString(R.string.set_delete_info),
                        Arrays.asList(set,sets,setlist,setlists,delete,remove),
                        "action_setDelete",
                        settings+"/"+managesets+"/"+delete),

                // Bible (add to set)
                new SettingItem(bible + " ("+set+")",c.getString(R.string.bible_browse),
                        Arrays.asList(set,sets,setlist,setlists,bible,scripture,verse,chapter,add,online,download,biblegateway),
                        c.getString(R.string.deeplink_bible),
                        settings+"/"+managesets+"/"+bible),

                // Bible download
                new SettingItem(download,c.getString(R.string.bible_download_for_offline),
                        Arrays.asList(bible,scripture,verse,chapter,download),
                        c.getString(R.string.deeplink_bible_download),
                        settings+"/"+managesets+"/"+bible+"/"+download),

                // Bible search offline
                new SettingItem(bible,c.getString(R.string.bible_browse_offline),
                        Arrays.asList(bible,scripture,verse,chapter,search),
                        "bottomSheet_browseBibleOffline",
                        settings+"/"+managesets+"/"+bible+"/"+c.getString(R.string.bible_browse)),

                // Bible search BibleGateway
                new SettingItem(bible,c.getString(R.string.search_biblegateway),
                        Arrays.asList(bible,scripture,verse,chapter,search,biblegateway,online),
                        "bottomSheet_browseBibleOnline",
                        settings+"/"+managesets+"/"+bible+"/"+c.getString(R.string.bible_browse)),

                // Custom slide
                new SettingItem(c.getString(R.string.custom_slide) + " ("+set+")",c.getString(R.string.add_custom_slide),
                        Arrays.asList(set,sets,setlist,setlists,add,custom,slide),
                        c.getString(R.string.deeplink_custom_slide),
                        settings+"/"+managesets+"/"+c.getString(R.string.custom_slide)),

                // Export set
                new SettingItem(export + " ("+set+")",c.getString(R.string.set_share_info),
                        Arrays.asList(set,sets,setlist,setlists,share,export),
                        "action_exportSet",
                        settings+"/"+managesets+"/"+export),

                // Import set
                new SettingItem(import_string + " ("+set+")",c.getString(R.string.import_from_file),
                        Arrays.asList(set,sets,setlist,setlists,file,import_string,get,load),
                        "action_importSet",
                        settings+"/"+managesets+"/"+import_string),

                // Backup sets
                new SettingItem(backup + " ("+set+")",c.getString(R.string.backup_info),
                        Arrays.asList(set,sets,setlist,setlists,backup,save,osbs),
                        "action_backupSet",
                        settings+"/"+managesets+"/"+backup),

                // Restore sets
                new SettingItem(restore + " ("+set+")",c.getString(R.string.backup_import),
                        Arrays.asList(set,sets,setlist,setlists,backup,restore,import_string,load,osbs),
                        "action_backupSet",
                        settings+"/"+managesets+"/"+backup),

                // Controls
                new SettingItem(controls,c.getString(R.string.controls_description),
                        Arrays.asList(controls,page,button,action,actions,foot,pedal,gesture,custom,tap,swipe,hot,zone),
                        c.getString(R.string.deeplink_controls),
                        settings+"/"+controls),

                // Page buttons
                new SettingItem(c.getString(R.string.page_buttons),c.getString(R.string.quicklaunch_title),
                        Arrays.asList(controls,page,button,action,actions,custom,tap),
                        c.getString(R.string.deeplink_page_buttons),
                        settings+"/"+controls+"/"+c.getString(R.string.page_buttons)),

                // Pedal
                new SettingItem(pedal,c.getString(R.string.pedal_controls),
                        Arrays.asList(controls,button,action,actions,pedal,foot,custom,tap),
                        c.getString(R.string.deeplink_pedals),
                        settings+"/"+controls+"/"+pedal),

                // Custom gestures
                new SettingItem(c.getString(R.string.custom_gestures),c.getString(R.string.custom_gestures_info),
                        Arrays.asList(controls,page,action,actions,gesture,custom,tap,swipe),
                        c.getString(R.string.deeplink_gestures),
                        settings+"/"+controls+"/"+c.getString(R.string.custom_gestures)),

                // Swipe settings
                new SettingItem(c.getString(R.string.swipe),c.getString(R.string.swipe_info),
                        Arrays.asList(controls,page,button,action,actions,gesture,custom,tap,swipe),
                        c.getString(R.string.deeplink_controls),
                        settings+"/"+controls+"/"+c.getString(R.string.swipe)),

                // Hot zones
                new SettingItem(hotzones,c.getString(R.string.hot_zones_info),
                        Arrays.asList(controls,page,action,actions,custom,tap,hot,zone,hotzones),
                        c.getString(R.string.deeplink_controls),
                        settings+"/"+controls+"/"+hotzones),

                // Connect devices
                new SettingItem(c.getString(R.string.connections_connect),c.getString(R.string.connections_description),
                        Arrays.asList(connect,device,master,slave,host,client,advertise,discover,controls,sync,synchronise,synchronise,nearby),
                        c.getString(R.string.deeplink_nearby),
                        settings+"/"+c.getString(R.string.connections_connect)),

                // Web server
                new SettingItem(webserver,c.getString(R.string.web_server_info),
                    Arrays.asList(webserver,web,server,share,connect,song,songs,device,browser),
                    c.getString(R.string.website_web_server),
                    settings+"/"+webserver),

                // MIDI devices
                new SettingItem(c.getString(R.string.midi),c.getString(R.string.midi_description),
                        Arrays.asList(midi,send,trigger,automatic,device,connect,bluetooth,ble,program,change,pc,cc,controller,value,message,messages,settings,song,custom,board,drum,clock,click,track,tick,tock),
                        "bottomSheet_songMidi",
                        settings+"/"+songactions+"/"+c.getString(R.string.midi)),

                // Profile
                new SettingItem(profile,c.getString(R.string.profile_explanation),
                        Arrays.asList(profile,load,save,reset,clear,delete,settings,preferences),
                        c.getString(R.string.deeplink_profiles),
                        settings+"/"+profile),

                // CCLI
                new SettingItem(ccli,c.getString(R.string.ccli_description),
                        Arrays.asList(ccli,church,license,licence,copyright,permission,presentation),
                        c.getString(R.string.deeplink_ccli),
                        settings+"/"+ccli),

                // Utilities
                new SettingItem(utilities,c.getString(R.string.utilities_info),
                        Arrays.asList(utilities,utility,tool,beatbuddy,beats,buddy,voicelive,tune,guitar,tuner,vl3,aeros,sound,level,volume,meter,audio,record,recorder,player,multitrack,track,database,settings),
                        c.getString(R.string.deeplink_utilities),
                        settings+"/"+utilities),

                // Sound level meter
                new SettingItem(c.getString(R.string.sound_level_meter),c.getString(R.string.sound_level_meter_info),
                        Arrays.asList(utilities,utility,sound,level,volume,meter,audio),
                        "bottomSheet_soundLevelMeter",
                        settings+"/"+utilities+"/"+c.getString(R.string.sound_level_meter)),

                // Tuner
                new SettingItem(tuner,c.getString(R.string.tuner_info),
                        Arrays.asList(utilities,utility,tool,tune,guitar,tuner,instrument),
                        "bottomSheet_tuner",
                        settings+"/"+utilities+"/"+tuner),

                // Aeros
                new SettingItem(aeros,c.getString(R.string.aeros_info),
                        Arrays.asList(utilities,utility,tool,aeros,looper,midi),
                        c.getString(R.string.deeplink_aeros),
                        settings+"/"+utilities+"/"+aeros),

                // Beatbuddy
                new SettingItem(beatbuddy,c.getString(R.string.beat_buddy_info),
                        Arrays.asList(utilities,utility,tool,beatbuddy,bb,beats,buddy,drum,midi,import_string,csv,sync,synchronise,synchronize,song,songs,pedal),
                        c.getString(R.string.deeplink_beatbuddy_options),
                        settings+"/"+utilities+"/"+beatbuddy),

                // Beatbuddy song commands
                new SettingItem(beatbuddy + ": " + c.getString(R.string.midi_commands),c.getString(R.string.beat_buddy_info),
                        Arrays.asList(utilities,utility,tool,beatbuddy,beats,buddy,bb,drum,midi,song,songs,pedal),
                        c.getString(R.string.deeplink_beatbuddy_commands),
                        settings+"/"+utilities+"/"+beatbuddy+"/"+c.getString(R.string.midi_commands)),

                // Beatbuddy import project
                new SettingItem(c.getString(R.string.beat_buddy_import_project),c.getString(R.string.beat_buddy_import_project_info),
                        Arrays.asList(utilities,utility,tool,beatbuddy,beats,buddy,bb,drum,midi,import_string,sync,synchronise,synchronize,pedal,csv),
                        c.getString(R.string.deeplink_beatbuddy_import),
                        settings+"/"+utilities+"/"+beatbuddy+"/"+c.getString(R.string.beat_buddy_import_project)),

                // Beatbuddy show songs
                new SettingItem(beatbuddy+":"+c.getString(+R.string.show_songs),c.getString(R.string.beat_buddy_browse_info),
                        Arrays.asList(utilities,utility,tool,beatbuddy,beats,buddy,bb,drum,song,songs,select),
                        "bottomSheet_beatBuddyShowSongs",
                        settings+"/"+utilities+"/"+beatbuddy+"/"+c.getString(R.string.show_songs)),

                // Beatbuddy auto send song
                new SettingItem(c.getString(R.string.beat_buddy_auto),c.getString(R.string.beat_buddy_auto_info),
                        Arrays.asList(utilities,utility,tool,beatbuddy,beats,buddy,bb,drum,midi,import_string,sync,synchronise,synchronize,pedal,csv,automatic,song,songs),
                        "action_beatBuddyAuto",
                        settings+"/"+utilities+"/"+beatbuddy+"/"+c.getString(R.string.beat_buddy_auto)),

                // Beatbuddy reset database
                new SettingItem(beatbuddy + ": " + reset,c.getString(R.string.beat_buddy_database_reset),
                        Arrays.asList(utilities,utility,tool,beatbuddy,beats,bb,buddy,drum,midi,sync,reset,database,clear),
                        "action_beatBuddyReset",
                        settings+"/"+utilities+"/"+beatbuddy+"/"+reset),

                // Voicelive
                new SettingItem(voicelive,c.getString(R.string.voicelive_info),
                        Arrays.asList(utilities,utility,tool,vl3,voicelive,midi,settings,key,automatic),
                        c.getString(R.string.deeplink_voicelive),
                        settings+"/"+utilities+"/"+voicelive),

                // Audio recorder
                new SettingItem(c.getString(R.string.audio_recorder),c.getString(R.string.audio_recorder_info),
                        Arrays.asList(utilities,utility,tool,audio,record,recorder),
                        "action_audioRecorder",
                        settings+"/"+utilities+"/"+c.getString(R.string.audio_recorder)),

                // Audio recorder
                new SettingItem(c.getString(R.string.audio_player),c.getString(R.string.audio_player_info),
                        Arrays.asList(utilities,utility,tool,audio,record,recorder),
                        "action_audioPlayer",
                        settings+"/"+utilities+"/"+c.getString(R.string.audio_player)),

                // Multitrack player - utility
                new SettingItem(c.getString(R.string.multitrack_player),c.getString(R.string.multitrack_player_info),
                        Arrays.asList(song,songs,track,music,multiple,multitrack,track,audio,stem,stems,player),
                        "action_multitrack",
                        settings+"/"+utilities+"/"+multitrack),

                // Database management
                new SettingItem(c.getString(R.string.database_management),c.getString(R.string.database_management_info),
                        Arrays.asList(database,backup,reset,clear,trim,song,songs,pdf,image),
                        c.getString(R.string.deeplink_database_utilities),
                        settings+"/"+utilities+"/"+c.getString(R.string.database_management)),

                // About
                new SettingItem(c.getString(R.string.about),c.getString(R.string.about_description),
                        Arrays.asList(about,version,web,review,help,user,guide,manual,forum,github,website,rate_string,rating,language,logs,crash,usage,paypal,contribute),
                        c.getString(R.string.deeplink_about),
                        settings+"/"+about),

                // Website and user guide
                new SettingItem(website + " / " + c.getString(R.string.user_guide), "https://www.opensongapp.com",
                        Arrays.asList(website,user,guide,manual,help),
                        "action_website",
                        settings + "/" + about + "/" + website + " / "+c.getString(R.string.user_guide)),

                // Version
                new SettingItem(mainActivityInterface.getVersionNumber().getFullVersionInfo(), String.valueOf(mainActivityInterface.getVersionNumber().getVersionCode()),
                        Arrays.asList(about,version,opensongapp),
                        "action_version",
                        settings+"/"+about+"/"+version),

                // Forum
                new SettingItem(forum,c.getString(R.string.forum_description),
                        Arrays.asList(about,forum,help,contribute,website),
                        "action_forum",
                        settings+"/"+about+"/"+forum),

                // Rate the app
                new SettingItem(c.getString(R.string.rate),c.getString(R.string.rate_description),
                        Arrays.asList(about,review,rate_string,rating),
                        "action_rate",
                        settings+"/"+about+"/"+c.getString(R.string.rate)),

                // Language
                new SettingItem(c.getString(R.string.language),mainActivityInterface.getLocale().getDisplayLanguage(),
                        Arrays.asList(language,translation),
                        c.getString(R.string.deeplink_language),
                        settings+"/"+about+"/"+language),

                // GitHub
                new SettingItem(github,c.getString(R.string.github_description),
                        Arrays.asList(github,contribute,open,source,code,website),
                        "action_github",
                        settings+"/"+about+"/"+github),

                // Paypal
                /*
                Removed after Google deemed this non compliant
                new SettingItem (paypal,c.getString(R.string.paypal_description),
                        Arrays.asList(paypal,donate,contribute,website),
                        "action_paypal",
                        settings+"/"+about+"/"+paypal),
                */

                // App logs
                new SettingItem (c.getString(R.string.log),c.getString(R.string.log_info),
                        Arrays.asList(usage,crash,usage,logs,share),
                        c.getString(R.string.deeplink_logs),
                        settings+"/"+about+"/"+c.getString(R.string.log))

        );
    }

    public void dealWithClick(int position) {
        Log.d(TAG,"Clicked on pos:"+position);
        if (position >= 0 && position < displayedItems.size()) {
            SettingItem settingItem = displayedItems.get(position);
            // Get the deeplink.
            // Try any action or bottom sheet first
            Log.d(TAG,"title:"+settingItem.title);
            Log.d(TAG,"titleLower:"+settingItem.titleLower);
            Log.d(TAG,"deeplink:"+settingItem.deeplink);
            Log.d(TAG,"description:"+settingItem.description);
            if (!dealWithAction(settingItem) && !dealWithBottomSheet(settingItem)) {
                if (settingItem.deeplink != null && !settingItem.deeplink.isEmpty()) {
                    mainActivityInterface.navigateToFragment(settingItem.deeplink, 0);
                }
            }
        }
    }

    public boolean dealWithAction(SettingItem settingItem) {
        if (settingItem.deeplink.startsWith("action_")) {
            Log.d(TAG,"This is an action that we need to deal with");
            // Deal with action ....

            switch (settingItem.deeplink) {
                case "action_camera":
                    mainActivityInterface.setWhattodo("camera");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_import),0);
                    break;

                case "action_importFile":
                    mainActivityInterface.setWhattodo("file");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_import),0);
                    break;

                case "action_importSet":
                    mainActivityInterface.setWhattodo("importset");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sets),0);
                    break;

                case "action_importIos":
                    mainActivityInterface.setWhattodo("ios");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_import),0);
                    break;

                case "action_churchSample":
                    mainActivityInterface.setWhattodo("church");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_import),0);
                    break;

                case "action_bandSample":
                    mainActivityInterface.setWhattodo("band");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_import),0);
                    break;

                case "action_deleteSong":
                    mainActivityInterface.setWhattodo("deleteSong");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_song_actions),0);
                    break;

                case "action_youtube":
                    mainActivityInterface.setWhattodo("youTube");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_song_actions),0);
                    break;

                case "action_youtubeMusic":
                    mainActivityInterface.setWhattodo("youTubeMusic");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_song_actions),0);
                    break;

                case "action_spotify":
                    mainActivityInterface.setWhattodo("spotify");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_song_actions),0);
                    break;

                case "action_hardwareAcceleration":
                    mainActivityInterface.setWhattodo("hardwareAcceleration");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_display),0);
                    break;

                case "action_setSave":
                    mainActivityInterface.setWhattodo("saveset");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sets_manage), 0);
                    break;

                case "action_setLoad":
                    mainActivityInterface.setWhattodo("loadset");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sets_manage), 0);
                    break;

                case "action_setRename":
                    mainActivityInterface.setWhattodo("renameset");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sets_manage), 0);
                    break;

                case "action_setDelete":
                    mainActivityInterface.setWhattodo("deleteset");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sets_manage), 0);
                    break;

                case "action_exportSet":
                    mainActivityInterface.setWhattodo("exportset");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sets_manage), 0);
                    break;

                case "action_beatBuddyAuto":
                    mainActivityInterface.setWhattodo("beatBuddyAuto");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_beatbuddy_options),0);
                    break;

                case "action_beatBuddyReset":
                    mainActivityInterface.setWhattodo("beatBuddyReset");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_beatbuddy_options),0);
                    break;

                case "action_audioPlayer":
                    mainActivityInterface.setWhattodo("audioPlayer");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_utilities),0);
                    break;

                case "action_audioRecorder":
                    mainActivityInterface.setWhattodo("audioRecorder");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_utilities),0);
                    break;

                case "action_multitrack":
                    mainActivityInterface.setWhattodo("multitrack");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_utilities),0);
                    break;

                case "action_website":
                    mainActivityInterface.openDocument(c.getString(R.string.website_address));
                    break;

                case "action_version":
                    mainActivityInterface.openDocument(c.getString(R.string.website_latest));
                    break;

                case "action_forum":
                    mainActivityInterface.openDocument(c.getString(R.string.website_forum));
                    break;

                case "action_rate":
                    mainActivityInterface.openDocument(c.getString(R.string.website_rate));
                    break;

                case "action_github":
                    mainActivityInterface.openDocument(c.getString(R.string.website_github));
                    break;

                case "action_paypal":
                    mainActivityInterface.openDocument(c.getString(R.string.website_paypal));
                    break;

            }

            return true;
        } else {
            return false;
        }
    }
    public boolean dealWithBottomSheet(SettingItem settingItem) {
        if (settingItem.deeplink.startsWith("bottomSheet_")) {
            Log.d(TAG,"This is a bottom sheet that we need to deal with");
            // Deal with bottom sheet ....

            switch (settingItem.deeplink) {
                case "bottomSheet_chordFingering":
                    mainActivityInterface.navHome();
                    ChordFingeringBottomSheet chordFingeringBottomSheet = new ChordFingeringBottomSheet();
                    chordFingeringBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "ChordFingeringBottomSheet");
                    break;
                case "bottomSheet_transpose":
                    mainActivityInterface.navHome();
                    TransposeBottomSheet transposeBottomSheet = new TransposeBottomSheet(false);
                    transposeBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "TransposeBottomSheet");
                    break;
                case "bottomSheet_songMidi":
                    mainActivityInterface.navHome();
                    MidiSongBottomSheet midiSongBottomSheet = new MidiSongBottomSheet();
                    midiSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiSongBottomSheet");
                    break;
                case "bottomSheet_createNewSet":
                    mainActivityInterface.displayAreYouSure("newSet", c.getString(R.string.set_new), null, "SearchSettingsFragment", searchMenuFragment, null);
                    break;
                case "bottomSheet_duplicateSong":
                    mainActivityInterface.setWhattodo("duplicateSong");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_song_actions),0);
                    break;
                case "bottomSheet_browseBibleOffline":
                    BibleOfflineBottomSheet bibleOfflineBottomSheet = new BibleOfflineBottomSheet();
                    bibleOfflineBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"bibleOffLineBottomSheet");
                    mainActivityInterface.navHome();
                    break;
                case "bottomSheet_browseBibleOnline":
                    BibleGatewayBottomSheet bibleGatewayBottomSheet = new BibleGatewayBottomSheet();
                    bibleGatewayBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"bibleGatewayBottomSheet");
                    mainActivityInterface.navHome();
                    break;
                case "bottomSheet_soundLevelMeter":
                    mainActivityInterface.navHome();
                    SoundLevelBottomSheet soundLevelBottomSheet = new SoundLevelBottomSheet();
                    soundLevelBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"soundLevelBottomSheet");
                    break;
                case "bottomSheet_tuner":
                    mainActivityInterface.navHome();
                    TunerBottomSheet tunerBottomSheet = new TunerBottomSheet();
                    tunerBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"tunerBottomSheet");
                    break;
                case "bottomSheet_beatBuddyShowSongs":
                    mainActivityInterface.setWhattodo("beatbuddySongs");
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_beatbuddy_commands),0);
                    break;
                case "bottomSheet_CreateSongBottomSheet":
                    mainActivityInterface.navHome();
                    CreateSongBottomSheet createSongBottomSheet = new CreateSongBottomSheet();
                    createSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"CreateSongBottomSheet");
                    break;
            }

            return true;
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public SearchSettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_search_item, parent, false);

        return new SearchSettingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchSettingsViewHolder holder, int pos) {
        int position = holder.getAbsoluteAdapterPosition();
        SettingItem settingItem = displayedItems.get(position);
        holder.setItem(settingItem.title,settingItem.description,settingItem.menulocation,settingItem.deeplink);
        holder.searchItemLayout.setOnClickListener(view -> dealWithClick(position));
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public void filterAndRank(String query) {
        displayedItems.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedItems.addAll(allItems); // Return original if no query
        } else {

            String[] queryWords = query.toLowerCase().split("\\s+");
            List<ScoredItem> scoredItems = new ArrayList<>();

            for (int i = 0; i < allItems.size(); i++) {
                SettingItem item = allItems.get(i);
                int score = 0;

                // Title match (multi-word, high weight)
                score += scoreMultiWord(item.titleLower, queryWords, 5, true);

                // Keywords match (medium weight)
                for (String kw : item.keywordsLower) {
                    score += scoreMultiWord(kw, queryWords, 3, false);
                }

                // Description match (low weight)
                score += scoreMultiWord(item.descriptionLower, queryWords, 1, false);

                if (score > 0) {
                    scoredItems.add(new ScoredItem(item, score));
                }
            }

            scoredItems.sort((a, b) -> Integer.compare(b.score, a.score));

            List<SettingItem> result = new ArrayList<>();
            for (ScoredItem si : scoredItems) {
                result.add(si.item);
            }

            displayedItems.addAll(result);
        }
        notifyDataSetChanged();
    }

    private int scoreMultiWord(String text, String[] queryWords, int weight, boolean earlyExit) {
        int score = 0;

        for (String word : queryWords) {
            int index = text.indexOf(word);

            if (index >= 0) {
                if (index == 0) score += 50;
                else score += 30;
                if (index > 0 && text.charAt(index - 1) == ' ') score += 10;
                score += Math.max(0, 20 - index);
                score += Math.max(0, 10 - text.length() / 5);
                if (earlyExit && score >= 100) break; // already good enough
            } else {
                // Only do fuzzy check if no normal match
                int distance = levenshteinDistance(text, word);
                int maxTypos = Math.max(1, word.length() / 4);
                if (distance <= maxTypos) {
                    score += (30 - distance * 5);
                }
            }
        }

        return score * weight;
    }

    private int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    private static class ScoredItem {
        SettingItem item;
        int score;

        ScoredItem(SettingItem item, int score) {
            this.item = item;
            this.score = score;
        }
    }
}
