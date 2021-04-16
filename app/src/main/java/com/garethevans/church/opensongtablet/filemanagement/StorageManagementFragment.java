package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageFolderDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

import de.blox.graphview.Graph;
import de.blox.graphview.GraphAdapter;
import de.blox.graphview.GraphView;
import de.blox.graphview.Node;
import de.blox.graphview.tree.BuchheimWalkerAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerConfiguration;

public class StorageManagementFragment extends Fragment {

    private StorageFolderDisplayBinding myView;

    protected GraphView graphView;
    private Graph graph;
    private Node songs, parentNode;
    private String fulladdress, folder, parent;

    private GraphAdapter<GraphView.ViewHolder> adapter;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> actualLocation, infos, dismisses, songIDs, availableFolders;
    private ArrayList<View> views = new ArrayList<>();
    private ArrayList<Boolean> rects = new ArrayList<>();
    private int redColor, greenColor;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageFolderDisplayBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.storage));

        graphView = myView.graph;

        redColor = requireContext().getResources().getColor(R.color.lightred);
        greenColor = requireContext().getResources().getColor(R.color.lightgreen);

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

            // Prepare the showcase
            initialiseShowcaseArrays();
            requireActivity().runOnUiThread(() -> {
                prepareShowcaseViews();
                mainActivityInterface.getShowCase().sequenceShowCase(requireActivity(),views,dismisses,infos,rects,"storageManagement");
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
        Node root = new Node("OpenSongApp\n(" + getString(R.string.root) + ")");
        actualLocation.add("root");
        songs = new Node("Songs\n(" + getString(R.string.mainfoldername) + ")");
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
                final TextView nodeButton;
                final TextView nodeActual;

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
        infos.add(getString(R.string.storage_reset));
        dismisses.add(null);
        rects.add(true);
        infos.add(getString(R.string.storage_main));
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
        graph = new Graph();
        setUpThread();
        myView.graph.invalidate();
    }

    private ArrayList<String> getFoldersFromFile() {
        // Scan the storage
        songIDs = mainActivityInterface.getStorageAccess().listSongs(requireContext(),mainActivityInterface.getPreferences(),mainActivityInterface.getLocale());
        mainActivityInterface.getStorageAccess().writeSongIDFile(requireContext(),mainActivityInterface.getPreferences(),songIDs);
        //songIDs = storageAccess.getSongIDsFromFile(requireContext());
        // Each subdir ends with /
        return mainActivityInterface.getStorageAccess().getSongFolders(requireContext(),songIDs,false,null);
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
