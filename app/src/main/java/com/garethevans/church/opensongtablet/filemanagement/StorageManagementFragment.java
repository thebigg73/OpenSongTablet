package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
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

public class StorageManagementFragment extends DialogFragment {

    StorageFolderDisplayBinding myView;
    SQLiteHelper sqLiteHelper;

    protected GraphView graphView;
    Graph graph;

    GraphAdapter<GraphView.ViewHolder> adapter;
    MainActivityInterface mainActivityInterface;
    ArrayList<String> actualLocation, infos, dismisses;
    ArrayList<View> views = new ArrayList<>();
    ArrayList<Boolean> rects = new ArrayList<>();

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
        ShowCase showCase = new ShowCase();

        mainActivityInterface.updateToolbar(getActivity().getResources().getString(R.string.storage_choose));
        graph = new Graph();

        // Do this as separate tasks in a new thread
        new Thread(() -> {
            createNodes();
            adapter = getGraphAdapter();

            requireActivity().runOnUiThread(() -> {
                graphView.setAdapter(adapter);
                graphView.setLayout(new BuchheimWalkerAlgorithm(getConfiguration()));
            });

            setListeners();

            // Prepare the showcase
            initialiseShowcaseArrays();
            requireActivity().runOnUiThread(() -> {
                prepareShowcaseViews();
                showCase.sequenceShowCase(requireActivity(),views,dismisses,infos,rects,"storageManagements");
            });
        }).start();

        return myView.getRoot();
    }

    private void setListeners() {
        graphView.setOnItemClickListener((parent, view, position, id) -> {
            boolean root = position==0;
            boolean songs = position==1;
            String location = "";
            if (actualLocation.size()>position && (position)>1) {
                Log.d("d", "actualLocation=" + actualLocation.get(position-2));  // Take away 2 as not OpenSong/ or Songs/
                location = actualLocation.get(position-2);
            }
            showActionDialog(root,songs,location);
        });
    }

    private void createNodes() {
        ArrayList<String> folders = sqLiteHelper.getFolders(requireContext());
        actualLocation = new ArrayList<>();

        // Set up top level folders
        //  This will return a list like MAIN, Band, Musicals, Musicals/HSM, Alex, etc.
        Node root = new Node("OpenSongApp");
        Node songs = new Node("Songs\n(" + getActivity().getResources().getString(R.string.mainfoldername) + ")");
        graph.addEdge(root, songs);
        Node currparent = songs;
        StringBuilder currParentString;
        for (String folder : folders) {
            Log.d("d","folder:" + folder);
            if (!folder.contains("/") && !folder.equals(getActivity().getResources().getString(R.string.mainfoldername))) {
                // Top level subfolders
                Node node = new Node(folder);
                graph.addEdge(songs,node);
                actualLocation.add(folder);
            } else if (!folder.equals(getActivity().getResources().getString(R.string.mainfoldername))) {
                // Sub folder.  Check the top part exists, if not, create it
                String[] nodebits = folder.split("/");
                currParentString = new StringBuilder();
                for (int i=0; i<nodebits.length; i++) {
                    String nodebit = nodebits[i];
                    Node node = new Node(nodebit);
                    if (!graph.contains(node)) {
                        // Create the node link
                        graph.addEdge(currparent,node);
                        String actLoc = currParentString + "/" + folder;
                        if (i!=0) {
                            currParentString.append("/").append(nodebit);
                            if (currParentString.toString().startsWith("/")) {
                                currParentString = new StringBuilder(currParentString.toString().replaceFirst("/", ""));
                            }
                        }
                        actualLocation.add(actLoc);
                    }
                    currparent = node;
                }
            }
        }
    }

    private GraphAdapter<GraphView.ViewHolder> getGraphAdapter() {
        return new GraphAdapter<GraphView.ViewHolder>(graph) {

            @Override
            public int getCount() {
                Log.d("d","getCount="+graph.getNodeCount());
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
                ((SimpleViewHolder) viewHolder).nodeButton.setText(((Node)data).getData().toString());
            }

            class SimpleViewHolder extends GraphView.ViewHolder {
                TextView nodeButton;

                SimpleViewHolder(View itemView) {
                    super(itemView);
                    nodeButton = itemView.findViewById(R.id.nodeButton);
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

    private void initialiseShowcaseArrays () {
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
        createNodes();
        adapter.notifyDataSetChanged();
    }
}
