package com.systems.automaton.classtimetableplanner.activities;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.systems.automaton.classtimetableplanner.R;
import com.systems.automaton.classtimetableplanner.adapters.ExamsAdapter;
import com.systems.automaton.classtimetableplanner.ads.AdManager;
import com.systems.automaton.classtimetableplanner.model.Exam;
import com.systems.automaton.classtimetableplanner.utils.AlertDialogsHelper;
import com.systems.automaton.classtimetableplanner.utils.DbHelper;
import com.systems.automaton.classtimetableplanner.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.Objects;

public class ExamsActivity extends AppCompatActivity {

    @NonNull
    private final AppCompatActivity context = this;
    private ListView listView;
    private ExamsAdapter adapter;
    private DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams);
        initAll();
    }

    private void initAll() {
        setupAdapter();
        setupListViewMultiSelect();
        setupCustomDialog();
        AdManager.instance.createAdView(getApplicationContext(), findViewById(R.id.ad_container));
    }

    private void setupAdapter() {
        db = new DbHelper(context);
        listView = findViewById(R.id.examslist);
        adapter = new ExamsAdapter(db, ExamsActivity.this, listView, R.layout.listview_exams_adapter, db.getExam());
        listView.setAdapter(adapter);
    }

    private void setupListViewMultiSelect() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(@NonNull ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " " + getResources().getString(R.string.selected));
                if (checkedCount == 0) mode.finish();
            }

            @Override
            public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.toolbar_action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(@NonNull final ActionMode mode, @NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    ArrayList<Exam> removelist = new ArrayList<>();
                    SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    for (int i = 0; i < checkedItems.size(); i++) {
                        int key = checkedItems.keyAt(i);
                        if (checkedItems.get(key)) {
                            db.deleteExamById(Objects.requireNonNull(adapter.getItem(key)));
                            removelist.add(adapter.getExamList().get(key));
                        }
                    }
                    adapter.getExamList().removeAll(removelist);
                    db.updateExam(adapter.getExam());
                    adapter.notifyDataSetChanged();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.dialog_add_exam, null);
        AlertDialogsHelper.getAddExamDialog(db, ExamsActivity.this, alertLayout, adapter);
    }
}
