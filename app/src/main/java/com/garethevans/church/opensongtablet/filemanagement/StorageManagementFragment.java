package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.animation.ShowCase;
import com.garethevans.church.opensongtablet.databinding.StorageFolderDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.util.ArrayList;

import de.blox.graphview.Graph;
import de.blox.graphview.GraphAdapter;
import de.blox.graphview.GraphView;
import de.blox.graphview.Node;
import de.blox.graphview.tree.BuchheimWalkerAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerConfiguration;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class StorageManagementFragment extends DialogFragment {

    StorageFolderDisplayBinding myView;
    SQLiteHelper sqLiteHelper;
    StorageAccess storageAccess;
    ShowCase showCase;

    protected GraphView graphView;
    Graph graph;
    Node songs, parentNode;
    String fulladdress, folder, parent;

    GraphAdapter<GraphView.ViewHolder> adapter;
    MainActivityInterface mainActivityInterface;
    ArrayList<String> actualLocation, infos, dismisses, songIDs, availableFolders;
    ArrayList<View> views = new ArrayList<>();
    ArrayList<Boolean> rects = new ArrayList<>();
    int redColor, greenColor;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageFolderDisplayBinding.inflate(inflater, container, false);
        graphView = myView.graph;

        sqLiteHelper = new SQLiteHelper(requireContext());
        storageAccess = new StorageAccess();
        showCase = new ShowCase();

        redColor = getActivity().getResources().getColor(R.color.lightred);
        greenColor = getActivity().getResources().getColor(R.color.lightgreen);

        // TODO remove this once this fragment is finished development
        MaterialShowcaseView.resetSingleUse(requireContext(),"storageManagement");

        mainActivityInterface.updateToolbar(null,getActivity().getResources().getString(R.string.storage_choose));
        graph = new Graph();

        // Do this as separate tasks in a new thread
        setUpThread();

        return myView.getRoot();
    }

    private void setUpThread() {
        new Thread(() -> {
            createNodes();
            adapter = getGraphAdapter();
            adapter.notifyDataSetChanged();

            requireActivity().runOnUiThread(() -> {
                graphView.setAdapter(adapter);
                graphView.setLayout(new BuchheimWalkerAlgorithm(getConfiguration()));
            });

            setListeners();

            showLocations();
            // Prepare the showcase
            initialiseShowcaseArrays();
            requireActivity().runOnUiThread(() -> {
                prepareShowcaseViews();
                showCase.sequenceShowCase(requireActivity(),views,dismisses,infos,rects,"storageManagement");
            });
        }).start();
    }
    private void setListeners() {
        graphView.setOnItemClickListener((parent, view, position, id) -> {
            boolean root = position==0;
            boolean songs = position==1;
            try {
                TextView tv = view.findViewById(R.id.actualButton);
                String actLoc = tv.getText().toString();
                Log.d("d","actLoc="+actLoc);
                showActionDialog(root,songs,actLoc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void createNodes() {
        ArrayList<String> folders = getFoldersFromFile();
        actualLocation = new ArrayList<>();

        // Set up top level folders
        //  This will return a list like MAIN, Band, Musicals, Musicals/HSM, Alex, etc.
        Node root = new Node("OpenSongApp\n("+getActivity().getResources().getString(R.string.root)+")");
        actualLocation.add("root");
        songs = new Node("Songs\n(" + getActivity().getResources().getString(R.string.mainfoldername) + ")");
        actualLocation.add("Songs");
        graph.addEdge(root, songs);

        for (String thisfolder:folders) {
            if (!thisfolder.contains("/")) {
                parent = "";
            } else {
                parent = thisfolder.substring(0,thisfolder.indexOf("/"));
            }
            folder = thisfolder;
            fulladdress = thisfolder;
            parentNode = songs;
            makeNodes();
        }
    }

    private GraphAdapter<GraphView.ViewHolder> getGraphAdapter() {
        return new GraphAdapter<GraphView.ViewHolder>(graph) {

            @Override
            public int getCount() {
                return graph.getNodeCount();
            }

            @Override
            public Object getItem(int position) {
                return graph.getNodeAtPosition(position);
            }

            @Override
            public boolean isEmpty() {
                return !graph.hasNodes();
            }

            @NonNull
            @Override
            public GraphView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_folder_display_node, parent, false);
                return new SimpleViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull GraphView.ViewHolder viewHolder, @NonNull Object data, int position) {
                int color = 66;
                if (position==0) {
                    // Color this red
                    color = redColor;
                } else if (position==1) {
                   color = greenColor;
                }
                String simpleText = ((Node)data).getData().toString();
                String actualText = simpleText;
                if (simpleText.contains("/")) {
                    simpleText = simpleText.substring(simpleText.lastIndexOf("/"));
                    simpleText = simpleText.replace("/","");
                }
                ((SimpleViewHolder) viewHolder).nodeButton.setText(simpleText);
                ((SimpleViewHolder) viewHolder).nodeActual.setText(actualText);

                if (color!=66 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((SimpleViewHolder) viewHolder).nodeButton.setBackgroundTintList(ColorStateList.valueOf(color));
                }
            }

            class SimpleViewHolder extends GraphView.ViewHolder {
                TextView nodeButton;
                TextView nodeActual;

                SimpleViewHolder(View itemView) {
                    super(itemView);
                    nodeButton = itemView.findViewById(R.id.nodeButton);
                    nodeActual = itemView.findViewById(R.id.actualButton);

                }
            }
        };
    }

    private BuchheimWalkerConfiguration getConfiguration() {
        return new BuchheimWalkerConfiguration.Builder()
                .setSiblingSeparation(50)
                .setLevelSeparation(100)
                .setSubtreeSeparation(100)
                .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT)
                .build();
    }

    private void initialiseShowcaseArrays() {
        views = new ArrayList<>();
        infos = new ArrayList<>();
        dismisses = new ArrayList<>();
        rects = new ArrayList<>();
        infos.add(getActivity().getResources().getString(R.string.info_reset_storage));
        dismisses.add(null);
        rects.add(true);
        infos.add(getActivity().getResources().getString(R.string.info_songs_folder));
        dismisses.add(null);
        rects.add(true);
    }

    private void prepareShowcaseViews() {
        views.add(graphView.getChildAt(0));
        views.add(graphView.getChildAt(1));
    }

    private void showActionDialog(boolean root, boolean songs, String folder) {
        FolderManagementDialog dialogFragment = new FolderManagementDialog(this,root,songs,folder);
        dialogFragment.show(requireActivity().getSupportFragmentManager(),"folderManagementDialog");
    }

    public void updateFragment() {
        // Called from MainActivity when change has been made from Dialog
        Log.d("d","Update fragment called");
        graph = new Graph();
        setUpThread();
    }

    private ArrayList<String> getFoldersFromFile() {
        songIDs = storageAccess.getSongIDsFromFile(getActivity());
        // Each subdir ends with /
        availableFolders = new ArrayList<>();
        for (String entry:songIDs) {
            if (entry.endsWith("/")) {
                String newtext = entry.substring(0,entry.lastIndexOf("/"));
                availableFolders.add(newtext);
                Log.d("d",newtext);
            }
        }
        return availableFolders;
    }

    private void showLocations() {
        for (String entry:actualLocation) {
            Log.d("d","actualLocation="+entry);
        }
    }

    private void makeNodes() {
        // We are sent each folder in turn.  Sometimes they are root folders, other times subfolders
        // We need to figure this out, create them and add them as appropriate
        if (folder.contains("/")) {
            // Deal with the subfolders one at a time by resending here
            // Split it up
            String[] subs = folder.split("/");
            for (String sub : subs) {
                folder = sub;
                makeNodes();
            }
        } else {
            String nodeName;
            if (folder.equals(parent) || parent.isEmpty()) {
                // Top level folder, so attach to songs
                nodeName = folder;


            } else {
                // Otherwise, find the parent, because it must have already been created!
                int position = -1;
                if (graph.contains(parentNode)) {
                    position = actualLocation.indexOf(parentNode.getData().toString());
                }
                if (position>-1) {
                    parentNode = graph.getNodeAtPosition(position);
                }

                if (parent.isEmpty()) {
                    nodeName = folder;
                } else {
                    nodeName = parent + "/" + folder;
                }
            }
            Node node = new Node(nodeName);
            if (!graph.contains(node)) {
               // Make the graph add edge to the parentNode
               graph.addEdge(parentNode, node);
               actualLocation.add(nodeName);
            }
            // This now becomes the new parent
            parent = nodeName;
            parentNode = new Node(parent);
        }
    }
}
