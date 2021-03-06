package com.diegojesuscampos.remember.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.diegojesuscampos.remember.App;
import com.diegojesuscampos.remember.R;
import com.diegojesuscampos.remember.activities.MainActivity;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;

import com.diegojesuscampos.remember.adapters.NoteListCustomAdapter;
import java.util.ArrayList;


public class NoteListFragment extends Fragment {

    private static Context ctx;

    private ListView noteListView;
    private static NoteListCustomAdapter noteListCustomAdapter;
    private FloatingActionButton addButton;
    private static ArrayList<Note> notes;
    public static MainActivity callBack;
    public static int totalNotas = 0;


    public static NoteListFragment  NoteListFragment() {
        NoteListFragment f = new NoteListFragment();

        return f;
    }

    public static NoteListFragment newInstance() {
        return new NoteListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        getActivity().setTitle(R.string.list_notes);
        this.ctx = getActivity();
        //enable options menu
        setHasOptionsMenu(true);
        //init list data
        notes = new ArrayList<>();
        initUIReferences(view);
        initEvents();
        // load data
        loadNotesData(NoteSortOrder.TITLE);
        return view;
    }

    private void initUIReferences(View view) {
        noteListView = (ListView) view.findViewById(R.id.note_list);
        noteListCustomAdapter = new NoteListCustomAdapter(getActivity(), notes);
        //setup adapter
        noteListView.setAdapter(noteListCustomAdapter);

        addButton = (FloatingActionButton) view.findViewById(R.id.add_button);
    }

    private void initEvents() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddScreen();
            }
        });

        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Note currentItem = noteListCustomAdapter.getItem(i);
                showDetailScreen(currentItem);
            }
        });
    }

    private void showAddScreen() {
        callBack.cargarFragment(App.FRAGMENT_ADD_NOTES,null);
    }

    private void showDetailScreen(Note note) {
        callBack.cargarFragment(App.FRAGMENT_DETAIL_NOTES,note);
    }

    // METODO PARA MOSTRAR MENSAJE DE ERRORE DE CARGA DE NOTAS
    private static void showErrorLoadNotes() {
        Toast.makeText(ctx, ctx.getString(R.string.error_getNotas), Toast.LENGTH_LONG).show();
    }


    public static void loadNotesData(NoteSortOrder orden) {
        final EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();

        final NoteFilter filter = new NoteFilter();
        filter.setOrder(orden.getValue());
        noteStoreClient.findNoteCountsAsync(filter, false, new EvernoteCallback<NoteCollectionCounts>() {
            @Override
            public void onSuccess(NoteCollectionCounts result) {

                for (String keyNoteBooks : result.getNotebookCounts().keySet()) {
                    totalNotas += result.getNotebookCounts().get(keyNoteBooks).intValue();
                }

                noteStoreClient.findNotesAsync(filter, 0, totalNotas, new EvernoteCallback<NoteList>() {
                    @Override
                    public void onSuccess(NoteList result) {
                        if (result != null) {
                            //clear data but not losing reference to keep adapter working
                            notes.clear();
                            notes.addAll(result.getNotes());
                            //refresh data
                            noteListCustomAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                        showErrorLoadNotes();
                    }
                });
            }

            @Override
            public void onException(Exception exception) {

            }
        });
    }
}