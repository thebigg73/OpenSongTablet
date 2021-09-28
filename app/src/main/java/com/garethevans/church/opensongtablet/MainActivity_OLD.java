/*


    // Casting
    private CastContext castContext;
    private MySessionManagerListener sessionManagerListener;
    private CastSession castSession;
    private CastStateListener castStateListener;
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouterCallback mediaRouterCallback;
    private CastDevice castDevice;
    private SessionManager sessionManager;
    private Display display;
    private ExternalDisplay externalDisplay;
    private PresentationCommon presentationCommon;
    //private PresentationServiceHDMI hdmi;

    // The song views.
    // Stored here so they can be accessed via the different modes and classes
    private ArrayList<View> sectionViews;
    private LinearLayout songSheetTitleLayout;
    private ArrayList<Integer> sectionWidths, sectionHeights, songSheetTitleLayoutSize;


    // Importing/exporting
    private String importFilename;
    private Uri importUri;
    private String whichMode;











    // MainActivity standard Overrides
    /*





























    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        //fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        navigateToFragment(null,caller.getId());
        //navController.navigate(caller.getId());
        /*fragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }











}
*/